/**
 * File: CompanionWaitGoal.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliCompanions;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class CompanionWaitGoal extends Goal {

    private static final double ARRIVED = 1.5;
    private static final double WALK_SPEED = 0.8;

    private final AvaliEntity avali;

    public CompanionWaitGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return avali.isCompanion() && avali.isWaiting() && !avali.isPassenger() && !avali.isSleeping()
            && distance() > AvaliCompanions.get(avali.level().getServer()).waitRadius();
    }

    @Override
    public boolean canContinueToUse() {
        return avali.isCompanion() && avali.isWaiting() && !avali.isSleeping() && distance() > ARRIVED;
    }

    @Override
    public void start() {
        Vec3 spot = Vec3.atBottomCenterOf(avali.getWaitPos());
        avali.getNavigation().moveTo(spot.x, spot.y, spot.z, WALK_SPEED);
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
    }

    private double distance() {
        return avali.position().distanceTo(Vec3.atBottomCenterOf(avali.getWaitPos()));
    }
}
