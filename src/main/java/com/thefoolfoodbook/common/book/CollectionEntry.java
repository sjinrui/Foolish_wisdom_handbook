package com.thefoolfoodbook.common.book;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public record CollectionEntry(
        String id,
        String displayName,
        ItemStack itemStack,
        EntityType<?> entityType,
        boolean bookmarkable,
        boolean collected,
        boolean recipeOpenable
) {
    public static CollectionEntry item(String id, String displayName, boolean bookmarkable) {
        return item(id, displayName, bookmarkable, false);
    }

    public static CollectionEntry item(String id, String displayName, boolean bookmarkable, boolean collected) {
        return item(id, displayName, bookmarkable, collected, true);
    }

    public static CollectionEntry item(String id, String displayName, boolean bookmarkable, boolean collected, boolean recipeOpenable) {
        return new CollectionEntry(id, displayName, null, null, bookmarkable, collected, recipeOpenable);
    }

    public static CollectionEntry item(Item item, boolean bookmarkable) {
        return item(item, bookmarkable, false);
    }

    public static CollectionEntry item(Item item, boolean bookmarkable, boolean collected) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            return null;
        }
        return new CollectionEntry(
                id.toString(),
                item.getDescription().getString(),
                item.getDefaultInstance(),
                null,
                bookmarkable,
                collected,
                true
        );
    }

    public static CollectionEntry morph(String id, String displayName, EntityType<?> entityType) {
        return morph(id, displayName, entityType, false);
    }

    public static CollectionEntry morph(String id, String displayName, EntityType<?> entityType, boolean collected) {
        return new CollectionEntry(id, displayName, null, entityType, false, collected, false);
    }

    public boolean hasItemIcon() {
        return itemStack != null && !itemStack.isEmpty();
    }

    public boolean hasEntityIcon() {
        return entityType != null;
    }

    public boolean matchesBookmarkKeys(Set<String> bookmarkKeys) {
        return matchesBookmarkKeys(id, itemBookmarkKey(itemIdString()), stackBookmarkKey(itemStack), bookmarkable, bookmarkKeys);
    }

    public static boolean matchesBookmarkKeys(String entryId, String itemKey, String stackKey, boolean bookmarkable, Set<String> bookmarkKeys) {
        if (!bookmarkable || bookmarkKeys.isEmpty()) {
            return false;
        }
        if (bookmarkKeys.contains(entryId)) {
            return true;
        }
        if (stackKey != null) {
            return bookmarkKeys.contains(stackKey);
        }
        return itemKey != null && bookmarkKeys.contains(itemKey);
    }

    public static String bookmarkKey(ItemStack stack) {
        ResourceLocation itemId = itemId(stack);
        if (itemId == null) {
            return null;
        }
        if (stack.hasTag()) {
            return stackBookmarkKey(itemId.toString(), stack.getTag().toString());
        }
        return itemBookmarkKey(itemId.toString());
    }

    public static String itemBookmarkKey(String itemId) {
        return itemId == null || itemId.isBlank() ? null : "item:" + itemId;
    }

    public static String stackBookmarkKey(String itemId, String tag) {
        if (itemId == null || itemId.isBlank() || tag == null || tag.isBlank()) {
            return null;
        }
        return "stack:" + itemId + "|" + tag;
    }

    private static String stackBookmarkKey(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        ResourceLocation itemId = itemId(stack);
        return itemId == null ? null : stackBookmarkKey(itemId.toString(), stack.getTag().toString());
    }

    private String itemIdString() {
        ResourceLocation itemId = itemId(itemStack);
        return itemId == null ? null : itemId.toString();
    }

    private static ResourceLocation itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation forgeId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (forgeId != null) {
            return forgeId;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
