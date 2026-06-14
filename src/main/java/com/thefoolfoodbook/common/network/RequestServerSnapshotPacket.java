package com.thefoolfoodbook.common.network;

import com.thefoolfoodbook.common.book.ServerCollectionSnapshotBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestServerSnapshotPacket() {
    public static void encode(RequestServerSnapshotPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestServerSnapshotPacket decode(FriendlyByteBuf buffer) {
        return new RequestServerSnapshotPacket();
    }

    public static void handle(RequestServerSnapshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                FoodWisdomNetwork.sendSnapshot(sender, ServerCollectionSnapshotBuilder.create(sender));
            }
        });
        context.setPacketHandled(true);
    }
}
