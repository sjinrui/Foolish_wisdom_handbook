package com.thefoolfoodbook.common.food;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class FoodCollectionGrouper {
    private static final Comparator<FoodItemInfo> FOOD_ORDER = Comparator
            .comparing(FoodItemInfo::displayName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(FoodItemInfo::id, String.CASE_INSENSITIVE_ORDER);

    private FoodCollectionGrouper() {
    }

    public static Map<String, List<FoodItemInfo>> groupUneaten(Collection<FoodItemInfo> allFoods, Set<String> eatenIds) {
        return allFoods.stream()
                .filter(food -> !eatenIds.contains(food.id()))
                .sorted(Comparator.comparing(FoodItemInfo::modName, String.CASE_INSENSITIVE_ORDER).thenComparing(FOOD_ORDER))
                .collect(Collectors.groupingBy(FoodItemInfo::modName, LinkedHashMap::new, Collectors.toList()));
    }
}
