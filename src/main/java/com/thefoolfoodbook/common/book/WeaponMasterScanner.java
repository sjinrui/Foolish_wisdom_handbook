package com.thefoolfoodbook.common.book;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class WeaponMasterScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeaponMasterScanner.class);
    private static final String UTIL_CLASS = "com.kurome.ageofmythology.utils.WeaponMasterUtil";
    private static final List<Category> CATEGORIES = List.of(
            new Category("sword", "\u5251", "getSwordList"),
            new Category("axe", "\u65a7", "getAxeList"),
            new Category("hoe", "\u9504", "getHoeList"),
            new Category("pickaxe", "\u9550", "getPickaxeList"),
            new Category("shovel", "\u9539", "getShovelList"),
            new Category("projectile", "\u8fdc\u7a0b\u6b66\u5668", "getProjectileWeaponList"),
            new Category("armor", "\u9632\u5177", "getArmorList"),
            new Category("wand", "\u9b54\u6756", "getWandList"),
            new Category("spell_book", "\u6cd5\u672f\u4e66", "getSpellBookList"),
            new Category("special", "\u7279\u6b8a\u6b66\u5668", "getSpecialList")
    );
    private static List<CategoryItems> cachedCategoryItems;

    private WeaponMasterScanner() {
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
        if (player == null) {
            return CollectionChapter.empty("weapon_master", "\u6b66\u5668\u5927\u5e08", false);
        }

        try {
            Class<?> utilClass = Class.forName(UTIL_CLASS);
            Method hasUsedWeapon = utilClass.getMethod("hasUsedWeapon", Player.class, Item.class);
            boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM);
            Set<String> usedWeaponIds = useServerSnapshot ? serverSnapshot.values(ServerCollectionSnapshot.KEY_WEAPON_USED) : Set.of();
            List<CollectionGroup> defaultGroups = new ArrayList<>();
            List<CollectionEntry> allMissing = new ArrayList<>();
            int total = 0;
            int collected = 0;

            for (CategoryItems category : categoryItems(utilClass)) {
                List<CollectionEntry> displayed = new ArrayList<>();
                for (Item item : category.items()) {
                    total++;
                    boolean used = useServerSnapshot ? usedWeaponIds.contains(itemId(item)) : Boolean.TRUE.equals(hasUsedWeapon.invoke(null, player, item));
                    if (used) {
                        collected++;
                    }

                    if (!displayMode.includes(used)) {
                        continue;
                    }
                    CollectionEntry entry = CollectionEntry.item(item, true, used);
                    if (entry != null) {
                        displayed.add(entry);
                        allMissing.add(entry);
                    }
                }
                if (!displayed.isEmpty()) {
                    displayed.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
                    defaultGroups.add(new CollectionGroup(category.id(), category.title(), displayed));
                }
            }

            List<CollectionGroup> groups = grouping == CollectionGrouping.MOD
                    ? CollectionEntryGrouper.groupByMod(allMissing, WeaponMasterScanner::modName)
                    : defaultGroups;
            return new CollectionChapter("weapon_master", "\u6b66\u5668\u5927\u5e08", groups, total, collected, true);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read AgeOfMythology weapon master records", ex);
            return CollectionChapter.empty("weapon_master", "\u6b66\u5668\u5927\u5e08", false);
        }
    }

    public static Set<String> usedWeaponIds(Player player) {
        if (player == null) {
            return Set.of();
        }
        try {
            Class<?> utilClass = Class.forName(UTIL_CLASS);
            Method hasUsedWeapon = utilClass.getMethod("hasUsedWeapon", Player.class, Item.class);
            Set<String> ids = new java.util.TreeSet<>();
            for (CategoryItems category : categoryItems(utilClass)) {
                for (Item item : category.items()) {
                    if (Boolean.TRUE.equals(hasUsedWeapon.invoke(null, player, item))) {
                        String id = itemId(item);
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
            }
            return ids;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read AgeOfMythology used weapon records", ex);
            return Set.of();
        }
    }

    private static List<CategoryItems> categoryItems(Class<?> utilClass) throws ReflectiveOperationException {
        if (cachedCategoryItems == null) {
            List<CategoryItems> items = new ArrayList<>();
            for (Category category : CATEGORIES) {
                Method listMethod = utilClass.getMethod(category.methodName());
                items.add(new CategoryItems(category.id(), category.title(), readItems(listMethod.invoke(null))));
            }
            cachedCategoryItems = List.copyOf(items);
        }
        return cachedCategoryItems;
    }

    private static Set<Item> readItems(Object result) {
        if (!(result instanceof Collection<?> values)) {
            return Set.of();
        }

        Set<Item> items = new LinkedHashSet<>();
        for (Object value : values) {
            if (value instanceof Item item) {
                items.add(item);
            }
        }
        return items;
    }

    private static String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> !name.isBlank())
                .orElse(namespace);
    }

    private static String itemId(Item item) {
        return ForgeRegistries.ITEMS.getKey(item) == null ? null : ForgeRegistries.ITEMS.getKey(item).toString();
    }

    private record Category(String id, String title, String methodName) {
    }

    private record CategoryItems(String id, String title, Set<Item> items) {
    }
}
