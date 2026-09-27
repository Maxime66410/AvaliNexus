/**
 * File: AvaliRestGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.goal;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import org.furranystudio.avalinexus.entity.AvaliEntity;
import org.furranystudio.avalinexus.entity.AvaliPose;

import java.util.EnumSet;

public class AvaliRestGoal extends Goal {

    private static final int REST_CHANCE = 1200;
    private static final int MIN_TICKS = 200;
    private static final int MAX_TICKS = 600;

    private final AvaliEntity avali;
    private int ticks;

    public AvaliRestGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return avali.getRandom().nextInt(REST_CHANCE) == 0 && canRest();
    }

    @Override
    public boolean canContinueToUse() {
        return ticks > 0 && canRest() && avali.hurtTime == 0;
    }

    @Override
    public void start() {
        ticks = Mth.nextInt(avali.getRandom(), MIN_TICKS, MAX_TICKS);
        AvaliPose[] poses = AvaliPose.values();
        avali.setRestPose(poses[avali.getRandom().nextInt(poses.length)]);
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        ticks--;
    }

    @Override
    public void stop() {
        avali.setRestPose(null);
    }

    private boolean canRest() {
        return avali.onGround() && !avali.isInWater() && !avali.isPassenger() && !avali.isSleeping()
            && !avali.getMood().earsDown();
    }
}
