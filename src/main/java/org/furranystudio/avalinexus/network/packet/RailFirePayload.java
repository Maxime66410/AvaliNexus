/**
 * File: RailFirePayload.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record RailFirePayload() implements CustomPacketPayload {

    public static final RailFirePayload INSTANCE = new RailFirePayload();
    public static final Type<RailFirePayload> TYPE = new Type<>(AvaliNexus.id("rail_fire"));
    public static final StreamCodec<ByteBuf, RailFirePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RailFirePayload> type() {
        return TYPE;
    }
}
