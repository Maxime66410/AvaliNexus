/**
 * File: AvaliSpearChargeGoal.java
 * Author: Maxime66410
 * Created: 2026-11-18
 * Last Modified: 2026-11-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;

import java.util.EnumSet;

public class AvaliSpearChargeGoal extends Goal {

    private static final double CHARGE_DISTANCE_SQ = 6.0 * 6.0;
    private static final double APPROACH_DISTANCE_SQ = 10.0 * 10.0;
    private static final double HIT_RADIUS_SQ = 2.0 * 2.0;
    private static final int COOLDOWN_TICKS = 200;
    private static final int MAX_FLEEING_TICKS = 100;
    private static final int MIN_REPOSITION_DISTANCE = 6;
    private static final int MAX_REPOSITION_DISTANCE = 7;
    private static final int MIN_COOLDOWN_DISTANCE = 9;
    private static final int MAX_COOLDOWN_DISTANCE = 11;
    private static final int REPOSITION_HEIGHT = 7;

    private final AvaliEntity avali;
    private long nextCharge;
    private int engageTime;
    private int fleeingTime;
    private Vec3 awayPos;
    private boolean done;

    public AvaliSpearChargeGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean wantsCharge(LivingEntity target) {
        return target != null && target.isAlive() && !avali.isBaby() && !avali.isUsingItem()
            && avali.level().getGameTime() >= nextCharge
            && avali.getMainHandItem().has(DataComponents.KINETIC_WEAPON)
            && avali.distanceToSqr(target) > CHARGE_DISTANCE_SQ
            && avali.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canUse() {
        return wantsCharge(avali.getTarget());
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = avali.getTarget();
        return !done && target != null && target.isAlive() && avali.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        engageTime = -1;
        fleeingTime = -1;
        awayPos = null;
        done = false;
        avali.setAggressive(true);
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
        avali.stopUsingItem();
        avali.setAggressive(false);
        nextCharge = avali.level().getGameTime() + COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        LivingEntity target = avali.getTarget();
        if (target == null) {
            return;
        }
        double distanceSq = avali.distanceToSqr(target);
        Entity mount = avali.getRootVehicle();
        double speed = AvaliFight.data(avali).chaseSpeed() * (mount instanceof Mob vehicle ? vehicle.chargeSpeedModifier() : 1.0F);
        int mountDistance = avali.isPassenger() ? 2 : 0;
        avali.lookAt(target, 30.0F, 30.0F);
        avali.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (engageTime < 0) {
            if (distanceSq > APPROACH_DISTANCE_SQ) {
                avali.getNavigation().moveTo(target, speed);
                return;
            }
            KineticWeapon kinetic = avali.getMainHandItem().get(DataComponents.KINETIC_WEAPON);
            engageTime = kinetic == null ? 0 : kinetic.computeDamageUseDuration();
            avali.startUsingItem(InteractionHand.MAIN_HAND);
        }
        if (engageTime > 0 && --engageTime == 0) {
            avali.stopUsingItem();
            double distance = Math.sqrt(distanceSq);
            awayPos = LandRandomPos.getPosAway(avali, Math.max(0.0, MIN_COOLDOWN_DISTANCE + mountDistance - distance),
                Math.max(1.0, MAX_COOLDOWN_DISTANCE + mountDistance - distance), REPOSITION_HEIGHT, target.position());
            fleeingTime = 1;
        }
        if (fleeingTime > 0 && ++fleeingTime > MAX_FLEEING_TICKS) {
            done = true;
            return;
        }
        if (awayPos != null) {
            avali.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, speed);
            if (avali.getNavigation().isDone()) {
                if (fleeingTime > 0) {
                    done = true;
                    return;
                }
                awayPos = null;
            }
        } else {
            avali.getNavigation().moveTo(target, speed);
            if (distanceSq < HIT_RADIUS_SQ || avali.getNavigation().isDone()) {
                double distance = Math.sqrt(distanceSq);
                awayPos = LandRandomPos.getPosAway(avali, MIN_REPOSITION_DISTANCE + mountDistance - distance,
                    MAX_REPOSITION_DISTANCE + mountDistance - distance, REPOSITION_HEIGHT, target.position());
            }
        }
    }
}
