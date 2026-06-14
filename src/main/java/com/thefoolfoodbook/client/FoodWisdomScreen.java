package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionBookScanner;
import com.thefoolfoodbook.common.book.CollectionChapter;
import com.thefoolfoodbook.common.book.CollectionDisplayMode;
import com.thefoolfoodbook.common.book.CollectionEntry;
import com.thefoolfoodbook.common.book.CollectionGroup;
import com.thefoolfoodbook.common.book.ChapterDefinition;
import com.thefoolfoodbook.common.book.CollectionGrouping;
import com.thefoolfoodbook.common.book.CatFoxCollectionScanner;
import com.thefoolfoodbook.common.book.BiomeTreatProgress;
import com.thefoolfoodbook.common.book.LazyCollectionBook;
import com.thefoolfoodbook.common.food.FoodIconGridMetrics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FoodWisdomScreen extends Screen {
    private static final String CATFOX_PARENT_ID = "catfox_relics";
    private static final int PANEL_COLOR = 0x66171A22;
    private static final int MENU_SELECTED_COLOR = 0x74F5F7FF;
    private static final int LINE_COLOR = 0xFF3E6A78;
    private static final int TITLE_COLOR = 0xFFEAF9FF;
    private static final int MUTED_COLOR = 0xFF9DB7C0;
    private static final int GROUP_COLOR = 0xFF78D8FF;
    private static final int TEXT_COLOR = 0xFFE7EDF0;
    private static final int CELL_FRAME = 0x88D7F7FF;
    private static final int CELL_FRAME_DIM = 0x55D7F7FF;
    private static final int BOOKMARKED_CELL_BACKGROUND = 0x48F5F7FF;
    private static final int COLLECTED_CELL_BACKGROUND = 0x3638C86A;
    private static final int MISSING_CELL_BACKGROUND = 0x36D84A4A;
    private static final int SCROLLBAR_TRACK = 0x55284F5B;
    private static final int SCROLLBAR_THUMB = 0xBB78D8FF;
    private static final int HEADER_HEIGHT = 18;
    private static final int ITEM_CELL_SIZE = 28;
    private static final int ENTITY_CELL_SIZE = 56;
    private static final int MENU_WIDTH = 86;
    private static long handCursor;
    private static String rememberedChapterId = "food";
    private static CollectionDisplayMode rememberedDisplayMode = CollectionDisplayMode.MISSING;
    private static boolean rememberedCatFoxMenuExpanded;
    private static String rememberedSearchText = "";
    private static final Map<String, Integer> rememberedScrollByChapter = new HashMap<>();
    private static final Map<String, CollectionGrouping> rememberedGroupingByChapter = new HashMap<>();

    private LazyCollectionBook book = new LazyCollectionBook(List.of());
    private String selectedChapterId = rememberedChapterId;
    private CollectionDisplayMode displayMode = rememberedDisplayMode;
    private final Map<String, CollectionGrouping> groupingByChapter = new HashMap<>();
    private final Set<String> collapsedGroups = new HashSet<>();
    private final Set<String> bookmarkedStackKeys = new HashSet<>();
    private final Map<String, LivingEntity> entityCache = new HashMap<>();
    private final DisplayedChapterCache displayedChapterCache = new DisplayedChapterCache();
    private int scroll;
    private int listLeft;
    private int listTop;
    private int listRight;
    private int listBottom;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int menuLeft;
    private int menuRight;
    private int groupingToggleLeft;
    private int groupingToggleTop;
    private int groupingToggleRight;
    private int groupingToggleBottom;
    private int displayToggleLeft;
    private int displayToggleTop;
    private int displayToggleRight;
    private int displayToggleBottom;
    private double lastMouseX;
    private double lastMouseY;
    private boolean handCursorActive;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;
    private boolean catFoxMenuExpanded = rememberedCatFoxMenuExpanded;
    private int menuScroll;
    private EditBox searchBox;

    public FoodWisdomScreen() {
        super(Component.translatable("screen.foolish_wisdom_handbook.foolish_wisdom_book"));
    }

    @Override
    protected void init() {
        groupingByChapter.clear();
        groupingByChapter.putAll(rememberedGroupingByChapter);
        displayMode = rememberedDisplayMode;
        selectedChapterId = rememberedChapterId;
        this.book = createBook();
        if (!hasChapter(selectedChapterId)) {
            selectedChapterId = book.firstChapterId();
        }
        book.load(selectedChapterId);
        refreshBookmarks();
        entityCache.clear();
        updateLayout();
        searchBox = new EditBox(font, listLeft, panelTop + 24, searchBoxWidth(), 18, Component.translatable("screen.foolish_wisdom_handbook.search"));
        searchBox.setMaxLength(80);
        searchBox.setHint(Component.translatable("screen.foolish_wisdom_handbook.search"));
        searchBox.setValue(rememberedSearchText);
        searchBox.setResponder(text -> {
            rememberedSearchText = text;
            displayedChapterCache.invalidate();
            scroll = 0;
            clampScroll();
        });
        addRenderableWidget(searchBox);
        scroll = rememberedScrollByChapter.getOrDefault(selectedChapterId, 0);
        clampScroll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        renderBackground(graphics);
        updateLayout();
        CollectionChapter chapter = currentChapter();
        CollectionChapter displayedChapter = displayedChapter(chapter);

        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, PANEL_COLOR);
        drawBorder(graphics, panelLeft, panelTop, panelRight, panelBottom, LINE_COLOR);
        graphics.fill(menuRight, panelTop + 8, menuRight + 1, panelBottom - 8, LINE_COLOR);

        graphics.drawString(font, title.copy().withStyle(ChatFormatting.BOLD), panelLeft + 14, panelTop + 10, TITLE_COLOR, false);
        graphics.drawString(font, summaryText(chapter), FoodWisdomScreenLayout.summaryTextX(listLeft, searchBoxWidth()), panelTop + 28, MUTED_COLOR, false);

        renderChapterMenu(graphics);
        renderGroupingButton(graphics, chapter);
        renderDisplayButton(graphics);
        graphics.enableScissor(listLeft, listTop, listRight, listBottom);
        renderGroups(graphics, displayedChapter);
        graphics.disableScissor();
        renderScrollbar(graphics, displayedChapter);
        updateCursor(mouseX, mouseY);

        if (displayedChapter.groups().isEmpty()) {
            Component emptyText = emptyText(displayedChapter).copy().withStyle(ChatFormatting.AQUA);
            graphics.drawCenteredString(font, emptyText, (listLeft + listRight) / 2, (listTop + listBottom) / 2 - 5, TEXT_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        HoveredTarget hovered = hoveredTarget(mouseX, mouseY);
        if (hovered instanceof HoveredTarget.Entry entry) {
            graphics.renderTooltip(font, Component.literal(entry.entry().displayName()), mouseX, mouseY);
        } else if (hovered instanceof HoveredTarget.GroupingButton) {
            graphics.renderTooltip(font, Component.literal(groupingTooltip(chapter)), mouseX, mouseY);
        } else if (hovered instanceof HoveredTarget.DisplayButton) {
            graphics.renderTooltip(font, Component.literal("\u5207\u6362\u663e\u793a\u5185\u5bb9"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        updateLayout();
        if (isMouseOverMenu(mouseX, mouseY)) {
            menuScroll = Math.max(0, Math.min(maxMenuScroll(), menuScroll - (int) (delta * 18.0D)));
            return true;
        }
        scroll = Math.max(0, Math.min(maxScroll(displayedChapter(currentChapter())), scroll - (int) (delta * 18.0D)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateLayout();
        if (searchBox != null && searchBox.isFocused()
                && FoodWisdomScreenLayout.shouldSearchBoxLoseFocus(mouseX, mouseY, searchBox.getX(), searchBox.getY(), searchBox.getX() + searchBox.getWidth(), searchBox.getY() + searchBox.getHeight())) {
            searchBox.setFocused(false);
            setFocused(null);
        }
        if (button == 0 && isMouseOverScrollbar(mouseX, mouseY)) {
            beginScrollbarDrag(mouseY);
            return true;
        }

        HoveredTarget hovered = hoveredTarget(mouseX, mouseY);
        if (button == 0 && hovered instanceof HoveredTarget.Chapter chapter) {
            saveCurrentScroll();
            selectedChapterId = chapter.id();
            book.load(selectedChapterId);
            displayedChapterCache.invalidate();
            refreshBookmarks();
            scroll = rememberedScrollByChapter.getOrDefault(selectedChapterId, 0);
            clampScroll();
            return true;
        }
        if (button == 0 && hovered instanceof HoveredTarget.MenuFolder) {
            catFoxMenuExpanded = !catFoxMenuExpanded;
            rememberedCatFoxMenuExpanded = catFoxMenuExpanded;
            clampMenuScroll();
            return true;
        }
        if (button == 0 && hovered instanceof HoveredTarget.DisplayButton) {
            toggleDisplayMode();
            return true;
        }
        if (button == 0 && hovered instanceof HoveredTarget.GroupingButton) {
            toggleGrouping(selectedChapterId);
            return true;
        }
        if (button == 0 && hovered instanceof HoveredTarget.Entry entry && entry.entry().hasItemIcon() && entry.entry().recipeOpenable()) {
            return JeiBookmarkBridge.showRecipesFor(entry.entry().itemStack().copy());
        }
        if (button == 1 && hovered instanceof HoveredTarget.Entry entry && canRemoveBiomeTreat(entry.entry())) {
            if (minecraft != null && BiomeTreatProgress.remove(minecraft.player, entry.entry().id())) {
                book.invalidate(selectedChapterId);
                book.load(selectedChapterId);
                displayedChapterCache.invalidate();
                clampScroll();
            }
            return true;
        }
        if (button == 0 && hovered instanceof HoveredTarget.Group group) {
            String key = collapsedKey(currentChapter().id(), group.id());
            if (collapsedGroups.contains(key)) {
                collapsedGroups.remove(key);
            } else {
                collapsedGroups.add(key);
            }
            clampScroll();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScrollbar) {
            updateLayout();
            scrollFromScrollbarMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 65) {
            updateLayout();
            HoveredTarget hovered = hoveredTarget(lastMouseX, lastMouseY);
            if (hovered instanceof HoveredTarget.Entry entry && entry.entry().bookmarkable() && entry.entry().hasItemIcon()) {
                if (JeiBookmarkBridge.toggleBookmark(entry.entry().itemStack().copy())) {
                    refreshBookmarks();
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (searchBox != null) {
            searchBox.tick();
        }
        super.tick();
    }

    @Override
    public void removed() {
        draggingScrollbar = false;
        rememberUiState();
        restoreCursor();
        entityCache.clear();
        super.removed();
    }

    public void refreshFromServerSnapshot() {
        saveCurrentScroll();
        this.book = createBook();
        book.load(selectedChapterId);
        displayedChapterCache.invalidate();
        refreshBookmarks();
        entityCache.clear();
        clampScroll();
    }

    private Component summaryText(CollectionChapter chapter) {
        if (!chapter.sourceAvailable()) {
            return Component.translatable("screen.foolish_wisdom_handbook.source_missing", chapter.title());
        }
        return Component.translatable(
                "screen.foolish_wisdom_handbook.chapter_summary",
                chapter.actualMissingCount(),
                chapter.totalCount(),
                chapter.collectedCount()
        );
    }

    private Component emptyText(CollectionChapter chapter) {
        if (!chapter.sourceAvailable()) {
            return Component.translatable("screen.foolish_wisdom_handbook.source_missing", chapter.title());
        }
        if (displayMode == CollectionDisplayMode.COLLECTED && chapter.collectedCount() == 0 && chapter.totalCount() > 0) {
            return Component.translatable("screen.foolish_wisdom_handbook.collection_not_started");
        }
        return Component.translatable("screen.foolish_wisdom_handbook.no_missing_entries", chapter.title());
    }

    private void renderChapterMenu(GuiGraphics graphics) {
        int y = listTop - menuScroll;
        int menuBottom = displayToggleTop - 4;
        graphics.enableScissor(menuLeft, listTop, menuRight, menuBottom);
        for (MenuRow row : visibleMenuRows()) {
            int rowBottom = y + 22;
            boolean selected = row.id().equals(selectedChapterId)
                    || row.parent() && !catFoxMenuExpanded && CatFoxCollectionScanner.isCatFoxChapter(selectedChapterId);
            if (selected) {
                fillRoundedRect(graphics, menuLeft + 3, y + 1, menuRight - 5, rowBottom - 1, MENU_SELECTED_COLOR);
            }
            int color = selected ? TITLE_COLOR : MUTED_COLOR;
            String title = row.parent() ? (catFoxMenuExpanded ? "- " : "+ ") + row.title() : row.title();
            graphics.drawString(font, title, menuLeft + (row.child() ? 18 : 10), y + 7, color, false);
            y += 24;
        }
        graphics.disableScissor();
    }

    private void renderGroupingButton(GuiGraphics graphics, CollectionChapter chapter) {
        if (!supportsGrouping(chapter.id())) {
            return;
        }

        fillRoundedRect(graphics, groupingToggleLeft, groupingToggleTop, groupingToggleRight, groupingToggleBottom, 0x443E6A78);
        drawRoundedFrame(graphics, groupingToggleLeft, groupingToggleTop, groupingToggleRight, groupingToggleBottom, CELL_FRAME_DIM);
        String label = groupingLabel(chapter.id(), groupingFor(chapter.id()));
        graphics.drawCenteredString(font, label, (groupingToggleLeft + groupingToggleRight) / 2, groupingToggleTop + 5, TITLE_COLOR);
    }

    private void renderDisplayButton(GuiGraphics graphics) {
        fillRoundedRect(graphics, displayToggleLeft, displayToggleTop, displayToggleRight, displayToggleBottom, 0x443E6A78);
        drawRoundedFrame(graphics, displayToggleLeft, displayToggleTop, displayToggleRight, displayToggleBottom, CELL_FRAME_DIM);
        graphics.drawCenteredString(font, displayMode.label(), (displayToggleLeft + displayToggleRight) / 2, displayToggleTop + 5, TITLE_COLOR);
    }

    private String groupingTooltip(CollectionChapter chapter) {
        if ("biome_treat".equals(chapter.id())) {
            return groupingFor(chapter.id()) == CollectionGrouping.DEFAULT
                    ? "\u5f53\u524d\uff1a\u9ed8\u8ba4\u5206\u7c7b\uff0c\u70b9\u51fb\u5207\u6362\u5230\u6309\u7ef4\u5ea6"
                    : "\u5f53\u524d\uff1a\u6309\u7ef4\u5ea6\uff0c\u70b9\u51fb\u5207\u6362\u5230\u9ed8\u8ba4\u5206\u7c7b";
        }
        return groupingFor(chapter.id()) == CollectionGrouping.DEFAULT
                ? "\u5f53\u524d\uff1a\u9ed8\u8ba4\u5206\u7c7b\uff0c\u70b9\u51fb\u5207\u6362\u5230\u6309\u6a21\u7ec4"
                : "\u5f53\u524d\uff1a\u6309\u6a21\u7ec4\uff0c\u70b9\u51fb\u5207\u6362\u5230\u9ed8\u8ba4\u5206\u7c7b";
    }

    private void toggleGrouping(String chapterId) {
        CollectionGrouping current = groupingFor(chapterId);
        CollectionGrouping next = "biome_treat".equals(chapterId)
                ? (current == CollectionGrouping.DEFAULT ? CollectionGrouping.DIMENSION : CollectionGrouping.DEFAULT)
                : (current == CollectionGrouping.DEFAULT ? CollectionGrouping.MOD : CollectionGrouping.DEFAULT);
        groupingByChapter.put(chapterId, next);
        book.invalidate(chapterId);
        book.load(chapterId);
        displayedChapterCache.invalidate();
        scroll = 0;
        clampScroll();
    }

    private CollectionGrouping groupingFor(String chapterId) {
        return groupingByChapter.getOrDefault(chapterId, CollectionGrouping.DEFAULT);
    }

    private void toggleDisplayMode() {
        displayMode = displayMode.next();
        rememberedDisplayMode = displayMode;
        saveCurrentScroll();
        book = createBook();
        book.load(selectedChapterId);
        displayedChapterCache.invalidate();
        refreshBookmarks();
        entityCache.clear();
        scroll = 0;
        rememberedScrollByChapter.put(selectedChapterId, scroll);
        clampScroll();
    }

    private LazyCollectionBook createBook() {
        return CollectionBookScanner.createLazyBook(this::groupingFor, displayMode);
    }

    private static boolean supportsGrouping(String chapterId) {
        return "morph".equals(chapterId)
                || "biome_treat".equals(chapterId)
                || "weapon_master".equals(chapterId)
                || "autism".equals(chapterId)
                || CatFoxCollectionScanner.isCatFoxChapter(chapterId);
    }

    private static String groupingLabel(String chapterId, CollectionGrouping grouping) {
        if (grouping == CollectionGrouping.DEFAULT) {
            return "\u5206\u7c7b";
        }
        return "biome_treat".equals(chapterId) ? "\u7ef4\u5ea6" : "\u6a21\u7ec4";
    }

    private void renderGroups(GuiGraphics graphics, CollectionChapter chapter) {
        int x = listLeft;
        int right = listRight - 6;
        int y = listTop - scroll;
        for (CollectionGroup group : chapter.groups()) {
            boolean collapsed = collapsedGroups.contains(collapsedKey(chapter.id(), group.id()));
            int height = groupHeight(chapter, group.entries().size(), collapsed);
            if (y + height >= listTop && y <= listBottom) {
                renderGroup(graphics, chapter, group, collapsed, x, y, right);
            }
            y += height;
        }
    }

    private void renderGroup(GuiGraphics graphics, CollectionChapter chapter, CollectionGroup group, boolean collapsed, int x, int y, int right) {
        String icon = collapsed ? "+ " : "- ";
        graphics.drawString(font, icon + group.title() + "  " + group.entries().size(), x, y + 4, GROUP_COLOR, false);
        graphics.fill(x, y + HEADER_HEIGHT - 2, right, y + HEADER_HEIGHT - 1, 0x663E6A78);
        if (collapsed) {
            return;
        }

        int cellSize = cellSize(chapter);
        int columns = gridColumns(chapter);
        for (int i = 0; i < group.entries().size(); i++) {
            CollectionEntry entry = group.entries().get(i);
            int cellX = x + (i % columns) * cellSize;
            int cellY = y + HEADER_HEIGHT + (i / columns) * cellSize;
            if (cellY + cellSize < listTop || cellY > listBottom) {
                continue;
            }
            renderEntryCell(graphics, entry, cellX, cellY, cellSize);
        }
    }

    private void renderEntryCell(GuiGraphics graphics, CollectionEntry entry, int x, int y, int cellSize) {
        boolean bookmarked = entry.matchesBookmarkKeys(bookmarkedStackKeys);
        if (displayMode == CollectionDisplayMode.ALL) {
            fillRoundedRect(graphics, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, entry.collected() ? COLLECTED_CELL_BACKGROUND : MISSING_CELL_BACKGROUND);
            drawRoundedFrame(graphics, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, entry.collected() ? 0x7738C86A : 0x77D84A4A);
        } else if (bookmarked) {
            fillRoundedRect(graphics, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, BOOKMARKED_CELL_BACKGROUND);
            drawRoundedFrame(graphics, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, CELL_FRAME);
        } else {
            drawRoundedFrame(graphics, x + 2, y + 2, x + cellSize - 2, y + cellSize - 2, CELL_FRAME_DIM);
        }

        if (entry.hasItemIcon()) {
            graphics.renderItem(entry.itemStack(), x + 6, y + 6);
        } else if (entry.hasEntityIcon()) {
            renderEntityIcon(graphics, entry, x, y, cellSize);
        }
    }

    private void renderEntityIcon(GuiGraphics graphics, CollectionEntry entry, int x, int y, int cellSize) {
        LivingEntity entity = entityCache.computeIfAbsent(entry.id(), id -> createEntity(entry));
        if (entity != null) {
            float largest = Math.max(entity.getBbHeight(), entity.getBbWidth());
            int size = Math.max(4, (int) ((cellSize - 10.0F) / Math.max(0.5F, largest)));
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + cellSize / 2, y + cellSize - 6, size, -10.0F, -10.0F, entity);
                return;
            } catch (RuntimeException ignored) {
                entityCache.remove(entry.id());
            }
        }

        ItemStack spawnEgg = fallbackSpawnEgg(entry);
        if (!spawnEgg.isEmpty()) {
            graphics.renderItem(spawnEgg, x + (cellSize - 16) / 2, y + (cellSize - 16) / 2);
        }
    }

    private LivingEntity createEntity(CollectionEntry entry) {
        if (minecraft == null || minecraft.level == null || entry.entityType() == null) {
            return null;
        }
        Entity entity = entry.entityType().create(minecraft.level);
        if (entity instanceof LivingEntity living) {
            if (living instanceof Mob mob) {
                mob.setNoAi(true);
            }
            return living;
        }
        return null;
    }

    private ItemStack fallbackSpawnEgg(CollectionEntry entry) {
        if (entry.entityType() == null) {
            return ItemStack.EMPTY;
        }
        SpawnEggItem egg = ForgeSpawnEggItem.fromEntityType(entry.entityType());
        return egg == null ? ItemStack.EMPTY : egg.getDefaultInstance();
    }

    private int contentHeight(CollectionChapter chapter) {
        int height = 0;
        for (CollectionGroup group : chapter.groups()) {
            height += groupHeight(chapter, group.entries().size(), collapsedGroups.contains(collapsedKey(chapter.id(), group.id())));
        }
        return height;
    }

    private void refreshBookmarks() {
        bookmarkedStackKeys.clear();
        bookmarkedStackKeys.addAll(JeiBookmarkBridge.bookmarkedStackKeys());
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(maxScroll(displayedChapter(currentChapter())), scroll));
    }

    private HoveredTarget hoveredTarget(double mouseX, double mouseY) {
        if (mouseX >= displayToggleLeft && mouseX <= displayToggleRight
                && mouseY >= displayToggleTop && mouseY <= displayToggleBottom) {
            return new HoveredTarget.DisplayButton();
        }

        if (supportsGrouping(selectedChapterId)
                && mouseX >= groupingToggleLeft && mouseX <= groupingToggleRight
                && mouseY >= groupingToggleTop && mouseY <= groupingToggleBottom) {
            return new HoveredTarget.GroupingButton();
        }

        if (isMouseOverMenu(mouseX, mouseY)) {
            int index = (int) ((mouseY - listTop + menuScroll) / 24);
            List<MenuRow> rows = visibleMenuRows();
            if (index >= 0 && index < rows.size()) {
                MenuRow row = rows.get(index);
                return row.parent() ? new HoveredTarget.MenuFolder() : new HoveredTarget.Chapter(row.id());
            }
        }

        if (mouseX < listLeft || mouseX > listRight || mouseY < listTop || mouseY > listBottom) {
            return null;
        }

        CollectionChapter chapter = displayedChapter(currentChapter());
        int y = listTop - scroll;
        int x = listLeft;
        int cellSize = cellSize(chapter);
        int columns = gridColumns(chapter);
        for (CollectionGroup group : chapter.groups()) {
            boolean collapsed = collapsedGroups.contains(collapsedKey(chapter.id(), group.id()));
            if (mouseY >= y && mouseY < y + HEADER_HEIGHT) {
                return new HoveredTarget.Group(group.id());
            }
            if (!collapsed) {
                int gridTop = y + HEADER_HEIGHT;
                int row = (int) ((mouseY - gridTop) / cellSize);
                int column = (int) ((mouseX - x) / cellSize);
                if (row >= 0 && column >= 0 && column < columns) {
                    int index = row * columns + column;
                    if (index >= 0 && index < group.entries().size()) {
                        int cellX = x + column * cellSize;
                        int cellY = gridTop + row * cellSize;
                        if (mouseX >= cellX && mouseX < cellX + cellSize && mouseY >= cellY && mouseY < cellY + cellSize) {
                            return new HoveredTarget.Entry(group.entries().get(index));
                        }
                    }
                }
            }
            y += groupHeight(chapter, group.entries().size(), collapsed);
        }
        return null;
    }

    private int groupHeight(CollectionChapter chapter, int itemCount, boolean collapsed) {
        if (collapsed) {
            return HEADER_HEIGHT;
        }
        int cellSize = cellSize(chapter);
        return HEADER_HEIGHT + FoodIconGridMetrics.rows(itemCount, gridColumns(chapter)) * cellSize;
    }

    private int gridColumns(CollectionChapter chapter) {
        return FoodIconGridMetrics.columns(Math.max(1, (listRight - 6) - listLeft), cellSize(chapter));
    }

    private static int cellSize(CollectionChapter chapter) {
        return isEntityGridChapter(chapter.id()) ? ENTITY_CELL_SIZE : ITEM_CELL_SIZE;
    }

    private static boolean isEntityGridChapter(String chapterId) {
        return "morph".equals(chapterId)
                || "cat_sin".equals(chapterId)
                || "cat_dream".equals(chapterId)
                || "fox_companion".equals(chapterId)
                || "fox_hunting".equals(chapterId);
    }

    private void updateLayout() {
        int panelWidth = Math.min(width - 24, 540);
        int panelHeight = Math.min(height - 24, 300);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        panelRight = panelLeft + panelWidth;
        panelBottom = panelTop + panelHeight;
        menuLeft = panelLeft + 10;
        menuRight = menuLeft + MENU_WIDTH;
        listLeft = menuRight + 14;
        listTop = panelTop + 48;
        listRight = panelRight - 18;
        listBottom = panelBottom - 12;
        groupingToggleRight = listRight - 6;
        groupingToggleLeft = groupingToggleRight - 44;
        groupingToggleTop = panelTop + 24;
        groupingToggleBottom = groupingToggleTop + 18;
        displayToggleLeft = menuLeft + 3;
        displayToggleRight = menuRight - 5;
        displayToggleBottom = panelBottom - 12;
        displayToggleTop = displayToggleBottom - 18;
        if (searchBox != null) {
            searchBox.setX(listLeft);
            searchBox.setY(panelTop + 24);
            searchBox.setWidth(searchBoxWidth());
        }
        clampMenuScroll();
    }

    private int searchBoxWidth() {
        return Math.max(70, Math.min(128, groupingToggleLeft - 8 - listLeft));
    }

    private int maxScroll(CollectionChapter chapter) {
        return Math.max(0, contentHeight(chapter) - Math.max(1, listBottom - listTop));
    }

    private void renderScrollbar(GuiGraphics graphics, CollectionChapter chapter) {
        int viewportHeight = Math.max(1, listBottom - listTop);
        int contentHeight = contentHeight(chapter);
        if (contentHeight <= viewportHeight) {
            return;
        }

        int trackLeft = panelRight - 13;
        int trackRight = panelRight - 9;
        graphics.fill(trackLeft, listTop, trackRight, listBottom, SCROLLBAR_TRACK);

        int thumbHeight = scrollbarThumbHeight(chapter);
        int thumbTop = scrollbarThumbTop(chapter);
        graphics.fill(trackLeft, thumbTop, trackRight, thumbTop + thumbHeight, SCROLLBAR_THUMB);
    }

    private void updateCursor(double mouseX, double mouseY) {
        boolean shouldUseHand = draggingScrollbar || hoveredTarget(mouseX, mouseY) != null || isMouseOverScrollbar(mouseX, mouseY);
        if (shouldUseHand == handCursorActive || minecraft == null) {
            return;
        }

        long window = minecraft.getWindow().getWindow();
        GLFW.glfwSetCursor(window, shouldUseHand ? handCursor() : 0L);
        handCursorActive = shouldUseHand;
    }

    private void restoreCursor() {
        if (minecraft != null && handCursorActive) {
            GLFW.glfwSetCursor(minecraft.getWindow().getWindow(), 0L);
            handCursorActive = false;
        }
    }

    private static long handCursor() {
        if (handCursor == 0L) {
            handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        }
        return handCursor;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        if (maxScroll(currentChapter()) <= 0) {
            return false;
        }

        int trackLeft = panelRight - 15;
        int trackRight = panelRight - 7;
        return mouseX >= trackLeft && mouseX <= trackRight && mouseY >= listTop && mouseY <= listBottom;
    }

    private void beginScrollbarDrag(double mouseY) {
        int thumbTop = scrollbarThumbTop(currentChapter());
        int thumbBottom = thumbTop + scrollbarThumbHeight(currentChapter());
        if (mouseY >= thumbTop && mouseY <= thumbBottom) {
            scrollbarDragOffset = (int) mouseY - thumbTop;
        } else {
            scrollbarDragOffset = scrollbarThumbHeight(currentChapter()) / 2;
            scrollFromScrollbarMouse(mouseY);
        }
        draggingScrollbar = true;
    }

    private void scrollFromScrollbarMouse(double mouseY) {
        CollectionChapter chapter = currentChapter();
        int travel = scrollbarTravel(chapter);
        if (travel <= 0) {
            scroll = 0;
            return;
        }

        int thumbTop = (int) mouseY - scrollbarDragOffset;
        int relativeThumbTop = Math.max(0, Math.min(travel, thumbTop - listTop));
        scroll = relativeThumbTop * maxScroll(chapter) / travel;
        clampScroll();
    }

    private int scrollbarThumbTop(CollectionChapter chapter) {
        int travel = scrollbarTravel(chapter);
        return listTop + scroll * travel / Math.max(1, maxScroll(chapter));
    }

    private int scrollbarTravel(CollectionChapter chapter) {
        return Math.max(1, (listBottom - listTop) - scrollbarThumbHeight(chapter));
    }

    private int scrollbarThumbHeight(CollectionChapter chapter) {
        int viewportHeight = Math.max(1, listBottom - listTop);
        int contentHeight = Math.max(1, contentHeight(chapter));
        return Math.max(18, viewportHeight * viewportHeight / contentHeight);
    }

    private CollectionChapter currentChapter() {
        if (selectedChapterId.isEmpty()) {
            return CollectionChapter.empty("empty", "", false);
        }
        return book.load(selectedChapterId);
    }

    private CollectionChapter displayedChapter(CollectionChapter chapter) {
        String query = searchBox == null ? rememberedSearchText : searchBox.getValue();
        return displayedChapterCache.get(chapter, query);
    }

    private boolean canRemoveBiomeTreat(CollectionEntry entry) {
        return FoodWisdomScreenLayout.canRemoveBiomeTreat(selectedChapterId, displayMode, entry.collected(), entry.hasItemIcon());
    }

    private boolean hasChapter(String id) {
        for (ChapterDefinition definition : book.definitions()) {
            if (definition.id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static String collapsedKey(String chapterId, String groupId) {
        return chapterId + ":" + groupId;
    }

    private void saveCurrentScroll() {
        if (!selectedChapterId.isEmpty()) {
            rememberedScrollByChapter.put(selectedChapterId, scroll);
        }
    }

    private void rememberUiState() {
        saveCurrentScroll();
        rememberedChapterId = selectedChapterId;
        rememberedDisplayMode = displayMode;
        rememberedCatFoxMenuExpanded = catFoxMenuExpanded;
        rememberedGroupingByChapter.clear();
        rememberedGroupingByChapter.putAll(groupingByChapter);
    }

    private List<MenuRow> visibleMenuRows() {
        List<MenuRow> rows = new java.util.ArrayList<>();
        boolean addedCatFoxParent = false;
        for (ChapterDefinition definition : book.definitions()) {
            if (CatFoxCollectionScanner.isCatFoxChapter(definition.id())) {
                if (!addedCatFoxParent) {
                    rows.add(new MenuRow(CATFOX_PARENT_ID, "\u732b\u72d0\u9057\u7269", true, false));
                    addedCatFoxParent = true;
                }
                if (catFoxMenuExpanded) {
                    rows.add(new MenuRow(definition.id(), definition.title(), false, true));
                }
            } else {
                rows.add(new MenuRow(definition.id(), definition.title(), false, false));
            }
        }
        return rows;
    }

    private boolean isMouseOverMenu(double mouseX, double mouseY) {
        return mouseX >= menuLeft && mouseX <= menuRight && mouseY >= listTop && mouseY <= displayToggleTop - 4;
    }

    private int maxMenuScroll() {
        int menuHeight = Math.max(1, displayToggleTop - 4 - listTop);
        return Math.max(0, visibleMenuRows().size() * 24 - menuHeight);
    }

    private void clampMenuScroll() {
        menuScroll = Math.max(0, Math.min(maxMenuScroll(), menuScroll));
    }

    private static void drawBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }

    private static void fillRoundedRect(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left + 2, top, right - 2, bottom, color);
        graphics.fill(left, top + 2, right, bottom - 2, color);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, color);
    }

    private static void drawRoundedFrame(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left + 2, top, right - 2, top + 1, color);
        graphics.fill(left + 2, bottom - 1, right - 2, bottom, color);
        graphics.fill(left, top + 2, left + 1, bottom - 2, color);
        graphics.fill(right - 1, top + 2, right, bottom - 2, color);
        graphics.fill(left + 1, top + 1, left + 2, top + 2, color);
        graphics.fill(right - 2, top + 1, right - 1, top + 2, color);
        graphics.fill(left + 1, bottom - 2, left + 2, bottom - 1, color);
        graphics.fill(right - 2, bottom - 2, right - 1, bottom - 1, color);
    }

    private sealed interface HoveredTarget {
        record Chapter(String id) implements HoveredTarget {
        }

        record MenuFolder() implements HoveredTarget {
        }

        record Group(String id) implements HoveredTarget {
        }

        record Entry(CollectionEntry entry) implements HoveredTarget {
        }

        record GroupingButton() implements HoveredTarget {
        }

        record DisplayButton() implements HoveredTarget {
        }
    }

    private record MenuRow(String id, String title, boolean parent, boolean child) {
    }
}
