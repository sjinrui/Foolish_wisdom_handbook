package com.thefoolfoodbook.common.book;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class CollectionChapterTest {
    private CollectionChapterTest() {
    }

    public static void main(String[] args) {
        countsMissingEntriesAcrossGroups();
        reportsActualMissingCountIndependentlyFromDisplayedEntries();
        exposesBookmarkableItemIdsOnly();
        loadsChaptersOnlyWhenRequested();
        groupsEntriesByModNamespace();
        invalidatesLoadedChapter();
        System.out.println("CollectionChapterTest passed");
    }

    private static void countsMissingEntriesAcrossGroups() {
        CollectionChapter chapter = new CollectionChapter(
                "weapon",
                "武器大师",
                List.of(
                        new CollectionGroup("sword", "剑", List.of(CollectionEntry.item("minecraft:iron_sword", "铁剑", true))),
                        new CollectionGroup("axe", "斧", List.of(
                                CollectionEntry.item("minecraft:iron_axe", "铁斧", true),
                                CollectionEntry.item("minecraft:golden_axe", "金斧", true)
                        ))
                ),
                5,
                2,
                true
        );

        assertEquals(3, chapter.missingCount(), "missing count");
    }

    private static void reportsActualMissingCountIndependentlyFromDisplayedEntries() {
        CollectionChapter chapter = new CollectionChapter(
                "food",
                "Food",
                List.of(new CollectionGroup("all", "All", List.of(
                        CollectionEntry.item("minecraft:apple", "Apple", true),
                        CollectionEntry.item("minecraft:bread", "Bread", true),
                        CollectionEntry.item("minecraft:carrot", "Carrot", true)
                ))),
                10,
                4,
                true
        );

        assertEquals(3, chapter.missingCount(), "displayed entry count");
        assertEquals(6, chapter.actualMissingCount(), "actual missing count");
    }

    private static void exposesBookmarkableItemIdsOnly() {
        CollectionGroup group = new CollectionGroup("mixed", "混合", List.of(
                CollectionEntry.item("minecraft:apple", "苹果", true),
                CollectionEntry.item("minecraft:dragon", "龙", false),
                CollectionEntry.morph("minecraft:zombie", "僵尸", null)
        ));

        assertEquals(List.of("minecraft:apple"), group.bookmarkableItemIds(), "bookmarkable ids");
    }

    private static void loadsChaptersOnlyWhenRequested() {
        AtomicInteger foodLoads = new AtomicInteger();
        AtomicInteger morphLoads = new AtomicInteger();
        LazyCollectionBook book = new LazyCollectionBook(List.of(
                new ChapterDefinition("food", "美食家", () -> {
                    foodLoads.incrementAndGet();
                    return CollectionChapter.empty("food", "美食家", true);
                }),
                new ChapterDefinition("morph", "变身", () -> {
                    morphLoads.incrementAndGet();
                    return CollectionChapter.empty("morph", "变身", true);
                })
        ));

        assertEquals(false, book.isLoaded("food"), "food starts unloaded");
        assertEquals("美食家", book.chapterOrPlaceholder("food").title(), "placeholder title");
        assertEquals(0, foodLoads.get(), "placeholder does not load food");

        book.load("food");
        book.load("food");

        assertEquals(true, book.isLoaded("food"), "food loaded");
        assertEquals(1, foodLoads.get(), "food loads once");
        assertEquals(0, morphLoads.get(), "morph stays unloaded");
    }

    private static void groupsEntriesByModNamespace() {
        List<CollectionGroup> groups = CollectionEntryGrouper.groupByMod(List.of(
                CollectionEntry.item("beta:hammer", "Hammer", true),
                CollectionEntry.item("alpha:sword", "Sword", true),
                CollectionEntry.item("beta:axe", "Axe", true)
        ), namespace -> namespace.equals("alpha") ? "Alpha Mod" : "Beta Mod");

        assertEquals(List.of("Alpha Mod", "Beta Mod"), groups.stream().map(CollectionGroup::title).toList(), "mod group order");
        assertEquals(List.of("alpha:sword"), groups.get(0).entries().stream().map(CollectionEntry::id).toList(), "alpha entries");
        assertEquals(List.of("beta:axe", "beta:hammer"), groups.get(1).entries().stream().map(CollectionEntry::id).toList(), "beta entries sorted");
    }

    private static void invalidatesLoadedChapter() {
        AtomicInteger loads = new AtomicInteger();
        LazyCollectionBook book = new LazyCollectionBook(List.of(
                new ChapterDefinition("weapon", "武器大师", () -> {
                    int count = loads.incrementAndGet();
                    return CollectionChapter.empty("weapon", "武器大师 " + count, true);
                })
        ));

        assertEquals("武器大师 1", book.load("weapon").title(), "first load title");
        assertEquals("武器大师 1", book.load("weapon").title(), "cached load title");
        book.invalidate("weapon");
        assertEquals("武器大师 2", book.load("weapon").title(), "reloaded title");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
