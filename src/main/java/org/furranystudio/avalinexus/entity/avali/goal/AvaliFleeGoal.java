/**
 * File: AvaliFleeGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliDangers;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;

import java.util.EnumSet;
import java.util.List;

public class AvaliFleeGoal extends Goal {

    private static final int CHECK_INTERVAL = 5;
    private static final int REPATH_INTERVAL = 10;
    private static final int RANDOM_TRIES = 5;
    private static final int MIN_FLEE_STEP = 4;
    private static final double PARENT_SEARCH = 32.0;
    private static final double PARENT_REACHED_SQ = 4.0;
    private static final float BACK_OFF = -1.0F;
    private static final int WALL_TICKS = 40;
    private static final double STEP_REACH = 0.3;
    private static final double BACK_JUMP_SPEED = 0.25;
    private static final double BACK_JUMP_LIFT = 0.45;
    private static final int MAX_DROP = 2;

    private final AvaliEntity avali;
    private List<LivingEntity> enemies = List.of();
    private LivingEntity danger;
    private AvaliEntity parent;
    private int checkCooldown;
    private int repath;
    private boolean backingOff;
    private int wallTicks;
    private boolean backJump;

    public AvaliFleeGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        List<LivingEntity> found = AvaliFight.enemies(avali);
        if (!AvaliFight.shouldFlee(avali, found)) {
            return false;
        }
        enemies = found;
        danger = AvaliFight.nearest(avali, found);
        return danger != null;
    }

    @Override
    public boolean canContinueToUse() {
        AvaliCombat.Data data = AvaliFight.data(avali);
        return !avali.isCornered() && danger != null && danger.isAlive() && avali.distanceTo(danger) < data.fleeDistance();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        repath = 0;
        backingOff = false;
        wallTicks = 0;
        parent = avali.isBaby() ? findParent() : null;
        avali.setTarget(null);
        avali.setSprinting(true);
        avali.setPanicking(true);
        if (avali.isSleeping()) {
            avali.stopSleeping();
        }
    }

    @Override
    public void stop() {
        lowerShield();
        avali.setSprinting(false);
        avali.setPanicking(false);
        avali.getNavigation().stop();
        enemies = List.of();
        danger = null;
        parent = null;
    }

    @Override
    public void tick() {
        AvaliCombat.Data data = AvaliFight.data(avali);
        if (--checkCooldown <= 0) {
            checkCooldown = CHECK_INTERVAL;
            List<LivingEntity> found = AvaliFight.enemies(avali);
            if (!found.isEmpty()) {
                enemies = found;
                danger = AvaliFight.nearest(avali, found);
            }
        }

        if (wallTicks > 0) {
            wallTicks--;
        }
        if (wallTicks <= 0 && avali.getGear().isShield(avali.getOffhandItem()) && avali.distanceTo(danger) <= data.shieldDistance()) {
            backOff();
            return;
        }
        if (backingOff) {
            backingOff = false;
            lowerShield();
            avali.setSprinting(true);
            repath = 0;
        }

        if (--repath > 0) {
            return;
        }
        repath = REPATH_INTERVAL;
        if (parent != null && parent.isAlive() && isSafe(parent, data)) {
            if (avali.distanceToSqr(parent) > PARENT_REACHED_SQ) {
                avali.getNavigation().moveTo(parent, data.fleeSpeed());
            }
            return;
        }
        Vec3 spot = findSpot(data.fleeDistance());
        if (spot != null && avali.getNavigation().moveTo(spot.x, spot.y, spot.z, data.fleeSpeed())) {
            return;
        }
        if (!avali.isBaby()) {
            avali.setCornered(data.corneredSeconds() * 20);
            avali.setTarget(danger);
        }
    }

    private void backOff() {
        if (!backingOff) {
            backingOff = true;
            avali.getNavigation().stop();
            avali.setSprinting(false);
        }
        avali.getLookControl().setLookAt(danger, 30.0F, 30.0F);
        avali.lookAt(danger, 30.0F, 30.0F);
        if (!avali.isUsingItem()) {
            avali.startUsingItem(InteractionHand.OFF_HAND);
        }
        if (!avali.onGround()) {
            Vec3 away = awayFromDanger();
            if (backJump && away != null) {
                avali.setDeltaMovement(away.x * BACK_JUMP_SPEED, avali.getDeltaMovement().y, away.z * BACK_JUMP_SPEED);
            }
            return;
        }
        backJump = false;
        Vec3 away = awayFromDanger();
        BlockPos behind = away == null ? null : behind(away);
        if (behind != null && !isFree(behind)) {
            if (canStepOn(behind)) {
                avali.setDeltaMovement(away.x * BACK_JUMP_SPEED, BACK_JUMP_LIFT, away.z * BACK_JUMP_SPEED);
                backJump = true;
                return;
            }
            wallTicks = WALL_TICKS;
        } else if (behind != null && isFree(behind.below())) {
            if (canDropTo(behind)) {
                Vec3 motion = avali.getDeltaMovement();
                avali.setDeltaMovement(away.x * BACK_JUMP_SPEED, motion.y, away.z * BACK_JUMP_SPEED);
                return;
            }
            wallTicks = WALL_TICKS;
        }
        avali.getMoveControl().strafe(BACK_OFF, 0.0F);
    }

    private Vec3 awayFromDanger() {
        Vec3 away = avali.position().subtract(danger.position()).multiply(1.0, 0.0, 1.0);
        return away.lengthSqr() < 1.0E-4 ? null : away.normalize();
    }

    private BlockPos behind(Vec3 away) {
        Vec3 point = avali.position().add(away.scale(avali.getBbWidth() / 2 + STEP_REACH));
        BlockPos pos = BlockPos.containing(point.x, avali.getY() + 0.5, point.z);
        return pos.equals(avali.blockPosition()) ? null : pos;
    }

    private boolean canStepOn(BlockPos step) {
        VoxelShape shape = avali.level().getBlockState(step).getCollisionShape(avali.level(), step);
        return shape.max(Direction.Axis.Y) <= 1.0 && isFree(step.above()) && isFree(step.above(2))
            && isFree(avali.blockPosition().above(2));
    }

    private boolean canDropTo(BlockPos edge) {
        AvaliDangers.Data dangers = AvaliDangers.get(avali.level().getServer());
        for (int depth = 0; depth <= MAX_DROP + 1; depth++) {
            BlockPos pos = edge.below(depth);
            if (!avali.level().getFluidState(pos).isEmpty()) {
                return false;
            }
            if (!isFree(pos)) {
                return depth >= 2 && !dangers.isHot(avali.level().getBlockState(pos));
            }
        }
        return false;
    }

    private boolean isFree(BlockPos pos) {
        return avali.level().getBlockState(pos).getCollisionShape(avali.level(), pos).isEmpty();
    }

    private Vec3 findSpot(int distance) {
        Vec3 away = Vec3.ZERO;
        for (LivingEntity enemy : enemies) {
            Vec3 offset = avali.position().subtract(enemy.position());
            double length = Math.max(offset.horizontalDistance(), 1.0);
            away = away.add(offset.x / (length * length), 0.0, offset.z / (length * length));
        }
        if (away.lengthSqr() > 1.0E-4) {
            Vec3 avoid = avali.position().subtract(away.normalize());
            for (int step = distance; step >= MIN_FLEE_STEP; step /= 2) {
                Vec3 spot = DefaultRandomPos.getPosAway(avali, step, 7, avoid);
                if (spot != null) {
                    return spot;
                }
            }
        }
        double current = avali.distanceToSqr(danger);
        for (int i = 0; i < RANDOM_TRIES; i++) {
            Vec3 spot = DefaultRandomPos.getPos(avali, distance, 7);
            if (spot != null && danger.distanceToSqr(spot) > current) {
                return spot;
            }
        }
        return null;
    }

    private boolean isSafe(AvaliEntity parent, AvaliCombat.Data data) {
        double parentDanger = parent.distanceTo(danger);
        return parentDanger > avali.distanceTo(danger) && parentDanger > data.parentSafeDistance();
    }

    private void lowerShield() {
        if (avali.isUsingItem() && avali.getUsedItemHand() == InteractionHand.OFF_HAND) {
            avali.stopUsingItem();
        }
    }

    private AvaliEntity findParent() {
        return avali.level().getEntitiesOfClass(AvaliEntity.class, avali.getBoundingBox().inflate(PARENT_SEARCH),
                other -> other.isAlive() && !other.isCompanion() && avali.getParents().contains(other.getUUID()))
            .stream().min((a, b) -> Double.compare(avali.distanceToSqr(a), avali.distanceToSqr(b))).orElse(null);
    }
}
