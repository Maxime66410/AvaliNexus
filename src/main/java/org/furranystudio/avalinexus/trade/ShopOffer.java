/**
 * File: ShopOffer.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ShopOffer(ShopTab tab, ItemStack item, int price, int maxUses, int uses, Optional<TagKey<Item>> accepts) {

    private static final long DISPLAY_CYCLE_MS = 1000L;

    public static final Codec<ShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ShopTab.CODEC.fieldOf("tab").forGetter(ShopOffer::tab),
        ItemStack.CODEC.fieldOf("item").forGetter(ShopOffer::item),
        Codec.INT.fieldOf("price").forGetter(ShopOffer::price),
        Codec.INT.fieldOf("max_uses").forGetter(ShopOffer::maxUses),
        Codec.INT.fieldOf("uses").forGetter(ShopOffer::uses),
        TagKey.codec(Registries.ITEM).optionalFieldOf("accepts").forGetter(ShopOffer::accepts)
    ).apply(instance, ShopOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopOffer> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(index -> ShopTab.values()[index], ShopTab::ordinal), ShopOffer::tab,
        ItemStack.STREAM_CODEC, ShopOffer::item,
        ByteBufCodecs.VAR_INT, ShopOffer::price,
        ByteBufCodecs.VAR_INT, ShopOffer::maxUses,
        ByteBufCodecs.VAR_INT, ShopOffer::uses,
        ByteBufCodecs.optional(Identifier.STREAM_CODEC.map(id -> TagKey.create(Registries.ITEM, id), TagKey::location)), ShopOffer::accepts,
        ShopOffer::new);

    public int remaining() {
        return Math.max(0, maxUses - uses);
    }

    public ShopOffer used() {
        return withUses(uses + 1);
    }

    public ShopOffer restocked() {
        return withUses(0);
    }

    public ShopOffer withUses(int uses) {
        return new ShopOffer(tab, item, price, maxUses, uses, accepts);
    }

    public ShopOffer withPrice(int price) {
        return new ShopOffer(tab, item, price, maxUses, uses, accepts);
    }

    public boolean takes(ItemStack stack) {
        return accepts.map(stack::is).orElseGet(() -> ItemStack.isSameItemSameComponents(stack, item));
    }

    public ItemStack shown(long millis) {
        if (accepts.isEmpty()) {
            return item;
        }
        List<Holder<Item>> items = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(accepts.get()).forEach(items::add);
        if (items.isEmpty()) {
            return item;
        }
        return new ItemStack(items.get((int) (millis / DISPLAY_CYCLE_MS % items.size())), item.getCount());
    }
}
