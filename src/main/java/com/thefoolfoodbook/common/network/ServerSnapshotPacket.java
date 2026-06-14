package com.thefoolfoodbook.common.network;

import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public record ServerSnapshotPacket(ServerCollectionSnapshot snapshot) {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSnapshotPacket.class);

    public static void encode(ServerSnapshotPacket packet, FriendlyByteBuf buffer) {
        writeStringSet(buffer, packet.snapshot.availableModules());
        buffer.writeVarInt(packet.snapshot.valuesByKey().size());
        for (Map.Entry<String, Set<String>> entry : packet.snapshot.valuesByKey().entrySet()) {
            buffer.writeUtf(entry.getKey());
            writeStringSet(buffer, entry.getValue());
        }
    }

    public static ServerSnapshotPacket decode(FriendlyByteBuf buffer) {
        Set<String> modules = readStringSet(buffer);
        int valueCount = buffer.readVarInt();
        Map<String, Set<String>> values = new LinkedHashMap<>();
        for (int i = 0; i < valueCount; i++) {
            values.put(buffer.readUtf(), readStringSet(buffer));
        }
        return new ServerSnapshotPacket(ServerCollectionSnapshot.of(modules, values));
    }

    public static void handle(ServerSnapshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> applyOnClient(packet.snapshot())
        ));
        context.setPacketHandled(true);
    }

    private static void applyOnClient(ServerCollectionSnapshot snapshot) {
        try {
            Class<?> clientClass = Class.forName("com.thefoolfoodbook.client.FoodWisdomBookClient");
            Method apply = clientClass.getMethod("applyServerSnapshot", ServerCollectionSnapshot.class);
            apply.invoke(null, snapshot);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not apply server collection snapshot on client", ex);
        }
    }

    private static void writeStringSet(FriendlyByteBuf buffer, Set<String> values) {
        buffer.writeVarInt(values.size());
        for (String value : new TreeSet<>(values)) {
            buffer.writeUtf(value);
        }
    }

    private static Set<String> readStringSet(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Set<String> values = new TreeSet<>();
        for (int i = 0; i < count; i++) {
            values.add(buffer.readUtf());
        }
        return values;
    }
}
