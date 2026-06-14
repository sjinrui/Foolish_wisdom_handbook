package com.thefoolfoodbook.common.book;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ServerCollectionSnapshot {
    public static final String MODULE_AOM = "aom";
    public static final String MODULE_BETTERMORPH = "bettermorph";
    public static final String MODULE_STRUCTURES = "structures";
    public static final String MODULE_RECIPES = "recipes";

    public static final String KEY_FOOD_EATEN = "food.eaten";
    public static final String KEY_VISIT_BIOMES = "aom.visit_biomes";
    public static final String KEY_LISTENED_DISC = "aom.listened_disc";
    public static final String KEY_WEAPON_USED = "aom.weapon_used";
    public static final String KEY_MORPH_UNLOCKED = "bettermorph.unlocked";
    public static final String KEY_FOX_EXPLORATION_CANDIDATES = "fox_exploration.candidates";
    public static final String KEY_FOX_BLOOD_ECHO_CANDIDATES = "fox_blood_echo.candidates";
    public static final String KEY_FOX_RESONANCE_CANDIDATES = "fox_resonance.candidates";

    private static final ServerCollectionSnapshot EMPTY = new ServerCollectionSnapshot(Set.of(), Map.of());

    private final Set<String> availableModules;
    private final Map<String, Set<String>> valuesByKey;

    private ServerCollectionSnapshot(Set<String> availableModules, Map<String, Set<String>> valuesByKey) {
        this.availableModules = Set.copyOf(new TreeSet<>(availableModules));
        Map<String, Set<String>> copied = new LinkedHashMap<>();
        valuesByKey.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> copied.put(entry.getKey(), Set.copyOf(new TreeSet<>(entry.getValue()))));
        this.valuesByKey = Map.copyOf(copied);
    }

    public static ServerCollectionSnapshot empty() {
        return EMPTY;
    }

    public static ServerCollectionSnapshot of(Set<String> availableModules, Map<String, Set<String>> valuesByKey) {
        if (availableModules.isEmpty() && valuesByKey.isEmpty()) {
            return EMPTY;
        }
        return new ServerCollectionSnapshot(availableModules, valuesByKey);
    }

    public static String extraStringKey(String fieldName) {
        return "aom.extra.string." + fieldName;
    }

    public static String extraItemKey(String fieldName) {
        return "aom.extra.item." + fieldName;
    }

    public boolean isEmpty() {
        return availableModules.isEmpty() && valuesByKey.isEmpty();
    }

    public boolean isModuleAvailable(String module) {
        return availableModules.contains(module);
    }

    public Set<String> availableModules() {
        return availableModules;
    }

    public Set<String> values(String key) {
        return valuesByKey.getOrDefault(key, Set.of());
    }

    public Map<String, Set<String>> valuesByKey() {
        return valuesByKey;
    }
}
