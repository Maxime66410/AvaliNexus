/**
 * File: AvaliPanicGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.goal;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.furranystudio.avalinexus.entity.AvaliEntity;

public class AvaliPanicGoal extends PanicGoal {

    public AvaliPanicGoal(AvaliEntity avali, double speed) {
        super(avali, speed);
    }

    @Override
    public void start() {
        super.start();
        mob.setSprinting(true);
    }

    @Override
    public void stop() {
        mob.setSprinting(false);
        super.stop();
    }
}
