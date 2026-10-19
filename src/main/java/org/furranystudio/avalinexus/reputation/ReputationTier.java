/**
 * File: ReputationTier.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.reputation;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum ReputationTier {
    HOSTILE(0xFFE63946),
    WARY(0xFFFFA640),
    NEUTRAL(0xFFBBBBBB),
    FRIENDLY(0xFF8FD16A),
    ALLY(0xFF4CD964);

    private final int color;

    ReputationTier(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component label() {
        return Component.translatable("avalinexus.reputation.tier." + key());
    }
}
