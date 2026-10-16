/**
 * File: HeaterScreen.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.heater;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.furranystudio.avalinexus.block.heater.HeaterMenu;
import org.furranystudio.avalinexus.client.machine.MachineScreen;
import org.furranystudio.avalinexus.client.ui.AvaliUi;

// Drawn with the Avali theme like the shop, no background texture needed
public class HeaterScreen extends AbstractContainerScreen<HeaterMenu> {

    // Fuel bar fills the rest of the row after the fuel slot
    private static final int BAR_LEFT = HeaterMenu.FUEL_X + 24;
    private static final int BAR_RIGHT = HeaterMenu.GAUGE_X - 10;
    private static final int BAR_HEIGHT = 8;

    public HeaterScreen(HeaterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        AvaliUi.panel(graphics, leftPos, topPos, imageWidth, imageHeight, AvaliUi.WINDOW_BACKDROP);
        // Plain dark squares, same slots as the Avali shop
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x, y, x + 16, y + 16, AvaliUi.SECONDARY_BORDER);
        }

        int left = leftPos + BAR_LEFT;
        int right = leftPos + BAR_RIGHT;
        int top = topPos + HeaterMenu.FUEL_Y + (16 - BAR_HEIGHT) / 2;
        graphics.fill(left - 1, top - 1, right + 1, top + BAR_HEIGHT + 1, AvaliUi.PRIMARY_BORDER);
        graphics.fill(left, top, right, top + BAR_HEIGHT, AvaliUi.SECONDARY_BORDER);
        // Burns down from the right as the fuel runs out
        int filled = Math.round(menu.litProgress() * (right - left));
        if (filled > 0) {
            graphics.fill(left, top, left + filled, top + BAR_HEIGHT, AvaliUi.PRIMARY_ORANGE);
            graphics.fill(left, top, left + filled, top + 2, AvaliUi.ORANGE_GLOW);
        }
        MachineScreen.gauge(graphics, leftPos + HeaterMenu.GAUGE_X, topPos + HeaterMenu.GAUGE_Y, HeaterMenu.GAUGE_HEIGHT, menu.fuelFluid(), menu.fuelAmount(), menu.fuelCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        int x = leftPos + HeaterMenu.GAUGE_X;
        int y = topPos + HeaterMenu.GAUGE_Y;
        if (mouseX >= x && mouseX < x + MachineScreen.GAUGE_WIDTH && mouseY >= y && mouseY < y + HeaterMenu.GAUGE_HEIGHT) {
            graphics.setTooltipForNextFrame(font, MachineScreen.tooltip(menu.fuelFluid(), menu.fuelAmount(), menu.fuelCapacity()), mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(title), titleLabelX, titleLabelY, AvaliUi.TEXT_SECONDARY);
        AvaliUi.shadowedText(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, AvaliUi.TEXT_PRIMARY);
    }
}
