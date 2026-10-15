/**
 * File: CryoFreezerMenu.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.freezer;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.inventory.ModMenus;

import java.util.List;

public class CryoFreezerMenu extends MachineMenu {

    public static final int COOLANT_X = 12;
    public static final int LIQUID_X = 34;
    public static final int INPUT_X = 62;
    public static final int OUTPUT_X = 134;
    public static final int ITEM_Y = 34;
    private static final List<Gauge> GAUGES = List.of(new Gauge(COOLANT_X), new Gauge(LIQUID_X));

    public CryoFreezerMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(4) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return CryoFreezerBlockEntity.accepts(slot, stack);
            }
        }, new SimpleContainerData(dataCount(2)));
    }

    public CryoFreezerMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.CRYO_FREEZER.get(), id, inventory, container, data);
    }

    @Override
    public List<Gauge> gauges() {
        return GAUGES;
    }

    @Override
    protected void addMachineSlots(Container container) {
        addBucketSlot(container, CryoFreezerBlockEntity.COOLANT_BUCKET, COOLANT_X);
        addBucketSlot(container, CryoFreezerBlockEntity.FLUID_BUCKET, LIQUID_X);
        addMachineSlot(container, CryoFreezerBlockEntity.INPUT, INPUT_X, ITEM_Y);
        addMachineSlot(container, CryoFreezerBlockEntity.OUTPUT, OUTPUT_X, ITEM_Y);
    }
}
