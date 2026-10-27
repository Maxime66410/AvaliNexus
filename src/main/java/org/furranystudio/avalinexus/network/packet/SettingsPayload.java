/**
 * File: SettingsPayload.java
 * Author: Maxime66410
 * Created: 2026-10-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.HashMap;
import java.util.Map;

public record SettingsPayload(Map<String, String> values, boolean editable) implements CustomPacketPayload {

    public static final Type<SettingsPayload> TYPE = new Type<>(AvaliNexus.id("settings"));
    public static final StreamCodec<ByteBuf, SettingsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.<ByteBuf, String, String, Map<String, String>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
        SettingsPayload::values,
        ByteBufCodecs.BOOL, SettingsPayload::editable,
        SettingsPayload::new);

    @Override
    public Type<SettingsPayload> type() {
        return TYPE;
    }
}
