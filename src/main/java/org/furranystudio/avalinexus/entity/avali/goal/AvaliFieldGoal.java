/**
 * File: AvaliFieldGoal.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.furranystudio.avalinexus.block.plant.AvalonPlantBlock;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.reputation.VillageData;
import org.furranystudio.avalinexus.reputation.VillageFields;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;

public class AvaliFieldGoal extends Goal {

    private static final int REPATH_INTERVAL = 20;
    private static final int MAX_WALK_TICKS = 400;
    private static final double REACH_SQ = 6.25;
    private static final double SPEED = 0.8;

    private final AvaliEntity avali;
    private BlockPos target;
    private Block plant;
    private int checkCooldown;
    private int walkTicks;

    public AvaliFieldGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || !canWork()) {
            return false;
        }
        checkCooldown = AvaliBehavior.get(avali.level().getServer()).village().fieldCheckSeconds() * 20;
        return findWork();
    }

    private boolean findWork() {
        AvaliBehavior.Village config = AvaliBehavior.get(avali.level().getServer()).village();
        ServerLevel level = (ServerLevel) avali.level();
        VillageData data = VillageData.get(level.getServer());
        double radiusSq = config.fieldRadius() * config.fieldRadius();
        Map<BlockPos, Block> spots = VillageFields.spots(level, avali.getCamp());
        Map.Entry<BlockPos, Block> work = spots.entrySet().stream()
            .filter(spot -> spot.getKey().distSqr(avali.blockPosition()) <= radiusSq && !data.isPlanted(spot.getKey()) && isRipe(level, spot.getKey()))
            .min(Comparator.comparingDouble(spot -> spot.getKey().distSqr(avali.blockPosition())))
            .orElseGet(() -> spots.entrySet().stream()
                .filter(spot -> spot.getKey().distSqr(avali.blockPosition()) <= radiusSq && canReplant(level, spot.getKey(), spot.getValue()))
                .min(Comparator.comparingDouble(spot -> spot.getKey().distSqr(avali.blockPosition())))
                .orElse(null));
        if (work == null) {
            target = null;
            return false;
        }
        target = work.getKey();
        plant = work.getValue();
        walkTicks = 0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && walkTicks <= MAX_WALK_TICKS && !avali.isCompanion() && !avali.isSleeping() && !avali.isBusy()
            && !avali.level().isDarkOutside();
    }

    @Override
    public void start() {
        walkTicks = 0;
        avali.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
    }

    @Override
    public void tick() {
        walkTicks++;
        avali.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        if (avali.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) < REACH_SQ) {
            work((ServerLevel) avali.level());
            if (findWork()) {
                avali.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
            }
        } else if (walkTicks % REPATH_INTERVAL == 0) {
            avali.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
        }
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
        target = null;
        plant = null;
    }

    private boolean canWork() {
        return !avali.isBaby() && !avali.isCompanion() && avali.isVillager() && avali.getCamp() != null
            && avali.level() instanceof ServerLevel level && avali.getProfession().equals(VillageFields.job(level, avali.getCamp()))
            && !level.isDarkOutside() && !avali.isSleeping() && !avali.isPassenger() && !avali.isBusy();
    }

    private void work(ServerLevel level) {
        if (VillageData.get(level.getServer()).isPlanted(target)) {
            return;
        }
        BlockState state = level.getBlockState(target);
        BlockState result;
        if (isRipe(level, target)) {
            result = ((AvalonPlantBlock) state.getBlock()).harvested(state);
            level.playSound(null, target, result.getValue(AvalonPlantBlock.AGE) > 0 ? SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES
                : SoundEvents.CROP_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else if (canReplant(level, target, plant)) {
            result = plant.defaultBlockState();
            level.playSound(null, target, SoundEvents.CROP_PLANTED, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            return;
        }
        avali.swingForAttack(InteractionHand.MAIN_HAND);
        level.setBlock(target, result, Block.UPDATE_ALL);
        level.gameEvent(GameEvent.BLOCK_CHANGE, target, GameEvent.Context.of(avali, result));
    }

    private static boolean isRipe(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof AvalonPlantBlock && state.getValue(AvalonPlantBlock.AGE) == AvalonPlantBlock.MAX_AGE;
    }

    private static boolean canReplant(ServerLevel level, BlockPos pos, Block plant) {
        return level.getBlockState(pos).isAir() && plant.defaultBlockState().canSurvive(level, pos);
    }
}
