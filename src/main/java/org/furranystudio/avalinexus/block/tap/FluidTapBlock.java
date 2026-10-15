/**
 * File: FluidTapBlock.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.block.pipe.PipeNetwork;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidPort;
import org.furranystudio.avalinexus.fluid.FluidPortBlock;

import java.util.EnumMap;
import java.util.Map;

public class FluidTapBlock extends Block implements FluidPortBlock {

    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Shapes.rotateHorizontal(Block.box(5, 5, 13.85, 11, 11, 16)));

    static {
        SHAPES.put(Direction.UP, Block.box(5, 0, 5, 11, 2.15, 11));
        SHAPES.put(Direction.DOWN, Block.box(5, 13.85, 5, 11, 16, 11));
    }

    public FluidTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide() || oldState.is(this)) {
            return;
        }
        Direction back = state.getValue(FACING).getOpposite();
        GraphenePipeBlock.attach(level, pos.relative(back), back.getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean hasPort(BlockState state, Direction side) {
        return side == state.getValue(FACING).getOpposite();
    }

    @Override
    public FluidPort port(Level level, BlockPos pos, BlockState state, Direction side) {
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.BUCKET)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Direction back = state.getValue(FACING).getOpposite();
        Fluid fluid = draw(level, pos.relative(back), back.getOpposite());
        if (fluid == Fluids.EMPTY) {
            player.sendOverlayMessage(Component.translatable("message.avalinexus.tap.empty"));
            return InteractionResult.FAIL;
        }
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, AvaliLiquids.filledBucket(fluid)));
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static Fluid draw(Level level, BlockPos source, Direction toTap) {
        BlockState state = level.getBlockState(source);
        if (state.getBlock() instanceof GraphenePipeBlock) {
            if (!state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(toTap))) {
                return Fluids.EMPTY;
            }
            PipeNetwork network = PipeNetwork.of(level, source);
            Fluid fluid = network.peek();
            if (fluid == Fluids.EMPTY || network.take(fluid, AvaliLiquids.BUCKET, true) < AvaliLiquids.BUCKET) {
                return Fluids.EMPTY;
            }
            network.take(fluid, AvaliLiquids.BUCKET, false);
            return fluid;
        }
        if (state.getBlock() instanceof FluidPortBlock block && block.hasPort(state, toTap)) {
            FluidPort port = block.port(level, source, state, toTap);
            if (port == null) {
                return Fluids.EMPTY;
            }
            Fluid fluid = port.fluid();
            if (fluid == Fluids.EMPTY || port.drain(fluid, AvaliLiquids.BUCKET, true) < AvaliLiquids.BUCKET) {
                return Fluids.EMPTY;
            }
            port.drain(fluid, AvaliLiquids.BUCKET, false);
            return fluid;
        }
        return Fluids.EMPTY;
    }
}
