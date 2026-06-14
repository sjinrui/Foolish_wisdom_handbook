package com.thefoolfoodbook.common.food;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public final class AomFoodHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AomFoodHistory.class);
    private static final String CAPABILITY_CLASS = "com.kurome.ageofmythology.capability.PlayerDataCapability";
    private static Capability<?> cachedCapability;
    private static boolean loadAttempted;

    private AomFoodHistory() {
    }

    public static Set<String> getEatenFoods(Player player) {
        Capability<?> capability = getCapability();
        if (player == null || capability == null) {
            return Set.of();
        }

        return player.getCapability(capability)
                .resolve()
                .map(AomFoodHistory::readFoodList)
                .orElse(Set.of());
    }

    public static boolean isAvailable(Player player) {
        Capability<?> capability = getCapability();
        return player != null && capability != null && player.getCapability(capability).isPresent();
    }

    private static Capability<?> getCapability() {
        if (loadAttempted) {
            return cachedCapability;
        }

        loadAttempted = true;
        try {
            Class<?> capabilityClass = Class.forName(CAPABILITY_CLASS);
            Field instanceField = capabilityClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            if (instance instanceof Capability<?> capability) {
                cachedCapability = capability;
            }
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("AgeOfMythology food capability is not available", ex);
        }
        return cachedCapability;
    }

    private static Set<String> readFoodList(Object playerData) {
        try {
            Method method = playerData.getClass().getMethod("getFoodList");
            Object result = method.invoke(playerData);
            if (!(result instanceof Collection<?> values)) {
                return Set.of();
            }

            Set<String> foods = new TreeSet<>();
            for (Object value : values) {
                if (value instanceof String foodId && !foodId.isBlank()) {
                    foods.add(foodId);
                }
            }
            return foods;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read AgeOfMythology food list", ex);
            return Set.of();
        }
    }
}
