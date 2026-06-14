package com.thefoolfoodbook.client;

public final class FoodWisdomKeyMappingsTest {
    private FoodWisdomKeyMappingsTest() {
    }

    public static void main(String[] args) {
        exposesStableTranslationKeys();
        usesUnboundDefaultKey();
        System.out.println("FoodWisdomKeyMappingsTest passed");
    }

    private static void exposesStableTranslationKeys() {
        assertEquals("key.foolish_wisdom_handbook.open_book", FoodWisdomKeyMappings.OPEN_BOOK_KEY, "open book key");
        assertEquals("key.categories.foolish_wisdom_handbook", FoodWisdomKeyMappings.CATEGORY, "category key");
    }

    private static void usesUnboundDefaultKey() {
        assertEquals(-1, FoodWisdomKeyMappings.DEFAULT_KEY_CODE, "default key code");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
