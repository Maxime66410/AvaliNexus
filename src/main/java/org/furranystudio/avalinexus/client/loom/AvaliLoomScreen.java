/**
 * File: AvaliLoomScreen.java
 * Author: Maxime66410
 * Created: 2026-10-11
 * Last Modified: 2026-10-12
 */
package org.furranystudio.avalinexus.client.loom;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.loom.AvaliLoomMenu;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.item.TapestryItems;

public class AvaliLoomScreen extends AbstractContainerScreen<AvaliLoomMenu> {

    private static final int PATTERNS_X = 46;
    private static final int PATTERNS_Y = 17;
    private static final int PATTERN_SIZE = 20;
    private static final int PATTERN_GAP = 2;
    private static final int PATTERN_COLUMNS = 4;
    private static final Component[] SLOT_HINTS = {
        Component.translatable("container.avalinexus.avali_loom.canvas"),
        Component.translatable("container.avalinexus.avali_loom.fibre"),
        Component.translatable("container.avalinexus.avali_loom.dye")
    };

    public AvaliLoomScreen(AvaliLoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        AvaliUi.panel(graphics, leftPos, topPos, imageWidth, imageHeight, AvaliUi.WINDOW_BACKDROP);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            if (slot.index == AvaliLoomMenu.RESULT_SLOT) {
                graphics.fill(x - 3, y - 3, x + 19, y + 19, AvaliUi.PRIMARY_ORANGE);
                graphics.fill(x - 2, y - 2, x + 18, y + 18, AvaliUi.PRIMARY_BORDER);
            }
            graphics.fill(x, y, x + 16, y + 16, AvaliUi.SECONDARY_BORDER);
        }

        TapestryPattern[] patterns = TapestryPattern.values();
        for (int i = 0; i < patterns.length; i++) {
            int x = patternX(i);
            int y = patternY(i);
            boolean hovered = inside(mouseX, mouseY, x, y);
            int border = i == menu.selectedPattern() ? AvaliUi.PRIMARY_ORANGE : hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER;
            graphics.fill(x, y, x + PATTERN_SIZE, y + PATTERN_SIZE, border);
            graphics.fill(x + 1, y + 1, x + PATTERN_SIZE - 1, y + PATTERN_SIZE - 1, AvaliUi.SECONDARY_BORDER);
            graphics.item(preview(patterns[i]), x + 2, y + 2);
            if (!menu.hasCanvas()) {
                graphics.fill(x + 1, y + 1, x + PATTERN_SIZE - 1, y + PATTERN_SIZE - 1, AvaliUi.BACKDROP);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(title), titleLabelX, titleLabelY, AvaliUi.TEXT_SECONDARY);
        AvaliUi.shadowedText(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, AvaliUi.TEXT_PRIMARY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        TapestryPattern[] patterns = TapestryPattern.values();
        for (int i = 0; i < patterns.length; i++) {
            if (inside(mouseX, mouseY, patternX(i), patternY(i))) {
                graphics.setTooltipForNextFrame(preview(patterns[i]).getHoverName(), mouseX, mouseY);
                return;
            }
        }

        for (int i = 0; i < SLOT_HINTS.length; i++) {
            Slot slot = menu.slots.get(i);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            if (!slot.hasItem() && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.setTooltipForNextFrame(SLOT_HINTS[i], mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        TapestryPattern[] patterns = TapestryPattern.values();
        for (int i = 0; i < patterns.length; i++) {
            if (inside(event.x(), event.y(), patternX(i), patternY(i)) && menu.clickMenuButton(minecraft.player, i)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private ItemStack preview(TapestryPattern pattern) {
        return new ItemStack(TapestryItems.get(pattern, menu.previewColor()));
    }

    private int patternX(int index) {
        return leftPos + PATTERNS_X + index % PATTERN_COLUMNS * (PATTERN_SIZE + PATTERN_GAP);
    }

    private int patternY(int index) {
        return topPos + PATTERNS_Y + index / PATTERN_COLUMNS * (PATTERN_SIZE + PATTERN_GAP);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + PATTERN_SIZE && mouseY >= y && mouseY < y + PATTERN_SIZE;
    }
}
