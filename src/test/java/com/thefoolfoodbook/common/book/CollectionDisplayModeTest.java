package com.thefoolfoodbook.common.book;

public final class CollectionDisplayModeTest {
    private CollectionDisplayModeTest() {
    }

    public static void main(String[] args) {
        filtersByCollectionState();
        cyclesInRequestedOrder();
        System.out.println("CollectionDisplayModeTest passed");
    }

    private static void filtersByCollectionState() {
        assertEquals(true, CollectionDisplayMode.MISSING.includes(false), "missing includes uncollected");
        assertEquals(false, CollectionDisplayMode.MISSING.includes(true), "missing excludes collected");
        assertEquals(true, CollectionDisplayMode.ALL.includes(false), "all includes uncollected");
        assertEquals(true, CollectionDisplayMode.ALL.includes(true), "all includes collected");
        assertEquals(false, CollectionDisplayMode.COLLECTED.includes(false), "collected excludes uncollected");
        assertEquals(true, CollectionDisplayMode.COLLECTED.includes(true), "collected includes collected");
    }

    private static void cyclesInRequestedOrder() {
        assertEquals(CollectionDisplayMode.ALL, CollectionDisplayMode.MISSING.next(), "missing next");
        assertEquals(CollectionDisplayMode.COLLECTED, CollectionDisplayMode.ALL.next(), "all next");
        assertEquals(CollectionDisplayMode.MISSING, CollectionDisplayMode.COLLECTED.next(), "collected next");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
