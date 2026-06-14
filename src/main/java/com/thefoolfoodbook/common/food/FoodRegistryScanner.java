package com.thefoolfoodbook.common.food;

import com.thefoolfoodbook.common.book.CollectionChapter;
import com.thefoolfoodbook.common.book.CollectionDisplayMode;
import com.thefoolfoodbook.common.book.CollectionEntry;
import com.thefoolfoodbook.common.book.CollectionGroup;
import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class FoodRegistryScanner {
    private static List<FoodItemInfo> cachedAllFoods;

    private FoodRegistryScanner() {
    }

    public static FoodCollectionSnapshot createSnapshot() {
        Player player = Minecraft.getInstance().player;
        Set<String> eatenFoods = AomFoodHistory.getEatenFoods(player);
        List<FoodItemInfo> allFoods = allFoods();
        Map<String, List<FoodItemInfo>> uneaten = FoodCollectionGrouper.groupUneaten(allFoods, eatenFoods);
        return new FoodCollectionSnapshot(
                uneaten,
                allFoods.size(),
                eatenFoods.size(),
                allFoods.size() - countMatchedFoods(allFoods, eatenFoods),
                AomFoodHistory.isAvailable(player)
        );
    }

    private static List<FoodItemInfo> allFoods() {
        if (cachedAllFoods == null) {
            cachedAllFoods = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(Item::isEdible)
                    .map(FoodRegistryScanner::toFoodInfo)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return cachedAllFoods;
    }

    public static CollectionChapter createChapter() {
        return createChapter(CollectionDisplayMode.MISSING);
    }

    public static CollectionChapter createChapter(CollectionDisplayMode displayMode) {
        return createChapter(displayMode, ServerCollectionSnapshot.empty());
    }

    public static CollectionChapter createChapter(CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Player player = Minecraft.getInstance().player;
        boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM);
        Set<String> eatenFoods = useServerSnapshot
                ? serverSnapshot.values(ServerCollectionSnapshot.KEY_FOOD_EATEN)
                : AomFoodHistory.getEatenFoods(player);
        FoodCollectionSnapshot snapshot = useServerSnapshot
                ? new FoodCollectionSnapshot(Map.of(), allFoods().size(), eatenFoods.size(), allFoods().size() - countMatchedFoods(allFoods(), eatenFoods), true)
                : createSnapshot();
        Map<String, List<FoodItemInfo>> foodsByMod = allFoods().stream()
                .filter(food -> displayMode.includes(eatenFoods.contains(food.id())))
                .collect(java.util.stream.Collectors.groupingBy(
                        FoodItemInfo::modName,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        List<CollectionGroup> groups = foodsByMod.entrySet().stream()
                .map(entry -> new CollectionGroup(entry.getKey(), entry.getKey(), entry.getValue().stream()
                        .map(food -> entryForFood(food, eatenFoods.contains(food.id())))
                        .filter(Objects::nonNull)
                        .toList()))
                .filter(group -> !group.entries().isEmpty())
                .toList();
        return new CollectionChapter(
                "food",
                "\u7f8e\u98df\u5bb6",
                groups,
                snapshot.totalFoodCount(),
                snapshot.eatenFoodCount(),
                snapshot.sourceAvailable()
        );
    }

    private static FoodItemInfo toFoodInfo(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            return null;
        }
        return new FoodItemInfo(id.toString(), item.getDescription().getString(), modName(id.getNamespace()));
    }

    private static Item itemById(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(location);
    }

    private static CollectionEntry entryForFood(FoodItemInfo food, boolean eaten) {
        Item item = itemById(food.id());
        return item == null ? null : CollectionEntry.item(item, true, eaten);
    }

    private static String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> !name.isBlank())
                .orElse(namespace);
    }

    private static int countMatchedFoods(List<FoodItemInfo> allFoods, Set<String> eatenFoods) {
        int count = 0;
        for (FoodItemInfo food : allFoods) {
            if (eatenFoods.contains(food.id())) {
                count++;
            }
        }
        return count;
    }
}
