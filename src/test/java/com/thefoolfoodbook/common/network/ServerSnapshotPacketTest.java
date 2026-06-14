package com.thefoolfoodbook.common.network;

import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.Set;

public final class ServerSnapshotPacketTest {
    private ServerSnapshotPacketTest() {
    }

    public static void main(String[] args) {
        roundTripsSnapshot();
        System.out.println("ServerSnapshotPacketTest passed");
    }

    private static void roundTripsSnapshot() {
        ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.of(
                Set.of(ServerCollectionSnapshot.MODULE_AOM, ServerCollectionSnapshot.MODULE_RECIPES),
                Map.of(
                        ServerCollectionSnapshot.KEY_FOOD_EATEN, Set.of("minecraft:apple", "minecraft:bread"),
                        ServerCollectionSnapshot.KEY_FOX_RESONANCE_CANDIDATES, Set.of("minecraft:cobblestone")
                )
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ServerSnapshotPacket.encode(new ServerSnapshotPacket(snapshot), buffer);
        ServerSnapshotPacket decoded = ServerSnapshotPacket.decode(buffer);

        assertEquals(true, decoded.snapshot().isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM), "aom available");
        assertEquals(true, decoded.snapshot().isModuleAvailable(ServerCollectionSnapshot.MODULE_RECIPES), "recipes available");
        assertEquals(Set.of("minecraft:apple", "minecraft:bread"), decoded.snapshot().values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "food values");
        assertEquals(Set.of("minecraft:cobblestone"), decoded.snapshot().values(ServerCollectionSnapshot.KEY_FOX_RESONANCE_CANDIDATES), "recipe values");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
