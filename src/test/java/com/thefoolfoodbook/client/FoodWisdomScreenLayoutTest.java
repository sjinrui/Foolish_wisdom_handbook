package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionDisplayMode;

public final class FoodWisdomScreenLayoutTest {
    private FoodWisdomScreenLayoutTest() {
    }

    public static void main(String[] args) {
        summaryStartsAfterSearchBox();
        clicksOutsideSearchBoxClearFocus();
        clicksInsideSearchBoxKeepFocusForWidget();
        collectedBiomeTreatsCanBeRemovedInCollectedOrAllMode();
        missingBiomeTreatsCannotBeRemovedInAllMode();
        System.out.println("FoodWisdomScreenLayoutTest passed");
    }

    private static void summaryStartsAfterSearchBox() {
        assertEquals(212, FoodWisdomScreenLayout.summaryTextX(70, 128), "summary x");
    }

    private static void clicksOutsideSearchBoxClearFocus() {
        assertEquals(true, FoodWisdomScreenLayout.shouldSearchBoxLoseFocus(250, 80, 70, 56, 198, 74), "outside click");
        assertEquals(true, FoodWisdomScreenLayout.shouldSearchBoxLoseFocus(69, 60, 70, 56, 198, 74), "left outside click");
    }

    private static void clicksInsideSearchBoxKeepFocusForWidget() {
        assertEquals(false, FoodWisdomScreenLayout.shouldSearchBoxLoseFocus(100, 60, 70, 56, 198, 74), "inside click");
    }

    private static void collectedBiomeTreatsCanBeRemovedInCollectedOrAllMode() {
        assertEquals(true, FoodWisdomScreenLayout.canRemoveBiomeTreat("biome_treat", CollectionDisplayMode.COLLECTED, true, true), "collected mode collected treat");
        assertEquals(true, FoodWisdomScreenLayout.canRemoveBiomeTreat("biome_treat", CollectionDisplayMode.ALL, true, true), "all mode collected treat");
    }

    private static void missingBiomeTreatsCannotBeRemovedInAllMode() {
        assertEquals(false, FoodWisdomScreenLayout.canRemoveBiomeTreat("biome_treat", CollectionDisplayMode.ALL, false, true), "all mode missing treat");
        assertEquals(false, FoodWisdomScreenLayout.canRemoveBiomeTreat("food", CollectionDisplayMode.ALL, true, true), "other chapter collected item");
        assertEquals(false, FoodWisdomScreenLayout.canRemoveBiomeTreat("biome_treat", CollectionDisplayMode.MISSING, false, true), "missing mode missing treat");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
