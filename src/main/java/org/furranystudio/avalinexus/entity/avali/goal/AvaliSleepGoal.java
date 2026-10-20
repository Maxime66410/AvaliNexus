/**
 * File: AvaliSleepGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AvaliSleepGoal extends Goal {

    private static final int SEARCH_COOLDOWN = 100;
    private static final double WAIT_BED_RANGE = 8.0;
    private static final int BED_HEIGHT = 4;
    private static final double NEIGHBOR_BED_SQ = 9.0;
    private static final double REACH_DISTANCE_SQ = 4.0;
    private static final int MAX_WALK_TICKS = 1200;
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
        if (avali.isPassenger() || avali.isInWater() || avali.getMood().earsDown() || avali.isFollowingOwner() || --searchCooldown > 0) {
            return false;
        }
        searchCooldown = SEARCH_COOLDOWN;
        boolean night = avali.level().isDarkOutside();
        if (!night && avali.getRandom().nextInt(NAP_CHANCE / SEARCH_COOLDOWN) != 0) {
            return false;
        }
        bed = ownBed();
        BlockPos partnerBed = partnerBed();
        if (partnerBed != null && !avali.isFemale() && (bed == null || bed.distSqr(partnerBed) > NEIGHBOR_BED_SQ)) {
            BlockPos closer = findFreeBed(partnerBed);
            if (closer != null && (bed == null || closer.distSqr(partnerBed) < bed.distSqr(partnerBed))) {
                bed = closer;
            }
        }
        if (bed == null) {
            bed = findFreeBed(partnerBed != null ? partnerBed : avali.blockPosition());
        }
        if (bed == null || avali.isCompanion() && !bed.closerThan(avali.getWaitPos(), WAIT_BED_RANGE)) {
            return false;
        }
        napTicks = night ? 0 : Mth.nextInt(avali.getRandom(), MIN_NAP_TICKS, MAX_NAP_TICKS);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (bed == null || !avali.isBedHead(bed) || avali.getMood().earsDown()) {
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
            avali.setBed(bed);
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
        avali.walkTowards(Vec3.atBottomCenterOf(bed), speed);
    }

    private BlockPos ownBed() {
        BlockPos own = avali.getBed();
        return own != null && avali.level().isLoaded(own) && avali.isBedHead(own) && isFree(own) ? own : null;
    }

    private BlockPos partnerBed() {
        AvaliEntity partner = avali.getPartner();
        return partner == null || partner.isCompanion() ? null : partner.getBed();
    }

    private BlockPos findFreeBed(BlockPos near) {
        AvaliBehavior.Life life = AvaliBehavior.get(avali.level().getServer()).life();
        int range = life.bedSearchRadius();
        Set<BlockPos> claimed = avali.level().getEntitiesOfClass(AvaliEntity.class,
                avali.getBoundingBox().inflate(life.bedMaxPackDistance() + range), other -> other != avali && other.getBed() != null)
            .stream().map(AvaliEntity::getBed).collect(Collectors.toSet());
        BlockPos center = avali.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-range, -BED_HEIGHT, -range),
                center.offset(range, BED_HEIGHT, range))) {
            double distance = pos.distSqr(near);
            if (distance < closestDistance && avali.isBedHead(pos) && isFree(pos) && !claimed.contains(pos)) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private boolean isFree(BlockPos pos) {
        BlockState state = avali.level().getBlockState(pos);
        return state.hasProperty(BlockStateProperties.OCCUPIED) && !state.getValue(BlockStateProperties.OCCUPIED);
    }
}
