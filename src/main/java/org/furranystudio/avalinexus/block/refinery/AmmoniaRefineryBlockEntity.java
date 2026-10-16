/**
 * File: AmmoniaRefineryBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.refinery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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

public class AmmoniaRefineryBlockEntity extends MachineBlockEntity {

    public static final int FUEL_BUCKET = 0;
    public static final int AMMONIA_BUCKET = 1;
    public static final int OUTPUT_BUCKET = 2;
    public static final int INGREDIENT = 3;
    public static final int COAL = 4;

    private static final int DURATION = 200;
    private static final int FUEL_COST = 250;
    private static final int COAL_CHARGES = 8;
    private static final int TANK_CAPACITY = 4 * AvaliLiquids.BUCKET;
    private static final int[] TOP_SLOTS = {INGREDIENT, COAL, OUTPUT_BUCKET};
    private static final int[] BOTTOM_SLOTS = {OUTPUT_BUCKET};
    private static final int[] NO_SLOTS = {};

    private final MachineTank fuel = new MachineTank("Fuel", TANK_CAPACITY, fluid -> is(fluid, AvaliFluidKind.FUEL), this::setChanged);
    private final MachineTank ammonia = new MachineTank("Ammonia", TANK_CAPACITY, fluid -> is(fluid, AvaliFluidKind.AMMONIA), this::setChanged);
    private final MachineTank output = new MachineTank("Output", TANK_CAPACITY, fluid -> true, this::setChanged);
    private final List<MachineTank> tanks = List.of(fuel, ammonia, output);
    private final ItemStack[] swapped = {ItemStack.EMPTY, ItemStack.EMPTY};
    private int progress;
    private int coalCharges;

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

    public AmmoniaRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AMMONIA_REFINERY.get(), pos, state, 5);
    }

    private static boolean is(Fluid fluid, AvaliFluidKind kind) {
        return fluid.isSame(ModFluids.source(kind));
    }

    public static boolean isCoal(ItemStack stack) {
        return stack.is(ItemTags.COALS);
    }

    public static boolean isIngredient(ItemStack stack) {
        return stack.is(ModItems.GRAPHENE.get()) || isCoal(stack);
    }

    @Override
    protected List<MachineTank> tanks() {
        return tanks;
    }

    @Override
    public FluidPort port(MachineSide side) {
        return switch (side) {
            case BACK -> fuel.port(FluidPort.Role.SINK);
            case SIDE -> ammonia.port(FluidPort.Role.SINK);
            case TOP -> output.port(FluidPort.Role.SOURCE);
            default -> null;
        };
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        swap(FUEL_BUCKET, fuel);
        swap(AMMONIA_BUCKET, ammonia);
        bucket(OUTPUT_BUCKET, output, true);
        ItemStack ingredient = getItem(INGREDIENT);
        Fluid product = ingredient.is(ModItems.GRAPHENE.get()) ? ModFluids.source(AvaliFluidKind.COOLANT)
            : isCoal(ingredient) ? ModFluids.source(AvaliFluidKind.FUEL) : null;
        int produced = isCoal(ingredient) ? 2 * AvaliLiquids.BUCKET : AvaliLiquids.BUCKET;
        boolean running = product != null
            && ammonia.amount() >= AvaliLiquids.BUCKET
            && output.fill(product, produced, true) == produced
            && hasHeat();
        if (running) {
            progress++;
            if (progress >= DURATION) {
                progress = 0;
                payHeat();
                ammonia.drain(ammonia.fluid(), AvaliLiquids.BUCKET, false);
                ingredient.shrink(1);
                output.fill(product, produced, false);
                level.playSound(null, pos, ModSounds.REFINERY_DONE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
        setLit(level, pos, state, running);
    }

    private void swap(int slot, MachineTank tank) {
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

    private void bucket(int slot, MachineTank tank, boolean output) {
        ItemStack stack = getItem(slot);
        ItemStack result = tank.useBucket(stack, output);
        if (result != stack) {
            setItem(slot, result);
        }
    }

    private boolean hasHeat() {
        return coalCharges > 0 || fuel.amount() >= FUEL_COST || isCoal(getItem(COAL));
    }

    private void payHeat() {
        if (fuel.amount() >= FUEL_COST) {
            fuel.drain(fuel.fluid(), FUEL_COST, false);
            return;
        }
        if (coalCharges <= 0) {
            getItem(COAL).shrink(1);
            coalCharges = COAL_CHARGES;
            level.playSound(null, worldPosition, ModSounds.REFINERY_IGNITE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        coalCharges--;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accepts(slot, stack);
    }

    public static boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case FUEL_BUCKET -> stack.is(Items.BUCKET) || is(AvaliLiquids.fromBucket(stack), AvaliFluidKind.FUEL);
            case AMMONIA_BUCKET -> stack.is(Items.BUCKET) || is(AvaliLiquids.fromBucket(stack), AvaliFluidKind.AMMONIA);
            case OUTPUT_BUCKET -> stack.is(Items.BUCKET);
            case INGREDIENT -> isIngredient(stack);
            case COAL -> isCoal(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? TOP_SLOTS : side == Direction.DOWN ? BOTTOM_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.UP && canPlaceItem(slot, stack) && (slot != OUTPUT_BUCKET || getItem(slot).isEmpty());
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == OUTPUT_BUCKET && !stack.is(Items.BUCKET);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.avalinexus.ammonia_refinery");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new AmmoniaRefineryMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
        output.putInt("CoalCharges", coalCharges);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("Progress", 0);
        coalCharges = input.getIntOr("CoalCharges", 0);
    }
}
