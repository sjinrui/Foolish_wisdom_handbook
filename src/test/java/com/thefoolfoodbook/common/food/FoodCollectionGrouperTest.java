package com.thefoolfoodbook.common.food;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FoodCollectionGrouperTest {
    private FoodCollectionGrouperTest() {
    }

    public static void main(String[] args) {
        groupsUneatenFoodsByNamespace();
        keepsGroupsAndEntriesSorted();
        hidesOnlyFoodsInCollapsedGroups();
        computesGridRows();
        System.out.println("FoodCollectionGrouperTest passed");
    }

    private static void groupsUneatenFoodsByNamespace() {
        List<FoodItemInfo> foods = List.of(
                new FoodItemInfo("minecraft:apple", "Apple", "Minecraft"),
                new FoodItemInfo("farmersdelight:apple_pie_slice", "Apple Pie Slice", "Farmer's Delight"),
                new FoodItemInfo("minecraft:bread", "Bread", "Minecraft"),
                new FoodItemInfo("ends_delight:chorus_cookie", "Chorus Cookie", "End's Delight")
        );

        Map<String, List<FoodItemInfo>> grouped = FoodCollectionGrouper.groupUneaten(
                foods,
                Set.of("minecraft:apple", "ends_delight:chorus_cookie")
        );

        assertEquals(2, grouped.size(), "group count");
        assertEquals(List.of("farmersdelight:apple_pie_slice"), ids(grouped.get("Farmer's Delight")), "Farmer's Delight ids");
        assertEquals(List.of("minecraft:bread"), ids(grouped.get("Minecraft")), "Minecraft ids");
    }

    private static void keepsGroupsAndEntriesSorted() {
        List<FoodItemInfo> foods = List.of(
                new FoodItemInfo("zeta:second", "Second", "Zeta"),
                new FoodItemInfo("alpha:z_second", "Z Second", "Alpha"),
                new FoodItemInfo("alpha:a_first", "A First", "Alpha"),
                new FoodItemInfo("minecraft:bread", "Bread", "Minecraft")
        );

        Map<String, List<FoodItemInfo>> grouped = FoodCollectionGrouper.groupUneaten(foods, Set.of());

        assertEquals(List.of("Alpha", "Minecraft", "Zeta"), List.copyOf(grouped.keySet()), "group order");
        assertEquals(List.of("alpha:a_first", "alpha:z_second"), ids(grouped.get("Alpha")), "entry order");
    }

    private static List<String> ids(List<FoodItemInfo> foods) {
        return foods.stream().map(FoodItemInfo::id).toList();
    }

    private static void hidesOnlyFoodsInCollapsedGroups() {
        Map<String, List<FoodItemInfo>> grouped = new LinkedHashMap<>();
        grouped.put("Alpha", List.of(
                new FoodItemInfo("alpha:a_first", "A First", "Alpha"),
                new FoodItemInfo("alpha:z_second", "Z Second", "Alpha")
        ));
        grouped.put("Beta", List.of(
                new FoodItemInfo("beta:snack", "Snack", "Beta")
        ));

        List<FoodBookRow> rows = FoodBookRows.visibleRows(grouped, Set.of("Alpha"));

        assertEquals(3, rows.size(), "visible row count");
        assertEquals("Alpha", ((FoodBookRow.Group) rows.get(0)).name(), "first group row");
        assertEquals(true, ((FoodBookRow.Group) rows.get(0)).collapsed(), "first group collapsed");
        assertEquals("Beta", ((FoodBookRow.Group) rows.get(1)).name(), "second group row");
        assertEquals("beta:snack", ((FoodBookRow.Food) rows.get(2)).food().id(), "visible food row");
    }

    private static void computesGridRows() {
        assertEquals(5, FoodIconGridMetrics.columns(100, 20), "grid columns");
        assertEquals(1, FoodIconGridMetrics.columns(8, 20), "minimum grid columns");
        assertEquals(0, FoodIconGridMetrics.rows(0, 5), "empty grid rows");
        assertEquals(3, FoodIconGridMetrics.rows(11, 5), "partial grid rows");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
