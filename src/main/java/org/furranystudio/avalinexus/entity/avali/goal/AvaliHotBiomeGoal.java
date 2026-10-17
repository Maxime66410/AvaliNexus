/**
 * File: AvaliHotBiomeGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliHotBiomeGoal extends Goal {

    private static final int CHECK_INTERVAL = 40;
    private static final int REPATH_INTERVAL = 40;
    private static final int MAX_WALK_TICKS = 1200;
    private static final double ARRIVAL = 2.0;
    private static final double SPEED = 1.0;

    private final AvaliEntity avali;
    private int checkCooldown;
    private int walkTicks;

    public AvaliHotBiomeGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        return avali.isHotBiome(avali.blockPosition()) && avali.position().distanceTo(avali.getPackCenter()) > ARRIVAL;
    }

    @Override
    public boolean canContinueToUse() {
        return walkTicks < MAX_WALK_TICKS && avali.isHotBiome(avali.blockPosition())
            && avali.position().distanceTo(avali.getPackCenter()) > ARRIVAL;
    }

    @Override
    public void start() {
        walkTicks = 0;
        if (avali.isSleeping()) {
            avali.stopSleeping();
        }
        avali.walkTowards(avali.getPackCenter(), SPEED);
    }

    @Override
    public void tick() {
        if (++walkTicks % REPATH_INTERVAL == 0 || avali.getNavigation().isDone()) {
            avali.walkTowards(avali.getPackCenter(), SPEED);
        }
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
    }
}
