/**
 * File: CloseDialoguePayload.java
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

public record CloseDialoguePayload(int entityId) implements CustomPacketPayload {

    public static final Type<CloseDialoguePayload> TYPE = new Type<>(AvaliNexus.id("close_dialogue"));
    public static final StreamCodec<ByteBuf, CloseDialoguePayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, CloseDialoguePayload::entityId, CloseDialoguePayload::new);

    @Override
    public Type<CloseDialoguePayload> type() {
        return TYPE;
    }
}
