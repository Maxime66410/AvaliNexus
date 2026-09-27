/**
 * File: AvaliTalkGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliTalkGoal extends Goal {

    private final AvaliEntity avali;

    public AvaliTalkGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return avali.getTalkingTo() != null;
    }

    @Override
    public void start() {
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        avali.getNavigation().stop();
        avali.getLookControl().setLookAt(avali.getTalkingTo(), 30.0F, 30.0F);
    }
}
