/**
 * File: ShopUpdatePayload.java
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

public record ShopUpdatePayload(int entityId, int offer, int uses) implements CustomPacketPayload {

    public static final Type<ShopUpdatePayload> TYPE = new Type<>(AvaliNexus.id("shop_update"));
    public static final StreamCodec<ByteBuf, ShopUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ShopUpdatePayload::entityId,
        ByteBufCodecs.VAR_INT, ShopUpdatePayload::offer,
        ByteBufCodecs.VAR_INT, ShopUpdatePayload::uses,
        ShopUpdatePayload::new);

    @Override
    public Type<ShopUpdatePayload> type() {
        return TYPE;
    }
}
