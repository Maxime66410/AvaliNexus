/**
 * File: DialogueChoicePayload.java
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

public record DialogueChoicePayload(int entityId, int choice) implements CustomPacketPayload {

    public static final Type<DialogueChoicePayload> TYPE = new Type<>(AvaliNexus.id("dialogue_choice"));
    public static final StreamCodec<ByteBuf, DialogueChoicePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, DialogueChoicePayload::entityId,
        ByteBufCodecs.VAR_INT, DialogueChoicePayload::choice,
        DialogueChoicePayload::new);

    @Override
    public Type<DialogueChoicePayload> type() {
        return TYPE;
    }
}
