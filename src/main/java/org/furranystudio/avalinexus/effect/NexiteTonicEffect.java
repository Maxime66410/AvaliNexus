/**
 * File: NexiteTonicEffect.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.furranystudio.avalinexus.AvaliNexus;

public class NexiteTonicEffect extends MobEffect {

    private static final int HEAL_INTERVAL = 50;
    private static final double SPEED_BONUS = 0.2;

    public NexiteTonicEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, AvaliNexus.id("effect.nexite_tonic"), SPEED_BONUS,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = HEAL_INTERVAL >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }
}
