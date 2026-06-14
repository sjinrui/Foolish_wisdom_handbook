package com.thefoolfoodbook.common.book;

import com.thefoolfoodbook.common.food.AomFoodHistory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ServerCollectionSnapshotBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCollectionSnapshotBuilder.class);
    private static final String IDENTITY_TYPE_CLASS = "liushuangwuyin.bettermorph.api.variant.IdentityType";
    private static final String PLAYER_UNLOCKS_CLASS = "liushuangwuyin.bettermorph.api.PlayerUnlocks";

    private ServerCollectionSnapshotBuilder() {
    }

    public static ServerCollectionSnapshot create(ServerPlayer player) {
        if (player == null) {
            return ServerCollectionSnapshot.empty();
        }

        Set<String> modules = new TreeSet<>();
        Map<String, Set<String>> values = new LinkedHashMap<>();
        appendAom(player, modules, values);
        appendBetterMorph(player, modules, values);
        appendStructures(player, modules, values);
        appendRecipes(player, modules, values);
        return ServerCollectionSnapshot.of(modules, values);
    }

    private static void appendAom(ServerPlayer player, Set<String> modules, Map<String, Set<String>> values) {
        if (!AomPlayerDataReader.isAvailable(player) && !AomFoodHistory.isAvailable(player)) {
            return;
        }

        modules.add(ServerCollectionSnapshot.MODULE_AOM);
        values.put(ServerCollectionSnapshot.KEY_FOOD_EATEN, AomFoodHistory.getEatenFoods(player));
        values.put(ServerCollectionSnapshot.KEY_VISIT_BIOMES, AomPlayerDataReader.readStringSet(player, "getVisitBiomes"));
        values.put(ServerCollectionSnapshot.KEY_LISTENED_DISC, AomPlayerDataReader.readStringSet(player, "getListenedDisc"));
        values.put(ServerCollectionSnapshot.KEY_WEAPON_USED, WeaponMasterScanner.usedWeaponIds(player));

        putExtraString(values, player, "spell_scroll");
        putExtraString(values, player, "enhancement_book");
        putExtraString(values, player, "damage_type");
        putExtraString(values, player, "effect_type");
        putExtraString(values, player, "explor_structure");
        putExtraString(values, player, "spyglass_record");
        putExtraString(values, player, "gluttony_fox_eat_block");

        putExtraItem(values, player, "upgrade_orb");
        putExtraItem(values, player, "sacrifice_item");
        putExtraItem(values, player, "blood_fox_item");
        putExtraItem(values, player, "resonance_fox_item");
    }

    private static void appendBetterMorph(ServerPlayer player, Set<String> modules, Map<String, Set<String>> values) {
        Set<String> unlocked = unlockedMorphEntityIds(player);
        if (unlocked == null) {
            return;
        }
        modules.add(ServerCollectionSnapshot.MODULE_BETTERMORPH);
        values.put(ServerCollectionSnapshot.KEY_MORPH_UNLOCKED, unlocked);
    }

    private static void appendStructures(ServerPlayer player, Set<String> modules, Map<String, Set<String>> values) {
        ServerSnapshotCandidateCache.CandidateSets candidates = ServerSnapshotCandidateCache.forLevel(player.level());
        modules.add(ServerCollectionSnapshot.MODULE_STRUCTURES);
        values.put(ServerCollectionSnapshot.KEY_FOX_EXPLORATION_CANDIDATES, candidates.structureIds());
    }

    private static void appendRecipes(ServerPlayer player, Set<String> modules, Map<String, Set<String>> values) {
        ServerSnapshotCandidateCache.CandidateSets candidates = ServerSnapshotCandidateCache.forLevel(player.level());
        modules.add(ServerCollectionSnapshot.MODULE_RECIPES);
        values.put(ServerCollectionSnapshot.KEY_FOX_BLOOD_ECHO_CANDIDATES, candidates.campfireCookingInputIds());
        values.put(ServerCollectionSnapshot.KEY_FOX_RESONANCE_CANDIDATES, candidates.createProcessingInputIds());
    }

    private static void putExtraString(Map<String, Set<String>> values, ServerPlayer player, String fieldName) {
        values.put(ServerCollectionSnapshot.extraStringKey(fieldName), AomPlayerDataReader.readExtraStringSet(player, fieldName));
    }

    private static void putExtraItem(Map<String, Set<String>> values, ServerPlayer player, String fieldName) {
        values.put(ServerCollectionSnapshot.extraItemKey(fieldName), AomPlayerDataReader.readExtraItemIdSet(player, fieldName));
    }

    private static Set<String> unlockedMorphEntityIds(ServerPlayer player) {
        try {
            Class<?> identityTypeClass = Class.forName(IDENTITY_TYPE_CLASS);
            Class<?> playerUnlocksClass = Class.forName(PLAYER_UNLOCKS_CLASS);
            Method getAllTypes = identityTypeClass.getMethod("getAllTypes", net.minecraft.world.level.Level.class);
            Method getEntityType = identityTypeClass.getMethod("getEntityType");
            Method hasUnlock = playerUnlocksClass.getMethod("has", net.minecraft.world.entity.player.Player.class, identityTypeClass);
            Object result = getAllTypes.invoke(null, player.level());
            if (!(result instanceof java.util.List<?> identityTypes)) {
                return Set.of();
            }

            Set<String> ids = new TreeSet<>();
            for (Object identityType : identityTypes) {
                if (!Boolean.TRUE.equals(hasUnlock.invoke(null, player, identityType))) {
                    continue;
                }
                Object entityTypeObject = getEntityType.invoke(identityType);
                if (entityTypeObject instanceof EntityType<?> entityType) {
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                    if (id != null) {
                        ids.add(id.toString());
                    }
                }
            }
            return ids;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not build BetterMorph server snapshot", ex);
            return null;
        }
    }
}
