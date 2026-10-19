/**
 * File: CompanionFollowGoal.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import org.furranystudio.avalinexus.entity.avali.AvaliCompanions;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class CompanionFollowGoal extends Goal {

    private static final int REPATH_INTERVAL = 10;

    private final AvaliEntity avali;
    private ServerPlayer owner;
    private int repath;

    public CompanionFollowGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!avali.isFollowingOwner() || avali.isPassenger()) {
            return false;
        }
        owner = avali.getOwner();
        return owner != null && owner.level() == avali.level()
            && avali.distanceTo(owner) > AvaliCompanions.get(avali.level().getServer()).followStartDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return avali.isFollowingOwner() && owner != null && owner.isAlive() && owner.level() == avali.level()
            && avali.distanceTo(owner) > AvaliCompanions.get(avali.level().getServer()).followStopDistance();
    }

    @Override
    public void start() {
        repath = 0;
    }

    @Override
    public void stop() {
        owner = null;
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        avali.getLookControl().setLookAt(owner, 10.0F, avali.getMaxHeadXRot());
        if (--repath <= 0) {
            repath = REPATH_INTERVAL;
            avali.getNavigation().moveTo(owner, AvaliCompanions.get(avali.level().getServer()).followSpeed());
        }
    }
}
