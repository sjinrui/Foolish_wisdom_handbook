package com.thefoolfoodbook.common.book;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MorphCollectionScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MorphCollectionScanner.class);
    private static final String IDENTITY_TYPE_CLASS = "liushuangwuyin.bettermorph.api.variant.IdentityType";
    private static final String PLAYER_UNLOCKS_CLASS = "liushuangwuyin.bettermorph.api.PlayerUnlocks";
    private static final List<Category> CATEGORIES = List.of(
            new Category("dragon", "\u5de8\u9f99", "dragon"),
            new Category("explode", "\u6838\u7206", "explode"),
            new Category("machine", "\u673a\u68b0", "machine"),
            new Category("magic", "\u9b54\u6cd5", "magic"),
            new Category("goety", "\u5deb\u6cd5", "goety"),
            new Category("abyss", "\u6df1\u6e0a", "abyss"),
            new Category("fire", "\u706b\u7cfb", "fire"),
            new Category("geomancy", "\u5ca9\u7cfb", "geomancy")
    );
    private static List<CategoryCandidates> cachedCandidates;

    private MorphCollectionScanner() {
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
        if (player == null || player.level() == null) {
            return CollectionChapter.empty("morph", "\u53d8\u8eab", false);
        }

        try {
            Class<?> identityTypeClass = Class.forName(IDENTITY_TYPE_CLASS);
            Class<?> playerUnlocksClass = Class.forName(PLAYER_UNLOCKS_CLASS);
            Method hasUnlock = playerUnlocksClass.getMethod("has", Player.class, identityTypeClass);
            boolean useServerSnapshot = serverSnapshot.isModuleAvailable(ServerCollectionSnapshot.MODULE_BETTERMORPH);
            java.util.Set<String> unlockedIds = useServerSnapshot ? serverSnapshot.values(ServerCollectionSnapshot.KEY_MORPH_UNLOCKED) : java.util.Set.of();

            List<CollectionGroup> groups = new ArrayList<>();
            List<CollectionEntry> allMissing = new ArrayList<>();
            int total = 0;
            int collected = 0;
            for (CategoryCandidates category : candidates(player.level(), identityTypeClass)) {
                List<CollectionEntry> displayed = new ArrayList<>();
                for (MorphCandidate candidate : category.candidates()) {
                    total++;
                    boolean unlocked = useServerSnapshot
                            ? unlockedIds.contains(candidate.entry().id())
                            : Boolean.TRUE.equals(hasUnlock.invoke(null, player, candidate.identityType()));
                    if (unlocked) {
                        collected++;
                    }
                    if (displayMode.includes(unlocked)) {
                        CollectionEntry entry = CollectionEntry.morph(
                                candidate.entry().id(),
                                candidate.entry().displayName(),
                                candidate.entry().entityType(),
                                unlocked
                        );
                        displayed.add(entry);
                        allMissing.add(entry);
                    }
                }
                displayed.sort(Comparator.comparing(CollectionEntry::displayName));
                if (!displayed.isEmpty()) {
                    groups.add(new CollectionGroup(category.id(), category.title(), displayed));
                }
            }
            if (grouping == CollectionGrouping.MOD) {
                groups = CollectionEntryGrouper.groupByMod(allMissing, MorphCollectionScanner::modName);
            }
            return new CollectionChapter("morph", "\u53d8\u8eab", groups, total, collected, true);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read BetterMorph manual unlock records", ex);
            return CollectionChapter.empty("morph", "\u53d8\u8eab", false);
        }
    }

    private static List<CategoryCandidates> candidates(Level level, Class<?> identityTypeClass) throws ReflectiveOperationException {
        if (cachedCandidates == null) {
            Method getAllTypes = identityTypeClass.getMethod("getAllTypes", Level.class);
            Method getEntityType = identityTypeClass.getMethod("getEntityType");
            Object result = getAllTypes.invoke(null, level);
            if (!(result instanceof List<?> identityTypes)) {
                cachedCandidates = List.of();
                return cachedCandidates;
            }

            List<CategoryCandidates> categories = new ArrayList<>();
            for (Category category : CATEGORIES) {
                TagKey<EntityType<?>> tag = tag(category.tagPath());
                List<MorphCandidate> morphs = new ArrayList<>();
                for (Object identityType : identityTypes) {
                    Object entityTypeObject = getEntityType.invoke(identityType);
                    if (!(entityTypeObject instanceof EntityType<?> entityType) || !entityType.is(tag)) {
                        continue;
                    }

                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                    if (id != null) {
                        morphs.add(new MorphCandidate(
                                identityType,
                                CollectionEntry.morph(id.toString(), entityType.getDescription().getString(), entityType)
                        ));
                    }
                }
                categories.add(new CategoryCandidates(category.id(), category.title(), List.copyOf(morphs)));
            }
            cachedCandidates = List.copyOf(categories);
        }
        return cachedCandidates;
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("ageofmythology", path));
    }

    private static String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> !name.isBlank())
                .orElse(namespace);
    }

    private record Category(String id, String title, String tagPath) {
    }

    private record CategoryCandidates(String id, String title, List<MorphCandidate> candidates) {
    }

    private record MorphCandidate(Object identityType, CollectionEntry entry) {
    }
}
