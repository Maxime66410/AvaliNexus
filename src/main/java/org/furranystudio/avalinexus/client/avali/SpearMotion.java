/**
 * File: SpearMotion.java
 * Author: Maxime66410
 * Created: 2026-11-18
 * Last Modified: 2026-11-18
 */
package org.furranystudio.avalinexus.client.avali;

import net.minecraft.util.Ease;
import net.minecraft.util.Mth;

final class SpearMotion {

    private SpearMotion() {
    }

    static float prepare(float swing) {
        return Ease.inOutSine(progress(swing, 0.0F, 0.05F));
    }

    static float attack(float swing) {
        return Ease.inQuad(progress(swing, 0.05F, 0.2F));
    }

    static float retract(float swing) {
        return Ease.inOutExpo(progress(swing, 0.4F, 1.0F));
    }

    private static float progress(float time, float start, float end) {
        return Mth.clamp(Mth.inverseLerp(time, start, end), 0.0F, 1.0F);
    }
}
