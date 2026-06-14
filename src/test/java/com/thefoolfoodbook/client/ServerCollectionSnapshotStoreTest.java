package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;

import java.util.Map;
import java.util.Set;

public final class ServerCollectionSnapshotStoreTest {
    private ServerCollectionSnapshotStoreTest() {
    }

    public static void main(String[] args) {
        startsEmpty();
        storesAndClearsSnapshot();
        System.out.println("ServerCollectionSnapshotStoreTest passed");
    }

    private static void startsEmpty() {
        ServerCollectionSnapshotStore.clear();

        assertEquals(false, ServerCollectionSnapshotStore.hasSnapshot(), "initial has snapshot");
        assertEquals(ServerCollectionSnapshot.empty(), ServerCollectionSnapshotStore.snapshot(), "initial snapshot");
    }

    private static void storesAndClearsSnapshot() {
        ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.of(
                Set.of(ServerCollectionSnapshot.MODULE_AOM),
                Map.of(ServerCollectionSnapshot.KEY_FOOD_EATEN, Set.of("minecraft:apple"))
        );

        ServerCollectionSnapshotStore.update(snapshot);
        assertEquals(true, ServerCollectionSnapshotStore.hasSnapshot(), "updated has snapshot");
        assertEquals(Set.of("minecraft:apple"), ServerCollectionSnapshotStore.snapshot().values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "stored values");

        ServerCollectionSnapshotStore.clear();
        assertEquals(false, ServerCollectionSnapshotStore.hasSnapshot(), "cleared has snapshot");
        assertEquals(Set.of(), ServerCollectionSnapshotStore.snapshot().values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "cleared values");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
