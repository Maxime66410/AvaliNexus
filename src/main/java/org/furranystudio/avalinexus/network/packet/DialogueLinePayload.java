/**
 * File: DialogueLinePayload.java
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

public record DialogueLinePayload(int entityId, String line, boolean closing) implements CustomPacketPayload {

    public static final Type<DialogueLinePayload> TYPE = new Type<>(AvaliNexus.id("dialogue_line"));
    public static final StreamCodec<ByteBuf, DialogueLinePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, DialogueLinePayload::entityId,
        ByteBufCodecs.STRING_UTF8, DialogueLinePayload::line,
        ByteBufCodecs.BOOL, DialogueLinePayload::closing,
        DialogueLinePayload::new);

    @Override
    public Type<DialogueLinePayload> type() {
        return TYPE;
    }
}
