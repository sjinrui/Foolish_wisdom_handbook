package com.thefoolfoodbook.common.registry;

import com.thefoolfoodbook.TheFoolFoodBook;
import com.thefoolfoodbook.common.item.FoodWisdomBookItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TheFoolFoodBook.MOD_ID);
    public static final String BOOK_ITEM_NAME = "foolish_wisdom_book";

    public static final RegistryObject<FoodWisdomBookItem> FOOD_WISDOM_BOOK = ITEMS.register(
            BOOK_ITEM_NAME,
            () -> new FoodWisdomBookItem(new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
