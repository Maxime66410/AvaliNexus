/**
 * File: AvaliShop.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

// The offers one Avali rolled, saved with it so its shop stays the same
public final class AvaliShop {

    public static final Codec<AvaliShop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ShopOffer.CODEC.listOf().fieldOf("offers").forGetter(shop -> shop.offers),
        Codec.LONG.fieldOf("last_restock_day").forGetter(shop -> shop.lastRestockDay)
    ).apply(instance, AvaliShop::new));

    private final List<ShopOffer> offers;
    private long lastRestockDay;

    public AvaliShop(List<ShopOffer> offers, long lastRestockDay) {
        this.offers = new ArrayList<>(offers);
        this.lastRestockDay = lastRestockDay;
    }

    public List<ShopOffer> offers() {
        return offers;
    }

    public void set(int index, ShopOffer offer) {
        offers.set(index, offer);
    }

    public void restockIfNewDay(long day) {
        if (day <= lastRestockDay) {
            return;
        }
        lastRestockDay = day;
        offers.replaceAll(ShopOffer::restocked);
    }
}
