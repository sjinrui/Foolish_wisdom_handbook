package com.thefoolfoodbook.client;

import net.minecraft.client.Minecraft;
import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;

public final class FoodWisdomBookClient {
    private FoodWisdomBookClient() {
    }

    public static void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new FoodWisdomScreen());
        FoodWisdomClientNetwork.requestSnapshotIfAvailable();
    }

    public static void applyServerSnapshot(ServerCollectionSnapshot snapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        ServerCollectionSnapshotStore.update(snapshot);
        if (minecraft.screen instanceof FoodWisdomScreen screen) {
            screen.refreshFromServerSnapshot();
        }
    }
}
