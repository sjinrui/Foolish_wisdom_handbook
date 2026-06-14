package com.thefoolfoodbook.common.book;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class ServerSnapshotCandidateCacheTest {
    private ServerSnapshotCandidateCacheTest() {
    }

    public static void main(String[] args) {
        reusesCandidatesForSameLevelKey();
        keepsDifferentLevelKeysIsolated();
        System.out.println("ServerSnapshotCandidateCacheTest passed");
    }

    private static void reusesCandidatesForSameLevelKey() {
        Map<String, ServerSnapshotCandidateCache.CandidateSets> cache = new HashMap<>();
        AtomicInteger scans = new AtomicInteger();

        ServerSnapshotCandidateCache.CandidateSets first = ServerSnapshotCandidateCache.cached(
                cache,
                "overworld",
                () -> scan(scans, "minecraft:village"),
                () -> scan(scans, "minecraft:porkchop"),
                () -> scan(scans, "minecraft:cobblestone")
        );
        ServerSnapshotCandidateCache.CandidateSets second = ServerSnapshotCandidateCache.cached(
                cache,
                "overworld",
                () -> scan(scans, "minecraft:desert_pyramid"),
                () -> scan(scans, "minecraft:beef"),
                () -> scan(scans, "minecraft:andesite")
        );

        assertSame(first, second, "same level key reuses candidate set");
        assertEquals(3, scans.get(), "each scanner called only once");
        assertEquals(Set.of("minecraft:village"), second.structureIds(), "cached structures");
        assertEquals(Set.of("minecraft:porkchop"), second.campfireCookingInputIds(), "cached campfire inputs");
        assertEquals(Set.of("minecraft:cobblestone"), second.createProcessingInputIds(), "cached processing inputs");
    }

    private static void keepsDifferentLevelKeysIsolated() {
        Map<String, ServerSnapshotCandidateCache.CandidateSets> cache = new HashMap<>();
        AtomicInteger scans = new AtomicInteger();

        ServerSnapshotCandidateCache.CandidateSets overworld = ServerSnapshotCandidateCache.cached(
                cache,
                "overworld",
                () -> scan(scans, "minecraft:village"),
                () -> scan(scans, "minecraft:porkchop"),
                () -> scan(scans, "minecraft:cobblestone")
        );
        ServerSnapshotCandidateCache.CandidateSets nether = ServerSnapshotCandidateCache.cached(
                cache,
                "nether",
                () -> scan(scans, "minecraft:bastion_remnant"),
                () -> scan(scans, "minecraft:chicken"),
                () -> scan(scans, "minecraft:netherrack")
        );

        assertEquals(false, overworld == nether, "different keys do not share candidate set");
        assertEquals(6, scans.get(), "different level keys scan independently");
        assertEquals(Set.of("minecraft:bastion_remnant"), nether.structureIds(), "nether structures");
    }

    private static Set<String> scan(AtomicInteger scans, String value) {
        scans.incrementAndGet();
        return Set.of(value);
    }

    private static void assertSame(Object expected, Object actual, String field) {
        if (expected != actual) {
            throw new AssertionError(field + " expected same instance");
        }
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
