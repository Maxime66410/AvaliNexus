/**
 * File: MachineScreen.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.client.machine;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;

import java.util.List;

public abstract class MachineScreen<T extends MachineMenu> extends AbstractContainerScreen<T> {

    private static final int GAUGE_WIDTH = 16;
    private static final int BAR_HEIGHT = 8;

    protected MachineScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, MachineMenu.HEIGHT);
    }

    protected abstract int barLeft();

    protected abstract int barRight();

    protected abstract int barY();

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        AvaliUi.panel(graphics, leftPos, topPos, imageWidth, imageHeight, AvaliUi.WINDOW_BACKDROP);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x, y, x + 16, y + 16, AvaliUi.SECONDARY_BORDER);
        }
        List<MachineMenu.Gauge> gauges = menu.gauges();
        for (int i = 0; i < gauges.size(); i++) {
            gauge(graphics, i, leftPos + gauges.get(i).x(), topPos + MachineMenu.GAUGE_Y);
        }
        int left = leftPos + barLeft();
        int right = leftPos + barRight();
        int top = topPos + barY();
        graphics.fill(left - 1, top - 1, right + 1, top + BAR_HEIGHT + 1, AvaliUi.PRIMARY_BORDER);
        graphics.fill(left, top, right, top + BAR_HEIGHT, AvaliUi.SECONDARY_BORDER);
        int filled = Math.round(menu.progress() * (right - left));
        if (filled > 0) {
            graphics.fill(left, top, left + filled, top + BAR_HEIGHT, AvaliUi.PRIMARY_ORANGE);
            graphics.fill(left, top, left + filled, top + 2, AvaliUi.ORANGE_GLOW);
        }
    }

    private void gauge(GuiGraphicsExtractor graphics, int index, int x, int y) {
        int bottom = y + MachineMenu.GAUGE_HEIGHT;
        graphics.fill(x - 1, y - 1, x + GAUGE_WIDTH + 1, bottom + 1, AvaliUi.PRIMARY_BORDER);
        graphics.fill(x, y, x + GAUGE_WIDTH, bottom, AvaliUi.SECONDARY_BORDER);
        int capacity = menu.gaugeCapacity(index);
        int amount = menu.gaugeAmount(index);
        Fluid fluid = menu.gaugeFluid(index);
        if (capacity <= 0 || amount <= 0 || fluid == Fluids.EMPTY) {
            return;
        }
        int height = Math.max(1, Math.round((float) Math.min(amount, capacity) / capacity * MachineMenu.GAUGE_HEIGHT));
        int color = AvaliLiquids.tint(fluid) | 0xFF000000;
        graphics.fill(x, bottom - height, x + GAUGE_WIDTH, bottom, color);
        graphics.fill(x, bottom - height, x + GAUGE_WIDTH, bottom - height + Math.min(2, height), lighter(color));
    }

    private static int lighter(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        red += (255 - red) * 2 / 5;
        green += (255 - green) * 2 / 5;
        blue += (255 - blue) * 2 / 5;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        List<MachineMenu.Gauge> gauges = menu.gauges();
        for (int i = 0; i < gauges.size(); i++) {
            int x = leftPos + gauges.get(i).x();
            int y = topPos + MachineMenu.GAUGE_Y;
            if (mouseX >= x && mouseX < x + GAUGE_WIDTH && mouseY >= y && mouseY < y + MachineMenu.GAUGE_HEIGHT) {
                graphics.setTooltipForNextFrame(font, tooltip(i), mouseX, mouseY);
            }
        }
    }

    private Component tooltip(int index) {
        Fluid fluid = menu.gaugeFluid(index);
        int amount = menu.gaugeAmount(index);
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return Component.translatable("gui.avalinexus.machine.empty", menu.gaugeCapacity(index));
        }
        return Component.translatable("gui.avalinexus.machine.gauge", AvaliLiquids.name(fluid), amount, menu.gaugeCapacity(index));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(title), titleLabelX, titleLabelY, AvaliUi.TEXT_SECONDARY);
        AvaliUi.shadowedText(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, AvaliUi.TEXT_PRIMARY);
    }
}
