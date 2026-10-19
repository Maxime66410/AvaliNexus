/**
 * File: DialogueLinePayload.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.List;

public record DialogueLinePayload(int entityId, String line, boolean closing, List<Integer> choices, int highlight) implements CustomPacketPayload {

    public static final Type<DialogueLinePayload> TYPE = new Type<>(AvaliNexus.id("dialogue_line"));
    public static final StreamCodec<ByteBuf, DialogueLinePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, DialogueLinePayload::entityId,
        ByteBufCodecs.STRING_UTF8, DialogueLinePayload::line,
        ByteBufCodecs.BOOL, DialogueLinePayload::closing,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), DialogueLinePayload::choices,
        ByteBufCodecs.VAR_INT, DialogueLinePayload::highlight,
        DialogueLinePayload::new);

    @Override
    public Type<DialogueLinePayload> type() {
        return TYPE;
    }
}
