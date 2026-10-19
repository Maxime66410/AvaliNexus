/**
 * File: AvaliShelterGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliDangers;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliShelterGoal extends Goal {

    private static final int CHECK_INTERVAL = 40;
    private static final int SEARCH_HEIGHT = 4;
    private static final int MAX_WALK_TICKS = 600;
    private static final double SPEED = 1.0;

    private final AvaliEntity avali;
    private BlockPos shelter;
    private int checkCooldown;
    private int walkTicks;

    public AvaliShelterGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger() || avali.isSleeping() || avali.isFollowingOwner()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        AvaliDangers.Data dangers = AvaliDangers.get(avali.level().getServer());
        if (!dangers.rainShelter() || !avali.level().isRainingAt(avali.blockPosition())) {
            return false;
        }
        shelter = findShelter(dangers.shelterSearchRadius());
        return shelter != null;
    }

    @Override
    public boolean canContinueToUse() {
        return shelter != null && walkTicks < MAX_WALK_TICKS && !avali.getNavigation().isDone();
    }

    @Override
    public void start() {
        walkTicks = 0;
        avali.walkTowards(Vec3.atBottomCenterOf(shelter), SPEED);
    }

    @Override
    public void tick() {
        walkTicks++;
    }

    @Override
    public void stop() {
        shelter = null;
    }

    private BlockPos findShelter(int radius) {
        Level level = avali.level();
        BlockPos center = avali.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -SEARCH_HEIGHT, -radius), center.offset(radius, SEARCH_HEIGHT, radius))) {
            double distance = pos.distSqr(center);
            if (distance < closestDistance && avali.isShelter(pos)
                    && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                    && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                    && level.getFluidState(pos).isEmpty()) {
                closest = pos.immutable();
                closestDistance = distance;
            }
        }
        return closest;
    }
}
