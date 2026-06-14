package com.thefoolfoodbook.common.book;

import java.util.List;

public record CollectionBookSnapshot(List<CollectionChapter> chapters) {
    public CollectionBookSnapshot {
        chapters = List.copyOf(chapters);
    }

    public static CollectionBookSnapshot empty() {
        return new CollectionBookSnapshot(List.of());
    }
}
