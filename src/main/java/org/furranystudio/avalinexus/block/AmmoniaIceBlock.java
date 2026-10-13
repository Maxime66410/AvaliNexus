/**
 * File: AmmoniaIceBlock.java
 * Author: Maxime66410
 * Created: 2026-10-13
 * Last Modified: 2026-10-13
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.ModFluids;

public class AmmoniaIceBlock extends HalfTransparentBlock {

    public AmmoniaIceBlock(Properties properties) {
        super(properties);
    }

    public static BlockState meltsInto() {
        return ModFluids.block(AvaliFluidKind.AMMONIA).defaultBlockState();
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            return;
        }
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.is(BlockTags.ICE_MELTS_WHEN_DESTROYED_ABOVE) || below.liquid()) {
            level.setBlockAndUpdate(pos, meltsInto());
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightDampening()) {
            melt(state, level, pos);
        }
    }

    protected void melt(BlockState state, Level level, BlockPos pos) {
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        level.setBlockAndUpdate(pos, meltsInto());
        level.neighborChanged(pos, meltsInto().getBlock(), null);
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
    }
}
