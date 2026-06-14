package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionChapter;
import com.thefoolfoodbook.common.book.CollectionEntry;
import com.thefoolfoodbook.common.book.CollectionGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

final class DisplayedChapterCache {
    private final BiPredicate<CollectionEntry, String> matcher;
    private CollectionChapter sourceChapter;
    private String query = "";
    private CollectionChapter displayedChapter;

    DisplayedChapterCache() {
        this(SearchMatcher::matches);
    }

    DisplayedChapterCache(BiPredicate<CollectionEntry, String> matcher) {
        this.matcher = matcher;
    }

    CollectionChapter get(CollectionChapter chapter, String rawQuery) {
        String nextQuery = rawQuery == null ? "" : rawQuery;
        if (nextQuery.isBlank()) {
            return chapter;
        }
        if (chapter == sourceChapter && nextQuery.equals(query) && displayedChapter != null) {
            return displayedChapter;
        }

        sourceChapter = chapter;
        query = nextQuery;
        displayedChapter = filter(chapter, nextQuery);
        return displayedChapter;
    }

    void invalidate() {
        sourceChapter = null;
        query = "";
        displayedChapter = null;
    }

    private CollectionChapter filter(CollectionChapter chapter, String query) {
        List<CollectionGroup> groups = new ArrayList<>();
        for (CollectionGroup group : chapter.groups()) {
            List<CollectionEntry> entries = group.entries().stream()
                    .filter(entry -> matcher.test(entry, query))
                    .toList();
            if (!entries.isEmpty()) {
                groups.add(new CollectionGroup(group.id(), group.title(), entries));
            }
        }
        return new CollectionChapter(chapter.id(), chapter.title(), groups, chapter.totalCount(), chapter.collectedCount(), chapter.sourceAvailable());
    }
}
