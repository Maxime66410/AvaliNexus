/**
 * File: AvaliStrollGoal.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

public class AvaliStrollGoal extends WaterAvoidingRandomStrollGoal {

    private static final float RUN_CHANCE = 0.15F;

    private final AvaliEntity avali;

    public AvaliStrollGoal(AvaliEntity avali, double speed) {
        super(avali, speed);
        this.avali = avali;
    }

    @Override
    public boolean canUse() {
        return !avali.shouldRegroup() && !avali.isShelteredFromRain() && super.canUse();
    }

    @Override
    protected Vec3 getPosition() {
        Vec3 pos = super.getPosition();
        return pos == null || avali.isHotBiome(BlockPos.containing(pos)) ? null : pos;
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
