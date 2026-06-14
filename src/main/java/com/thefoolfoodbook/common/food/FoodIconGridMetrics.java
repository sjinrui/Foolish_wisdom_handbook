package com.thefoolfoodbook.common.food;

public final class FoodIconGridMetrics {
    private FoodIconGridMetrics() {
    }

    public static int columns(int width, int cellSize) {
        if (cellSize <= 0) {
            return 1;
        }
        return Math.max(1, width / cellSize);
    }

    public static int rows(int itemCount, int columns) {
        if (itemCount <= 0) {
            return 0;
        }
        return (itemCount + Math.max(1, columns) - 1) / Math.max(1, columns);
    }
}
