/**
 * File: CryoFreezerBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.freezer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

public class CryoFreezerBlockEntity extends MachineBlockEntity {

    public static final int COOLANT_BUCKET = 0;
    public static final int FLUID_BUCKET = 1;
    public static final int INPUT = 2;
    public static final int OUTPUT = 3;

    private static final int DURATION = 100;
    private static final int COOLANT_COST = 250;
    private static final int TANK_CAPACITY = 4 * AvaliLiquids.BUCKET;
    private static final int[] TOP_SLOTS = {INPUT};
    private static final int[] BOTTOM_SLOTS = {OUTPUT};
    private static final int[] NO_SLOTS = {};

    private final MachineTank coolant = new MachineTank("Coolant", TANK_CAPACITY, fluid -> is(fluid, AvaliFluidKind.COOLANT), this::setChanged);
    private final MachineTank liquid = new MachineTank("Liquid", TANK_CAPACITY, CryoFreezerBlockEntity::freezable, this::setChanged);
    private final List<MachineTank> tanks = List.of(coolant, liquid);
    private final ItemStack[] swapped = {ItemStack.EMPTY, ItemStack.EMPTY};
    private int progress;

    private final ContainerData data = MachineMenu.data(tanks, new MachineMenu.Progress() {
        @Override
        public int progress() {
            return progress;
        }

        @Override
        public int duration() {
            return DURATION;
        }
    });

    public CryoFreezerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYO_FREEZER.get(), pos, state, 4);
    }

    private static boolean is(Fluid fluid, AvaliFluidKind kind) {
        return fluid.isSame(ModFluids.source(kind));
    }

    private static boolean freezable(Fluid fluid) {
        return fluid.isSame(Fluids.WATER) || is(fluid, AvaliFluidKind.AMMONIA);
    }

    private static Item packed(ItemStack stack) {
        if (stack.is(Items.ICE)) {
            return Items.PACKED_ICE;
        }
        if (stack.is(Items.PACKED_ICE)) {
            return Items.BLUE_ICE;
        }
        if (stack.is(ModItems.AMMONIA_ICE.get())) {
            return ModItems.PACKED_AMMONIA_ICE.get();
        }
        return null;
    }

    public static boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case COOLANT_BUCKET -> stack.is(Items.BUCKET) || is(AvaliLiquids.fromBucket(stack), AvaliFluidKind.COOLANT);
            case FLUID_BUCKET -> stack.is(Items.BUCKET) || freezable(AvaliLiquids.fromBucket(stack));
            case INPUT -> packed(stack) != null || stack.is(Items.BUCKET);
            default -> false;
        };
    }

    @Override
    protected List<MachineTank> tanks() {
        return tanks;
    }

    @Override
    public FluidPort port(MachineSide side) {
        return switch (side) {
            case BACK -> coolant.port(FluidPort.Role.SINK);
            case SIDE -> liquid.port(FluidPort.Role.SINK);
            default -> null;
        };
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        bucket(COOLANT_BUCKET, coolant);
        bucket(FLUID_BUCKET, liquid);
        ItemStack result = result();
        boolean running = !result.isEmpty() && coolant.amount() >= COOLANT_COST && fits(result);
        if (running) {
            progress++;
            if (progress >= DURATION) {
                progress = 0;
                finish(result);
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
        setLit(level, pos, state, running);
    }

    private ItemStack result() {
        ItemStack input = getItem(INPUT);
        Item packed = packed(input);
        if (packed != null) {
            return new ItemStack(packed);
        }
        boolean water = liquid.amount() >= AvaliLiquids.BUCKET && liquid.fluid().isSame(Fluids.WATER);
        if (input.is(Items.BUCKET) && water) {
            return new ItemStack(Items.POWDER_SNOW_BUCKET);
        }
        if (liquid.amount() < AvaliLiquids.BUCKET) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(water ? Items.ICE : ModItems.AMMONIA_ICE.get());
    }

    private boolean fits(ItemStack result) {
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize();
    }

    private void finish(ItemStack result) {
        coolant.drain(coolant.fluid(), COOLANT_COST, false);
        ItemStack input = getItem(INPUT);
        if (packed(input) != null || result.is(Items.POWDER_SNOW_BUCKET)) {
            input.shrink(1);
        }
        if (!result.is(Items.PACKED_ICE) && !result.is(Items.BLUE_ICE) && !result.is(ModItems.PACKED_AMMONIA_ICE.get())) {
            liquid.drain(liquid.fluid(), AvaliLiquids.BUCKET, false);
        }
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            setItem(OUTPUT, result);
        } else {
            output.grow(1);
        }
        level.playSound(null, worldPosition, result.is(Items.POWDER_SNOW_BUCKET) ? ModSounds.FREEZER_POWDER_SNOW.get() : ModSounds.FREEZER_DONE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void bucket(int slot, MachineTank tank) {
        ItemStack stack = getItem(slot);
        if (stack == swapped[slot]) {
            return;
        }
        ItemStack result = tank.useBucket(stack, stack.is(Items.BUCKET));
        if (result != stack) {
            setItem(slot, result);
            swapped[slot] = result;
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
        return Component.translatable("container.avalinexus.cryo_freezer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new CryoFreezerMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("Progress", 0);
    }
}
