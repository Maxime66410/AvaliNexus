/**
 * File: FluidPortBlock.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface FluidPortBlock {

    boolean hasPort(BlockState state, Direction side);

    FluidPort port(Level level, BlockPos pos, BlockState state, Direction side);

    static boolean linkable(BlockGetter level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FluidPortBlock block && block.hasPort(state, side);
    }
}
