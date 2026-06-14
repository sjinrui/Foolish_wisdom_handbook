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

public final class BiomeTreatCollectionScanner {
    private static final ResourceLocation BIOME_TREAT_ID = new ResourceLocation("alexscaves", "biome_treat");
    private static final String CAVE_BIOME_TAG = "CaveBiome";

    private BiomeTreatCollectionScanner() {
    }

    public static CollectionChapter createChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode) {
        if (player == null) {
            return CollectionChapter.empty("biome_treat", "\u751f\u7269\u7fa4\u7cfb\u68d2\u7cd6", false);
        }

        Set<String> collectedTreats = BiomeTreatProgress.loadAndScan(player);
        return grouping == CollectionGrouping.DIMENSION
                ? dimensionChapter(collectedTreats, displayMode)
                : defaultChapter(collectedTreats, displayMode);
    }

    private static CollectionChapter defaultChapter(Set<String> collectedTreats, CollectionDisplayMode displayMode) {
        ScanResult result = scanBiomeTreats(TravelerBiomeCatalog.OVERLIMIT, collectedTreats, displayMode);
        List<CollectionGroup> groups = result.entries().isEmpty()
                ? List.of()
                : List.of(new CollectionGroup("biome_treat", "\u5168\u90e8", result.entries()));
        return new CollectionChapter("biome_treat", "\u751f\u7269\u7fa4\u7cfb\u68d2\u7cd6", groups, result.total(), result.collected(), true);
    }

    private static CollectionChapter dimensionChapter(Set<String> collectedTreats, CollectionDisplayMode displayMode) {
        List<CollectionGroup> groups = new ArrayList<>();
        int total = 0;
        int collected = 0;
        for (TravelerBiomeCatalog.TargetGroup group : TravelerBiomeCatalog.BIOME_TREAT_GROUPS) {
            ScanResult result = scanBiomeTreats(group.biomeIds(), collectedTreats, displayMode);
            total += result.total();
            collected += result.collected();
            if (!result.entries().isEmpty()) {
                groups.add(new CollectionGroup(group.id(), group.title(), result.entries()));
            }
        }
        return new CollectionChapter("biome_treat", "\u751f\u7269\u7fa4\u7cfb\u68d2\u7cd6", groups, total, collected, true);
    }

    private static ScanResult scanBiomeTreats(List<String> biomeIds, Set<String> biomeTreats, CollectionDisplayMode displayMode) {
        List<CollectionEntry> displayed = new ArrayList<>();
        int total = 0;
        int collected = 0;
        for (String biomeId : biomeIds) {
            ResourceLocation id = ResourceLocation.tryParse(biomeId);
            if (id == null) {
                continue;
            }

            total++;
            boolean hasTreat = biomeTreats.contains(biomeId);
            if (hasTreat) {
                collected++;
            }
            if (displayMode.includes(hasTreat)) {
                displayed.add(biomeEntry(id, hasTreat));
            }
        }
        displayed.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return new ScanResult(total, collected, List.copyOf(displayed));
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

    private static String biomeName(ResourceLocation biomeId) {
        return Component.translatable(Util.makeDescriptionId("biome", biomeId)).getString();
    }

    private record ScanResult(int total, int collected, List<CollectionEntry> entries) {
    }
}
