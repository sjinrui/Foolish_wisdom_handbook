package com.thefoolfoodbook.common.food;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FoodBookRows {
    private FoodBookRows() {
    }

    public static List<FoodBookRow> visibleRows(Map<String, List<FoodItemInfo>> groupedFoods, Set<String> collapsedGroups) {
        List<FoodBookRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<FoodItemInfo>> entry : groupedFoods.entrySet()) {
            String groupName = entry.getKey();
            boolean collapsed = collapsedGroups.contains(groupName);
            rows.add(new FoodBookRow.Group(groupName, entry.getValue().size(), collapsed));
            if (!collapsed) {
                for (FoodItemInfo food : entry.getValue()) {
                    rows.add(new FoodBookRow.Food(food));
                }
            }
        }
        return rows;
    }
}
