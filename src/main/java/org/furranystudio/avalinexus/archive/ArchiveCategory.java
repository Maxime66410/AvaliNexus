/**
 * File: ArchiveCategory.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.archive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// A tab of the archives, like the Avali lore or the things added by the mod
public record ArchiveCategory(String id) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ArchiveCategory> STREAM_CODEC =
        ByteBufCodecs.STRING_UTF8.map(ArchiveCategory::new, ArchiveCategory::id).cast();

    public Component title() {
        return Component.translatable("avalinexus.archive.category." + id);
    }
}
