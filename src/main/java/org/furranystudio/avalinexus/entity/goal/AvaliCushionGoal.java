/**
 * File: AvaliCushionGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.goal;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.Cushion;
import org.furranystudio.avalinexus.entity.AvaliEntity;

import java.util.Comparator;
import java.util.EnumSet;

public class AvaliCushionGoal extends Goal {

    private static final int SEARCH_CHANCE = 400;
    private static final double SEARCH_RANGE = 12.0;
    private static final double SIT_DISTANCE_SQ = 2.0;
    private static final int MAX_WALK_TICKS = 300;
    private static final int MIN_SIT_TICKS = 400;
    private static final int MAX_SIT_TICKS = 1200;

    private final AvaliEntity avali;
    private final double speed;
    private Cushion cushion;
    private int walkTicks;
    private int sitTicks;

    public AvaliCushionGoal(AvaliEntity avali, double speed) {
        this.avali = avali;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (avali.getRandom().nextInt(SEARCH_CHANCE) != 0 || avali.isPassenger() || avali.isSleeping()
                || avali.isInWater() || avali.getMood().earsDown()) {
            return false;
        }
        cushion = avali.level().getEntitiesOfClass(Cushion.class, avali.getBoundingBox().inflate(SEARCH_RANGE),
                candidate -> !candidate.isVehicle())
            .stream()
            .min(Comparator.comparingDouble(avali::distanceToSqr))
            .orElse(null);
        return cushion != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (cushion == null || !cushion.isAlive() || avali.getMood().earsDown()) {
            return false;
        }
        if (avali.getVehicle() == cushion) {
            return sitTicks > 0;
        }
        return walkTicks < MAX_WALK_TICKS && !cushion.isVehicle();
    }

    @Override
    public void start() {
        walkTicks = 0;
        sitTicks = Mth.nextInt(avali.getRandom(), MIN_SIT_TICKS, MAX_SIT_TICKS);
        avali.getNavigation().moveTo(cushion, speed);
    }

    @Override
    public void tick() {
        if (avali.getVehicle() == cushion) {
            sitTicks--;
            return;
        }
        walkTicks++;
        if (avali.distanceToSqr(cushion) < SIT_DISTANCE_SQ) {
            avali.getNavigation().stop();
            avali.startRiding(cushion);
        } else if (avali.getNavigation().isDone()) {
            avali.getNavigation().moveTo(cushion, speed);
        }
    }

    @Override
    public void stop() {
        if (cushion != null && avali.getVehicle() == cushion) {
            avali.stopRiding();
        }
        cushion = null;
    }
}
