/**
 * File: HeaterBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block.heater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.furranystudio.avalinexus.block.ModBlockEntities;

// One fuel slot, burns like a furnace but only takes new fuel when someone is around to be warmed
public class HeaterBlockEntity extends BaseContainerBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int litTime;
    private int litDuration;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? litTime : litDuration;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                litTime = value;
            } else {
                litDuration = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public HeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEATER.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL);
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean wasLit = litTime > 0;
        if (litTime > 0) {
            litTime--;
        }
        if (litTime <= 0 && someoneNearby(level, pos)) {
            burnNextFuel(level);
        }

        boolean lit = litTime > 0;
        if (lit) {
            HeaterZones.mark(level, pos);
        }
        if (wasLit != lit) {
            level.setBlock(pos, state.setValue(HeaterBlock.LIT, lit), 3);
            setChanged();
        }
    }

    private void burnNextFuel(ServerLevel level) {
        ItemStack fuel = items.get(0);
        int duration = ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL, CookingFuel::burnTime, getLootContext(level), 0);
        if (duration <= 0) {
            return;
        }
        litTime = duration;
        litDuration = duration;
        // Lava leaves its bucket behind like in a furnace
        if (fuel.is(Items.LAVA_BUCKET)) {
            items.set(0, new ItemStack(Items.BUCKET));
        } else {
            fuel.shrink(1);
        }
        setChanged();
    }

    private static boolean someoneNearby(ServerLevel level, BlockPos pos) {
        return level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HeaterZones.RADIUS, false) != null;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.avalinexus.heater");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isFuel(stack);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new HeaterMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("LitTime", litTime);
        output.putInt("LitDuration", litDuration);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        litTime = input.getIntOr("LitTime", 0);
        litDuration = input.getIntOr("LitDuration", 0);
    }
}
