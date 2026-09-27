/**
 * File: AvaliSleepGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliSleepGoal extends Goal {

    private static final int SEARCH_COOLDOWN = 100;
    private static final int BED_RANGE = 16;
    private static final int BED_HEIGHT = 4;
    private static final double REACH_DISTANCE_SQ = 4.0;
    private static final int MAX_WALK_TICKS = 400;
    private static final int NAP_CHANCE = 12000;
    private static final int MIN_NAP_TICKS = 600;
    private static final int MAX_NAP_TICKS = 1200;

    private final AvaliEntity avali;
    private final double speed;
    private BlockPos bed;
    private int searchCooldown;
    private int walkTicks;
    private int napTicks;

    public AvaliSleepGoal(AvaliEntity avali, double speed) {
        this.avali = avali;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (avali.isPassenger() || avali.isInWater() || avali.getMood().earsDown() || --searchCooldown > 0) {
            return false;
        }
        searchCooldown = SEARCH_COOLDOWN;
        boolean night = avali.level().isDarkOutside();
        if (!night && avali.getRandom().nextInt(NAP_CHANCE / SEARCH_COOLDOWN) != 0) {
            return false;
        }
        bed = findFreeBed();
        if (bed == null) {
            return false;
        }
        napTicks = night ? 0 : Mth.nextInt(avali.getRandom(), MIN_NAP_TICKS, MAX_NAP_TICKS);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (bed == null || !isBed(bed) || avali.getMood().earsDown()) {
            return false;
        }
        boolean wantsSleep = napTicks > 0 || avali.level().isDarkOutside();
        if (avali.isSleeping()) {
            return wantsSleep;
        }
        return wantsSleep && walkTicks < MAX_WALK_TICKS && isFree(bed);
    }

    @Override
    public void start() {
        walkTicks = 0;
        moveToBed();
    }

    @Override
    public void tick() {
        if (avali.isSleeping()) {
            if (napTicks > 0) {
                napTicks--;
            }
            return;
        }
        walkTicks++;
        if (avali.distanceToSqr(Vec3.atCenterOf(bed)) < REACH_DISTANCE_SQ) {
            avali.getNavigation().stop();
            avali.setNapping(avali.getRandom().nextBoolean());
            avali.startSleeping(bed);
        } else if (avali.getNavigation().isDone()) {
            moveToBed();
        }
    }

    @Override
    public void stop() {
        if (avali.isSleeping()) {
            avali.stopSleeping();
        }
        avali.getNavigation().stop();
        bed = null;
        napTicks = 0;
    }

    private void moveToBed() {
        avali.getNavigation().moveTo(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5, speed);
    }

    private BlockPos findFreeBed() {
        BlockPos center = avali.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-BED_RANGE, -BED_HEIGHT, -BED_RANGE),
                center.offset(BED_RANGE, BED_HEIGHT, BED_RANGE))) {
            double distance = pos.distSqr(center);
            if (distance < closestDistance && isBed(pos) && isFree(pos)) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private boolean isBed(BlockPos pos) {
        BlockState state = avali.level().getBlockState(pos);
        return state.hasProperty(BlockStateProperties.BED_PART)
            && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD;
    }

    private boolean isFree(BlockPos pos) {
        BlockState state = avali.level().getBlockState(pos);
        return state.hasProperty(BlockStateProperties.OCCUPIED) && !state.getValue(BlockStateProperties.OCCUPIED);
    }
}
