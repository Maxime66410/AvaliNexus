/**
 * File: AvaliFleeGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
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

    private final AvaliEntity avali;
    private List<LivingEntity> enemies = List.of();
    private LivingEntity danger;
    private AvaliEntity parent;
    private int checkCooldown;
    private int repath;
    private boolean backingOff;

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

        if (avali.getGear().isShield(avali.getOffhandItem()) && avali.distanceTo(danger) <= data.shieldDistance()) {
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
        avali.getMoveControl().strafe(BACK_OFF, 0.0F);
        if (!avali.isUsingItem()) {
            avali.startUsingItem(InteractionHand.OFF_HAND);
        }
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
                other -> other.isAlive() && avali.getParents().contains(other.getUUID()))
            .stream().min((a, b) -> Double.compare(avali.distanceToSqr(a), avali.distanceToSqr(b))).orElse(null);
    }
}
