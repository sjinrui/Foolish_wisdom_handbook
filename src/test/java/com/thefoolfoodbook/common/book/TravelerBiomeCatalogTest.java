package com.thefoolfoodbook.common.book;

public final class TravelerBiomeCatalogTest {
    private TravelerBiomeCatalogTest() {
    }

    public static void main(String[] args) {
        standardTravelerUsesOnlySevenRecorderTargets();
        overlimitUsesDeduplicatedTheoryList();
        biomeTreatGroupsKeepRequestedDimensionBreakdown();
        System.out.println("TravelerBiomeCatalogTest passed");
    }

    private static void standardTravelerUsesOnlySevenRecorderTargets() {
        assertEquals(7, TravelerBiomeCatalog.STANDARD_TRAVELERS.size(), "standard traveler group count");
        assertEquals(53, TravelerBiomeCatalog.OVERWORLD.size(), "overworld count");
        assertEquals(53, TravelerBiomeCatalog.LOST_CITY.size(), "lost city count");
        assertEquals(5, TravelerBiomeCatalog.NETHER.size(), "nether count");
        assertEquals(5, TravelerBiomeCatalog.END.size(), "end count");
        assertEquals(21, TravelerBiomeCatalog.TWILIGHT_FOREST.size(), "twilight forest count");
        assertEquals(4, TravelerBiomeCatalog.OTHERSIDE.size(), "otherside count");
        assertEquals(4, TravelerBiomeCatalog.AETHER.size(), "aether count");
    }

    private static void overlimitUsesDeduplicatedTheoryList() {
        assertEquals(121, TravelerBiomeCatalog.OVERLIMIT.size(), "overlimit count");
        assertEquals(true, TravelerBiomeCatalog.OVERLIMIT.contains("minecraft:plains"), "overlimit contains overworld biome");
        assertEquals(true, TravelerBiomeCatalog.OVERLIMIT.contains("alexscaves:candy_cavity"), "overlimit contains alex caves biome");
        assertEquals(false, TravelerBiomeCatalog.OVERWORLD.contains("allthemodium:mining"), "overworld excludes ATM mining dimension");
        assertEquals(false, TravelerBiomeCatalog.OVERWORLD.contains("ad_astra:the_beyond"), "overworld excludes The Beyond");
    }

    private static void biomeTreatGroupsKeepRequestedDimensionBreakdown() {
        assertEquals(15, TravelerBiomeCatalog.BIOME_TREAT_GROUPS.size(), "biome treat group count");
        assertEquals(
                java.util.List.of(53, 5, 5, 21, 4, 4, 5, 6, 3, 3, 1, 3, 1, 1, 6),
                TravelerBiomeCatalog.BIOME_TREAT_GROUPS.stream().map(group -> group.biomeIds().size()).toList(),
                "biome treat group sizes"
        );
        assertEquals("natural_overworld", TravelerBiomeCatalog.BIOME_TREAT_GROUPS.get(0).id(), "natural overworld group id");
        assertEquals(false, TravelerBiomeCatalog.BIOME_TREAT_GROUPS.stream().anyMatch(group -> "lostcity".equals(group.dimensionPath())), "lost city excluded");
    }

    private static void assertEquals(Object expected, Object actual, String field) {
        if (!expected.equals(actual)) {
            throw new AssertionError(field + " expected " + expected + " but got " + actual);
        }
    }
}
