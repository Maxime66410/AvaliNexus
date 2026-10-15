/**
 * File: MachineBlock.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.fluid.FluidPort;
import org.furranystudio.avalinexus.fluid.FluidPortBlock;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class MachineBlock extends Block implements EntityBlock, FluidPortBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final Set<MachineSide> ports;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    protected MachineBlock(Properties properties, Set<MachineSide> ports, VoxelShape westShape) {
        super(properties);
        this.ports = ports;
        Map<Direction, VoxelShape> rotated = Shapes.rotateHorizontal(westShape);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            shapes.put(direction, rotated.get(direction.getClockWise()));
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.WEST).setValue(LIT, false));
    }

    protected abstract Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide() || oldState.is(this)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (hasPort(state, direction)) {
                GraphenePipeBlock.attach(level, pos.relative(direction), direction.getOpposite());
            }
        }
    }

    @Override
    public boolean hasPort(BlockState state, Direction side) {
        return ports.contains(MachineSide.of(state.getValue(FACING), side));
    }

    @Override
    public FluidPort port(Level level, BlockPos pos, BlockState state, Direction side) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            return machine.port(MachineSide.of(state.getValue(FACING), side));
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider machine) {
            player.openMenu(machine);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != type().get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<MachineBlockEntity>) (tickLevel, pos, tickState, machine) ->
            machine.serverTick((ServerLevel) tickLevel, pos, tickState);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
