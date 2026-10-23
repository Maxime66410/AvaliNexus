/**
 * File: ModBlocks.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.heater.HeaterBlock;
import org.furranystudio.avalinexus.block.plant.AvalonPlantBlock;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.block.loom.AvaliLoomBlock;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerBlock;
import org.furranystudio.avalinexus.block.press.GraphenePressBlock;
import org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchBlock;
import org.furranystudio.avalinexus.block.pump.FluidPumpBlock;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryBlock;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlock;
import org.furranystudio.avalinexus.block.tap.FluidTapBlock;
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

    public static final RegistryEntry<Block> NEXITE_BLOCK = ModRegistry.register(Registries.BLOCK, "nexite_block",
        key -> new Block(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.GOLD)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 30.0F)
            .sound(SoundType.METAL)));

    public static final RegistryEntry<Block> AVALI_LAMP = ModRegistry.register(Registries.BLOCK, "avali_lamp",
        key -> new Block(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_ORANGE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(1.5F, 30.0F)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 15)));

    // Thin glowing strip
    public static final RegistryEntry<Block> AVALI_LIGHT_STRIP = ModRegistry.register(Registries.BLOCK, "avali_light_strip",
        key -> new LightStripBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_ORANGE)
            .instabreak()
            .noCollision()
            .noOcclusion()
            .sound(SoundType.GLASS)
            .lightLevel(state -> 10)
            .pushReaction(PushReaction.POPPED)));

    private static final int[] NAKATI_LIGHT = {3, 6, 8, 10};

    public static final RegistryEntry<Block> PIRU = ModRegistry.register(Registries.BLOCK, "piru",
        key -> new AvalonPlantBlock(plant(key, MapColor.COLOR_LIGHT_GRAY), () -> ModItems.PIRU_SPORE.get(), null));

    public static final RegistryEntry<Block> NAKATI = ModRegistry.register(Registries.BLOCK, "nakati",
        key -> new AvalonPlantBlock(plant(key, MapColor.COLOR_CYAN).lightLevel(state -> NAKATI_LIGHT[state.getValue(AvalonPlantBlock.AGE)]),
            () -> ModItems.NAKATI_CYST.get(), null));

    public static final RegistryEntry<Block> KIRI = ModRegistry.register(Registries.BLOCK, "kiri",
        key -> new AvalonPlantBlock(plant(key, MapColor.COLOR_PINK), () -> ModItems.KIRI_FRUIT.get(),
            ResourceKey.create(Registries.LOOT_TABLE, AvaliNexus.id("harvest/kiri"))));

    public static final RegistryEntry<Block> CRYSTAL_POT = ModRegistry.register(Registries.BLOCK, "crystal_pot",
        key -> new CrystalPotBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.GOLD)
            .instabreak()
            .sound(SoundType.AMETHYST)
            .lightLevel(state -> 7)
            .noOcclusion()
            .pushReaction(PushReaction.POPPED)));

    public static final RegistryEntry<Block> HEATER = ModRegistry.register(Registries.BLOCK, "heater",
        key -> new HeaterBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.5F, 30.0F)
            .sound(SoundType.METAL)
            .noOcclusion()
            .lightLevel(state -> state.getValue(HeaterBlock.LIT) ? 13 : 0)));

    // Loom toughness
    public static final RegistryEntry<Block> AVALI_LOOM = ModRegistry.register(Registries.BLOCK, "avali_loom",
        key -> new AvaliLoomBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.WOOD)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .ignitedByLava()));

    // The screen glows a little
    public static final RegistryEntry<Block> TERMINAL = ModRegistry.register(Registries.BLOCK, "terminal",
        key -> new TerminalBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .requiresCorrectToolForDrops()
            .strength(3.5F, 30.0F)
            .sound(SoundType.METAL)
            .noOcclusion()
            .lightLevel(state -> 7)));

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

    public static final RegistryEntry<Block> AVALI_CARPET = ModRegistry.register(Registries.BLOCK, "avali_carpet",
        key -> new CarpetBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(DyeColor.ORANGE.getMapColor())
            .strength(0.1F)
            .sound(SoundType.WOOL)
            .ignitedByLava()));

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

    public static final RegistryEntry<Block> GLOWING_AEROGEL = ModRegistry.register(Registries.BLOCK, "glowing_aerogel",
        key -> new TransparentBlock(aerogel(key).lightLevel(state -> 12)) {
        });

    public static final RegistryEntry<Block> GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> CUT_GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "cut_graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> CHISELED_GRAPHENE_BLOCK = ModRegistry.register(Registries.BLOCK, "chiseled_graphene_block",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> GRAPHENE_PILLAR = ModRegistry.register(Registries.BLOCK, "graphene_pillar",
        key -> new RotatedPillarBlock(graphene(key)));

    public static final BlockSetType GRAPHENE_SET = new BlockSetType("avalinexus:graphene", true, true, false,
        BlockSetType.PressurePlateSensitivity.MOBS, SoundType.IRON, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

    public static final RegistryEntry<Block> GRAPHENE_TRAPDOOR = ModRegistry.register(Registries.BLOCK, "graphene_trapdoor",
        key -> new TrapDoorBlock(GRAPHENE_SET, graphene(key).noOcclusion().isValidSpawn((state, level, pos, type) -> false)) {
        });

    public static final RegistryEntry<Block> GRAPHENE_DOOR = ModRegistry.register(Registries.BLOCK, "graphene_door",
        key -> new DoorBlock(GRAPHENE_SET, graphene(key).noOcclusion().pushReaction(PushReaction.POPPED)) {
        });

    public static final RegistryEntry<Block> GRAPHENE_TANK = ModRegistry.register(Registries.BLOCK, "graphene_tank",
        key -> new GrapheneTankBlock(graphene(key)
            .noOcclusion()
            .pushReaction(PushReaction.IMMOVEABLE)
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false)));
    public static final RegistryEntry<Block> GRAPHENE_PIPE = ModRegistry.register(Registries.BLOCK, "graphene_pipe",
        key -> new GraphenePipeBlock(6.0F, graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> FLUID_PUMP = ModRegistry.register(Registries.BLOCK, "fluid_pump",
        key -> new FluidPumpBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> FLUID_TAP = ModRegistry.register(Registries.BLOCK, "fluid_tap",
        key -> new FluidTapBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> AMMONIA_REFINERY = ModRegistry.register(Registries.BLOCK, "ammonia_refinery",
        key -> new AmmoniaRefineryBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> CRYO_FREEZER = ModRegistry.register(Registries.BLOCK, "cryo_freezer",
        key -> new CryoFreezerBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> GRAPHENE_PRESS = ModRegistry.register(Registries.BLOCK, "graphene_press",
        key -> new GraphenePressBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> WEAPON_WORKBENCH = ModRegistry.register(Registries.BLOCK, "weapon_workbench",
        key -> new WeaponWorkbenchBlock(graphene(key).noOcclusion()));

    public static final RegistryEntry<Block> GRAPHENE_VENT = ModRegistry.register(Registries.BLOCK, "graphene_vent",
        key -> new WaterloggedTransparentBlock(graphene(key)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false)) {
        });

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

    public static final RegistryEntry<Block> GRAPHENE_BRICKS = ModRegistry.register(Registries.BLOCK, "graphene_bricks",
        key -> new Block(graphene(key)));

    public static final RegistryEntry<Block> GRAPHENE_BRICK_SLAB = ModRegistry.register(Registries.BLOCK, "graphene_brick_slab",
        key -> new SlabBlock(graphene(key)));

    public static final RegistryEntry<Block> GRAPHENE_BRICK_STAIRS = ModRegistry.register(Registries.BLOCK, "graphene_brick_stairs",
        key -> new StairBlock(GRAPHENE_BRICKS.get().defaultBlockState(), graphene(key)) {
        });

    public static final RegistryEntry<Block> GRAPHENE_BRICK_WALL = ModRegistry.register(Registries.BLOCK, "graphene_brick_wall",
        key -> new WallBlock(graphene(key)));

    public static final RegistryEntry<Block> NANOFIBRE = ModRegistry.register(Registries.BLOCK, "nanofibre",
        key -> new RotatedPillarBlock(nanofibre(key)));

    public static final RegistryEntry<Block> NANOFIBRE_WALL = ModRegistry.register(Registries.BLOCK, "nanofibre_wall",
        key -> new WallBlock(nanofibre(key)));

    private ModBlocks() {
    }

    public static void init() {
    }

    public static final RegistryEntry<Block> ICE_BRICKS = ModRegistry.register(Registries.BLOCK, "ice_bricks",
        key -> new Block(iceBricks(key)));

    public static final RegistryEntry<Block> ICE_BRICK_SLAB = ModRegistry.register(Registries.BLOCK, "ice_brick_slab",
        key -> new SlabBlock(iceBricks(key)));

    public static final RegistryEntry<Block> ICE_BRICK_STAIRS = ModRegistry.register(Registries.BLOCK, "ice_brick_stairs",
        key -> new StairBlock(ICE_BRICKS.get().defaultBlockState(), iceBricks(key)) {
        });

    public static final RegistryEntry<Block> ICE_BRICK_WALL = ModRegistry.register(Registries.BLOCK, "ice_brick_wall",
        key -> new WallBlock(iceBricks(key)));

    public static final RegistryEntry<Block> PACKED_SNOW = ModRegistry.register(Registries.BLOCK, "packed_snow",
        key -> new Block(packedSnow(key)));

    public static final RegistryEntry<Block> PACKED_SNOW_SLAB = ModRegistry.register(Registries.BLOCK, "packed_snow_slab",
        key -> new SlabBlock(packedSnow(key)));

    public static final RegistryEntry<Block> PACKED_SNOW_STAIRS = ModRegistry.register(Registries.BLOCK, "packed_snow_stairs",
        key -> new StairBlock(PACKED_SNOW.get().defaultBlockState(), packedSnow(key)) {
        });

    public static final RegistryEntry<Block> PACKED_SNOW_WALL = ModRegistry.register(Registries.BLOCK, "packed_snow_wall",
        key -> new WallBlock(packedSnow(key)));

    public static final RegistryEntry<Block> CUT_PACKED_SNOW = ModRegistry.register(Registries.BLOCK, "cut_packed_snow",
        key -> new Block(packedSnow(key)));

    public static final RegistryEntry<Block> CUT_PACKED_SNOW_SLAB = ModRegistry.register(Registries.BLOCK, "cut_packed_snow_slab",
        key -> new SlabBlock(packedSnow(key)));

    public static final RegistryEntry<Block> CUT_PACKED_SNOW_STAIRS = ModRegistry.register(Registries.BLOCK, "cut_packed_snow_stairs",
        key -> new StairBlock(CUT_PACKED_SNOW.get().defaultBlockState(), packedSnow(key)) {
        });

    public static final RegistryEntry<Block> CUT_PACKED_SNOW_WALL = ModRegistry.register(Registries.BLOCK, "cut_packed_snow_wall",
        key -> new WallBlock(packedSnow(key)));

    public static final RegistryEntry<Block> CHISELED_PACKED_SNOW = ModRegistry.register(Registries.BLOCK, "chiseled_packed_snow",
        key -> new Block(packedSnow(key)));

    public static final RegistryEntry<Block> AMMONIA_ICE = ModRegistry.register(Registries.BLOCK, "ammonia_ice",
        key -> new AmmoniaIceBlock(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.ICE)
            .friction(0.98F)
            .randomTicks()
            .strength(0.5F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)));
    public static final RegistryEntry<Block> PACKED_AMMONIA_ICE = ModRegistry.register(Registries.BLOCK, "packed_ammonia_ice",
        key -> new Block(BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.ICE)
            .instrument(NoteBlockInstrument.CHIME)
            .friction(0.98F)
            .strength(0.5F)
            .sound(SoundType.GLASS)));

    private static BlockBehaviour.Properties plant(ResourceKey<Block> key, MapColor color) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(color)
            .noCollision()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.POPPED);
    }

    private static BlockBehaviour.Properties iceBricks(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.ICE)
            .instrument(NoteBlockInstrument.CHIME)
            .strength(1.0F)
            .sound(SoundType.GLASS);
    }

    private static BlockBehaviour.Properties packedSnow(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.SNOW)
            .strength(0.6F)
            .sound(SoundType.SNOW);
    }

    private static BlockBehaviour.Properties graphene(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 30.0F)
            .sound(SoundType.IRON);
    }

    private static BlockBehaviour.Properties nanofibre(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.3F, 15.0F)
            .sound(SoundType.COPPER);
    }

    static BlockBehaviour.Properties aerogel(ResourceKey<Block> key) {
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
