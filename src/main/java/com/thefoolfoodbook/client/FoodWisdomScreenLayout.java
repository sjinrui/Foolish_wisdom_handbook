package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionDisplayMode;

public final class FoodWisdomScreenLayout {
    private static final int SUMMARY_SEARCH_GAP = 14;

    private FoodWisdomScreenLayout() {
    }

    public static int summaryTextX(int searchBoxLeft, int searchBoxWidth) {
        return searchBoxLeft + searchBoxWidth + SUMMARY_SEARCH_GAP;
    }

    public static boolean shouldSearchBoxLoseFocus(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX < left || mouseX > right || mouseY < top || mouseY > bottom;
    }

    public static boolean canRemoveBiomeTreat(String chapterId, CollectionDisplayMode displayMode, boolean collected, boolean hasItemIcon) {
        return "biome_treat".equals(chapterId)
                && hasItemIcon
                && collected
                && (displayMode == CollectionDisplayMode.COLLECTED || displayMode == CollectionDisplayMode.ALL);
    }
}
