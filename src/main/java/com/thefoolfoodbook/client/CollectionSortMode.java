package com.thefoolfoodbook.client;

import net.minecraft.network.chat.Component;

/**
 * 列表排序模式：
 * DEFAULT（默认，按模组/分类内置顺序）、
 * TIME_NEWEST（按获取时间，最新在前）、
 * TIME_OLDEST（按获取时间，最早在前）。
 */
public enum CollectionSortMode {
    DEFAULT("sort_default"),
    TIME_NEWEST("sort_newest"),
    TIME_OLDEST("sort_oldest");

    private static final String KEY_PREFIX = "screen.foolish_wisdom_handbook.";

    private final String translationKey;

    CollectionSortMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(KEY_PREFIX + translationKey);
    }

    public boolean byTime() {
        return this != DEFAULT;
    }

    public boolean newestFirst() {
        return this == TIME_NEWEST;
    }

    public CollectionSortMode next() {
        return switch (this) {
            case DEFAULT -> TIME_NEWEST;
            case TIME_NEWEST -> TIME_OLDEST;
            case TIME_OLDEST -> DEFAULT;
        };
    }
}
