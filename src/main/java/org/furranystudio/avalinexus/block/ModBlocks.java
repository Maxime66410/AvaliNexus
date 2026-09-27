/**
 * File: ModBlocks.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModBlocks {

    public static final RegistryEntry<Block> NEXITE_ORE = ModRegistry.register(Registries.BLOCK, "nexite_ore",
        key -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 3.0F)));

    public static final RegistryEntry<Block> DEEPSLATE_NEXITE_ORE = ModRegistry.register(Registries.BLOCK, "deepslate_nexite_ore",
        key -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.DEEPSLATE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(4.5F, 3.0F)
            .sound(SoundType.DEEPSLATE)));

    private ModBlocks() {
    }

    public static void init() {
    }
}
