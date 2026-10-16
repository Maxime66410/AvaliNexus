/**
 * File: GraphenePressBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.press;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
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
import org.furranystudio.avalinexus.item.ModItems;

import java.util.List;

public class GraphenePressBlockEntity extends MachineBlockEntity {

    public static final int FUEL_BUCKET = 0;
    public static final int COAL = 1;
    public static final int SHARD = 2;
    public static final int OUTPUT = 3;

    private static final int DURATION = 200;
    private static final int FUEL_COST = 100;
    private static final int COAL_COST = 4;
    private static final int GRAPHENE_COUNT = 6;
    private static final int TANK_CAPACITY = 4 * AvaliLiquids.BUCKET;
    private static final int[] TOP_SLOTS = {COAL, SHARD};
    private static final int[] BOTTOM_SLOTS = {OUTPUT};
    private static final int[] NO_SLOTS = {};

    private final MachineTank fuel = new MachineTank("Fuel", TANK_CAPACITY, fluid -> fluid.isSame(ModFluids.source(AvaliFluidKind.FUEL)), this::setChanged);
    private final List<MachineTank> tanks = List.of(fuel);
    private ItemStack swapped = ItemStack.EMPTY;
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

    public GraphenePressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAPHENE_PRESS.get(), pos, state, 4);
    }

    public static boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case FUEL_BUCKET -> stack.is(Items.BUCKET) || AvaliLiquids.fromBucket(stack).isSame(ModFluids.source(AvaliFluidKind.FUEL));
            case COAL -> stack.is(ItemTags.COALS);
            case SHARD -> stack.is(ModItems.NEXITE_SHARD.get());
            default -> false;
        };
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
        boolean running = getItem(COAL).getCount() >= COAL_COST
            && !getItem(SHARD).isEmpty()
            && fuel.amount() >= FUEL_COST
            && fits();
        if (running) {
            progress++;
            if (progress >= DURATION) {
                progress = 0;
                finish();
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
        setLit(level, pos, state, running);
    }

    private boolean fits() {
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ModItems.GRAPHENE.get()) && output.getCount() + GRAPHENE_COUNT <= output.getMaxStackSize();
    }

    private void finish() {
        fuel.drain(fuel.fluid(), FUEL_COST, false);
        getItem(COAL).shrink(COAL_COST);
        getItem(SHARD).shrink(1);
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            setItem(OUTPUT, new ItemStack(ModItems.GRAPHENE.get(), GRAPHENE_COUNT));
        } else {
            output.grow(GRAPHENE_COUNT);
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
        return Component.translatable("container.avalinexus.graphene_press");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new GraphenePressMenu(id, inventory, this, data);
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
