package com.thefoolfoodbook.common.book;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TravelerBiomeCatalog {
    public static final List<String> OVERWORLD = List.of(
            "minecraft:sunflower_plains",
            "minecraft:plains",
            "minecraft:birch_forest",
            "minecraft:forest",
            "minecraft:desert",
            "minecraft:bamboo_jungle",
            "minecraft:jungle",
            "minecraft:sparse_jungle",
            "minecraft:savanna",
            "minecraft:savanna_plateau",
            "minecraft:badlands",
            "minecraft:wooded_badlands",
            "minecraft:deep_frozen_ocean",
            "minecraft:ice_spikes",
            "minecraft:frozen_peaks",
            "minecraft:cold_ocean",
            "minecraft:deep_cold_ocean",
            "minecraft:frozen_river",
            "minecraft:frozen_ocean",
            "minecraft:snowy_slopes",
            "minecraft:snowy_beach",
            "minecraft:snowy_taiga",
            "minecraft:snowy_plains",
            "minecraft:grove",
            "minecraft:taiga",
            "minecraft:swamp",
            "minecraft:mangrove_swamp",
            "minecraft:dark_forest",
            "minecraft:old_growth_spruce_taiga",
            "minecraft:old_growth_pine_taiga",
            "minecraft:old_growth_birch_forest",
            "minecraft:deep_ocean",
            "minecraft:lukewarm_ocean",
            "minecraft:deep_lukewarm_ocean",
            "minecraft:dripstone_caves",
            "minecraft:stony_shore",
            "minecraft:warm_ocean",
            "minecraft:river",
            "minecraft:ocean",
            "minecraft:flower_forest",
            "minecraft:lush_caves",
            "minecraft:meadow",
            "minecraft:mushroom_fields",
            "minecraft:stony_peaks",
            "minecraft:eroded_badlands",
            "minecraft:deep_dark",
            "minecraft:jagged_peaks",
            "minecraft:cherry_grove",
            "minecraft:beach",
            "minecraft:windswept_hills",
            "minecraft:windswept_forest",
            "minecraft:windswept_gravelly_hills",
            "minecraft:windswept_savanna"
    );
    public static final List<String> LOST_CITY = OVERWORLD;
    public static final List<String> NETHER = List.of(
            "minecraft:soul_sand_valley",
            "minecraft:warped_forest",
            "minecraft:crimson_forest",
            "minecraft:basalt_deltas",
            "minecraft:nether_wastes"
    );
    public static final List<String> END = List.of(
            "minecraft:end_highlands",
            "minecraft:small_end_islands",
            "minecraft:end_barrens",
            "minecraft:end_midlands",
            "minecraft:the_end"
    );
    public static final List<String> TWILIGHT_FOREST = List.of(
            "twilightforest:dark_forest_center",
            "twilightforest:spooky_forest",
            "twilightforest:enchanted_forest",
            "twilightforest:lake",
            "twilightforest:glacier",
            "twilightforest:underground",
            "twilightforest:clearing",
            "twilightforest:forest",
            "twilightforest:fire_swamp",
            "twilightforest:thornlands",
            "twilightforest:dense_mushroom_forest",
            "twilightforest:final_plateau",
            "twilightforest:mushroom_forest",
            "twilightforest:highlands",
            "twilightforest:oak_savannah",
            "twilightforest:stream",
            "twilightforest:dense_forest",
            "twilightforest:dark_forest",
            "twilightforest:firefly_forest",
            "twilightforest:swamp",
            "twilightforest:snowy_forest"
    );
    public static final List<String> OTHERSIDE = List.of(
            "deeperdarker:echoing_forest",
            "deeperdarker:overcast_columns",
            "deeperdarker:deeplands",
            "deeperdarker:blooming_caverns"
    );
    public static final List<String> AETHER = List.of(
            "aether:skyroot_grove",
            "aether:skyroot_woodland",
            "aether:skyroot_forest",
            "aether:skyroot_meadow"
    );
    public static final List<String> ALEX_CAVES = List.of(
            "alexscaves:magnetic_caves",
            "alexscaves:primordial_caves",
            "alexscaves:toxic_caves",
            "alexscaves:abyssal_chasm",
            "alexscaves:forlorn_hollows",
            "alexscaves:candy_cavity"
    );
    public static final List<String> AOM_FAR_EAST = List.of(
            "ageofmythology:nether_forest",
            "ageofmythology:ender_forest",
            "ageofmythology:rune_plain",
            "ageofmythology:machine_realm",
            "ageofmythology:cursed_realm"
    );
    public static final List<String> AOM_FAR_SOUTH = List.of(
            "ageofmythology:ice_forest",
            "ageofmythology:fire_forest",
            "ageofmythology:blood_forest",
            "ageofmythology:lightning_forest",
            "ageofmythology:holy_forest",
            "ageofmythology:nature_forest"
    );
    public static final List<String> AOM_FAR_WEST = List.of(
            "ageofmythology:deep_desert",
            "ageofmythology:crimson_plain",
            "ageofmythology:flaming_peaks"
    );
    public static final List<String> AOM_FAR_NORTH = List.of(
            "ageofmythology:frozen_peaks",
            "ageofmythology:dread_forest",
            "ageofmythology:sea_spirit"
    );
    public static final List<String> AOM_ABOVE_HELL = List.of("ageofmythology:putrid_island");
    public static final List<String> AOM_BELOW_HELL = List.of(
            "ageofmythology:blood_cave",
            "ageofmythology:soul_cave",
            "ageofmythology:flesh_cave"
    );
    public static final List<String> DIMENSION_DUNGEON = List.of("dimdungeons:biome_dungeon");
    public static final List<String> FOOL_PATH = List.of("minecraft:the_void");
    public static final List<TargetGroup> STANDARD_TRAVELERS = List.of(
            new TargetGroup("overworld", "\u4e3b\u4e16\u754c", "overworld", OVERWORLD),
            new TargetGroup("lostcity", "\u5931\u843d\u4e4b\u57ce", "lostcity", LOST_CITY),
            new TargetGroup("the_nether", "\u4e0b\u754c", "the_nether", NETHER),
            new TargetGroup("the_end", "\u672b\u5730", "the_end", END),
            new TargetGroup("twilight_forest", "\u66ae\u8272\u68ee\u6797", "twilight_forest", TWILIGHT_FOREST),
            new TargetGroup("otherside", "\u5e7d\u51a5", "otherside", OTHERSIDE),
            new TargetGroup("the_aether", "\u5929\u5883", "the_aether", AETHER)
    );
    public static final List<TargetGroup> BIOME_TREAT_GROUPS = List.of(
            new TargetGroup("natural_overworld", "\u81ea\u7136\u65c5\u8005\u00b7\u4e3b\u4e16\u754c", "overworld", OVERWORLD),
            new TargetGroup("the_nether", "\u4e0b\u754c\u65c5\u8005\u00b7\u4e0b\u754c", "the_nether", NETHER),
            new TargetGroup("the_end", "\u672b\u5730\u65c5\u8005\u00b7\u672b\u5730", "the_end", END),
            new TargetGroup("twilight_forest", "\u66ae\u8272\u65c5\u8005\u00b7\u66ae\u8272\u68ee\u6797", "twilight_forest", TWILIGHT_FOREST),
            new TargetGroup("otherside", "\u5e7d\u51a5\u65c5\u8005\u00b7\u5e7d\u51a5", "otherside", OTHERSIDE),
            new TargetGroup("the_aether", "\u5929\u5883\u65c5\u8005\u00b7\u5929\u5883", "the_aether", AETHER),
            new TargetGroup("far_east", "\u6781\u4e1c\u4e4b\u5730", "far_east", AOM_FAR_EAST),
            new TargetGroup("far_south", "\u6781\u5357\u4e4b\u5730", "far_south", AOM_FAR_SOUTH),
            new TargetGroup("far_west", "\u6781\u897f\u4e4b\u5730", "far_west", AOM_FAR_WEST),
            new TargetGroup("far_north", "\u6781\u5317\u4e4b\u5730", "far_north", AOM_FAR_NORTH),
            new TargetGroup("above_hell", "\u5730\u72f1\u4e4b\u4e0a", "above_hell", AOM_ABOVE_HELL),
            new TargetGroup("below_hell", "\u5730\u72f1\u4e4b\u4e0b", "below_hell", AOM_BELOW_HELL),
            new TargetGroup("dimension_dungeon", "\u7ef4\u5ea6\u5730\u7262", "dimension_dungeon", DIMENSION_DUNGEON),
            new TargetGroup("fool_path", "\u611a\u8005\u4e4b\u5f84", "fool_path", FOOL_PATH),
            new TargetGroup("alex_caves", "Alex\u6d1e\u7a74", "alex_caves", ALEX_CAVES)
    );
    public static final List<String> OVERLIMIT = dedupe(
            OVERWORLD,
            TWILIGHT_FOREST,
            OTHERSIDE,
            AETHER,
            ALEX_CAVES,
            NETHER,
            END,
            DIMENSION_DUNGEON,
            FOOL_PATH,
            AOM_FAR_EAST,
            AOM_FAR_SOUTH,
            AOM_FAR_WEST,
            AOM_FAR_NORTH,
            AOM_ABOVE_HELL,
            AOM_BELOW_HELL
    );

    private TravelerBiomeCatalog() {
    }

    @SafeVarargs
    private static List<String> dedupe(List<String>... groups) {
        Set<String> ids = new LinkedHashSet<>();
        for (List<String> group : groups) {
            ids.addAll(group);
        }
        return List.copyOf(new ArrayList<>(ids));
    }

    public record TargetGroup(String id, String title, String dimensionPath, List<String> biomeIds) {
        public TargetGroup {
            biomeIds = List.copyOf(biomeIds);
        }
    }
}
