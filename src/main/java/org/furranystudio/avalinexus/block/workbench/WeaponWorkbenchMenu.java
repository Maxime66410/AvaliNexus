/**
 * File: WeaponWorkbenchMenu.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.workbench;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.inventory.ModMenus;

import java.util.List;

public class WeaponWorkbenchMenu extends MachineMenu {

    public static final int FUEL_X = 12;
    public static final int RECIPES_X = 34;
    public static final int RECIPES_Y = 18;
    public static final int RECIPE_SIZE = 18;
    public static final int RECIPE_COLUMNS = 3;
    public static final int RECIPE_ROWS = 3;
    public static final int GRID_X = 94;
    public static final int GRID_Y = 18;
    public static final int OUTPUT_X = 152;
    public static final int OUTPUT_Y = 36;
    public static final int BAR_Y = 74;
    private static final List<Gauge> GAUGES = List.of(new Gauge(FUEL_X));

    private final Container container;
    private final ContainerData data;

    public WeaponWorkbenchMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(WeaponWorkbenchBlockEntity.SIZE) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return WeaponWorkbenchBlockEntity.accepts(slot, stack);
            }
        }, new SimpleContainerData(dataCount(1) + 1));
    }

    public WeaponWorkbenchMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.WEAPON_WORKBENCH.get(), id, inventory, container, data);
        this.container = container;
        this.data = data;
    }

    @Override
    public List<Gauge> gauges() {
        return GAUGES;
    }

    @Override
    protected void addMachineSlots(Container container) {
        addBucketSlot(container, WeaponWorkbenchBlockEntity.FUEL_BUCKET, FUEL_X);
        for (int i = 0; i < WeaponWorkbenchBlockEntity.INGREDIENTS; i++) {
            addMachineSlot(container, WeaponWorkbenchBlockEntity.FIRST_INGREDIENT + i, GRID_X + i % 3 * 18, GRID_Y + i / 3 * 18);
        }
        addMachineSlot(container, WeaponWorkbenchBlockEntity.OUTPUT, OUTPUT_X, OUTPUT_Y);
    }

    public int selected() {
        return data.get(dataCount(1));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (WeaponRecipe.get(id) == null) {
            return false;
        }
        if (container instanceof WeaponWorkbenchBlockEntity workbench) {
            workbench.select(id);
        }
        return true;
    }
}
