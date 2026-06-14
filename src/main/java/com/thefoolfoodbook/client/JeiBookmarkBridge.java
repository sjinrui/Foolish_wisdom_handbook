package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionEntry;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class JeiBookmarkBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger(JeiBookmarkBridge.class);

    private JeiBookmarkBridge() {
    }

    public static Set<String> bookmarkedStackKeys() {
        Optional<Object> bookmarkList = bookmarkList();
        if (bookmarkList.isEmpty()) {
            return Set.of();
        }

        try {
            Method getElements = bookmarkList.get().getClass().getMethod("getElements");
            Object elements = getElements.invoke(bookmarkList.get());
            if (!(elements instanceof Collection<?> collection)) {
                return Set.of();
            }

            Set<String> ids = new TreeSet<>();
            for (Object element : collection) {
                itemStackFromElement(element).ifPresent(stack -> {
                    String key = CollectionEntry.bookmarkKey(stack);
                    if (key != null) {
                        ids.add(key);
                    }
                });
            }
            return ids;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read JEI bookmarks", ex);
            return Set.of();
        }
    }

    public static boolean toggleBookmark(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Optional<Object> bookmarkList = bookmarkList();
        Optional<Object> ingredientManager = ingredientManager();
        if (bookmarkList.isEmpty() || ingredientManager.isEmpty()) {
            return false;
        }

        try {
            Optional<?> typedIngredient = createTypedIngredient(ingredientManager.get(), stack);
            if (typedIngredient.isEmpty()) {
                return false;
            }

            Class<?> ingredientBookmarkClass = Class.forName("mezz.jei.gui.bookmarks.IngredientBookmark");
            Class<?> typedIngredientClass = Class.forName("mezz.jei.api.ingredients.ITypedIngredient");
            Class<?> ingredientManagerClass = Class.forName("mezz.jei.api.runtime.IIngredientManager");
            Method create = ingredientBookmarkClass.getMethod("create", typedIngredientClass, ingredientManagerClass);
            Object bookmark = create.invoke(null, typedIngredient.get(), ingredientManager.get());
            Method toggle = bookmarkList.get().getClass().getMethod("toggleBookmark", Class.forName("mezz.jei.gui.bookmarks.IBookmark"));
            toggle.invoke(bookmarkList.get(), bookmark);
            return true;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not toggle JEI bookmark", ex);
            return false;
        }
    }

    public static boolean showRecipesFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Optional<Object> runtime = jeiRuntime();
        Optional<Object> ingredientManager = runtime.flatMap(value -> invokeNoArg(value, "getIngredientManager"));
        Optional<Object> recipesGui = runtime.flatMap(value -> invokeNoArg(value, "getRecipesGui"));
        Optional<Object> recipeManager = runtime.flatMap(value -> invokeNoArg(value, "getRecipeManager"));
        Optional<Object> helpers = runtime.flatMap(value -> invokeNoArg(value, "getJeiHelpers"));
        if (ingredientManager.isEmpty() || recipesGui.isEmpty() || recipeManager.isEmpty() || helpers.isEmpty()) {
            return false;
        }

        try {
            Optional<?> typedIngredient = createTypedIngredient(ingredientManager.get(), stack);
            if (typedIngredient.isEmpty()) {
                return false;
            }

            Object focusFactory = invokeNoArg(helpers.get(), "getFocusFactory").orElse(null);
            if (focusFactory == null) {
                return false;
            }

            Object outputFocus = createOutputFocus(focusFactory, typedIngredient.get());
            if (outputFocus == null || !hasRecipes(recipeManager.get(), outputFocus)) {
                return false;
            }

            Method show = recipesGui.get().getClass().getMethod("show", List.class);
            show.invoke(recipesGui.get(), List.of(outputFocus));
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("Could not open JEI recipes", ex);
            return false;
        }
    }

    private static Optional<ItemStack> itemStackFromElement(Object element) {
        try {
            Method getTypedIngredient = element.getClass().getMethod("getTypedIngredient");
            Object typedIngredient = getTypedIngredient.invoke(element);
            Method getItemStack = typedIngredient.getClass().getMethod("getItemStack");
            Object itemStackOptional = getItemStack.invoke(typedIngredient);
            if (itemStackOptional instanceof Optional<?> optional && optional.orElse(null) instanceof ItemStack stack) {
                return Optional.of(stack);
            }
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not read JEI bookmark element", ex);
        }
        return Optional.empty();
    }

    private static Optional<Object> createTypedIngredient(Object ingredientManager, ItemStack stack) throws ReflectiveOperationException {
        try {
            Method createTypedIngredient = ingredientManager.getClass().getMethod("createTypedIngredient", Object.class);
            Object result = createTypedIngredient.invoke(ingredientManager, stack);
            if (result instanceof Optional<?> optional) {
                return optional.map(value -> value);
            }
        } catch (NoSuchMethodException ignored) {
            Class<?> vanillaTypes = Class.forName("mezz.jei.api.constants.VanillaTypes");
            Object itemStackType = vanillaTypes.getField("ITEM_STACK").get(null);
            Class<?> ingredientTypeClass = Class.forName("mezz.jei.api.ingredients.IIngredientType");
            Method createTypedIngredient = ingredientManager.getClass().getMethod("createTypedIngredient", ingredientTypeClass, Object.class);
            Object result = createTypedIngredient.invoke(ingredientManager, itemStackType, stack);
            if (result instanceof Optional<?> optional) {
                return optional.map(value -> value);
            }
        }
        return Optional.empty();
    }

    private static Object createOutputFocus(Object focusFactory, Object typedIngredient) throws ReflectiveOperationException {
        Class<?> roleClass = Class.forName("mezz.jei.api.recipe.RecipeIngredientRole");
        Class<?> typedIngredientClass = Class.forName("mezz.jei.api.ingredients.ITypedIngredient");
        @SuppressWarnings({"rawtypes", "unchecked"})
        Object outputRole = Enum.valueOf((Class<? extends Enum>) roleClass.asSubclass(Enum.class), "OUTPUT");
        Method createFocus = focusFactory.getClass().getMethod("createFocus", roleClass, typedIngredientClass);
        return createFocus.invoke(focusFactory, outputRole, typedIngredient);
    }

    private static boolean hasRecipes(Object recipeManager, Object outputFocus) throws ReflectiveOperationException {
        Object categoryLookup = invokeNoArg(recipeManager, "createRecipeCategoryLookup").orElse(null);
        if (categoryLookup == null) {
            return false;
        }

        Method limitFocus = categoryLookup.getClass().getMethod("limitFocus", Collection.class);
        Object focusedLookup = limitFocus.invoke(categoryLookup, List.of(outputFocus));
        Method get = focusedLookup.getClass().getMethod("get");
        Object result = get.invoke(focusedLookup);
        if (result instanceof Stream<?> stream) {
            try (stream) {
                return stream.findAny().isPresent();
            }
        }
        return false;
    }

    private static Optional<Object> ingredientManager() {
        return jeiRuntime().flatMap(runtime -> invokeNoArg(runtime, "getIngredientManager"));
    }

    private static Optional<Object> bookmarkList() {
        return jeiRuntime()
                .flatMap(runtime -> invokeNoArg(runtime, "getBookmarkOverlay"))
                .flatMap(JeiBookmarkBridge::readBookmarkListField);
    }

    private static Optional<Object> jeiRuntime() {
        try {
            Class<?> internalClass = Class.forName("mezz.jei.common.Internal");
            Method getJeiRuntime = internalClass.getMethod("getJeiRuntime");
            return Optional.ofNullable(getJeiRuntime.invoke(null));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("JEI runtime is not available", ex);
            return Optional.empty();
        }
    }

    private static Optional<Object> readBookmarkListField(Object bookmarkOverlay) {
        try {
            Field field = bookmarkOverlay.getClass().getDeclaredField("bookmarkList");
            field.setAccessible(true);
            return Optional.ofNullable(field.get(bookmarkOverlay));
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not access JEI bookmark list", ex);
            return Optional.empty();
        }
    }

    private static Optional<Object> invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return Optional.ofNullable(method.invoke(target));
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("Could not invoke JEI runtime method {}", methodName, ex);
            return Optional.empty();
        }
    }
}
