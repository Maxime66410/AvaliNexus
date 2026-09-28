/**
 * File: OpenArchivesPayload.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.archive.ArchiveCategory;
import org.furranystudio.avalinexus.archive.ArchiveEntry;

import java.util.ArrayList;
import java.util.List;

public record OpenArchivesPayload(List<ArchiveCategory> categories, List<ArchiveEntry> entries) implements CustomPacketPayload {

    public static final Type<OpenArchivesPayload> TYPE = new Type<>(AvaliNexus.id("open_archives"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArchivesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ArchiveCategory.STREAM_CODEC), OpenArchivesPayload::categories,
        ByteBufCodecs.collection(ArrayList::new, ArchiveEntry.STREAM_CODEC), OpenArchivesPayload::entries,
        OpenArchivesPayload::new);

    @Override
    public Type<OpenArchivesPayload> type() {
        return TYPE;
    }
}
