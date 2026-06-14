package com.thefoolfoodbook.common.book;

import java.util.Set;

public final class CollectionEntryTest {
    private CollectionEntryTest() {
    }

    public static void main(String[] args) {
        itemEntriesDefaultToRecipeOpenable();
        itemEntriesCanDisableRecipeOpening();
        bookmarkMatchesEntryIdOrStackKey();
        System.out.println("CollectionEntryTest passed");
    }

    private static void itemEntriesDefaultToRecipeOpenable() {
        CollectionEntry entry = CollectionEntry.item("test:item", "Test Item", true);
        assertEquals(true, entry.recipeOpenable(), "default recipe openable");
    }

    private static void itemEntriesCanDisableRecipeOpening() {
        CollectionEntry entry = CollectionEntry.item("test:concept", "Concept", false, false, false);
        assertEquals(false, entry.recipeOpenable(), "disabled recipe openable");
    }

    private static void bookmarkMatchesEntryIdOrStackKey() {
        String itemKey = CollectionEntry.itemBookmarkKey("irons_spellbooks:scroll");
        String fireballStackKey = CollectionEntry.stackBookmarkKey("irons_spellbooks:scroll", "{spell:\"irons_spellbooks:fireball\",level:1}");
        String blazeStormStackKey = CollectionEntry.stackBookmarkKey("irons_spellbooks:scroll", "{spell:\"irons_spellbooks:blaze_storm\",level:1}");

        assertEquals(true, CollectionEntry.matchesBookmarkKeys("irons_spellbooks:fireball", itemKey, null, true, Set.of(itemKey)), "plain item bookmark match");
        assertEquals(true, CollectionEntry.matchesBookmarkKeys("irons_spellbooks:fireball", itemKey, fireballStackKey, true, Set.of(fireballStackKey)), "stack bookmark match");
        assertEquals(false, CollectionEntry.matchesBookmarkKeys("irons_spellbooks:fireball", itemKey, fireballStackKey, true, Set.of(itemKey)), "tagged stack ignores shared item key");
        assertEquals(false, CollectionEntry.matchesBookmarkKeys("irons_spellbooks:fireball", itemKey, fireballStackKey, true, Set.of(blazeStormStackKey)), "different stack key mismatch");
        assertEquals(false, CollectionEntry.matchesBookmarkKeys("irons_spellbooks:fireball", itemKey, fireballStackKey, false, Set.of(fireballStackKey)), "unbookmarkable mismatch");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
