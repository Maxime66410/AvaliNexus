/**
 * File: GraphenePipeBlock.java
 * Author: Maxime66410
 * Created: 2026-10-13
 * Last Modified: 2026-10-13
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
import org.furranystudio.avalinexus.block.pipe.PipeFluid;
import org.furranystudio.avalinexus.fluid.FluidPortBlock;

public class GraphenePipeBlock extends PipeBlock implements SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<PipeFluid> FLUID = EnumProperty.create("fluid", PipeFluid.class);

    public GraphenePipeBlock(float size, Properties properties) {
        super(size, properties);
        BlockState state = stateDefinition.any().setValue(WATERLOGGED, false).setValue(FLUID, PipeFluid.EMPTY);
        for (BooleanProperty side : PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(side, false);
        }
        registerDefaultState(state);
    }

    private boolean isPipe(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos.relative(direction)).is(this);
    }

    private boolean canLink(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && state.getValue(FLUID).compatible(neighbor.getValue(FLUID));
    }

    private boolean isSolid(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos side = pos.relative(direction);
        return level.getBlockState(side).isFaceSturdy(level, side, direction.getOpposite()) || isPort(level, pos, direction);
    }

    private boolean isPort(BlockGetter level, BlockPos pos, Direction direction) {
        return FluidPortBlock.linkable(level, pos.relative(direction), direction.getOpposite());
    }

    public BlockState relink(BlockGetter level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(this)) {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), canLink(state, neighbor));
            }
        }
        return state;
    }

    public static void attach(Level level, BlockPos pos, Direction toward) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof GraphenePipeBlock) {
            level.setBlock(pos, state.setValue(PROPERTY_BY_DIRECTION.get(toward), true), Block.UPDATE_ALL);
        }
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
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED, FLUID);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction anchor = context.getClickedFace().getOpposite();
        BlockState state = defaultBlockState().setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(this) && state.getValue(FLUID) == PipeFluid.EMPTY) {
                state = state.setValue(FLUID, neighbor.getValue(FLUID));
            }
        }
        for (Direction direction : Direction.values()) {
            boolean linked = canLink(state, level.getBlockState(pos.relative(direction)))
                || isPort(level, pos, direction)
                || direction == anchor && isSolid(level, pos, direction);
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
        boolean linked = canLink(state, neighborState) || state.getValue(side) && isSolid(level, pos, direction);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraphenePipeBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.GRAPHENE_PIPE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<GraphenePipeBlockEntity>) (tickLevel, pos, tickState, pipe) ->
            pipe.serverTick((ServerLevel) tickLevel, pos);
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
