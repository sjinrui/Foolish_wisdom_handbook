package com.thefoolfoodbook.common.book;

import java.util.List;

public record CollectionChapter(
        String id,
        String title,
        List<CollectionGroup> groups,
        int totalCount,
        int collectedCount,
        boolean sourceAvailable
) {
    public CollectionChapter {
        groups = List.copyOf(groups);
    }

    public int missingCount() {
        int count = 0;
        for (CollectionGroup group : groups) {
            count += group.entries().size();
        }
        return count;
    }

    public int actualMissingCount() {
        return Math.max(0, totalCount - collectedCount);
    }

    public static CollectionChapter empty(String id, String title, boolean sourceAvailable) {
        return new CollectionChapter(id, title, List.of(), 0, 0, sourceAvailable);
    }
}
