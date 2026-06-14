package com.thefoolfoodbook.common.book;

import java.util.List;

public final class CatFoxCollectionScannerTest {
    private CatFoxCollectionScannerTest() {
    }

    public static void main(String[] args) {
        exposesRequestedRelicChapters();
        prefersExplorersCompassStructuresAndKeepsCollectedFallback();
        fallsBackToLocalRegistryWhenCompassCacheIsEmpty();
        translatesStructuresOrFallsBackToReadableId();
        createsBookmarkableLowestLevelEnchantedBookEntries();
        createsBookmarkableSpellScrollEntries();
        System.out.println("CatFoxCollectionScannerTest passed");
    }

    private static void exposesRequestedRelicChapters() {
        List<String> ids = CatFoxCollectionScanner.chapterDefinitions(null, ignored -> CollectionGrouping.DEFAULT, CollectionDisplayMode.MISSING)
                .stream()
                .map(ChapterDefinition::id)
                .toList();

        assertEquals(13, ids.size(), "cat fox chapter count");
        assertEquals(true, ids.contains("cat_sin"), "contains cat_sin");
        assertEquals(true, ids.contains("fox_resonance"), "contains fox_resonance");
        assertEquals(true, ids.contains("fox_hunting"), "contains fox_hunting");
        assertEquals(false, ids.contains("fox_weapon"), "excludes fox_weapon");
    }

    private static void prefersExplorersCompassStructuresAndKeepsCollectedFallback() {
        List<String> ids = CatFoxCollectionScanner.mergeStructureCandidateIds(
                        List.of("minecraft:village_plains", "minecraft:desert_pyramid"),
                        List.of("minecraft:mineshaft"),
                        List.of("minecraft:ancient_city"))
                .stream()
                .map(Object::toString)
                .toList();

        assertEquals(List.of("minecraft:ancient_city", "minecraft:desert_pyramid", "minecraft:village_plains"), ids, "compass structures preferred");
    }

    private static void fallsBackToLocalRegistryWhenCompassCacheIsEmpty() {
        List<String> ids = CatFoxCollectionScanner.mergeStructureCandidateIds(
                        List.of(),
                        List.of("minecraft:mineshaft"),
                        List.of("minecraft:village_savanna"))
                .stream()
                .map(Object::toString)
                .toList();

        assertEquals(List.of("minecraft:mineshaft", "minecraft:village_savanna"), ids, "local registry fallback");
    }

    private static void translatesStructuresOrFallsBackToReadableId() {
        assertEquals("Ancient City", CatFoxCollectionScanner.structureDisplayName("minecraft:ancient_city"), "structure display fallback");
    }

    private static void createsBookmarkableLowestLevelEnchantedBookEntries() {
        CollectionEntry entry = CatFoxCollectionScanner.enchantmentEntryForTest("minecraft:sharpness", "Sharpness V", 1, true);

        assertEquals("minecraft:sharpness", entry.id(), "enchantment id");
        assertEquals(true, entry.bookmarkable(), "enchantment bookmarkable");
        assertEquals(false, entry.recipeOpenable(), "enchantment recipes disabled");
    }

    private static void createsBookmarkableSpellScrollEntries() {
        CollectionEntry entry = CatFoxCollectionScanner.spellEntryForTest("irons_spellbooks:fireball", "Fireball", true);

        assertEquals("irons_spellbooks:fireball", entry.id(), "spell id");
        assertEquals(true, entry.bookmarkable(), "spell bookmarkable");
        assertEquals(true, entry.recipeOpenable(), "spell recipes openable");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
