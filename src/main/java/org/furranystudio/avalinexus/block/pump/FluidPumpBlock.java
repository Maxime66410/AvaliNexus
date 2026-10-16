/**
 * File: FluidPumpBlock.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.pump;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidPort;
import org.furranystudio.avalinexus.fluid.FluidPortBlock;
import org.furranystudio.avalinexus.block.pipe.PipeNetwork;
import org.furranystudio.avalinexus.sound.ModSounds;

public class FluidPumpBlock extends Block implements FluidPortBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    // 250 mB per second
    private static final int RATE = 250 * PipeNetwork.PERIOD / 20;

    public FluidPumpBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static Fluid pumped(BlockGetter level, BlockPos pos) {
        FluidState below = level.getFluidState(pos.below());
        return below.isSource() && AvaliLiquids.isCarried(below.getType()) ? AvaliLiquids.source(below.getType()) : Fluids.EMPTY;
    }

    @Override
    public boolean hasPort(BlockState state, Direction side) {
        return side != state.getValue(FACING) && side != Direction.DOWN;
    }

    @Override
    public FluidPort port(Level level, BlockPos pos, BlockState state, Direction side) {
        return new PumpFluidPort(level, pos.immutable());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            Fluid fluid = pumped(level, pos);
            player.sendOverlayMessage(fluid == Fluids.EMPTY
                ? Component.translatable("message.avalinexus.pump.dry")
                : Component.translatable("message.avalinexus.pump.pumping", AvaliLiquids.name(fluid)));
        }
        return InteractionResult.SUCCESS;
    }

    private record PumpFluidPort(Level level, BlockPos pos) implements FluidPort {

        @Override
        public Role role() {
            return Role.SOURCE;
        }

        @Override
        public Object identity() {
            return pos;
        }

        @Override
        public Fluid fluid() {
            return pumped(level, pos);
        }

        @Override
        public int amount() {
            return 0;
        }

        @Override
        public int capacity() {
            return 0;
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            Fluid pumped = pumped(level, pos);
            int taken = pumped != Fluids.EMPTY && pumped.isSame(fluid) ? Math.clamp(amount, 0, RATE) : 0;
            if (!simulate && taken > 0 && level.getRandom().nextInt(8) == 0) {
                level.playSound(null, pos, ModSounds.PUMP_WORKING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return taken;
        }
    }
}
