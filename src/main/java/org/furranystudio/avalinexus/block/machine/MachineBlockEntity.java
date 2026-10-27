/**
 * File: MachineBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.fluid.FluidPort;

import java.util.List;
import java.util.function.Supplier;

public abstract class MachineBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    private NonNullList<ItemStack> items;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        items = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    protected abstract List<MachineTank> tanks();

    public abstract FluidPort port(MachineSide side);

    public abstract void serverTick(ServerLevel level, BlockPos pos, BlockState state);

    protected abstract Component getDefaultName();

    protected abstract AbstractContainerMenu createMenu(int id, Inventory inventory);

    protected void setLit(ServerLevel level, BlockPos pos, BlockState state, boolean lit, Supplier<String> stopReason) {
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), 3);
            AvaliNexus.debug("{} at {}: {}", BuiltInRegistries.BLOCK.getKey(state.getBlock()), pos.toShortString(),
                lit ? "starts" : "stops, " + stopReason.get());
        }
    }

    protected void logFinished(BlockPos pos, BlockState state) {
        AvaliNexus.debug("{} at {}: finishes a recipe", BuiltInRegistries.BLOCK.getKey(state.getBlock()), pos.toShortString());
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return createMenu(id, inventory);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        for (MachineTank tank : tanks()) {
            tank.save(output);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        for (MachineTank tank : tanks()) {
            tank.load(input);
        }
    }
}
