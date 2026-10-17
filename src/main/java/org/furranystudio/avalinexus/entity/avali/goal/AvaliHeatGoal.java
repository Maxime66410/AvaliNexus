/**
 * File: AvaliHeatGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliDangers;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliHeatGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;
    private static final int FLEE_HEIGHT = 4;
    private static final double SPEED = 1.0;

    private final AvaliEntity avali;
    private Vec3 away;
    private int checkCooldown;

    public AvaliHeatGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        AvaliDangers.Data dangers = AvaliDangers.get(avali.level().getServer());
        BlockPos heat = findHeat(dangers);
        if (heat == null) {
            return false;
        }
        away = DefaultRandomPos.getPosAway(avali, dangers.heatFleeDistance(), FLEE_HEIGHT, Vec3.atCenterOf(heat));
        return away != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !avali.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (avali.isSleeping()) {
            avali.stopSleeping();
        }
        avali.setSprinting(true);
        avali.getNavigation().moveTo(away.x, away.y, away.z, SPEED);
    }

    @Override
    public void stop() {
        avali.setSprinting(false);
        away = null;
    }

    private BlockPos findHeat(AvaliDangers.Data dangers) {
        int radius = dangers.heatDetectRadius();
        BlockPos center = avali.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            double distance = pos.distSqr(center);
            if (distance < closestDistance && dangers.isHot(avali.level().getBlockState(pos))) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }
        return closest;
    }
}
