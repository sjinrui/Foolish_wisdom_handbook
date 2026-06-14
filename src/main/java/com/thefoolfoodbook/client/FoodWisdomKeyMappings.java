package com.thefoolfoodbook.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.thefoolfoodbook.TheFoolFoodBook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class FoodWisdomKeyMappings {
    public static final String OPEN_BOOK_KEY = "key.foolish_wisdom_handbook.open_book";
    public static final String CATEGORY = "key.categories.foolish_wisdom_handbook";
    public static final int DEFAULT_KEY_CODE = -1;

    private static final KeyMapping OPEN_BOOK = new KeyMapping(
            OPEN_BOOK_KEY,
            InputConstants.Type.KEYSYM,
            DEFAULT_KEY_CODE,
            CATEGORY
    );

    private FoodWisdomKeyMappings() {
    }

    static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BOOK);
    }

    static void openScreenWhenPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_BOOK.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                FoodWisdomBookClient.openScreen();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TheFoolFoodBook.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            FoodWisdomKeyMappings.register(event);
        }
    }

    @Mod.EventBusSubscriber(modid = TheFoolFoodBook.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeBusEvents {
        private ForgeBusEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                FoodWisdomKeyMappings.openScreenWhenPressed();
            }
        }
    }
}
