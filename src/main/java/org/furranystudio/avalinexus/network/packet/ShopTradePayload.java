/**
 * File: ShopTradePayload.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record ShopTradePayload(int entityId, int offer) implements CustomPacketPayload {

    public static final Type<ShopTradePayload> TYPE = new Type<>(AvaliNexus.id("shop_trade"));
    public static final StreamCodec<ByteBuf, ShopTradePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ShopTradePayload::entityId,
        ByteBufCodecs.VAR_INT, ShopTradePayload::offer,
        ShopTradePayload::new);

    @Override
    public Type<ShopTradePayload> type() {
        return TYPE;
    }
}
