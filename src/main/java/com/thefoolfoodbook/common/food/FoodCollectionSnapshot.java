package com.thefoolfoodbook.common.food;

import java.util.List;
import java.util.Map;

public record FoodCollectionSnapshot(
        Map<String, List<FoodItemInfo>> uneatenByMod,
        int totalFoodCount,
        int eatenFoodCount,
        int uneatenFoodCount,
        boolean sourceAvailable
) {
    public static FoodCollectionSnapshot empty() {
        return new FoodCollectionSnapshot(Map.of(), 0, 0, 0, false);
    }
}
