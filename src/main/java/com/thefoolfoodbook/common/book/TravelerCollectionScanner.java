package com.thefoolfoodbook.common.book;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class TravelerCollectionScanner {
    private static final ResourceLocation BIOME_TREAT_ID = new ResourceLocation("alexscaves", "biome_treat");
    private static final String CAVE_BIOME_TAG = "CaveBiome";

    private TravelerCollectionScanner() {
    }

    public static CollectionChapter createChapter(Player player) {
        return createChapter(player, CollectionDisplayMode.MISSING);
    }

    public static CollectionChapter createChapter(Player player, CollectionDisplayMode displayMode) {
        return createChapter(player, displayMode, ServerCollectionSnapshot.empty());
    }

    public static CollectionChapter createChapter(Player player, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        if (player == null || player.level() == null) {
            return CollectionChapter.empty("traveler", "\u65c5\u884c\u5bb6", false);
        }

        boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM);
        Set<String> visited = useServerSnapshot
                ? serverSnapshot.values(ServerCollectionSnapshot.KEY_VISIT_BIOMES)
                : AomPlayerDataReader.readStringSet(player, "getVisitBiomes");
        List<CollectionGroup> groups = new ArrayList<>();
        int total = 0;
        int collected = 0;

        for (TravelerBiomeCatalog.TargetGroup traveler : TravelerBiomeCatalog.STANDARD_TRAVELERS) {
            ScanResult result = scanDimension(traveler.biomeIds(), visited, traveler.dimensionPath(), displayMode);
            total += result.total();
            collected += result.collected();
            if (!result.missing().isEmpty()) {
                groups.add(new CollectionGroup(traveler.id(), traveler.title(), result.missing()));
            }
        }

        return new CollectionChapter("traveler", "\u65c5\u884c\u5bb6", groups, total, collected, useServerSnapshot || AomPlayerDataReader.isAvailable(player));
    }

    public static CollectionChapter createOverlimitChapter(Player player) {
        return createOverlimitChapter(player, CollectionDisplayMode.MISSING);
    }

    public static CollectionChapter createOverlimitChapter(Player player, CollectionDisplayMode displayMode) {
        return createOverlimitChapter(player, displayMode, ServerCollectionSnapshot.empty());
    }

    public static CollectionChapter createOverlimitChapter(Player player, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        if (player == null || player.level() == null) {
            return CollectionChapter.empty("traveler_overlimit", "\u65c5\u8005\u8d85\u9650", false);
        }

        boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM);
        Set<String> visited = useServerSnapshot
                ? serverSnapshot.values(ServerCollectionSnapshot.KEY_VISIT_BIOMES)
                : AomPlayerDataReader.readStringSet(player, "getVisitBiomes");
        List<CollectionGroup> groups = new ArrayList<>();
        int total = 0;
        int collected = 0;

        for (TravelerBiomeCatalog.TargetGroup traveler : TravelerBiomeCatalog.STANDARD_TRAVELERS) {
            ScanResult result = scanDimension(TravelerBiomeCatalog.OVERLIMIT, visited, traveler.dimensionPath(), displayMode);
            total += result.total();
            collected += result.collected();
            if (!result.missing().isEmpty()) {
                groups.add(new CollectionGroup("overlimit_" + traveler.id(), traveler.title(), result.missing()));
            }
        }

        return new CollectionChapter("traveler_overlimit", "\u65c5\u8005\u8d85\u9650", groups, total, collected, useServerSnapshot || AomPlayerDataReader.isAvailable(player));
    }

    private static ScanResult scanDimension(List<String> biomeIds, Set<String> visited, String dimensionPath, CollectionDisplayMode displayMode) {
        List<CollectionEntry> displayed = new ArrayList<>();
        int total = 0;
        int collected = 0;
        for (String biomeId : biomeIds) {
            ResourceLocation id = ResourceLocation.tryParse(biomeId);
            if (id == null) {
                continue;
            }

            total++;
            boolean isVisited = visited.contains(wrappedVisitKey(dimensionPath, id));
            if (isVisited) {
                collected++;
            }
            if (!displayMode.includes(isVisited)) {
                continue;
            }
            displayed.add(biomeEntry(id, isVisited));
        }
        return new ScanResult(total, collected, sorted(displayed));
    }

    private static CollectionEntry biomeEntry(ResourceLocation biomeId, boolean collected) {
        return new CollectionEntry(biomeId.toString(), biomeName(biomeId), biomeTreatStack(biomeId), null, false, collected, true);
    }

    private static ItemStack biomeTreatStack(ResourceLocation biomeId) {
        Item item = ForgeRegistries.ITEMS.getValue(BIOME_TREAT_ID);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = item.getDefaultInstance();
        stack.getOrCreateTag().putString(CAVE_BIOME_TAG, biomeId.toString());
        return stack;
    }

    private static String wrappedVisitKey(String dimensionPath, ResourceLocation biomeId) {
        return dimensionPath + ":" + Util.makeDescriptionId("biome", biomeId);
    }

    private static String biomeName(ResourceLocation biomeId) {
        return Component.translatable(Util.makeDescriptionId("biome", biomeId)).getString();
    }

    private static List<CollectionEntry> sorted(List<CollectionEntry> entries) {
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return List.copyOf(entries);
    }

    private record ScanResult(int total, int collected, List<CollectionEntry> missing) {
    }
}
