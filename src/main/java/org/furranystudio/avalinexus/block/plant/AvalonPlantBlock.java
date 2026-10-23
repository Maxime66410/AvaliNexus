/**
 * File: AvalonPlantBlock.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.reputation.VillageWatch;

import java.util.function.Supplier;

public class AvalonPlantBlock extends VegetationBlock implements BonemealableBlock {

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final TagKey<Block> SOIL = TagKey.create(Registries.BLOCK, AvaliNexus.id("avalon_plant_soil"));

    private static final int GROWTH_CHANCE = 16;
    private static final int REGROW_AGE = 1;
    private static final VoxelShape SHAPE_YOUNG = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape SHAPE_GROWN = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    private final Supplier<Item> seed;
    private final ResourceKey<LootTable> harvest;

    public AvalonPlantBlock(Properties properties, Supplier<Item> seed, ResourceKey<LootTable> harvest) {
        super(properties);
        this.seed = seed;
        this.harvest = harvest;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(SOIL);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(seed.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) == 0 ? SHAPE_YOUNG : SHAPE_GROWN;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) == 0) {
            grow(level, pos, state);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (harvest == null || state.getValue(AGE) < MAX_AGE) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        if (level instanceof ServerLevel serverLevel) {
            if (player instanceof ServerPlayer serverPlayer && VillageWatch.isFieldCrop(serverLevel, pos)) {
                VillageWatch.tookCrop(serverPlayer, pos);
            }
            Block.dropFromBlockInteractLootTable(serverLevel, harvest, pos, state, null, player.getMainHandItem(), player,
                (dropLevel, stack) -> Block.popResource(dropLevel, pos, stack));
            serverLevel.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                0.8F + serverLevel.getRandom().nextFloat() * 0.4F);
            BlockState picked = harvested(state);
            serverLevel.setBlock(pos, picked, Block.UPDATE_CLIENTS);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player) {
            VillageWatch.planted(player, pos);
        }
    }

    public BlockState harvested(BlockState state) {
        return state.setValue(AGE, harvest == null ? 0 : REGROW_AGE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        grow(level, pos, state);
    }

    private static void grow(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState grown = state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1));
        level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
    }
}
