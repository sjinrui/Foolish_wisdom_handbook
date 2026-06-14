package com.thefoolfoodbook.common.book;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CatFoxCollectionScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatFoxCollectionScanner.class);
    private static final String IDENTITY_TYPE_CLASS = "liushuangwuyin.bettermorph.api.variant.IdentityType";
    private static final String PLAYER_UNLOCKS_CLASS = "liushuangwuyin.bettermorph.api.PlayerUnlocks";
    private static final String UPGRADE_ORB_CLASS = "io.redspace.ironsspellbooks.item.UpgradeOrbItem";
    private static final String SPELL_REGISTRY_CLASS = "io.redspace.ironsspellbooks.api.registry.SpellRegistry";
    private static final String ABSTRACT_SPELL_CLASS = "io.redspace.ironsspellbooks.api.spells.AbstractSpell";
    private static final String SPELL_CONTAINER_CLASS = "io.redspace.ironsspellbooks.api.spells.ISpellContainer";
    private static final String CREATE_RECIPE_TYPES_CLASS = "com.simibubi.create.AllRecipeTypes";
    private static final String EXPLORERS_COMPASS_CLASS = "com.chaosthedude.explorerscompass.ExplorersCompass";
    private static final List<RelicDefinition> RELICS = List.of(
            new RelicDefinition("cat_sin", "\u7f6a\u4e4b\u732b"),
            new RelicDefinition("cat_dream", "\u68a6\u4e4b\u732b"),
            new RelicDefinition("cat_mana", "\u62fe\u9b54\u4e4b\u732b"),
            new RelicDefinition("cat_dragon_book", "\u4e66\u9f99\u4e4b\u732b"),
            new RelicDefinition("cat_sacrifice", "\u732e\u796d\u4e4b\u732b"),
            new RelicDefinition("cat_tenacity", "\u575a\u97e7\u4e4b\u732b"),
            new RelicDefinition("cat_brewing", "\u917f\u6001\u4e4b\u732b"),
            new RelicDefinition("fox_blood_echo", "\u8840\u9e23\u4e4b\u72d0"),
            new RelicDefinition("fox_resonance", "\u5171\u632f\u4e4b\u72d0"),
            new RelicDefinition("fox_exploration", "\u63a2\u9669\u4e4b\u72d0"),
            new RelicDefinition("fox_companion", "\u966a\u4f34\u4e4b\u72d0"),
            new RelicDefinition("fox_gluttony", "\u8d2a\u98df\u4e4b\u72d0"),
            new RelicDefinition("fox_hunting", "\u72e9\u730e\u4e4b\u72d0")
    );
    private static final List<String> COMMON_DAMAGE_TYPES = List.of(
            "inFire", "lightningBolt", "onFire", "lava", "hotFloor", "inWall", "cramming", "drown",
            "starve", "cactus", "fall", "flyIntoWall", "outOfWorld", "generic", "magic", "wither",
            "dragonBreath", "dryout", "sweetBerryBush", "freeze", "fallingBlock", "anvil", "fallingStalactite",
            "stalagmite", "mob", "mob.player", "player", "arrow", "trident", "fireworks", "fireball",
            "unattributedFireball", "witherSkull", "thrown", "indirectMagic", "thorns", "explosion",
            "playerExplosion", "sonic_boom", "badRespawnPoint", "sting", "falling_block", "spit"
    );
    private static List<Item> cachedUpgradeOrbs;
    private static List<Item> cachedSacrificeItems;
    private static List<Block> cachedOreBlocks;
    private static List<EntityType<?>> cachedObservableEntities;
    private static List<EntityType<?>> cachedBossEntities;

    private CatFoxCollectionScanner() {
    }

    public static List<ChapterDefinition> chapterDefinitions(Player player, Function<String, CollectionGrouping> grouping, CollectionDisplayMode displayMode) {
        return chapterDefinitions(player, grouping, displayMode, ServerCollectionSnapshot.empty());
    }

    public static List<ChapterDefinition> chapterDefinitions(Player player, Function<String, CollectionGrouping> grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        return RELICS.stream()
                .map(relic -> new ChapterDefinition(relic.id(), relic.title(), () -> createChapter(player, relic.id(), grouping.apply(relic.id()), displayMode, serverSnapshot)))
                .toList();
    }

    public static boolean isCatFoxChapter(String chapterId) {
        for (RelicDefinition relic : RELICS) {
            if (relic.id().equals(chapterId)) {
                return true;
            }
        }
        return false;
    }

    private static CollectionChapter createChapter(Player player, String id, CollectionGrouping grouping, CollectionDisplayMode displayMode) {
        return createChapter(player, id, grouping, displayMode, ServerCollectionSnapshot.empty());
    }

    private static CollectionChapter createChapter(Player player, String id, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        String title = title(id);
        if (player == null) {
            return CollectionChapter.empty(id, title, false);
        }

        return switch (id) {
            case "cat_sin" -> morphChapter(player, id, title, grouping, displayMode, type -> type.getCategory() == MobCategory.MONSTER, "enemy", serverSnapshot);
            case "cat_dream" -> morphChapter(player, id, title, grouping, displayMode, type -> type.getCategory() != MobCategory.MONSTER && type.getCategory() != MobCategory.MISC, "friendly", serverSnapshot);
            case "cat_mana" -> manaChapter(player, grouping, displayMode, serverSnapshot);
            case "cat_dragon_book" -> enchantmentChapter(player, grouping, displayMode, serverSnapshot);
            case "cat_sacrifice" -> itemChapter(player, id, title, grouping, displayMode, sacrificeItems(), aomExtraItemSet(player, serverSnapshot, "sacrifice_item"), "relics", "\u9057\u7269", true, aomAvailable(player, serverSnapshot));
            case "cat_tenacity" -> damageChapter(player, grouping, displayMode, serverSnapshot);
            case "cat_brewing" -> effectChapter(player, grouping, displayMode, serverSnapshot);
            case "fox_blood_echo" -> itemChapter(player, id, title, grouping, displayMode, recipeCandidateItems(player.level(), serverSnapshot, ServerCollectionSnapshot.KEY_FOX_BLOOD_ECHO_CANDIDATES, RecipeType.CAMPFIRE_COOKING), aomExtraItemSet(player, serverSnapshot, "blood_fox_item"), "meat", "\u751f\u8089", true, aomAvailable(player, serverSnapshot));
            case "fox_resonance" -> itemChapter(player, id, title, grouping, displayMode, recipeCandidateItems(player.level(), serverSnapshot, ServerCollectionSnapshot.KEY_FOX_RESONANCE_CANDIDATES, null), aomExtraItemSet(player, serverSnapshot, "resonance_fox_item"), "materials", "\u7c89\u788e\u6750\u6599", true, aomAvailable(player, serverSnapshot));
            case "fox_exploration" -> structureChapter(player, grouping, displayMode, serverSnapshot);
            case "fox_companion" -> observableEntityChapter(player, grouping, displayMode, serverSnapshot);
            case "fox_gluttony" -> oreChapter(player, grouping, displayMode, serverSnapshot);
            case "fox_hunting" -> bossChapter(player, grouping, displayMode, serverSnapshot);
            default -> CollectionChapter.empty(id, title, true);
        };
    }

    private static CollectionChapter manaChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collectedOrbs = aomExtraItemSet(player, serverSnapshot, "upgrade_orb");
        Set<String> collectedSpells = aomExtraStringSet(player, serverSnapshot, "spell_scroll");
        List<CollectionEntry> orbEntries = itemEntries(upgradeOrbs(), collectedOrbs, true, displayMode);
        List<CollectionEntry> spellEntries = spellEntries(player, collectedSpells, displayMode);
        List<CollectionEntry> displayed = new ArrayList<>();
        displayed.addAll(orbEntries);
        displayed.addAll(spellEntries);
        List<CollectionGroup> groups = grouping == CollectionGrouping.MOD
                ? groupByMod(displayed)
                : groups(
                new CollectionGroup("upgrade_orbs", "\u5347\u7ea7\u6cd5\u7403", orbEntries),
                new CollectionGroup("spell_scrolls", "\u6cd5\u672f\u5377\u8f74", spellEntries)
        );
        int total = upgradeOrbs().size() + allSpells(player).size();
        int collected = Math.min(total, collectedOrbs.size() + collectedSpells.size());
        return new CollectionChapter("cat_mana", "\u62fe\u9b54\u4e4b\u732b", groups, total, collected, aomAvailable(player, serverSnapshot));
    }

    private static CollectionChapter enchantmentChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "enhancement_book");
        List<CollectionEntry> entries = new ArrayList<>();
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            if (id == null) {
                continue;
            }
            boolean isCollected = collected.contains(id.toString());
            if (displayMode.includes(isCollected)) {
                entries.add(enchantmentEntry(id.toString(), enchantment, isCollected));
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter("cat_dragon_book", "\u4e66\u9f99\u4e4b\u732b", grouping, entries, ForgeRegistries.ENCHANTMENTS.getValues().size(), collected.size(), aomAvailable(player, serverSnapshot), "enchantments", "\u9644\u9b54");
    }

    private static CollectionChapter damageChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "damage_type");
        Set<String> ids = new TreeSet<>(COMMON_DAMAGE_TYPES);
        ids.addAll(collected);
        List<CollectionEntry> entries = new ArrayList<>();
        for (String damageId : ids) {
            boolean isCollected = collected.contains(damageId);
            if (displayMode.includes(isCollected)) {
                entries.add(genericItemEntry("damage:" + damageId, Component.translatable("death.attack." + damageId).getString(), Items.SHIELD, false, isCollected, false));
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter("cat_tenacity", "\u575a\u97e7\u4e4b\u732b", grouping, entries, ids.size(), collected.size(), aomAvailable(player, serverSnapshot), "damage", "\u4f24\u5bb3\u7c7b\u578b");
    }

    private static CollectionChapter effectChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "effect_type");
        List<CollectionEntry> entries = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (id == null) {
                continue;
            }
            boolean isCollected = collected.contains(effect.getDescriptionId());
            if (displayMode.includes(isCollected)) {
                entries.add(new CollectionEntry(id.toString(), effect.getDisplayName().getString(), potionStackForEffect(effect), null, false, isCollected, true));
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter("cat_brewing", "\u917f\u6001\u4e4b\u732b", grouping, entries, ForgeRegistries.MOB_EFFECTS.getValues().size(), collected.size(), aomAvailable(player, serverSnapshot), "effects", "BUFF");
    }

    private static CollectionChapter structureChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "explor_structure");
        List<String> localStructureIds = new ArrayList<>();
        if (player.level() != null) {
            player.level().registryAccess()
                    .registry(Registries.STRUCTURE)
                    .map(Registry::keySet)
                    .ifPresent(keys -> keys.forEach(id -> localStructureIds.add(id.toString())));
        }
        Collection<?> primaryStructureIds = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_STRUCTURES)
                ? serverSnapshot.values(ServerCollectionSnapshot.KEY_FOX_EXPLORATION_CANDIDATES)
                : explorersCompassAllowedStructureIds();
        List<ResourceLocation> ids = mergeStructureCandidateIds(primaryStructureIds, localStructureIds, collected);

        List<CollectionEntry> entries = new ArrayList<>();
        for (ResourceLocation id : ids) {
            boolean isCollected = collected.contains(id.toString());
            if (displayMode.includes(isCollected)) {
                entries.add(genericItemEntry(id.toString(), structureDisplayName(id), itemById("explorerscompass:explorerscompass", Items.COMPASS), false, isCollected, false));
            }
        }
        return collectionChapter("fox_exploration", "\u63a2\u9669\u4e4b\u72d0", grouping, entries, ids.size(), collected.size(), aomAvailable(player, serverSnapshot), "structures", "\u7ed3\u6784");
    }

    public static String structureDisplayName(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed == null ? id : structureDisplayName(parsed);
    }

    static String structureDisplayName(ResourceLocation id) {
        String compassName = explorersCompassPrettyStructureName(id);
        if (!compassName.isBlank() && !compassName.equals(id.toString())) {
            return compassName;
        }

        String dottedPath = id.getPath().replace('/', '.');
        String[] keys = {
                "structure." + id.getNamespace() + "." + dottedPath,
                "structure." + id.getNamespace() + "." + id.getPath(),
                "configured_structure_feature." + id.getNamespace() + "." + dottedPath,
                "explorerscompass.structure." + id.getNamespace() + "." + dottedPath
        };
        for (String key : keys) {
            String translated = Component.translatable(key).getString();
            if (!translated.equals(key) && !translated.isBlank()) {
                return translated;
            }
        }
        return readableId(id);
    }

    private static String explorersCompassPrettyStructureName(ResourceLocation id) {
        try {
            Class<?> structureUtils = Class.forName("com.chaosthedude.explorerscompass.util.StructureUtils", false, CatFoxCollectionScanner.class.getClassLoader());
            Method prettyName = structureUtils.getMethod("getPrettyStructureName", ResourceLocation.class);
            Object result = prettyName.invoke(null, id);
            return result instanceof String text ? text : "";
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("Could not read Explorer's Compass structure display name", ex);
            return "";
        }
    }

    static List<ResourceLocation> mergeStructureCandidateIds(Collection<?> explorersCompassIds, Collection<?> localRegistryIds, Collection<?> collectedIds) {
        Set<ResourceLocation> ids = new TreeSet<>(Comparator.comparing(ResourceLocation::toString));
        Collection<?> primaryCandidates = explorersCompassIds.isEmpty() ? localRegistryIds : explorersCompassIds;
        addParsedResourceLocations(ids, primaryCandidates);
        addParsedResourceLocations(ids, collectedIds);
        return List.copyOf(ids);
    }

    private static void addParsedResourceLocations(Set<ResourceLocation> target, Collection<?> values) {
        for (Object value : values) {
            ResourceLocation parsed = parseResourceLocation(value);
            if (parsed != null) {
                target.add(parsed);
            }
        }
    }

    private static List<String> explorersCompassAllowedStructureIds() {
        try {
            Class<?> compassClass = Class.forName(EXPLORERS_COMPASS_CLASS, false, CatFoxCollectionScanner.class.getClassLoader());
            Object value = compassClass.getField("allowedStructureKeys").get(null);
            if (!(value instanceof Collection<?> structures)) {
                return List.of();
            }
            List<String> ids = new ArrayList<>();
            for (Object structure : structures) {
                ResourceLocation parsed = parseResourceLocation(structure);
                if (parsed != null) {
                    ids.add(parsed.toString());
                }
            }
            return ids;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read Explorer's Compass structure cache", ex);
            return List.of();
        }
    }

    private static ResourceLocation parseResourceLocation(Object value) {
        if (value instanceof ResourceLocation id) {
            return id;
        }
        if (value instanceof CharSequence text) {
            return ResourceLocation.tryParse(text.toString());
        }
        return null;
    }

    private static CollectionChapter observableEntityChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "spyglass_record");
        List<CollectionEntry> entries = new ArrayList<>();
        for (EntityType<?> entityType : observableEntities()) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (id == null) {
                continue;
            }
            boolean isCollected = collected.contains(entityType.getDescriptionId());
            if (displayMode.includes(isCollected)) {
                entries.add(CollectionEntry.morph(id.toString(), entityType.getDescription().getString(), entityType, isCollected));
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter("fox_companion", "\u966a\u4f34\u4e4b\u72d0", grouping, entries, observableEntities().size(), collected.size(), aomAvailable(player, serverSnapshot), "entities", "\u89c2\u5bdf\u751f\u7269");
    }

    private static CollectionChapter oreChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        Set<String> collected = aomExtraStringSet(player, serverSnapshot, "gluttony_fox_eat_block");
        List<CollectionEntry> entries = new ArrayList<>();
        for (Block block : oreBlocks()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null) {
                continue;
            }
            boolean isCollected = collected.contains(block.getDescriptionId());
            if (displayMode.includes(isCollected)) {
                Item item = block.asItem();
                if (item != Items.AIR) {
                    entries.add(new CollectionEntry(id.toString(), block.getName().getString(), item.getDefaultInstance(), null, true, isCollected, true));
                }
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter("fox_gluttony", "\u8d2a\u98df\u4e4b\u72d0", grouping, entries, oreBlocks().size(), collected.size(), aomAvailable(player, serverSnapshot), "ores", "\u77ff\u7269");
    }

    private static CollectionChapter bossChapter(Player player, CollectionGrouping grouping, CollectionDisplayMode displayMode, ServerCollectionSnapshot serverSnapshot) {
        return morphChapter(player, "fox_hunting", "\u72e9\u730e\u4e4b\u72d0", grouping, displayMode, CatFoxCollectionScanner::isBossType, "bosses", serverSnapshot);
    }

    private static CollectionChapter morphChapter(Player player, String id, String title, CollectionGrouping grouping, CollectionDisplayMode displayMode, Predicate<EntityType<?>> filter, String groupId, ServerCollectionSnapshot serverSnapshot) {
        List<CollectionEntry> entries = new ArrayList<>();
        int total = 0;
        int collected = 0;
        try {
            Class<?> identityTypeClass = Class.forName(IDENTITY_TYPE_CLASS);
            Class<?> playerUnlocksClass = Class.forName(PLAYER_UNLOCKS_CLASS);
            Method getAllTypes = identityTypeClass.getMethod("getAllTypes", Level.class);
            Method getEntityType = identityTypeClass.getMethod("getEntityType");
            Method hasUnlock = playerUnlocksClass.getMethod("has", Player.class, identityTypeClass);
            boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_BETTERMORPH);
            Set<String> unlockedIds = useServerSnapshot ? serverSnapshot.values(ServerCollectionSnapshot.KEY_MORPH_UNLOCKED) : Set.of();
            Object result = getAllTypes.invoke(null, player.level());
            if (!(result instanceof List<?> identityTypes)) {
                return CollectionChapter.empty(id, title, AomPlayerDataReader.isAvailable(player));
            }

            for (Object identityType : identityTypes) {
                Object entityTypeObject = getEntityType.invoke(identityType);
                if (!(entityTypeObject instanceof EntityType<?> entityType) || !filter.test(entityType)) {
                    continue;
                }
                ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                if (entityId == null) {
                    continue;
                }
                total++;
                boolean unlocked = useServerSnapshot
                        ? unlockedIds.contains(entityId.toString())
                        : Boolean.TRUE.equals(hasUnlock.invoke(null, player, identityType));
                if (unlocked) {
                    collected++;
                }
                if (displayMode.includes(unlocked)) {
                    entries.add(CollectionEntry.morph(entityId.toString(), entityType.getDescription().getString(), entityType, unlocked));
                }
            }
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read BetterMorph records for {}", id, ex);
            return CollectionChapter.empty(id, title, false);
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return collectionChapter(id, title, grouping, entries, total, collected, true, groupId, groupTitle(id));
    }

    private static CollectionChapter itemChapter(Player player, String id, String title, CollectionGrouping grouping, CollectionDisplayMode displayMode, Collection<Item> candidates, Set<String> collectedIds, String groupId, String groupTitle, boolean bookmarkable, boolean sourceAvailable) {
        List<CollectionEntry> entries = itemEntries(candidates, collectedIds, bookmarkable, displayMode);
        return collectionChapter(id, title, grouping, entries, candidates.size(), collectedIds.size(), sourceAvailable, groupId, groupTitle);
    }

    private static boolean aomAvailable(Player player, ServerCollectionSnapshot serverSnapshot) {
        return serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM) || AomPlayerDataReader.isAvailable(player);
    }

    private static Set<String> aomExtraStringSet(Player player, ServerCollectionSnapshot serverSnapshot, String fieldName) {
        return serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM)
                ? serverSnapshot.values(ServerCollectionSnapshot.extraStringKey(fieldName))
                : AomPlayerDataReader.readExtraStringSet(player, fieldName);
    }

    private static Set<String> aomExtraItemSet(Player player, ServerCollectionSnapshot serverSnapshot, String fieldName) {
        return serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_AOM)
                ? serverSnapshot.values(ServerCollectionSnapshot.extraItemKey(fieldName))
                : AomPlayerDataReader.readExtraItemIdSet(player, fieldName);
    }

    private static List<Item> recipeCandidateItems(Level level, ServerCollectionSnapshot serverSnapshot, String snapshotKey, RecipeType<?> fallbackType) {
        if (serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_RECIPES)) {
            return itemsById(serverSnapshot.values(snapshotKey));
        }
        return fallbackType == null ? createProcessingInputs(level) : cookingInputs(level, fallbackType);
    }

    private static CollectionChapter collectionChapter(String id, String title, CollectionGrouping grouping, List<CollectionEntry> entries, int total, int collected, boolean sourceAvailable, String groupId, String groupTitle) {
        List<CollectionGroup> groups = grouping == CollectionGrouping.MOD
                ? groupByMod(entries)
                : groups(new CollectionGroup(groupId, groupTitle, entries));
        return new CollectionChapter(id, title, groups, total, Math.min(collected, total), sourceAvailable);
    }

    private static List<Item> itemsById(Collection<String> ids) {
        List<Item> items = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation parsed = ResourceLocation.tryParse(id);
            if (parsed == null) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(parsed);
            if (item != null && item != Items.AIR) {
                items.add(item);
            }
        }
        items.sort(Comparator.comparing(item -> item.getDescription().getString()));
        return List.copyOf(items);
    }

    public static Set<String> serverStructureCandidateIds(Level level) {
        List<String> localStructureIds = new ArrayList<>();
        if (level != null) {
            level.registryAccess()
                    .registry(Registries.STRUCTURE)
                    .map(Registry::keySet)
                    .ifPresent(keys -> keys.forEach(id -> localStructureIds.add(id.toString())));
        }
        List<ResourceLocation> ids = mergeStructureCandidateIds(explorersCompassAllowedStructureIds(), localStructureIds, List.of());
        return ids.stream().map(ResourceLocation::toString).collect(java.util.stream.Collectors.toCollection(TreeSet::new));
    }

    public static Set<String> campfireCookingInputIds(Level level) {
        return itemIds(cookingInputs(level, RecipeType.CAMPFIRE_COOKING));
    }

    public static Set<String> createProcessingInputIds(Level level) {
        return itemIds(createProcessingInputs(level));
    }

    private static Set<String> itemIds(Collection<Item> items) {
        Set<String> ids = new TreeSet<>();
        for (Item item : items) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null) {
                ids.add(id.toString());
            }
        }
        return ids;
    }

    private static List<CollectionEntry> itemEntries(Collection<Item> items, Set<String> collectedIds, boolean bookmarkable, CollectionDisplayMode displayMode) {
        List<CollectionEntry> entries = new ArrayList<>();
        for (Item item : items) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) {
                continue;
            }
            boolean collected = collectedIds.contains(id.toString());
            if (displayMode.includes(collected)) {
                CollectionEntry entry = CollectionEntry.item(item, bookmarkable, collected);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return entries;
    }

    private static List<CollectionEntry> spellEntries(Player player, Set<String> collectedIds, CollectionDisplayMode displayMode) {
        List<CollectionEntry> entries = new ArrayList<>();
        for (SpellCandidate spell : allSpells(player)) {
            boolean collected = collectedIds.contains(spell.id());
            if (displayMode.includes(collected)) {
                entries.add(spellEntry(spell, collected));
            }
        }
        entries.sort(Comparator.comparing(CollectionEntry::displayName).thenComparing(CollectionEntry::id));
        return entries;
    }

    private static CollectionEntry spellEntry(SpellCandidate spell, boolean collected) {
        return new CollectionEntry(spell.id(), spell.displayName(), spellScrollStack(spell), null, true, collected, true);
    }

    private static ItemStack spellScrollStack(SpellCandidate spell) {
        ItemStack stack = itemById("irons_spellbooks:scroll", Items.PAPER).getDefaultInstance();
        try {
            if (spell.spellObject() == null) {
                return stack;
            }

            Class<?> spellClass = Class.forName(ABSTRACT_SPELL_CLASS);
            Class<?> containerClass = Class.forName(SPELL_CONTAINER_CLASS);
            Method createScrollContainer = containerClass.getMethod("createScrollContainer", spellClass, int.class, ItemStack.class);
            createScrollContainer.invoke(null, spell.spellObject(), spellMinLevel(spell.spellObject()), stack);
            return stack;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("Could not create spell scroll stack for {}", spell.id(), ex);
            return stack;
        }
    }

    private static int spellMinLevel(Object spell) {
        try {
            Method getMinLevel = spell.getClass().getMethod("getMinLevel");
            Object value = getMinLevel.invoke(spell);
            return value instanceof Number number ? Math.max(1, number.intValue()) : 1;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("Could not read spell min level", ex);
            return 1;
        }
    }

    private static CollectionEntry enchantmentEntry(String id, Enchantment enchantment, boolean collected) {
        int minLevel = Math.max(1, enchantment.getMinLevel());
        ItemStack stack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, minLevel));
        return new CollectionEntry(id, enchantment.getFullname(minLevel).getString(), stack, null, true, collected, false);
    }

    public static CollectionEntry enchantmentEntryForTest(String id, String displayName, int minLevel, boolean collected) {
        return new CollectionEntry(id, displayName, null, null, true, collected, false);
    }

    public static CollectionEntry spellEntryForTest(String id, String displayName, boolean collected) {
        return new CollectionEntry(id, displayName, null, null, true, collected, true);
    }

    private static ItemStack potionStackForEffect(MobEffect effect) {
        Potion best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
            if (potion == Potions.EMPTY) {
                continue;
            }
            boolean containsEffect = false;
            for (MobEffectInstance instance : potion.getEffects()) {
                if (instance.getEffect() == effect) {
                    containsEffect = true;
                    break;
                }
            }
            if (!containsEffect) {
                continue;
            }
            int score = potionScore(effect, potion);
            if (score < bestScore) {
                best = potion;
                bestScore = score;
            }
        }
        return potionStack(best == null ? Potions.WATER : best);
    }

    private static int potionScore(MobEffect effect, Potion potion) {
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(potion);
        String effectPath = effectId == null ? "" : effectId.getPath();
        String potionPath = potionId == null ? "" : potionId.getPath();
        int score = potion.getEffects().size() * 10;
        if (potionPath.equals(effectPath)) {
            score -= 8;
        }
        if (potionPath.startsWith("long_") || potionPath.startsWith("strong_")) {
            score += 5;
        }
        return score;
    }

    private static ItemStack potionStack(Potion potion) {
        ItemStack stack = Items.POTION.getDefaultInstance();
        PotionUtils.setPotion(stack, potion);
        return stack;
    }

    private static List<SpellCandidate> allSpells(Player player) {
        try {
            Class<?> registryClass = Class.forName(SPELL_REGISTRY_CLASS);
            Method getEnabledSpells = registryClass.getMethod("getEnabledSpells");
            Object result = getEnabledSpells.invoke(null);
            if (!(result instanceof List<?> spells)) {
                return List.of();
            }
            List<SpellCandidate> candidates = new ArrayList<>();
            for (Object spell : spells) {
                Method getSpellId = spell.getClass().getMethod("getSpellId");
                Method getSpellName = spell.getClass().getMethod("getSpellName");
                String id = String.valueOf(getSpellId.invoke(spell));
                String displayName = String.valueOf(getSpellName.invoke(spell));
                try {
                    Method getDisplayName = spell.getClass().getMethod("getDisplayName", Player.class);
                    Object component = getDisplayName.invoke(spell, player);
                    if (component instanceof Component text) {
                        displayName = text.getString();
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Older Iron's Spells builds still expose getSpellName.
                }
                candidates.add(new SpellCandidate(id, displayName, spell));
            }
            candidates.sort(Comparator.comparing(SpellCandidate::displayName).thenComparing(SpellCandidate::id));
            return candidates;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read Iron's Spells spell registry", ex);
            return List.of();
        }
    }

    private static List<Item> upgradeOrbs() {
        if (cachedUpgradeOrbs == null) {
            try {
                Class<?> upgradeOrbClass = Class.forName(UPGRADE_ORB_CLASS);
                cachedUpgradeOrbs = ForgeRegistries.ITEMS.getValues().stream()
                        .filter(upgradeOrbClass::isInstance)
                        .sorted(Comparator.comparing(item -> item.getDescription().getString()))
                        .toList();
            } catch (ReflectiveOperationException | LinkageError ex) {
                LOGGER.debug("Could not read Iron's Spells upgrade orb class", ex);
                cachedUpgradeOrbs = List.of();
            }
        }
        return cachedUpgradeOrbs;
    }

    private static List<Item> sacrificeItems() {
        if (cachedSacrificeItems == null) {
            TagKey<Item> skullTag = TagKey.create(Registries.ITEM, new ResourceLocation("curios", "skull"));
            cachedSacrificeItems = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item.builtInRegistryHolder().is(skullTag))
                    .sorted(Comparator.comparing(item -> item.getDescription().getString()))
                    .toList();
        }
        return cachedSacrificeItems;
    }

    private static List<Block> oreBlocks() {
        if (cachedOreBlocks == null) {
            cachedOreBlocks = ForgeRegistries.BLOCKS.getValues().stream()
                    .filter(block -> block.defaultBlockState().is(Tags.Blocks.ORES) || block == Blocks.ANCIENT_DEBRIS)
                    .filter(block -> block.asItem() != Items.AIR)
                    .sorted(Comparator.comparing(block -> block.getName().getString()))
                    .toList();
        }
        return cachedOreBlocks;
    }

    private static List<EntityType<?>> observableEntities() {
        if (cachedObservableEntities == null) {
            cachedObservableEntities = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                    .filter(type -> type.getCategory() != MobCategory.MISC)
                    .sorted(Comparator.comparing(type -> type.getDescription().getString()))
                    .toList();
        }
        return cachedObservableEntities;
    }

    private static List<Item> cookingInputs(Level level, RecipeType<?> type) {
        if (level == null) {
            return List.of();
        }
        return recipeInputs(level, type);
    }

    private static List<Item> createProcessingInputs(Level level) {
        if (level == null) {
            return List.of();
        }
        Set<Item> items = new LinkedHashSet<>();
        recipeType("CRUSHING").ifPresent(type -> items.addAll(recipeInputs(level, type)));
        recipeType("MILLING").ifPresent(type -> items.addAll(recipeInputs(level, type)));
        return items.stream()
                .sorted(Comparator.comparing(item -> item.getDescription().getString()))
                .toList();
    }

    private static java.util.Optional<RecipeType<?>> recipeType(String name) {
        try {
            Class<?> allRecipeTypes = Class.forName(CREATE_RECIPE_TYPES_CLASS);
            Object value = Enum.valueOf(allRecipeTypes.asSubclass(Enum.class), name);
            Method getType = allRecipeTypes.getMethod("getType");
            Object type = getType.invoke(value);
            if (type instanceof RecipeType<?> recipeType) {
                return java.util.Optional.of(recipeType);
            }
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read Create recipe type {}", name, ex);
        }
        return java.util.Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Item> recipeInputs(Level level, RecipeType<?> type) {
        RecipeManager manager = level.getRecipeManager();
        Set<Item> items = new LinkedHashSet<>();
        for (Object recipeObject : manager.getAllRecipesFor((RecipeType) type)) {
            Recipe<?> recipe = (Recipe<?>) recipeObject;
            for (Ingredient ingredient : (List<Ingredient>) recipe.getIngredients()) {
                for (ItemStack stack : ingredient.getItems()) {
                    if (!stack.isEmpty()) {
                        items.add(stack.getItem());
                    }
                }
            }
        }
        return items.stream()
                .sorted(Comparator.comparing(item -> item.getDescription().getString()))
                .toList();
    }

    private static List<EntityType<?>> bossEntities() {
        if (cachedBossEntities == null) {
            cachedBossEntities = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                    .filter(CatFoxCollectionScanner::isBossType)
                    .sorted(Comparator.comparing(type -> type.getDescription().getString()))
                    .toList();
        }
        return cachedBossEntities;
    }

    private static boolean isBossType(EntityType<?> entityType) {
        for (int level = 1; level <= 6; level++) {
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("ageofmythology", "boss_level_" + level));
            if (entityType.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static CollectionEntry genericItemEntry(String id, String displayName, Item icon, boolean bookmarkable, boolean collected) {
        return genericItemEntry(id, displayName, icon, bookmarkable, collected, true);
    }

    private static CollectionEntry genericItemEntry(String id, String displayName, Item icon, boolean bookmarkable, boolean collected, boolean recipeOpenable) {
        return new CollectionEntry(id, displayName, icon.getDefaultInstance(), null, bookmarkable, collected, recipeOpenable);
    }

    private static List<CollectionGroup> groupByMod(List<CollectionEntry> entries) {
        return CollectionEntryGrouper.groupByMod(entries, CatFoxCollectionScanner::modName);
    }

    private static List<CollectionGroup> groups(CollectionGroup... groups) {
        List<CollectionGroup> present = new ArrayList<>();
        for (CollectionGroup group : groups) {
            if (!group.entries().isEmpty()) {
                present.add(group);
            }
        }
        return present;
    }

    private static Item itemById(String id, Item fallback) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            return fallback;
        }
        Item item = ForgeRegistries.ITEMS.getValue(parsed);
        return item == null ? fallback : item;
    }

    private static String modName(String namespace) {
        if ("damage".equals(namespace)) {
            return "\u4f24\u5bb3\u7c7b\u578b";
        }
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> !name.isBlank())
                .orElse(namespace);
    }

    private static String title(String id) {
        for (RelicDefinition relic : RELICS) {
            if (relic.id().equals(id)) {
                return relic.title();
            }
        }
        return id;
    }

    private static String groupTitle(String chapterId) {
        return switch (chapterId) {
            case "cat_sin" -> "\u975e\u53cb\u5584\u751f\u7269";
            case "cat_dream" -> "\u53cb\u5584\u751f\u7269";
            case "fox_hunting" -> "BOSS";
            default -> "\u5168\u90e8";
        };
    }

    private static String readableId(ResourceLocation id) {
        String path = id.getPath().replace('_', ' ').replace('/', ' ');
        String[] words = path.split(" ");
        List<String> titled = new ArrayList<>();
        for (String word : words) {
            if (!word.isBlank()) {
                titled.add(word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
            }
        }
        return String.join(" ", titled);
    }

    private record RelicDefinition(String id, String title) {
    }

    private record SpellCandidate(String id, String displayName, Object spellObject) {
    }
}
