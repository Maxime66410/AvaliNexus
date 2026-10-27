/**
 * File: AvaliShop.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

// The offers one Avali rolled, saved with it so its shop stays the same
public final class AvaliShop {

    public static final Codec<AvaliShop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("profession", "").forGetter(shop -> shop.profession),
        ShopOffer.CODEC.listOf().fieldOf("offers").forGetter(shop -> shop.offers),
        ShopSlot.CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(shop -> shop.slots),
        Codec.LONG.fieldOf("last_restock_day").forGetter(shop -> shop.lastRestockDay),
        Codec.LONG.optionalFieldOf("last_rotation_day", 0L).forGetter(shop -> shop.lastRotationDay)
    ).apply(instance, AvaliShop::new));

    private final String profession;
    private final List<ShopOffer> offers;
    private final List<ShopSlot> slots;
    private long lastRestockDay;
    private long lastRotationDay;

    public AvaliShop(String profession, List<ShopOffer> offers, List<ShopSlot> slots, long lastRestockDay, long lastRotationDay) {
        this.profession = profession;
        this.offers = new ArrayList<>(offers);
        this.slots = new ArrayList<>(slots);
        this.lastRestockDay = lastRestockDay;
        this.lastRotationDay = lastRotationDay;
    }

    public String profession() {
        return profession;
    }

    public List<ShopOffer> offers() {
        return offers;
    }

    public List<ShopSlot> slots() {
        return slots;
    }

    public void set(int index, ShopOffer offer) {
        offers.set(index, offer);
    }

    public int visible(ShopSlot unlocked) {
        int count = 0;
        while (count < slots.size() && slots.get(count).ordinal() <= unlocked.ordinal()) {
            count++;
        }
        return count;
    }

    public boolean restockIfNewDay(long day) {
        if (day <= lastRestockDay) {
            return false;
        }
        lastRestockDay = day;
        offers.replaceAll(ShopOffer::restocked);
        return true;
    }

    public boolean shouldRotate(long day, int interval) {
        return day - lastRotationDay >= interval;
    }

    public void rotated(long day) {
        lastRotationDay = day;
    }

    public void pause(long day) {
        lastRestockDay = Math.max(lastRestockDay, day);
        lastRotationDay = Math.max(lastRotationDay, day);
    }
}
