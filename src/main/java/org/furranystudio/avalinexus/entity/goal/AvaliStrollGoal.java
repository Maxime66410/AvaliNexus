/**
 * File: AvaliStrollGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.goal;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.furranystudio.avalinexus.entity.AvaliEntity;

public class AvaliStrollGoal extends WaterAvoidingRandomStrollGoal {

    private static final float RUN_CHANCE = 0.15F;

    public AvaliStrollGoal(AvaliEntity avali, double speed) {
        super(avali, speed);
    }

    @Override
    public void start() {
        mob.setSprinting(mob.getRandom().nextFloat() < RUN_CHANCE);
        super.start();
    }

    @Override
    public void stop() {
        mob.setSprinting(false);
        super.stop();
    }
}
