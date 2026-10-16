/**
 * File: WeaponWorkbenchBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.workbench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.block.machine.MachineBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineMenu;
import org.furranystudio.avalinexus.block.machine.MachineSide;
import org.furranystudio.avalinexus.block.machine.MachineTank;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidPort;
import org.furranystudio.avalinexus.fluid.ModFluids;

import java.util.List;

public class WeaponWorkbenchBlockEntity extends MachineBlockEntity {

    public static final int FUEL_BUCKET = 0;
    public static final int FIRST_INGREDIENT = 1;
    public static final int INGREDIENTS = 9;
    public static final int OUTPUT = FIRST_INGREDIENT + INGREDIENTS;
    public static final int SIZE = OUTPUT + 1;

    private static final int FUEL_COST = 200;
    private static final int TANK_CAPACITY = 4 * AvaliLiquids.BUCKET;
    private static final int[] TOP_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] BOTTOM_SLOTS = {OUTPUT};
    private static final int[] NO_SLOTS = {};

    private final MachineTank fuel = new MachineTank("Fuel", TANK_CAPACITY, fluid -> fluid.isSame(ModFluids.source(AvaliFluidKind.FUEL)), this::setChanged);
    private final List<MachineTank> tanks = List.of(fuel);
    private ItemStack swapped = ItemStack.EMPTY;
    private int progress;
    private int selected = -1;

    private final ContainerData machineData = MachineMenu.data(tanks, new MachineMenu.Progress() {
        @Override
        public int progress() {
            return progress;
        }

        @Override
        public int duration() {
            WeaponRecipe recipe = WeaponRecipe.get(selected);
            return recipe == null ? 0 : recipe.duration();
        }
    });

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index < machineData.getCount() ? machineData.get(index) : selected;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return machineData.getCount() + 1;
        }
    };

    public WeaponWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEAPON_WORKBENCH.get(), pos, state, SIZE);
    }

    public static boolean accepts(int slot, ItemStack stack) {
        if (slot == FUEL_BUCKET) {
            return stack.is(Items.BUCKET) || AvaliLiquids.fromBucket(stack).isSame(ModFluids.source(AvaliFluidKind.FUEL));
        }
        return slot >= FIRST_INGREDIENT && slot < OUTPUT && WeaponRecipe.isIngredient(stack);
    }

    public void select(int index) {
        if (index != selected && WeaponRecipe.get(index) != null) {
            selected = index;
            progress = 0;
            setChanged();
        }
    }

    @Override
    protected List<MachineTank> tanks() {
        return tanks;
    }

    @Override
    public FluidPort port(MachineSide side) {
        return side == MachineSide.BACK ? fuel.port(FluidPort.Role.SINK) : null;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack bucket = getItem(FUEL_BUCKET);
        if (bucket != swapped) {
            ItemStack result = fuel.useBucket(bucket, bucket.is(Items.BUCKET));
            if (result != bucket) {
                setItem(FUEL_BUCKET, result);
                swapped = result;
            }
        }
        WeaponRecipe recipe = WeaponRecipe.get(selected);
        boolean running = recipe != null && fuel.amount() >= FUEL_COST && hasCosts(recipe) && fits(recipe.output());
        if (running) {
            progress++;
            if (progress >= recipe.duration()) {
                progress = 0;
                finish(recipe);
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
        setLit(level, pos, state, running);
    }

    private boolean hasCosts(WeaponRecipe recipe) {
        for (WeaponRecipe.Cost cost : recipe.costs()) {
            int found = 0;
            for (int slot = FIRST_INGREDIENT; slot < OUTPUT; slot++) {
                ItemStack stack = getItem(slot);
                if (stack.is(cost.item().get())) {
                    found += stack.getCount();
                }
            }
            if (found < cost.count()) {
                return false;
            }
        }
        return true;
    }

    private boolean fits(ItemStack result) {
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void finish(WeaponRecipe recipe) {
        fuel.drain(fuel.fluid(), FUEL_COST, false);
        for (WeaponRecipe.Cost cost : recipe.costs()) {
            int left = cost.count();
            for (int slot = FIRST_INGREDIENT; slot < OUTPUT && left > 0; slot++) {
                ItemStack stack = getItem(slot);
                if (stack.is(cost.item().get())) {
                    int taken = Math.min(left, stack.getCount());
                    stack.shrink(taken);
                    left -= taken;
                }
            }
        }
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            setItem(OUTPUT, recipe.output());
        } else {
            output.grow(recipe.count());
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accepts(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? TOP_SLOTS : side == Direction.DOWN ? BOTTOM_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == OUTPUT;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.avalinexus.weapon_workbench");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new WeaponWorkbenchMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
        output.putInt("Selected", selected);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("Progress", 0);
        selected = input.getIntOr("Selected", -1);
    }
}
