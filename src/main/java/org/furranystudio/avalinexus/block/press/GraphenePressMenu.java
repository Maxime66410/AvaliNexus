/**
 * File: GraphenePressMenu.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.press;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.inventory.ModMenus;

import java.util.List;

public class GraphenePressMenu extends MachineMenu {

    public static final int FUEL_X = 12;
    public static final int INGREDIENT_X = 62;
    public static final int COAL_Y = 22;
    public static final int SHARD_Y = 46;
    public static final int OUTPUT_X = 134;
    public static final int OUTPUT_Y = 34;
    private static final List<Gauge> GAUGES = List.of(new Gauge(FUEL_X));

    public GraphenePressMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(4) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return GraphenePressBlockEntity.accepts(slot, stack);
            }
        }, new SimpleContainerData(dataCount(1)));
    }

    public GraphenePressMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.GRAPHENE_PRESS.get(), id, inventory, container, data);
    }

    @Override
    public List<Gauge> gauges() {
        return GAUGES;
    }

    @Override
    protected void addMachineSlots(Container container) {
        addBucketSlot(container, GraphenePressBlockEntity.FUEL_BUCKET, FUEL_X);
        addMachineSlot(container, GraphenePressBlockEntity.COAL, INGREDIENT_X, COAL_Y);
        addMachineSlot(container, GraphenePressBlockEntity.SHARD, INGREDIENT_X, SHARD_Y);
        addMachineSlot(container, GraphenePressBlockEntity.OUTPUT, OUTPUT_X, OUTPUT_Y);
    }
}
