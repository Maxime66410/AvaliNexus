/**
 * File: NanocanvasTints.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.block;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;

import java.util.List;
import java.util.function.BiConsumer;

// Items get the same tint from their item model json
public final class NanocanvasTints {

    private NanocanvasTints() {
    }

    // Matches the register(List, Block...) of every loader color event
    public static void register(BiConsumer<List<BlockTintSource>, Block[]> sink) {
        for (DyeColor color : DyeColor.values()) {
            sink.accept(List.of(BlockTintSources.constant(0xFF000000 | color.getTextureDiffuseColor())),
                NanocanvasBlocks.all(color).toArray(Block[]::new));
        }
    }
}
