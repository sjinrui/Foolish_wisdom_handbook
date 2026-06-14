package com.thefoolfoodbook.common.book;

import com.thefoolfoodbook.common.food.FoodRegistryScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CollectionBookScanner {
    private CollectionBookScanner() {
    }

    public static CollectionBookSnapshot createSnapshot() {
        Player player = Minecraft.getInstance().player;
        ServerCollectionSnapshot serverSnapshot = ServerCollectionSnapshotContext.snapshot();
        return new CollectionBookSnapshot(List.of(
                MorphCollectionScanner.createChapter(player, CollectionGrouping.DEFAULT, CollectionDisplayMode.MISSING, serverSnapshot),
                FoodRegistryScanner.createChapter(CollectionDisplayMode.MISSING, serverSnapshot),
                WeaponMasterScanner.createChapter(player, CollectionGrouping.DEFAULT, CollectionDisplayMode.MISSING, serverSnapshot),
                TravelerCollectionScanner.createChapter(player, CollectionDisplayMode.MISSING, serverSnapshot),
                TravelerCollectionScanner.createOverlimitChapter(player, CollectionDisplayMode.MISSING, serverSnapshot),
                BiomeTreatCollectionScanner.createChapter(player, CollectionGrouping.DEFAULT, CollectionDisplayMode.MISSING),
                RecordCollectionScanner.createChapter(player, CollectionGrouping.DEFAULT, CollectionDisplayMode.MISSING, serverSnapshot)
        ));
    }

    public static LazyCollectionBook createLazyBook() {
        return createLazyBook(ignored -> CollectionGrouping.DEFAULT);
    }

    public static LazyCollectionBook createLazyBook(Function<String, CollectionGrouping> grouping) {
        return createLazyBook(grouping, CollectionDisplayMode.MISSING);
    }

    public static LazyCollectionBook createLazyBook(Function<String, CollectionGrouping> grouping, CollectionDisplayMode displayMode) {
        Player player = Minecraft.getInstance().player;
        ServerCollectionSnapshot serverSnapshot = ServerCollectionSnapshotContext.snapshot();
        List<ChapterDefinition> definitions = new ArrayList<>();
        definitions.add(new ChapterDefinition("morph", "\u53d8\u8eab", () -> MorphCollectionScanner.createChapter(player, grouping.apply("morph"), displayMode, serverSnapshot)));
        definitions.add(new ChapterDefinition("food", "\u7f8e\u98df\u5bb6", () -> FoodRegistryScanner.createChapter(displayMode, serverSnapshot)));
        definitions.add(new ChapterDefinition("weapon_master", "\u6b66\u5668\u5927\u5e08", () -> WeaponMasterScanner.createChapter(player, grouping.apply("weapon_master"), displayMode, serverSnapshot)));
        definitions.add(new ChapterDefinition("traveler", "\u65c5\u884c\u5bb6", () -> TravelerCollectionScanner.createChapter(player, displayMode, serverSnapshot)));
        definitions.add(new ChapterDefinition("traveler_overlimit", "\u65c5\u8005\u8d85\u9650", () -> TravelerCollectionScanner.createOverlimitChapter(player, displayMode, serverSnapshot)));
        definitions.add(new ChapterDefinition("biome_treat", "\u751f\u7269\u7fa4\u7cfb\u68d2\u7cd6", () -> BiomeTreatCollectionScanner.createChapter(player, grouping.apply("biome_treat"), displayMode)));
        definitions.add(new ChapterDefinition("autism", "\u5b64\u72ec\u75c7\u60a3\u8005", () -> RecordCollectionScanner.createChapter(player, grouping.apply("autism"), displayMode, serverSnapshot)));
        definitions.addAll(CatFoxCollectionScanner.chapterDefinitions(player, grouping, displayMode, serverSnapshot));
        return new LazyCollectionBook(definitions);
    }
}
