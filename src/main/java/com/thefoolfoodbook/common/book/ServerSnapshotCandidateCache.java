package com.thefoolfoodbook.common.book;

import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class ServerSnapshotCandidateCache {
    private static final Map<Level, CandidateSets> LEVEL_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private ServerSnapshotCandidateCache() {
    }

    public static CandidateSets forLevel(Level level) {
        if (level == null) {
            return CandidateSets.empty();
        }
        synchronized (LEVEL_CACHE) {
            return cached(
                    LEVEL_CACHE,
                    level,
                    () -> CatFoxCollectionScanner.serverStructureCandidateIds(level),
                    () -> CatFoxCollectionScanner.campfireCookingInputIds(level),
                    () -> CatFoxCollectionScanner.createProcessingInputIds(level)
            );
        }
    }

    public static void clear() {
        synchronized (LEVEL_CACHE) {
            LEVEL_CACHE.clear();
        }
    }

    static <K> CandidateSets cached(
            Map<K, CandidateSets> cache,
            K key,
            Supplier<Set<String>> structureIds,
            Supplier<Set<String>> campfireCookingInputIds,
            Supplier<Set<String>> createProcessingInputIds
    ) {
        CandidateSets existing = cache.get(key);
        if (existing != null) {
            return existing;
        }
        CandidateSets created = new CandidateSets(
                structureIds.get(),
                campfireCookingInputIds.get(),
                createProcessingInputIds.get()
        );
        cache.put(key, created);
        return created;
    }

    public record CandidateSets(
            Set<String> structureIds,
            Set<String> campfireCookingInputIds,
            Set<String> createProcessingInputIds
    ) {
        public CandidateSets {
            structureIds = copy(structureIds);
            campfireCookingInputIds = copy(campfireCookingInputIds);
            createProcessingInputIds = copy(createProcessingInputIds);
        }

        public static CandidateSets empty() {
            return new CandidateSets(Set.of(), Set.of(), Set.of());
        }

        private static Set<String> copy(Set<String> values) {
            if (values == null || values.isEmpty()) {
                return Set.of();
            }
            return Set.copyOf(new TreeSet<>(values));
        }
    }
}
