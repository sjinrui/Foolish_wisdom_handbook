package com.thefoolfoodbook.common.book;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CollectionEntryGrouper {
    private CollectionEntryGrouper() {
    }

    public static List<CollectionGroup> groupByMod(List<CollectionEntry> entries, Function<String, String> modNameResolver) {
        Map<String, List<CollectionEntry>> grouped = new LinkedHashMap<>();
        entries.stream()
                .sorted(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id))
                .forEach(entry -> grouped
                        .computeIfAbsent(modNameResolver.apply(namespace(entry.id())), ignored -> new ArrayList<>())
                        .add(entry));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CollectionGroup(entry.getKey(), entry.getKey(), entry.getValue()))
                .toList();
    }

    private static String namespace(String id) {
        int separator = id.indexOf(':');
        return separator > 0 ? id.substring(0, separator) : id;
    }
}
