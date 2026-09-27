/**
 * File: OpenDialoguePayload.java
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

public record OpenDialoguePayload(int entityId, String line) implements CustomPacketPayload {

    public static final Type<OpenDialoguePayload> TYPE = new Type<>(AvaliNexus.id("open_dialogue"));
    public static final StreamCodec<ByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, OpenDialoguePayload::entityId,
        ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::line,
        OpenDialoguePayload::new);

    @Override
    public Type<OpenDialoguePayload> type() {
        return TYPE;
    }
}
