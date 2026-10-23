/**
 * File: WarmthEffect.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WarmthEffect extends MobEffect {

    public WarmthEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
