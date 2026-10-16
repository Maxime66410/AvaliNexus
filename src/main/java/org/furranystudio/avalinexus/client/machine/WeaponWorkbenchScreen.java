/**
 * File: WeaponWorkbenchScreen.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.client.machine;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.workbench.WeaponRecipe;
import org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchMenu;
import org.furranystudio.avalinexus.client.ui.AvaliUi;

import java.util.ArrayList;
import java.util.List;

public class WeaponWorkbenchScreen extends MachineScreen<WeaponWorkbenchMenu> {

    private static final int VISIBLE = WeaponWorkbenchMenu.RECIPE_COLUMNS * WeaponWorkbenchMenu.RECIPE_ROWS;
    private static final int SCROLL_X = WeaponWorkbenchMenu.RECIPES_X + WeaponWorkbenchMenu.RECIPE_COLUMNS * WeaponWorkbenchMenu.RECIPE_SIZE + 1;
    private static final int SCROLL_WIDTH = 4;
    private static final int SCROLL_HEIGHT = WeaponWorkbenchMenu.RECIPE_ROWS * WeaponWorkbenchMenu.RECIPE_SIZE;

    private int firstRow;
    private boolean dragging;

    public WeaponWorkbenchScreen(WeaponWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected int barLeft() {
        return WeaponWorkbenchMenu.GRID_X;
    }

    @Override
    protected int barRight() {
        return WeaponWorkbenchMenu.OUTPUT_X + 16;
    }

    @Override
    protected int barY() {
        return WeaponWorkbenchMenu.BAR_Y;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (maxRow() > 0) {
            int x = leftPos + SCROLL_X;
            int y = topPos + WeaponWorkbenchMenu.RECIPES_Y;
            graphics.fill(x, y, x + SCROLL_WIDTH, y + SCROLL_HEIGHT, AvaliUi.PRIMARY_BORDER);
            int thumb = thumbHeight();
            int top = y + Math.round((float) firstRow / maxRow() * (SCROLL_HEIGHT - thumb));
            graphics.fill(x, top, x + SCROLL_WIDTH, top + thumb, dragging ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
        }
        for (int cell = 0; cell < VISIBLE; cell++) {
            int index = firstRow * WeaponWorkbenchMenu.RECIPE_COLUMNS + cell;
            WeaponRecipe recipe = WeaponRecipe.get(index);
            if (recipe == null) {
                return;
            }
            int x = cellX(cell);
            int y = cellY(cell);
            int size = WeaponWorkbenchMenu.RECIPE_SIZE;
            int border = index == menu.selected() ? AvaliUi.PRIMARY_ORANGE : inside(mouseX, mouseY, x, y) ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER;
            graphics.fill(x, y, x + size, y + size, border);
            graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, AvaliUi.SECONDARY_BORDER);
            graphics.item(recipe.output(), x + 1, y + 1);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        int index = hoveredRecipe(mouseX, mouseY);
        WeaponRecipe recipe = WeaponRecipe.get(index);
        if (recipe == null) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(recipe.output().getHoverName());
        for (WeaponRecipe.Cost cost : recipe.costs()) {
            lines.add(Component.translatable("gui.avalinexus.weapon_workbench.cost", cost.count(), new ItemStack(cost.item().get()).getHoverName()).withStyle(style -> style.withColor(AvaliUi.TEXT_PRIMARY)));
        }
        graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (maxRow() > 0 && onScrollBar(event.x(), event.y())) {
            dragging = true;
            scrollTo(event.y());
            return true;
        }
        int index = hoveredRecipe(event.x(), event.y());
        if (index >= 0 && menu.clickMenuButton(minecraft.player, index)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxRow() > 0) {
            firstRow = Mth.clamp(firstRow - (int) Math.signum(scrollY), 0, maxRow());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            scrollTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    private static int maxRow() {
        int rows = Mth.positiveCeilDiv(WeaponRecipe.ALL.size(), WeaponWorkbenchMenu.RECIPE_COLUMNS);
        return Math.max(0, rows - WeaponWorkbenchMenu.RECIPE_ROWS);
    }

    private static int thumbHeight() {
        int rows = WeaponWorkbenchMenu.RECIPE_ROWS + maxRow();
        return Math.max(8, SCROLL_HEIGHT * WeaponWorkbenchMenu.RECIPE_ROWS / rows);
    }

    private boolean onScrollBar(double mouseX, double mouseY) {
        int x = leftPos + SCROLL_X;
        int y = topPos + WeaponWorkbenchMenu.RECIPES_Y;
        return mouseX >= x && mouseX < x + SCROLL_WIDTH && mouseY >= y && mouseY < y + SCROLL_HEIGHT;
    }

    private void scrollTo(double mouseY) {
        int thumb = thumbHeight();
        float position = (float) (mouseY - topPos - WeaponWorkbenchMenu.RECIPES_Y - thumb / 2.0) / (SCROLL_HEIGHT - thumb);
        firstRow = Mth.clamp(Math.round(position * maxRow()), 0, maxRow());
    }

    private int hoveredRecipe(double mouseX, double mouseY) {
        for (int cell = 0; cell < VISIBLE; cell++) {
            int index = firstRow * WeaponWorkbenchMenu.RECIPE_COLUMNS + cell;
            if (WeaponRecipe.get(index) != null && inside(mouseX, mouseY, cellX(cell), cellY(cell))) {
                return index;
            }
        }
        return -1;
    }

    private int cellX(int cell) {
        return leftPos + WeaponWorkbenchMenu.RECIPES_X + cell % WeaponWorkbenchMenu.RECIPE_COLUMNS * WeaponWorkbenchMenu.RECIPE_SIZE;
    }

    private int cellY(int cell) {
        return topPos + WeaponWorkbenchMenu.RECIPES_Y + cell / WeaponWorkbenchMenu.RECIPE_COLUMNS * WeaponWorkbenchMenu.RECIPE_SIZE;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + WeaponWorkbenchMenu.RECIPE_SIZE && mouseY >= y && mouseY < y + WeaponWorkbenchMenu.RECIPE_SIZE;
    }
}
