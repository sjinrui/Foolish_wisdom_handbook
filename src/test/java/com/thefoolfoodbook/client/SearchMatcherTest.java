package com.thefoolfoodbook.client;

public final class SearchMatcherTest {
    private SearchMatcherTest() {
    }

    public static void main(String[] args) {
        matchesEnglishIdAndDisplayName();
        matchesChineseTextAndFallbackPinyin();
        ignoresBlankQueries();
        System.out.println("SearchMatcherTest passed");
    }

    private static void matchesEnglishIdAndDisplayName() {
        assertEquals(true, SearchMatcher.matches("minecraft:cherry_grove", "Cherry Grove", "cherry"), "english display");
        assertEquals(true, SearchMatcher.matches("alexscaves:candy_cavity", "Candy Cavity", "alex candy"), "id words");
        assertEquals(false, SearchMatcher.matches("minecraft:plains", "Plains", "desert"), "unmatched english");
    }

    private static void matchesChineseTextAndFallbackPinyin() {
        assertEquals(true, SearchMatcher.matches("minecraft:plains", "平原", "平"), "chinese substring");
        assertEquals(true, SearchMatcher.matches("minecraft:plains", "平原", "pingyuan"), "fallback full pinyin");
        assertEquals(true, SearchMatcher.matches("minecraft:plains", "平原", "py"), "fallback pinyin initials");
    }

    private static void ignoresBlankQueries() {
        assertEquals(true, SearchMatcher.matches("minecraft:plains", "平原", ""), "blank query");
        assertEquals(true, SearchMatcher.matches("minecraft:plains", "平原", "   "), "blank padded query");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
