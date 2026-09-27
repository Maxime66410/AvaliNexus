/**
 * File: AvaliSwimGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.AvaliEntity;

import java.util.EnumSet;

public class AvaliSwimGoal extends Goal {

    private static final int SHORE_RANGE = 16;
    private static final int SHORE_HEIGHT = 4;
    private static final int REPATH_TICKS = 40;

    private final AvaliEntity avali;
    private final double speed;
    private int repathCooldown;

    public AvaliSwimGoal(AvaliEntity avali, double speed) {
        this.avali = avali;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return avali.isInDeepWater();
    }

    @Override
    public boolean canContinueToUse() {
        return avali.isInWater();
    }

    @Override
    public void start() {
        avali.setSprinting(true);
        headToShore();
    }

    @Override
    public void tick() {
        avali.setSprinting(avali.isInDeepWater());
        if (--repathCooldown <= 0 && avali.getNavigation().isDone()) {
            headToShore();
        }
    }

    @Override
    public void stop() {
        avali.setSprinting(false);
        avali.getNavigation().stop();
    }

    private void headToShore() {
        repathCooldown = REPATH_TICKS;
        BlockPos shore = findClosestShore();
        if (shore != null) {
            avali.getNavigation().moveTo(shore.getX() + 0.5, shore.getY(), shore.getZ() + 0.5, speed);
            return;
        }
        Vec3 land = LandRandomPos.getPos(avali, SHORE_RANGE, SHORE_HEIGHT);
        if (land != null) {
            avali.getNavigation().moveTo(land.x, land.y, land.z, speed);
        }
    }

    private BlockPos findClosestShore() {
        BlockPos center = avali.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SHORE_RANGE, -SHORE_HEIGHT, -SHORE_RANGE),
                center.offset(SHORE_RANGE, SHORE_HEIGHT, SHORE_RANGE))) {
            double distance = pos.distSqr(center);
            if (distance < closestDistance && isDryGround(pos)) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private boolean isDryGround(BlockPos pos) {
        Level level = avali.level();
        BlockPos below = pos.below();
        BlockPos above = pos.above();
        return level.getFluidState(pos).isEmpty()
            && level.getFluidState(below).isEmpty()
            && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
            && level.getBlockState(above).getCollisionShape(level, above).isEmpty()
            && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
