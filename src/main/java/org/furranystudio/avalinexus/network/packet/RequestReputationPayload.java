/**
 * File: RequestReputationPayload.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record RequestReputationPayload() implements CustomPacketPayload {

    public static final RequestReputationPayload INSTANCE = new RequestReputationPayload();
    public static final Type<RequestReputationPayload> TYPE = new Type<>(AvaliNexus.id("request_reputation"));
    public static final StreamCodec<ByteBuf, RequestReputationPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RequestReputationPayload> type() {
        return TYPE;
    }
}
