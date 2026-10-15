/**
 * File: AmmoniaRefineryMenu.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.refinery;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.inventory.ModMenus;

import java.util.List;

public class AmmoniaRefineryMenu extends MachineMenu {

    public static final int FUEL_X = 12;
    public static final int AMMONIA_X = 34;
    public static final int OUTPUT_X = 148;
    public static final int INGREDIENT_X = 62;
    public static final int INGREDIENT_Y = 22;
    public static final int COAL_Y = 46;
    private static final List<Gauge> GAUGES = List.of(new Gauge(FUEL_X), new Gauge(AMMONIA_X), new Gauge(OUTPUT_X));

    public AmmoniaRefineryMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(5) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return AmmoniaRefineryBlockEntity.accepts(slot, stack);
            }
        }, new SimpleContainerData(dataCount(3)));
    }

    public AmmoniaRefineryMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.AMMONIA_REFINERY.get(), id, inventory, container, data);
    }

    @Override
    public List<Gauge> gauges() {
        return GAUGES;
    }

    @Override
    protected void addMachineSlots(Container container) {
        addBucketSlot(container, AmmoniaRefineryBlockEntity.FUEL_BUCKET, FUEL_X);
        addBucketSlot(container, AmmoniaRefineryBlockEntity.AMMONIA_BUCKET, AMMONIA_X);
        addBucketSlot(container, AmmoniaRefineryBlockEntity.OUTPUT_BUCKET, OUTPUT_X);
        addMachineSlot(container, AmmoniaRefineryBlockEntity.INGREDIENT, INGREDIENT_X, INGREDIENT_Y);
        addMachineSlot(container, AmmoniaRefineryBlockEntity.COAL, INGREDIENT_X, COAL_Y);
    }
}
