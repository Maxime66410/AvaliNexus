/**
 * File: OpenShopPayload.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.trade.ShopOffer;

import java.util.ArrayList;
import java.util.List;

public record OpenShopPayload(int entityId, List<ShopOffer> offers) implements CustomPacketPayload {

    public static final Type<OpenShopPayload> TYPE = new Type<>(AvaliNexus.id("open_shop"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShopPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, OpenShopPayload::entityId,
        ByteBufCodecs.collection(ArrayList::new, ShopOffer.STREAM_CODEC), OpenShopPayload::offers,
        OpenShopPayload::new);

    @Override
    public Type<OpenShopPayload> type() {
        return TYPE;
    }
}
