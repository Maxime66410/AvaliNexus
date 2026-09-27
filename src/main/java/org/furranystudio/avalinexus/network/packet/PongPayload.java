/**
 * File: PongPayload.java
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

public record PongPayload(long sentAt) implements CustomPacketPayload {

    public static final Type<PongPayload> TYPE = new Type<>(AvaliNexus.id("pong"));
    public static final StreamCodec<ByteBuf, PongPayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_LONG, PongPayload::sentAt, PongPayload::new);

    @Override
    public Type<PongPayload> type() {
        return TYPE;
    }
}
