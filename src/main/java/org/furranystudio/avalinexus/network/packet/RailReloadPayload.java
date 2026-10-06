/**
 * File: RailReloadPayload.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record RailReloadPayload() implements CustomPacketPayload {

    public static final RailReloadPayload INSTANCE = new RailReloadPayload();
    public static final Type<RailReloadPayload> TYPE = new Type<>(AvaliNexus.id("rail_reload"));
    public static final StreamCodec<ByteBuf, RailReloadPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RailReloadPayload> type() {
        return TYPE;
    }
}
