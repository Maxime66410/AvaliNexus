/**
 * File: GraphenePipeBlock.java
 * Author: Maxime66410
 * Created: 2026-10-13
 * Last Modified: 2026-10-13
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GraphenePipeBlock extends PipeBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public GraphenePipeBlock(float size, Properties properties) {
        super(size, properties);
        BlockState state = stateDefinition.any().setValue(WATERLOGGED, false);
        for (BooleanProperty side : PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(side, false);
        }
        registerDefaultState(state);
    }

    private boolean isPipe(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos.relative(direction)).is(this);
    }

    private boolean isSolid(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos side = pos.relative(direction);
        return level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite());
    }

    private BlockState extendEnd(BlockGetter level, BlockPos pos, BlockState state) {
        Direction only = null;
        for (Direction direction : Direction.values()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                if (only != null) {
                    return state;
                }
                only = direction;
            }
        }
        if (only != null && isSolid(level, pos, only.getOpposite())) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(only.getOpposite()), true);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction anchor = context.getClickedFace().getOpposite();
        BlockState state = defaultBlockState().setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        for (Direction direction : Direction.values()) {
            boolean linked = isPipe(level, pos, direction) || direction == anchor && isSolid(level, pos, direction);
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), linked);
        }
        return extendEnd(level, pos, state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        BooleanProperty side = PROPERTY_BY_DIRECTION.get(direction);
        boolean linked = neighborState.is(this) || state.getValue(side) && isSolid(level, pos, direction);
        return extendEnd(level, pos, state.setValue(side, linked));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Vec3 local = hit.getLocation().subtract(Vec3.atCenterOf(pos));
        Direction direction = Direction.getApproximateNearest(local.x, local.y, local.z);
        BooleanProperty side = PROPERTY_BY_DIRECTION.get(direction);
        if (isPipe(level, pos, direction) || !isSolid(level, pos, direction) && !state.getValue(side)) {
            return InteractionResult.PASS;
        }
        level.setBlock(pos, state.cycle(side), Block.UPDATE_ALL);
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
