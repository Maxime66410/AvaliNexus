/**
 * File: FluidBridge.java
 * Author: Maxime66410
 * Created: 2026-11-12
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.block.pipe.PipeFluid;

public final class FluidBridge {

    public static final int DROPLETS_PER_MILLIBUCKET = 81;

    public interface ForeignLookup {
        FluidPort find(Level level, BlockPos pos, Direction side, boolean anyFluid);
    }

    private static ForeignLookup foreignLookup = (level, pos, side, anyFluid) -> null;

    private FluidBridge() {
    }

    public static void setForeignLookup(ForeignLookup lookup) {
        foreignLookup = lookup;
    }

    public static boolean supported(Fluid fluid) {
        return fluid != Fluids.EMPTY && PipeFluid.of(fluid) != PipeFluid.EMPTY;
    }

    public static boolean allowed(Fluid fluid, boolean anyFluid) {
        return anyFluid ? fluid != Fluids.EMPTY : supported(fluid);
    }

    public static boolean exposes(Block block) {
        return block instanceof FluidPortBlock && block instanceof EntityBlock;
    }

    public static FluidPort exposed(Level level, BlockPos pos, BlockState state, Direction side) {
        if (!(state.getBlock() instanceof FluidPortBlock block) || !exposes(state.getBlock())) {
            return null;
        }
        if (side != null) {
            return block.hasPort(state, side) ? block.port(level, pos, state, side) : null;
        }
        for (Direction direction : Direction.values()) {
            FluidPort port = block.hasPort(state, direction) ? block.port(level, pos, state, direction) : null;
            if (port != null) {
                return new ReadOnlyPort(port);
            }
        }
        return null;
    }

    public static FluidPort foreign(Level level, BlockPos pos, Direction side) {
        return foreign(level, pos, side, false);
    }

    public static FluidPort foreignAnyFluid(Level level, BlockPos pos, Direction side) {
        return foreign(level, pos, side, true);
    }

    private static FluidPort foreign(Level level, BlockPos pos, Direction side, boolean anyFluid) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof FluidPortBlock || state.getBlock() instanceof GraphenePipeBlock) {
            return null;
        }
        return foreignLookup.find(level, pos.immutable(), side, anyFluid);
    }

    private record ReadOnlyPort(FluidPort port) implements FluidPort {

        @Override
        public Role role() {
            return port.role();
        }

        @Override
        public Object identity() {
            return port.identity();
        }

        @Override
        public Fluid fluid() {
            return port.fluid();
        }

        @Override
        public int amount() {
            return port.amount();
        }

        @Override
        public int capacity() {
            return port.capacity();
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            return 0;
        }
    }
}
