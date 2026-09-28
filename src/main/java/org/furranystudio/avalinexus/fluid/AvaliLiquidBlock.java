/**
 * File: AvaliLiquidBlock.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

// The placed fluid: freezes players, heals Avalis and reacts with the lava it touches
public class AvaliLiquidBlock extends LiquidBlock {

    private static final int HEAL_INTERVAL = 40;
    // A little over the cap so the vanilla thaw still leaves the player fully frozen
    private static final int CAP_MARGIN = 2;

    private final AvaliFluidKind kind;

    public AvaliLiquidBlock(FlowingFluid fluid, AvaliFluidKind kind, Properties properties) {
        super(fluid, properties);
        this.kind = kind;
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
        // Warm armor or an Avali body keeps the cold out, like powder snow
        if (kind.freezing() > 0 && entity.canFreeze() && !(entity instanceof AvaliEntity)) {
            int cap = entity.getTicksRequiredToFreeze() + CAP_MARGIN;
            entity.setTicksFrozen(Math.min(cap, entity.getTicksFrozen() + 2 + kind.freezing()));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        reactWithLava(level, pos);
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, Orientation orientation, boolean movedByPiston) {
        reactWithLava(level, pos);
        super.neighborChanged(state, level, pos, neighbor, orientation, movedByPiston);
    }

    // Ammonia and coolant freeze the lava they touch, fuel catches fire next to it
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
            } else {
                BlockPos firePos = pos.above();
                if (level.getBlockState(firePos).isAir()) {
                    level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
                }
            }
        }
    }
}
