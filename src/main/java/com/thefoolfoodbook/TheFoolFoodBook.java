package com.thefoolfoodbook;

import com.thefoolfoodbook.common.registry.ModItems;
import com.thefoolfoodbook.common.network.FoodWisdomNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TheFoolFoodBook.MOD_ID)
public final class TheFoolFoodBook {
    public static final String MOD_ID = "foolish_wisdom_handbook";

    public TheFoolFoodBook() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        FoodWisdomNetwork.register();
        modBus.addListener(this::addCreativeTabItems);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES && ModItems.FOOD_WISDOM_BOOK.isPresent()) {
            event.accept(ModItems.FOOD_WISDOM_BOOK.get());
        }
    }
}
