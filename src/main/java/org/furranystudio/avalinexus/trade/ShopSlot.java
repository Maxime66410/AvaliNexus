/**
 * File: ShopSlot.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum ShopSlot {
    BUY,
    SELL,
    FRIENDLY,
    ALLY;

    public static final Codec<ShopSlot> CODEC = Codec.STRING.xmap(
        name -> ShopSlot.valueOf(name.toUpperCase(Locale.ROOT)),
        slot -> slot.name().toLowerCase(Locale.ROOT));
}
