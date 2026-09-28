/**
 * File: ArchiveEntry.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.archive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

// One page of the Avali archives, its title and text live in the lang files
public record ArchiveEntry(String id, Identifier icon) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ArchiveEntry> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ArchiveEntry::id,
        Identifier.STREAM_CODEC, ArchiveEntry::icon,
        ArchiveEntry::new);

    public Component title() {
        return Component.translatable("avalinexus.archive." + id + ".title");
    }

    public Component text() {
        return Component.translatable("avalinexus.archive." + id + ".text");
    }
}
