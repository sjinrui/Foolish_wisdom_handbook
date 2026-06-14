package com.thefoolfoodbook.client;

import com.thefoolfoodbook.TheFoolFoodBook;
import com.thefoolfoodbook.common.book.BiomeTreatProgress;
import com.thefoolfoodbook.common.network.FoodWisdomNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class FoodWisdomClientNetwork {
    private FoodWisdomClientNetwork() {
    }

    public static void requestSnapshotIfAvailable() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            ServerCollectionSnapshotStore.clear();
            return;
        }

        Connection connection = minecraft.getConnection().getConnection();
        if (FoodWisdomNetwork.isRemotePresent(connection)) {
            FoodWisdomNetwork.requestServerSnapshot();
        } else {
            ServerCollectionSnapshotStore.clear();
        }
    }

    @Mod.EventBusSubscriber(modid = TheFoolFoodBook.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ServerCollectionSnapshotStore.clear();
            BiomeTreatProgress.clearCache();
        }
    }
}
