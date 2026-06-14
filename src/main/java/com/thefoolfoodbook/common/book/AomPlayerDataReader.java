package com.thefoolfoodbook.common.book;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public final class AomPlayerDataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AomPlayerDataReader.class);
    private static final String CAPABILITY_CLASS = "com.kurome.ageofmythology.capability.PlayerDataCapability";
    private static Capability<?> cachedCapability;
    private static boolean loadAttempted;

    private AomPlayerDataReader() {
    }

    public static Set<String> readStringSet(Player player, String methodName) {
        Capability<?> capability = getCapability();
        if (player == null || capability == null) {
            return Set.of();
        }

        return player.getCapability(capability)
                .resolve()
                .map(data -> readStringSet(data, methodName))
                .orElse(Set.of());
    }

    public static boolean isAvailable(Player player) {
        Capability<?> capability = getCapability();
        return player != null && capability != null && player.getCapability(capability).isPresent();
    }

    public static Set<String> readExtraStringSet(Player player, String fieldName) {
        Capability<?> capability = getCapability();
        if (player == null || capability == null) {
            return Set.of();
        }

        return player.getCapability(capability)
                .resolve()
                .map(data -> readExtraStringSet(data, fieldName))
                .orElse(Set.of());
    }

    public static Set<String> readExtraItemIdSet(Player player, String fieldName) {
        Capability<?> capability = getCapability();
        if (player == null || capability == null) {
            return Set.of();
        }

        return player.getCapability(capability)
                .resolve()
                .map(data -> readExtraItemIdSet(data, fieldName))
                .orElse(Set.of());
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
            LOGGER.debug("AgeOfMythology player capability is not available", ex);
        }
        return cachedCapability;
    }

    private static Set<String> readStringSet(Object playerData, String methodName) {
        try {
            Method method = playerData.getClass().getMethod(methodName);
            Object result = method.invoke(playerData);
            if (!(result instanceof Collection<?> values)) {
                return Set.of();
            }

            Set<String> strings = new TreeSet<>();
            for (Object value : values) {
                if (value instanceof String text && !text.isBlank()) {
                    strings.add(text);
                }
            }
            return strings;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read AgeOfMythology player data method {}", methodName, ex);
            return Set.of();
        }
    }

    private static Set<String> readExtraStringSet(Object playerData, String fieldName) {
        Object values = readExtraField(playerData, fieldName);
        if (!(values instanceof Collection<?> collection)) {
            return Set.of();
        }

        Set<String> strings = new TreeSet<>();
        for (Object value : collection) {
            if (value instanceof String text && !text.isBlank()) {
                strings.add(text);
            }
        }
        return strings;
    }

    private static Set<String> readExtraItemIdSet(Object playerData, String fieldName) {
        Object values = readExtraField(playerData, fieldName);
        if (!(values instanceof Collection<?> collection)) {
            return Set.of();
        }

        Set<String> ids = new TreeSet<>();
        for (Object value : collection) {
            if (value instanceof Item item && ForgeRegistries.ITEMS.getKey(item) != null) {
                ids.add(ForgeRegistries.ITEMS.getKey(item).toString());
            }
        }
        return ids;
    }

    private static Object readExtraField(Object playerData, String fieldName) {
        try {
            Method method = playerData.getClass().getMethod("getExtraInfo");
            Object extraInfo = method.invoke(playerData);
            if (extraInfo == null) {
                return null;
            }

            Field field = extraInfo.getClass().getField(fieldName);
            return field.get(extraInfo);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read AgeOfMythology extra info field {}", fieldName, ex);
            return null;
        }
    }
}
