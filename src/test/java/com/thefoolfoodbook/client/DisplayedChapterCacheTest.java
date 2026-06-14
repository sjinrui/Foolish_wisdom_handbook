package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionChapter;
import com.thefoolfoodbook.common.book.CollectionEntry;
import com.thefoolfoodbook.common.book.CollectionGroup;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class DisplayedChapterCacheTest {
    private DisplayedChapterCacheTest() {
    }

    public static void main(String[] args) {
        reusesFilteredChapterForSameSourceAndQuery();
        invalidatesWhenQueryChanges();
        blankQueryReturnsSourceChapter();
        System.out.println("DisplayedChapterCacheTest passed");
    }

    private static void reusesFilteredChapterForSameSourceAndQuery() {
        AtomicInteger matches = new AtomicInteger();
        DisplayedChapterCache cache = new DisplayedChapterCache((entry, query) -> {
            matches.incrementAndGet();
            return entry.displayName().contains(query);
        });
        CollectionChapter source = chapter(
                entry("minecraft:apple", "Apple"),
                entry("minecraft:bread", "Bread")
        );

        CollectionChapter first = cache.get(source, "Apple");
        CollectionChapter second = cache.get(source, "Apple");

        assertSame(first, second, "same source and query reuse filtered chapter");
        assertEquals(2, matches.get(), "matcher called only for first calculation");
        assertEquals(1, first.groups().get(0).entries().size(), "filtered entry count");
    }

    private static void invalidatesWhenQueryChanges() {
        AtomicInteger matches = new AtomicInteger();
        DisplayedChapterCache cache = new DisplayedChapterCache((entry, query) -> {
            matches.incrementAndGet();
            return entry.displayName().contains(query);
        });
        CollectionChapter source = chapter(
                entry("minecraft:apple", "Apple"),
                entry("minecraft:bread", "Bread")
        );

        cache.get(source, "Apple");
        CollectionChapter second = cache.get(source, "Bread");

        assertEquals(4, matches.get(), "matcher called again when query changes");
        assertEquals("minecraft:bread", second.groups().get(0).entries().get(0).id(), "new query result");
    }

    private static void blankQueryReturnsSourceChapter() {
        DisplayedChapterCache cache = new DisplayedChapterCache((entry, query) -> false);
        CollectionChapter source = chapter(entry("minecraft:apple", "Apple"));

        assertSame(source, cache.get(source, ""), "blank query returns source");
        assertSame(source, cache.get(source, "   "), "blank padded query returns source");
    }

    private static CollectionChapter chapter(CollectionEntry... entries) {
        return new CollectionChapter(
                "food",
                "Food",
                List.of(new CollectionGroup("all", "All", List.of(entries))),
                entries.length,
                0,
                true
        );
    }

    private static CollectionEntry entry(String id, String displayName) {
        return CollectionEntry.item(id, displayName, true);
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
