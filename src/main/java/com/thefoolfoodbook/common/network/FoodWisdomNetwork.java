package com.thefoolfoodbook.common.network;

import com.thefoolfoodbook.TheFoolFoodBook;
import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class FoodWisdomNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel;
    private static int nextPacketId;

    private FoodWisdomNetwork() {
    }

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TheFoolFoodBook.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
                NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
        );
        channel.registerMessage(
                nextPacketId++,
                RequestServerSnapshotPacket.class,
                RequestServerSnapshotPacket::encode,
                RequestServerSnapshotPacket::decode,
                RequestServerSnapshotPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        channel.registerMessage(
                nextPacketId++,
                ServerSnapshotPacket.class,
                ServerSnapshotPacket::encode,
                ServerSnapshotPacket::decode,
                ServerSnapshotPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static boolean isRemotePresent(Connection connection) {
        return channel != null && connection != null && channel.isRemotePresent(connection);
    }

    public static void requestServerSnapshot() {
        if (channel != null) {
            channel.sendToServer(new RequestServerSnapshotPacket());
        }
    }

    public static void sendSnapshot(ServerPlayer player, ServerCollectionSnapshot snapshot) {
        if (channel != null) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), new ServerSnapshotPacket(snapshot));
        }
    }
}
