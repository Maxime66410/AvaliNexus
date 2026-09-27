/**
 * File: PingPayload.java
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

public record PingPayload(long sentAt) implements CustomPacketPayload {

    public static final Type<PingPayload> TYPE = new Type<>(AvaliNexus.id("ping"));
    public static final StreamCodec<ByteBuf, PingPayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_LONG, PingPayload::sentAt, PingPayload::new);

    @Override
    public Type<PingPayload> type() {
        return TYPE;
    }
}
