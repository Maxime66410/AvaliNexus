/**
 * File: LightStripBlock.java
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class LightStripBlock extends Block implements SimpleWaterloggedBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape[]> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            SHAPES.put(facing, new VoxelShape[]{shape(facing, false), shape(facing, true)});
        }
    }

    public LightStripBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(ROTATED, false)
            .setValue(WATERLOGGED, false));
    }

    private static Direction.Axis length(Direction facing, boolean rotated) {
        if (facing.getAxis() == Direction.Axis.Y) {
            return rotated ? Direction.Axis.Z : Direction.Axis.X;
        }
        if (rotated) {
            return Direction.Axis.Y;
        }
        return facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    private static VoxelShape shape(Direction facing, boolean rotated) {
        Direction.Axis along = length(facing, rotated);
        double[] min = new double[3];
        double[] max = new double[3];
        for (Direction.Axis axis : Direction.Axis.values()) {
            int i = axis.ordinal();
            if (axis == facing.getAxis()) {
                boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                min[i] = positive ? 0 : 15;
                max[i] = positive ? 1 : 16;
            } else if (axis == along) {
                min[i] = 0;
                max[i] = 16;
            } else {
                min[i] = 7;
                max[i] = 9;
            }
        }
        return Block.box(min[0], min[1], min[2], max[0], max[1], max[2]);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATED, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(ROTATED) ? 1 : 0];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        boolean rotated = facing.getAxis() == Direction.Axis.Y
            ? context.getHorizontalDirection().getAxis() == Direction.Axis.Z
            : context.isSecondaryUseActive();
        BlockState state = defaultBlockState()
            .setValue(FACING, facing)
            .setValue(ROTATED, rotated)
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        level.setBlock(pos, state.cycle(ROTATED), Block.UPDATE_ALL);
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        boolean flat = state.getValue(FACING).getAxis() == Direction.Axis.Y;
        boolean quarter = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        return state
            .setValue(FACING, rotation.rotate(state.getValue(FACING)))
            .setValue(ROTATED, state.getValue(ROTATED) ^ (flat && quarter));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
