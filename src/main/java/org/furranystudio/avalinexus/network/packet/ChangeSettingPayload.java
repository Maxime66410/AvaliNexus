/**
 * File: ChangeSettingPayload.java
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

public record ChangeSettingPayload(String key, String value) implements CustomPacketPayload {

    public static final Type<ChangeSettingPayload> TYPE = new Type<>(AvaliNexus.id("change_setting"));
    public static final StreamCodec<ByteBuf, ChangeSettingPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ChangeSettingPayload::key,
        ByteBufCodecs.STRING_UTF8, ChangeSettingPayload::value,
        ChangeSettingPayload::new);

    @Override
    public Type<ChangeSettingPayload> type() {
        return TYPE;
    }
}
