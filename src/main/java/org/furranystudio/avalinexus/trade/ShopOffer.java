/**
 * File: ShopOffer.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ShopOffer(ShopTab tab, ItemStack item, int price, int maxUses, int uses) {

    public static final Codec<ShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ShopTab.CODEC.fieldOf("tab").forGetter(ShopOffer::tab),
        ItemStack.CODEC.fieldOf("item").forGetter(ShopOffer::item),
        Codec.INT.fieldOf("price").forGetter(ShopOffer::price),
        Codec.INT.fieldOf("max_uses").forGetter(ShopOffer::maxUses),
        Codec.INT.fieldOf("uses").forGetter(ShopOffer::uses)
    ).apply(instance, ShopOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopOffer> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(index -> ShopTab.values()[index], ShopTab::ordinal), ShopOffer::tab,
        ItemStack.STREAM_CODEC, ShopOffer::item,
        ByteBufCodecs.VAR_INT, ShopOffer::price,
        ByteBufCodecs.VAR_INT, ShopOffer::maxUses,
        ByteBufCodecs.VAR_INT, ShopOffer::uses,
        ShopOffer::new);

    public int remaining() {
        return Math.max(0, maxUses - uses);
    }

    public ShopOffer used() {
        return new ShopOffer(tab, item, price, maxUses, uses + 1);
    }

    public ShopOffer restocked() {
        return new ShopOffer(tab, item, price, maxUses, 0);
    }
}
