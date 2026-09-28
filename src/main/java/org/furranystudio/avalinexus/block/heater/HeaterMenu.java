/**
 * File: HeaterMenu.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block.heater;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.inventory.ModMenus;

public class HeaterMenu extends AbstractContainerMenu {

    public static final int FUEL_X = 20;
    public static final int FUEL_Y = 36;
    public static final int INVENTORY_Y = 84;
    private static final int FUEL_SLOT = 0;

    private final Container container;
    private final ContainerData data;

    // Client side, the real container and data come through the sync
    public HeaterMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    public HeaterMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.HEATER.get(), id);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        addSlot(new Slot(container, FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return HeaterBlockEntity.isFuel(stack);
            }
        });
        addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        addDataSlots(data);
    }

    public boolean isLit() {
        return data.get(0) > 0;
    }

    // How much of the current fuel is left, from 1 down to 0
    public float litProgress() {
        int duration = data.get(1);
        return duration <= 0 ? 0.0F : Math.min(1.0F, (float) data.get(0) / duration);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == FUEL_SLOT) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!HeaterBlockEntity.isFuel(stack) || !moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
