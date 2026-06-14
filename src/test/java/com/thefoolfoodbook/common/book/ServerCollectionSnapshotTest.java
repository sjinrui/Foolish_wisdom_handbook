package com.thefoolfoodbook.common.book;

import java.util.Map;
import java.util.Set;

public final class ServerCollectionSnapshotTest {
    private ServerCollectionSnapshotTest() {
    }

    public static void main(String[] args) {
        exposesModuleAvailabilityAndValues();
        missingModulesAndKeysFallBackToEmpty();
        copiesInputCollections();
        System.out.println("ServerCollectionSnapshotTest passed");
    }

    private static void exposesModuleAvailabilityAndValues() {
        ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.of(
                Set.of(ServerCollectionSnapshot.MODULE_AOM, ServerCollectionSnapshot.MODULE_STRUCTURES),
                Map.of(
                        ServerCollectionSnapshot.KEY_FOOD_EATEN, Set.of("minecraft:apple"),
                        ServerCollectionSnapshot.KEY_FOX_EXPLORATION_CANDIDATES, Set.of("minecraft:village_plains")
                )
        );

        assertEquals(true, snapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM), "aom module available");
        assertEquals(true, snapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_STRUCTURES), "structures module available");
        assertEquals(Set.of("minecraft:apple"), snapshot.values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "food values");
        assertEquals(Set.of("minecraft:village_plains"), snapshot.values(ServerCollectionSnapshot.KEY_FOX_EXPLORATION_CANDIDATES), "structure values");
    }

    private static void missingModulesAndKeysFallBackToEmpty() {
        ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.empty();

        assertEquals(false, snapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM), "missing module unavailable");
        assertEquals(Set.of(), snapshot.values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "missing values empty");
    }

    private static void copiesInputCollections() {
        java.util.Set<String> modules = new java.util.TreeSet<>();
        java.util.Set<String> foods = new java.util.TreeSet<>();
        modules.add(ServerCollectionSnapshot.MODULE_AOM);
        foods.add("minecraft:apple");

        ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.of(
                modules,
                Map.of(ServerCollectionSnapshot.KEY_FOOD_EATEN, foods)
        );
        modules.clear();
        foods.clear();

        assertEquals(true, snapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM), "copied module available");
        assertEquals(Set.of("minecraft:apple"), snapshot.values(ServerCollectionSnapshot.KEY_FOOD_EATEN), "copied values retained");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
