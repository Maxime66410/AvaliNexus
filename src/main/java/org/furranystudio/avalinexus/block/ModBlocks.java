/**
 * File: ModBlocks.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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

    // Same settings as the vanilla beds
    public static final RegistryEntry<Block> AVALI_BED = ModRegistry.register(Registries.BLOCK, "avali_bed",
        key -> new BedBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(state -> state.getValue(BedBlock.PART) == BedPart.FOOT ? DyeColor.ORANGE.getMapColor() : MapColor.WOOL)
            .sound(SoundType.WOOD)
            .strength(0.2F)
            .bounceRestitution(0.75F)
            .fallDistanceReduction(0.5F)
            .noOcclusion()
            .ignitedByLava()
            .pushReaction(PushReaction.POPPED)));

    // Same settings as the vanilla wool carpets
    public static final RegistryEntry<Block> AVALI_CARPET = ModRegistry.register(Registries.BLOCK, "avali_carpet",
        key -> new CarpetBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(DyeColor.ORANGE.getMapColor())
            .strength(0.1F)
            .sound(SoundType.WOOL)
            .ignitedByLava()));

    // Vanilla keeps the glass and pane constructors protected, hence the empty subclasses
    public static final RegistryEntry<Block> AEROGEL = ModRegistry.register(Registries.BLOCK, "aerogel",
        key -> new TransparentBlock(aerogel(key)) {
        });

    public static final RegistryEntry<Block> AEROGEL_PANE = ModRegistry.register(Registries.BLOCK, "aerogel_pane",
        key -> new IronBarsBlock(aerogel(key)) {
        });

    public static final RegistryEntry<Block> AEROGEL_SLAB = ModRegistry.register(Registries.BLOCK, "aerogel_slab",
        key -> new SlabBlock(aerogel(key)));

    public static final RegistryEntry<Block> AEROGEL_STAIRS = ModRegistry.register(Registries.BLOCK, "aerogel_stairs",
        key -> new StairBlock(AEROGEL.get().defaultBlockState(), aerogel(key)) {
        });

    public static final RegistryEntry<Block> AEROGEL_WALL = ModRegistry.register(Registries.BLOCK, "aerogel_wall",
        key -> new WallBlock(aerogel(key)));

    public static final RegistryEntry<Block> GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> CUT_GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "cut_graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> CHISELED_GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "chiseled_graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> GRAPHENE_SLAB = ModRegistry.register(Registries.BLOCK, "graphene_slab",
        key -> new SlabBlock(graphene(key)));

    public static final RegistryEntry<Block> GRAPHENE_STAIRS = ModRegistry.register(Registries.BLOCK, "graphene_stairs",
        key -> new StairBlock(GRAPHENE_BLOCK.get().defaultBlockState(), graphene(key)) {
        });

    public static final RegistryEntry<Block> GRAPHENE_WALL = ModRegistry.register(Registries.BLOCK, "graphene_wall",
        key -> new WallBlock(graphene(key)));

    public static final RegistryEntry<Block> CUT_GRAPHENE_SLAB = ModRegistry.register(Registries.BLOCK, "cut_graphene_slab",
        key -> new SlabBlock(graphene(key)));

    public static final RegistryEntry<Block> CUT_GRAPHENE_STAIRS = ModRegistry.register(Registries.BLOCK, "cut_graphene_stairs",
        key -> new StairBlock(CUT_GRAPHENE_BLOCK.get().defaultBlockState(), graphene(key)) {
        });

    public static final RegistryEntry<Block> CUT_GRAPHENE_WALL = ModRegistry.register(Registries.BLOCK, "cut_graphene_wall",
        key -> new WallBlock(graphene(key)));

    public static final RegistryEntry<Block> NANOFIBRE = ModRegistry.register(Registries.BLOCK, "nanofibre",
        key -> new RotatedPillarBlock(nanofibre(key)));

    public static final RegistryEntry<Block> NANOFIBRE_WALL = ModRegistry.register(Registries.BLOCK, "nanofibre_wall",
        key -> new WallBlock(nanofibre(key)));

    private ModBlocks() {
    }

    public static void init() {
    }

    // Iron block hardness, tough enough to shrug off TNT and charged creepers
    private static BlockBehaviour.Properties graphene(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 30.0F)
            .sound(SoundType.IRON);
    }

    // Same toughness as the nanocanvas it holds up
    private static BlockBehaviour.Properties nanofibre(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.3F, 15.0F)
            .sound(SoundType.COPPER);
    }

    // Glass settings, every shape stays see through so nothing behind it gets culled
    private static BlockBehaviour.Properties aerogel(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.ICE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false);
    }
}
