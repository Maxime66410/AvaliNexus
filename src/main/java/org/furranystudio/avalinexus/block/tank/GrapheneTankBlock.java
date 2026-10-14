/**
 * File: GrapheneTankBlock.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;

import java.util.Locale;

public class GrapheneTankBlock extends Block implements EntityBlock {

    public GrapheneTankBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty property : PipeBlock.PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.NORTH, PipeBlock.EAST, PipeBlock.SOUTH, PipeBlock.WEST, PipeBlock.UP, PipeBlock.DOWN);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GrapheneTankBlockEntity tank) {
            TankStructure.at(level, pos).release(tank);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TankForming.reform(level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (stack.is(Items.BUCKET)) {
            return fillBucket(stack, level, pos, player, hand);
        }
        if (stack.getItem() instanceof BucketItem) {
            return emptyBucket(stack, level, pos, player, hand);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult emptyBucket(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        Fluid fluid = AvaliLiquids.fromBucket(stack);
        if (fluid == Fluids.EMPTY) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        TankStructure structure = TankStructure.at(level, pos);
        if (structure.insert(fluid, AvaliLiquids.BUCKET, true) < AvaliLiquids.BUCKET) {
            player.sendOverlayMessage(Component.translatable(structure.fluid() != Fluids.EMPTY && !structure.fluid().isSame(fluid)
                ? "message.avalinexus.tank.other_fluid" : "message.avalinexus.tank.full"));
            return InteractionResult.FAIL;
        }
        structure.insert(fluid, AvaliLiquids.BUCKET, false);
        player.setItemInHand(hand, BucketItem.getEmptySuccessItem(stack, player));
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult fillBucket(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        TankStructure structure = TankStructure.at(level, pos);
        Fluid fluid = structure.fluid();
        if (structure.extract(AvaliLiquids.BUCKET, true) < AvaliLiquids.BUCKET) {
            player.sendOverlayMessage(Component.translatable("message.avalinexus.tank.not_enough"));
            return InteractionResult.FAIL;
        }
        structure.extract(AvaliLiquids.BUCKET, false);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, AvaliLiquids.filledBucket(fluid)));
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            TankStructure structure = TankStructure.at(level, pos);
            String capacity = buckets(structure.capacity());
            player.sendOverlayMessage(structure.fluid() == Fluids.EMPTY
                ? Component.translatable("message.avalinexus.tank.empty", capacity)
                : Component.translatable("message.avalinexus.tank.content", AvaliLiquids.name(structure.fluid()), buckets(structure.amount()), capacity));
        }
        return InteractionResult.SUCCESS;
    }

    public static String buckets(int amount) {
        String text = String.format(Locale.ROOT, "%.1f", amount / (float) AvaliLiquids.BUCKET);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        TankStructure structure = TankStructure.at(level, pos);
        if (structure.amount() <= 0) {
            return 0;
        }
        return 1 + (int) (14.0F * structure.amount() / structure.capacity());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrapheneTankBlockEntity(pos, state);
    }
}
