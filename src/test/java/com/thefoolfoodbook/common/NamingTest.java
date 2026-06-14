package com.thefoolfoodbook.common;

import com.thefoolfoodbook.TheFoolFoodBook;
import com.thefoolfoodbook.common.item.FoolishWisdomBookStyle;
import com.thefoolfoodbook.common.registry.ModItems;
import net.minecraft.ChatFormatting;

public final class NamingTest {
    private NamingTest() {
    }

    public static void main(String[] args) {
        exposesReleaseModId();
        registersRenamedBookItem();
        colorsBookNameOrange();
        System.out.println("NamingTest passed");
    }

    private static void exposesReleaseModId() {
        assertEquals("foolish_wisdom_handbook", TheFoolFoodBook.MOD_ID, "mod id");
    }

    private static void registersRenamedBookItem() {
        assertEquals("foolish_wisdom_book", ModItems.BOOK_ITEM_NAME, "book item registry name");
    }

    private static void colorsBookNameOrange() {
        assertEquals(ChatFormatting.GOLD, FoolishWisdomBookStyle.NAME_COLOR, "book name color");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
