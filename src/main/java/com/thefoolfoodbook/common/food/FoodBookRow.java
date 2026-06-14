package com.thefoolfoodbook.common.food;

public sealed interface FoodBookRow permits FoodBookRow.Group, FoodBookRow.Food {
    record Group(String name, int count, boolean collapsed) implements FoodBookRow {
    }

    record Food(FoodItemInfo food) implements FoodBookRow {
    }
}
