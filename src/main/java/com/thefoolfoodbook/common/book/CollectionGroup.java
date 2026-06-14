package com.thefoolfoodbook.common.book;

import java.util.List;

public record CollectionGroup(String id, String title, List<CollectionEntry> entries) {
    public CollectionGroup {
        entries = List.copyOf(entries);
    }

    public List<String> bookmarkableItemIds() {
        return entries.stream()
                .filter(CollectionEntry::bookmarkable)
                .map(CollectionEntry::id)
                .toList();
    }
}
