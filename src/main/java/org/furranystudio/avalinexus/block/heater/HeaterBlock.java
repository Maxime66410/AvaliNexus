/**
 * File: HeaterBlock.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block.heater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

// Burns furnace fuel to keep the polar cold away in a radius, faces the player like a furnace
public class HeaterBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // The heater leans on the wall behind it and takes a bit more than half the block
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    // How far the front of the model sits from the block center
    private static final double FRONT_OFFSET = 0.14;

    static {
        SHAPES.put(Direction.WEST, Block.box(6, 0, 0, 16, 16, 16));
        SHAPES.put(Direction.EAST, Block.box(0, 0, 0, 10, 16, 16));
        SHAPES.put(Direction.NORTH, Block.box(0, 0, 6, 16, 16, 16));
        SHAPES.put(Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 10));
    }

    public HeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof HeaterBlockEntity heater) {
            player.openMenu(heater);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeaterBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.HEATER.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<HeaterBlockEntity>) (tickLevel, pos, tickState, heater) ->
            heater.serverTick((ServerLevel) tickLevel, pos, tickState);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || random.nextInt(3) != 0) {
            return;
        }
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() * FRONT_OFFSET + (random.nextDouble() - 0.5) * 0.6 * Math.abs(facing.getStepZ());
        double y = pos.getY() + 0.2 + random.nextDouble() * 0.6;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * FRONT_OFFSET + (random.nextDouble() - 0.5) * 0.6 * Math.abs(facing.getStepX());
        level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
