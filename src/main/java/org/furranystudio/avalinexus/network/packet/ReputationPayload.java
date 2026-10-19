/**
 * File: ReputationPayload.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.ArrayList;
import java.util.List;

public record ReputationPayload(List<Entry> packs, List<Integer> bounds, int friendlyPercent, int allyPercent) implements CustomPacketPayload {

    public record Entry(String pack, int color, int value, boolean met) {

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Entry::pack,
            ByteBufCodecs.INT, Entry::color,
            ByteBufCodecs.VAR_INT, Entry::value,
            ByteBufCodecs.BOOL, Entry::met,
            Entry::new);
    }

    public static final Type<ReputationPayload> TYPE = new Type<>(AvaliNexus.id("reputation"));
    public static final StreamCodec<ByteBuf, ReputationPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC), ReputationPayload::packs,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ReputationPayload::bounds,
        ByteBufCodecs.VAR_INT, ReputationPayload::friendlyPercent,
        ByteBufCodecs.VAR_INT, ReputationPayload::allyPercent,
        ReputationPayload::new);

    @Override
    public Type<ReputationPayload> type() {
        return TYPE;
    }
}
