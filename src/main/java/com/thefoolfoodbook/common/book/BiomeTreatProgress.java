package com.thefoolfoodbook.common.book;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thefoolfoodbook.TheFoolFoodBook;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;

public final class BiomeTreatProgress {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeTreatProgress.class);
    private static final ResourceLocation BIOME_TREAT_ID = new ResourceLocation("alexscaves", "biome_treat");
    private static final String CAVE_BIOME_TAG = "CaveBiome";
    private static final int INVENTORY_SLOT_LIMIT = 36;
    private static final int OFFHAND_SLOT_LIMIT = 1;
    private static final Map<Path, Set<String>> PROGRESS_CACHE = new HashMap<>();

    private BiomeTreatProgress() {
    }

    public static Set<String> loadAndScan(Player player) {
        if (player == null) {
            return Set.of();
        }

        Path file = progressFile(player);
        return loadAndMerge(file, scanPlayerInventory(player));
    }

    public static Set<String> scanPlayerInventory(Player player) {
        if (player == null) {
            return Set.of();
        }
        return scanPlayerSlots(player.getInventory().items, player.getInventory().offhand, BiomeTreatProgress::biomeFromStack);
    }

    public static <T> Set<String> scanPlayerSlots(List<T> inventory, List<T> offhand, Function<T, String> extractor) {
        TreeSet<String> found = new TreeSet<>();
        scanSlots(found, inventory, INVENTORY_SLOT_LIMIT, extractor);
        scanSlots(found, offhand, OFFHAND_SLOT_LIMIT, extractor);
        return found;
    }

    public static Set<String> merge(Collection<String> existing, Collection<String> discovered) {
        TreeSet<String> merged = new TreeSet<>();
        addValid(merged, existing);
        addValid(merged, discovered);
        return merged;
    }

    public static Set<String> without(Collection<String> existing, String biomeId) {
        TreeSet<String> remaining = new TreeSet<>();
        addValid(remaining, existing);
        if (isValidBiomeId(biomeId)) {
            remaining.remove(biomeId);
        }
        return remaining;
    }

    public static boolean remove(Player player, String biomeId) {
        if (player == null || !isValidBiomeId(biomeId)) {
            return false;
        }

        Path file = progressFile(player);
        return remove(file, biomeId);
    }

    static Set<String> loadAndMerge(Path file, Collection<String> discovered) {
        if (file == null) {
            return merge(Set.of(), discovered);
        }

        Path key = cacheKey(file);
        Set<String> cached = cachedProgress(key);
        Set<String> merged = merge(cached, discovered);
        if (!merged.equals(cached)) {
            write(key, merged);
            remember(key, merged);
        }
        return merged;
    }

    static boolean remove(Path file, String biomeId) {
        if (file == null || !isValidBiomeId(biomeId)) {
            return false;
        }

        Path key = cacheKey(file);
        Set<String> cached = cachedProgress(key);
        Set<String> remaining = without(cached, biomeId);
        if (remaining.equals(cached)) {
            return false;
        }
        write(key, remaining);
        remember(key, remaining);
        return true;
    }

    public static void clearCache() {
        synchronized (PROGRESS_CACHE) {
            PROGRESS_CACHE.clear();
        }
    }

    public static String progressFileName(String worldOrServerId, UUID playerId) {
        String safeScope = worldOrServerId == null || worldOrServerId.isBlank() ? "unknown" : worldOrServerId;
        safeScope = safeScope.replaceAll("[^A-Za-z0-9._-]", "_");
        return safeScope + "_" + playerId + ".json";
    }

    private static <T> void scanSlots(Set<String> found, List<T> slots, int limit, Function<T, String> extractor) {
        if (slots == null || extractor == null) {
            return;
        }

        int count = Math.min(limit, slots.size());
        for (int i = 0; i < count; i++) {
            String biomeId = extractor.apply(slots.get(i));
            if (isValidBiomeId(biomeId)) {
                found.add(biomeId);
            }
        }
    }

    private static void addValid(Set<String> target, Collection<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (isValidBiomeId(value)) {
                target.add(value);
            }
        }
    }

    private static String biomeFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        Item expected = ForgeRegistries.ITEMS.getValue(BIOME_TREAT_ID);
        if (expected == null || stack.getItem() != expected) {
            return "";
        }

        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(CAVE_BIOME_TAG);
    }

    private static boolean isValidBiomeId(String value) {
        return value != null && !value.isBlank() && value.indexOf(':') > 0 && ResourceLocation.tryParse(value) != null;
    }

    private static Path progressFile(Player player) {
        return FMLPaths.CONFIGDIR.get()
                .resolve(TheFoolFoodBook.MOD_ID)
                .resolve("biome_treat_progress")
                .resolve(progressFileName(scopeId(), player.getUUID()));
    }

    private static String scopeId() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCurrentServer() != null && minecraft.getCurrentServer().ip != null) {
            return "server:" + minecraft.getCurrentServer().ip;
        }
        if (minecraft.getSingleplayerServer() != null) {
            return "world:" + minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }
        return "unknown";
    }

    private static Path cacheKey(Path file) {
        return file.toAbsolutePath().normalize();
    }

    private static Set<String> cachedProgress(Path file) {
        synchronized (PROGRESS_CACHE) {
            Set<String> cached = PROGRESS_CACHE.get(file);
            if (cached != null) {
                return cached;
            }
        }

        Set<String> read = read(file);
        remember(file, read);
        return read;
    }

    private static void remember(Path file, Set<String> values) {
        synchronized (PROGRESS_CACHE) {
            PROGRESS_CACHE.put(file, Set.copyOf(new TreeSet<>(values)));
        }
    }

    private static Set<String> read(Path file) {
        if (!Files.isRegularFile(file)) {
            return Set.of();
        }

        TreeSet<String> values = new TreeSet<>();
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                return Set.of();
            }
            for (JsonElement element : root.getAsJsonArray()) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    String value = element.getAsString();
                    if (isValidBiomeId(value)) {
                        values.add(value);
                    }
                }
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.debug("Could not read biome treat progress from {}", file, ex);
        }
        return values;
    }

    private static void write(Path file, Set<String> values) {
        try {
            Files.createDirectories(file.getParent());
            JsonArray array = new JsonArray();
            for (String value : values) {
                array.add(value);
            }
            try (Writer writer = Files.newBufferedWriter(file)) {
                writer.write(array.toString());
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.debug("Could not write biome treat progress to {}", file, ex);
        }
    }
}
