package com.thefoolfoodbook.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class FoodWisdomBookItem extends Item {
    public FoodWisdomBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(FoolishWisdomBookStyle.NAME_COLOR);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FoodWisdomBookItem::openClientScreen);
        }
        return InteractionResultHolder.fail(stack);
    }

    private static void openClientScreen() {
        com.thefoolfoodbook.client.FoodWisdomBookClient.openScreen();
    }
}
