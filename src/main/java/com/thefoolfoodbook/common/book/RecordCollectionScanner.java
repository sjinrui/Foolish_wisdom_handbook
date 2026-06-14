package com.thefoolfoodbook.common.book;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class RecordCollectionScanner {
    private static List<Item> cachedRecords;

    private RecordCollectionScanner() {
    }

    public static CollectionChapter createChapter(Player player) {
        return createChapter(player, CollectionGrouping.DEFAULT);
    }

    public static CollectionChapter createChapter(Player player, CollectionGrouping grouping) {
        return createChapter(player, grouping, CollectionDisplayMode.MISSING);
    }

    public static CollectionChapter createChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode) {
        return createChapter(player, grouping, displayMode, ServerCollectionSnapshot.empty());
    }

    public static CollectionChapter createChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM);
        Set<String> listened = unwrappedListenedDiscIds(useServerSnapshot
                ? serverSnapshot.values(ServerCollectionSnapshot.KEY_LISTENED_DISC)
                : AomPlayerDataReader.readStringSet(player, "getListenedDisc"));
        List<Item> records = records();

        List<CollectionEntry> displayed = new ArrayList<>();
        int collected = 0;
        for (Item item : records) {
            boolean isListened = listened.contains(item.getDescriptionId());
            if (isListened) {
                collected++;
            }
            if (!displayMode.includes(isListened)) {
                continue;
            }

            CollectionEntry entry = recordEntry(item, isListened);
            if (entry != null) {
                displayed.add(entry);
            }
        }

        List<CollectionGroup> groups = grouping == CollectionGrouping.MOD
                ? CollectionEntryGrouper.groupByMod(displayed, RecordCollectionScanner::modName)
                : defaultGroups(displayed);
        return new CollectionChapter(
                "autism",
                "\u5b64\u72ec\u75c7\u60a3\u8005",
                groups,
                records.size(),
                collected,
                useServerSnapshot || AomPlayerDataReader.isAvailable(player)
        );
    }

    private static List<CollectionGroup> defaultGroups(List<CollectionEntry> missing) {
        return missing.isEmpty()
                ? List.of()
                : List.of(new CollectionGroup("records", "\u5531\u7247", missing));
    }

    private static List<Item> records() {
        if (cachedRecords == null) {
            cachedRecords = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item instanceof RecordItem)
                    .sorted(Comparator.comparing(RecordCollectionScanner::recordDisplayName))
                    .toList();
        }
        return cachedRecords;
    }

    private static CollectionEntry recordEntry(Item item, boolean listened) {
        if (!(item instanceof RecordItem)) {
            return CollectionEntry.item(item, true, listened);
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            return null;
        }
        return new CollectionEntry(id.toString(), recordDisplayName(item), item.getDefaultInstance(), null, true, listened, true);
    }

    private static String recordDisplayName(Item item) {
        if (item instanceof RecordItem recordItem) {
            return recordItem.getDisplayName().getString();
        }
        return item.getDescription().getString();
    }

    private static String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> !name.isBlank())
                .orElse(namespace);
    }

    private static Set<String> unwrappedListenedDiscIds(Set<String> wrappedIds) {
        Set<String> result = new TreeSet<>();
        for (String id : wrappedIds) {
            if (id.startsWith("listen:")) {
                result.add(id.substring("listen:".length()));
            } else {
                result.add(id);
            }
        }
        return result;
    }
}
