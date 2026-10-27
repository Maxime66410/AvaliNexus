/**
 * File: AvaliLiquidBlock.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

// The placed fluid: freezes players, heals Avalis and reacts with the lava it touches
public class AvaliLiquidBlock extends LiquidBlock {

    private static final int HEAL_INTERVAL = 40;
    private static final int CAP_MARGIN = 2;

    private final AvaliFluidKind kind;

    public AvaliLiquidBlock(FlowingFluid fluid, AvaliFluidKind kind, Properties properties) {
        super(fluid, properties);
        this.kind = kind;
    }

    public AvaliFluidKind kind() {
        return kind;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        super.entityInside(state, level, pos, entity, effects, intersects);
        if (level.isClientSide()) {
            return;
        }
        if (kind.healsAvali() && entity instanceof AvaliEntity avali && avali.tickCount % HEAL_INTERVAL == 0) {
            avali.heal(1.0F);
        }
        // Warm armor or an Avali body keeps the cold out
        int rate = kind.freezing() * Config.LIQUID_COLD_SPEED.get();
        if (rate > 0 && entity.canFreeze() && !(entity instanceof AvaliEntity)) {
            long time = level.getGameTime();
            int cap = entity.getTicksRequiredToFreeze() + CAP_MARGIN;
            entity.setTicksFrozen(Math.min(cap, entity.getTicksFrozen() + 2 + (int) ((time + 1) * rate / 100 - time * rate / 100)));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        reactWithLava(level, pos);
        if (freezeOnCoolant(state, level, pos)) {
            return;
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, Orientation orientation, boolean movedByPiston) {
        reactWithLava(level, pos);
        if (freezeOnCoolant(state, level, pos)) {
            return;
        }
        super.neighborChanged(state, level, pos, neighbor, orientation, movedByPiston);
    }

    private boolean freezeOnCoolant(BlockState state, Level level, BlockPos pos) {
        if (kind != AvaliFluidKind.AMMONIA || state.getValue(LEVEL) != 0 || level.isClientSide()) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).getType().isSame(ModFluids.source(AvaliFluidKind.COOLANT))) {
                level.setBlockAndUpdate(pos, ModBlocks.AMMONIA_ICE.get().defaultBlockState());
                AvaliNexus.debug("Ammonia at {} freezes into ammonia ice next to coolant", pos.toShortString());
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    // Ammonia and coolant freeze the lava they touch
    private void reactWithLava(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos lavaPos = pos.relative(direction);
            if (!level.getFluidState(lavaPos).is(FluidTags.LAVA)) {
                continue;
            }
            Block result = kind.lavaResult();
            if (result != null) {
                level.setBlockAndUpdate(lavaPos, result.defaultBlockState());
                server.playSound(null, lavaPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                server.levelEvent(1501, lavaPos, 0);
                AvaliNexus.debug("{} at {} turns the lava at {} into {}", kind.name().toLowerCase(), pos.toShortString(), lavaPos.toShortString(),
                    BuiltInRegistries.BLOCK.getKey(result));
            } else {
                BlockPos firePos = pos.above();
                if (level.getBlockState(firePos).isAir()) {
                    level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
                    AvaliNexus.debug("{} at {} catches fire next to lava", kind.name().toLowerCase(), pos.toShortString());
                }
            }
        }
    }
}
