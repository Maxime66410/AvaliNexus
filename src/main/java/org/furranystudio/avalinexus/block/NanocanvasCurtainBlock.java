/**
 * File: NanocanvasCurtainBlock.java
 * Author: Maxime66410
 * Created: 2026-10-13
 * Last Modified: 2026-10-13
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class NanocanvasCurtainBlock extends Block {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;

    private static final int MAX_LINKED = 64;
    private static final Map<Direction, VoxelShape> CLOSED_SHAPES = Shapes.rotateHorizontal(Block.box(0, 0, 0, 16, 16, 2));
    private static final Map<Direction, VoxelShape> OPEN_LEFT_SHAPES = Shapes.rotateHorizontal(Block.box(0, 0, 0, 4, 16, 2));
    private static final Map<Direction, VoxelShape> OPEN_RIGHT_SHAPES = Shapes.rotateHorizontal(Block.box(12, 0, 0, 16, 16, 2));

    public NanocanvasCurtainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HINGE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing).setValue(HINGE, hingeFor(level, pos, facing));
    }

    private DoorHingeSide hingeFor(Level level, BlockPos pos, Direction facing) {
        for (Direction vertical : new Direction[]{Direction.DOWN, Direction.UP}) {
            BlockState stacked = level.getBlockState(pos.relative(vertical));
            if (stacked.is(this) && stacked.getValue(FACING) == facing) {
                return stacked.getValue(HINGE);
            }
        }
        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        if (left.is(this) && left.getValue(FACING) == facing && left.getValue(HINGE) == DoorHingeSide.LEFT) {
            return DoorHingeSide.RIGHT;
        }
        return DoorHingeSide.LEFT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(OPEN)) {
            return CLOSED_SHAPES.get(state.getValue(FACING));
        }
        return (state.getValue(HINGE) == DoorHingeSide.LEFT ? OPEN_LEFT_SHAPES : OPEN_RIGHT_SHAPES).get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        boolean open = !state.getValue(OPEN);
        if (player.isSecondaryUseActive()) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        } else {
            for (BlockPos linked : linkedCurtains(level, pos, state)) {
                level.setBlock(linked, level.getBlockState(linked).setValue(OPEN, open), Block.UPDATE_ALL);
            }
        }
        level.playSound(player, pos, open ? SoundEvents.WOOL_BREAK : SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private Set<BlockPos> linkedCurtains(Level level, BlockPos start, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction[] spread = {Direction.UP, Direction.DOWN, facing.getClockWise(), facing.getCounterClockWise()};
        Set<BlockPos> found = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        found.add(start);
        queue.add(start);
        while (!queue.isEmpty() && found.size() < MAX_LINKED) {
            BlockPos pos = queue.poll();
            for (Direction direction : spread) {
                BlockPos next = pos.relative(direction);
                BlockState nextState = level.getBlockState(next);
                if (found.size() < MAX_LINKED && nextState.is(this) && nextState.getValue(FACING) == facing && found.add(next)) {
                    queue.add(next);
                }
            }
        }
        return found;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        return state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }
}
