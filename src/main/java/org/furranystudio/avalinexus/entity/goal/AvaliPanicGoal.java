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

    private final AvaliEntity avali;

    public AvaliPanicGoal(AvaliEntity avali, double speed) {
        super(avali, speed);
        this.avali = avali;
    }

    @Override
    public void start() {
        super.start();
        avali.setSprinting(true);
        avali.setPanicking(true);
    }

    @Override
    public void stop() {
        avali.setSprinting(false);
        avali.setPanicking(false);
        super.stop();
    }
}
