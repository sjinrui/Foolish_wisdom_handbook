package com.thefoolfoodbook.common.book;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BiomeTreatProgressTest {
    private BiomeTreatProgressTest() {
    }

    public static void main(String[] args) {
        mergesValidBiomeIdsAndKeepsSortedOrder();
        removesOnlyRequestedBiomeId();
        scansOnlyPlayerInventoryAndOffhandSlots();
        buildsStableSafeProgressFileName();
        cachesProgressByPathAndKeepsFilesIsolated();
        System.out.println("BiomeTreatProgressTest passed");
    }

    private static void mergesValidBiomeIdsAndKeepsSortedOrder() {
        Set<String> merged = BiomeTreatProgress.merge(
                Set.of("minecraft:plains"),
                List.of("minecraft:desert", "", "bad id", "minecraft:plains", "alexscaves:candy_cavity")
        );

        assertEquals(
                List.of("alexscaves:candy_cavity", "minecraft:desert", "minecraft:plains"),
                List.copyOf(merged),
                "merged biome ids"
        );
    }

    private static void removesOnlyRequestedBiomeId() {
        Set<String> removed = BiomeTreatProgress.without(
                Set.of("minecraft:plains", "minecraft:desert", "alexscaves:candy_cavity"),
                "minecraft:desert"
        );

        assertEquals(
                List.of("alexscaves:candy_cavity", "minecraft:plains"),
                List.copyOf(removed),
                "removed biome ids"
        );
        assertEquals(
                List.of("minecraft:desert", "minecraft:plains"),
                List.copyOf(BiomeTreatProgress.without(Set.of("minecraft:desert", "minecraft:plains"), "bad id")),
                "invalid removal ignored"
        );
    }

    private static void scansOnlyPlayerInventoryAndOffhandSlots() {
        List<Integer> inventory = java.util.stream.IntStream.range(0, 40).boxed().toList();
        List<Integer> offhand = List.of(100, 101);

        Set<String> found = BiomeTreatProgress.scanPlayerSlots(
                inventory,
                offhand,
                value -> "test:biome_" + value
        );

        assertEquals(true, found.contains("test:biome_0"), "slot 0 scanned");
        assertEquals(true, found.contains("test:biome_35"), "slot 35 scanned");
        assertEquals(false, found.contains("test:biome_36"), "slot 36 ignored");
        assertEquals(true, found.contains("test:biome_100"), "first offhand scanned");
        assertEquals(false, found.contains("test:biome_101"), "extra offhand ignored");
        assertEquals(37, found.size(), "slot count");
    }

    private static void buildsStableSafeProgressFileName() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000123");
        String fileName = BiomeTreatProgress.progressFileName("server:Example/World?", playerId);

        assertEquals(
                "server_Example_World__00000000-0000-0000-0000-000000000123.json",
                fileName,
                "progress file name"
        );
    }

    private static void cachesProgressByPathAndKeepsFilesIsolated() {
        try {
            java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("biome-treat-progress-test");
            java.nio.file.Path overworld = directory.resolve("overworld_player.json");
            java.nio.file.Path nether = directory.resolve("nether_player.json");
            BiomeTreatProgress.clearCache();

            assertEquals(
                    Set.of("minecraft:plains"),
                    BiomeTreatProgress.loadAndMerge(overworld, Set.of("minecraft:plains")),
                    "overworld initial merge"
            );
            assertEquals(
                    Set.of("minecraft:warped_forest"),
                    BiomeTreatProgress.loadAndMerge(nether, Set.of("minecraft:warped_forest")),
                    "nether initial merge"
            );

            assertEquals(true, BiomeTreatProgress.remove(overworld, "minecraft:plains"), "remove cached overworld value");
            assertEquals(
                    Set.of(),
                    BiomeTreatProgress.loadAndMerge(overworld, Set.of()),
                    "overworld cache updates after remove"
            );
            assertEquals(
                    Set.of("minecraft:warped_forest"),
                    BiomeTreatProgress.loadAndMerge(nether, Set.of()),
                    "nether cache remains isolated"
            );

            BiomeTreatProgress.clearCache();
        } catch (java.io.IOException ex) {
            throw new AssertionError("temp progress file setup failed", ex);
        }
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
