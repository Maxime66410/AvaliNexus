/**
 * File: RailEjectPayload.java
 * Author: Maxime66410
 * Created: 2026-10-10
 * Last Modified: 2026-10-10
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record RailEjectPayload() implements CustomPacketPayload {

    public static final RailEjectPayload INSTANCE = new RailEjectPayload();
    public static final Type<RailEjectPayload> TYPE = new Type<>(AvaliNexus.id("rail_eject"));
    public static final StreamCodec<ByteBuf, RailEjectPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RailEjectPayload> type() {
        return TYPE;
    }
}
