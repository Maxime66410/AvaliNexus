/**
 * File: RequestSettingsPayload.java
 * Author: Maxime66410
 * Created: 2026-10-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

public record RequestSettingsPayload() implements CustomPacketPayload {

    public static final RequestSettingsPayload INSTANCE = new RequestSettingsPayload();
    public static final Type<RequestSettingsPayload> TYPE = new Type<>(AvaliNexus.id("request_settings"));
    public static final StreamCodec<ByteBuf, RequestSettingsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RequestSettingsPayload> type() {
        return TYPE;
    }
}
