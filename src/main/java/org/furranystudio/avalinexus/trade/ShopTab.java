/**
 * File: ShopTab.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;

import java.util.Locale;

// Seen from the player: BUY means the player pays Nexite, SELL means the player gets Nexite
public enum ShopTab {
    BUY,
    SELL;

    public static final Codec<ShopTab> CODEC = Codec.STRING.xmap(
        name -> ShopTab.valueOf(name.toUpperCase(Locale.ROOT)),
        tab -> tab.name().toLowerCase(Locale.ROOT));
}
