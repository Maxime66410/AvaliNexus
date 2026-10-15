/**
 * File: MachineMenu.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public abstract class MachineMenu extends AbstractContainerMenu {

    public static final int HEIGHT = 180;
    public static final int INVENTORY_Y = 98;
    public static final int GAUGE_Y = 18;
    public static final int GAUGE_HEIGHT = 44;
    public static final int BUCKET_Y = 66;

    private final Container container;
    private final ContainerData data;
    private final int machineSlots;

    protected MachineMenu(MenuType<?> type, int id, Inventory inventory, Container container, ContainerData data) {
        super(type, id);
        checkContainerDataCount(data, dataCount(gauges().size()));
        this.container = container;
        this.data = data;
        this.machineSlots = container.getContainerSize();
        addMachineSlots(container);
        addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        addDataSlots(data);
    }

    public static int dataCount(int tanks) {
        return 2 + tanks * 3;
    }

    public static ContainerData data(List<MachineTank> tanks, Progress progress) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return progress.progress();
                }
                if (index == 1) {
                    return progress.duration();
                }
                MachineTank tank = tanks.get((index - 2) / 3);
                return switch ((index - 2) % 3) {
                    case 0 -> BuiltInRegistries.FLUID.getId(tank.fluid());
                    case 1 -> tank.amount();
                    default -> tank.capacity();
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return dataCount(tanks.size());
            }
        };
    }

    public interface Progress {
        int progress();

        int duration();
    }

    public record Gauge(int x) {
    }

    public abstract List<Gauge> gauges();

    protected abstract void addMachineSlots(Container container);


    public Fluid gaugeFluid(int gauge) {
        return BuiltInRegistries.FLUID.byId(data.get(2 + gauge * 3));
    }

    public int gaugeAmount(int gauge) {
        return data.get(3 + gauge * 3);
    }

    public int gaugeCapacity(int gauge) {
        return data.get(4 + gauge * 3);
    }

    public float progress() {
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
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveIntoMachine(stack)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private boolean moveIntoMachine(ItemStack stack) {
        boolean moved = false;
        for (int i = 0; i < machineSlots && !stack.isEmpty(); i++) {
            if (slots.get(i).mayPlace(stack) && moveItemStackTo(stack, i, i + 1, false)) {
                moved = true;
            }
        }
        return moved;
    }

    protected void addMachineSlot(Container container, int index, int x, int y) {
        addSlot(new Slot(container, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(index, stack);
            }
        });
    }

    protected void addBucketSlot(Container container, int index, int x) {
        addSlot(new Slot(container, index, x, BUCKET_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(index, stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
