/**
 * File: SliderFormat.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.client.settings;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum SliderFormat {
    PERCENT,
    BLOCKS,
    DECIMAL;

    private static final float DECIMAL_SCALE = 10.0F;

    public Component text(int value) {
        return switch (this) {
            case PERCENT -> Component.literal(value + "%");
            case BLOCKS -> Component.translatable("avalinexus.settings.unit.blocks", value);
            case DECIMAL -> Component.literal(String.format(Locale.ROOT, "%.1f", value / DECIMAL_SCALE));
        };
    }
}
