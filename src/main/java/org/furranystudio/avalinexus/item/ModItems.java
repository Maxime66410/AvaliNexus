/**
 * File: ModItems.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.tank.GrapheneTankItem;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModItems {

    // Only used as the creative tab icon, never shown in any tab
    public static final RegistryEntry<Item> ICON = ModRegistry.register(Registries.ITEM, "icon",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> NEXITE_SHARD = ModRegistry.register(Registries.ITEM, "nexite_shard",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<BlockItem> NEXITE_BLOCK = ModRegistry.register(Registries.ITEM, "nexite_block",
        key -> new BlockItem(ModBlocks.NEXITE_BLOCK.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    // One ingot per upgrade, like netherite
    public static final RegistryEntry<Item> NEXITE_INGOT = ModRegistry.register(Registries.ITEM, "nexite_ingot",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> NEXITE_NUGGET = ModRegistry.register(Registries.ITEM, "nexite_nugget",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> GRAPHENE = ModRegistry.register(Registries.ITEM, "graphene",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> AVALI_FEATHER = ModRegistry.register(Registries.ITEM, "avali_feather",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> BOND_FEATHER = ModRegistry.register(Registries.ITEM, "bond_feather",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> NEST_RATION = ModRegistry.register(Registries.ITEM, "nest_ration",
        key -> new Item(new Item.Properties().setId(key).food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build())));

    public static final RegistryEntry<BlockItem> NEXITE_ORE =ModRegistry.register(Registries.ITEM, "nexite_ore",
        key -> new BlockItem(ModBlocks.NEXITE_ORE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> DEEPSLATE_NEXITE_ORE = ModRegistry.register(Registries.ITEM, "deepslate_nexite_ore",
        key -> new BlockItem(ModBlocks.DEEPSLATE_NEXITE_ORE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    // The entity type may not be registered yet when items are, so it gets resolved later
    public static final RegistryEntry<SpawnEggItem> AVALI_SPAWN_EGG = ModRegistry.register(Registries.ITEM, "avali_spawn_egg",
        key -> new SpawnEggItem(new Item.Properties().setId(key).delayedComponent(DataComponents.ENTITY_DATA,
            registries -> TypedEntityData.of(ModEntities.AVALI.get(), new CompoundTag()))));

    public static final RegistryEntry<BlockItem> GRAPHENE_BLOCK = ModRegistry.register(Registries.ITEM, "graphene_block",
        key -> new BlockItem(ModBlocks.GRAPHENE_BLOCK.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_SLAB = ModRegistry.register(Registries.ITEM, "graphene_slab",
        key -> new BlockItem(ModBlocks.GRAPHENE_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_STAIRS = ModRegistry.register(Registries.ITEM, "graphene_stairs",
        key -> new BlockItem(ModBlocks.GRAPHENE_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_WALL = ModRegistry.register(Registries.ITEM, "graphene_wall",
        key -> new BlockItem(ModBlocks.GRAPHENE_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_GRAPHENE_BLOCK = ModRegistry.register(Registries.ITEM, "cut_graphene_block",
        key -> new BlockItem(ModBlocks.CUT_GRAPHENE_BLOCK.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_GRAPHENE_SLAB = ModRegistry.register(Registries.ITEM, "cut_graphene_slab",
        key -> new BlockItem(ModBlocks.CUT_GRAPHENE_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_GRAPHENE_STAIRS = ModRegistry.register(Registries.ITEM, "cut_graphene_stairs",
        key -> new BlockItem(ModBlocks.CUT_GRAPHENE_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_GRAPHENE_WALL = ModRegistry.register(Registries.ITEM, "cut_graphene_wall",
        key -> new BlockItem(ModBlocks.CUT_GRAPHENE_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_BRICKS = ModRegistry.register(Registries.ITEM, "graphene_bricks",
        key -> new BlockItem(ModBlocks.GRAPHENE_BRICKS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_BRICK_SLAB = ModRegistry.register(Registries.ITEM, "graphene_brick_slab",
        key -> new BlockItem(ModBlocks.GRAPHENE_BRICK_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_BRICK_STAIRS = ModRegistry.register(Registries.ITEM, "graphene_brick_stairs",
        key -> new BlockItem(ModBlocks.GRAPHENE_BRICK_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_BRICK_WALL = ModRegistry.register(Registries.ITEM, "graphene_brick_wall",
        key -> new BlockItem(ModBlocks.GRAPHENE_BRICK_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CHISELED_GRAPHENE_BLOCK = ModRegistry.register(Registries.ITEM, "chiseled_graphene_block",
        key -> new BlockItem(ModBlocks.CHISELED_GRAPHENE_BLOCK.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_PILLAR = ModRegistry.register(Registries.ITEM, "graphene_pillar",
        key -> new BlockItem(ModBlocks.GRAPHENE_PILLAR.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_TRAPDOOR = ModRegistry.register(Registries.ITEM, "graphene_trapdoor",
        key -> new BlockItem(ModBlocks.GRAPHENE_TRAPDOOR.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<DoubleHighBlockItem> GRAPHENE_DOOR = ModRegistry.register(Registries.ITEM, "graphene_door",
        key -> new DoubleHighBlockItem(ModBlocks.GRAPHENE_DOOR.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_TANK = ModRegistry.register(Registries.ITEM, "graphene_tank",
        key -> new GrapheneTankItem(ModBlocks.GRAPHENE_TANK.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_PIPE = ModRegistry.register(Registries.ITEM, "graphene_pipe",
        key -> new BlockItem(ModBlocks.GRAPHENE_PIPE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> FLUID_PUMP = ModRegistry.register(Registries.ITEM, "fluid_pump",
        key -> new BlockItem(ModBlocks.FLUID_PUMP.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> FLUID_TAP = ModRegistry.register(Registries.ITEM, "fluid_tap",
        key -> new BlockItem(ModBlocks.FLUID_TAP.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AMMONIA_REFINERY = ModRegistry.register(Registries.ITEM, "ammonia_refinery",
        key -> new BlockItem(ModBlocks.AMMONIA_REFINERY.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CRYO_FREEZER = ModRegistry.register(Registries.ITEM, "cryo_freezer",
        key -> new BlockItem(ModBlocks.CRYO_FREEZER.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_PRESS = ModRegistry.register(Registries.ITEM, "graphene_press",
        key -> new BlockItem(ModBlocks.GRAPHENE_PRESS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> WEAPON_WORKBENCH = ModRegistry.register(Registries.ITEM, "weapon_workbench",
        key -> new BlockItem(ModBlocks.WEAPON_WORKBENCH.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GRAPHENE_VENT = ModRegistry.register(Registries.ITEM, "graphene_vent",
        key -> new BlockItem(ModBlocks.GRAPHENE_VENT.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> ICE_BRICKS = ModRegistry.register(Registries.ITEM, "ice_bricks",
        key -> new BlockItem(ModBlocks.ICE_BRICKS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> ICE_BRICK_SLAB = ModRegistry.register(Registries.ITEM, "ice_brick_slab",
        key -> new BlockItem(ModBlocks.ICE_BRICK_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> ICE_BRICK_STAIRS = ModRegistry.register(Registries.ITEM, "ice_brick_stairs",
        key -> new BlockItem(ModBlocks.ICE_BRICK_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> ICE_BRICK_WALL = ModRegistry.register(Registries.ITEM, "ice_brick_wall",
        key -> new BlockItem(ModBlocks.ICE_BRICK_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> PACKED_SNOW = ModRegistry.register(Registries.ITEM, "packed_snow",
        key -> new BlockItem(ModBlocks.PACKED_SNOW.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> PACKED_SNOW_SLAB = ModRegistry.register(Registries.ITEM, "packed_snow_slab",
        key -> new BlockItem(ModBlocks.PACKED_SNOW_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> PACKED_SNOW_STAIRS = ModRegistry.register(Registries.ITEM, "packed_snow_stairs",
        key -> new BlockItem(ModBlocks.PACKED_SNOW_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> PACKED_SNOW_WALL = ModRegistry.register(Registries.ITEM, "packed_snow_wall",
        key -> new BlockItem(ModBlocks.PACKED_SNOW_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_PACKED_SNOW = ModRegistry.register(Registries.ITEM, "cut_packed_snow",
        key -> new BlockItem(ModBlocks.CUT_PACKED_SNOW.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_PACKED_SNOW_SLAB = ModRegistry.register(Registries.ITEM, "cut_packed_snow_slab",
        key -> new BlockItem(ModBlocks.CUT_PACKED_SNOW_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_PACKED_SNOW_STAIRS = ModRegistry.register(Registries.ITEM, "cut_packed_snow_stairs",
        key -> new BlockItem(ModBlocks.CUT_PACKED_SNOW_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CUT_PACKED_SNOW_WALL = ModRegistry.register(Registries.ITEM, "cut_packed_snow_wall",
        key -> new BlockItem(ModBlocks.CUT_PACKED_SNOW_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CHISELED_PACKED_SNOW = ModRegistry.register(Registries.ITEM, "chiseled_packed_snow",
        key -> new BlockItem(ModBlocks.CHISELED_PACKED_SNOW.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AMMONIA_ICE = ModRegistry.register(Registries.ITEM, "ammonia_ice",
        key -> new BlockItem(ModBlocks.AMMONIA_ICE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> PACKED_AMMONIA_ICE = ModRegistry.register(Registries.ITEM, "packed_ammonia_ice",
        key -> new BlockItem(ModBlocks.PACKED_AMMONIA_ICE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> NANOFIBRE = ModRegistry.register(Registries.ITEM, "nanofibre",
        key -> new BlockItem(ModBlocks.NANOFIBRE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> NANOFIBRE_WALL = ModRegistry.register(Registries.ITEM, "nanofibre_wall",
        key -> new BlockItem(ModBlocks.NANOFIBRE_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AEROGEL = ModRegistry.register(Registries.ITEM, "aerogel",
        key -> new BlockItem(ModBlocks.AEROGEL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AEROGEL_PANE = ModRegistry.register(Registries.ITEM, "aerogel_pane",
        key -> new BlockItem(ModBlocks.AEROGEL_PANE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AEROGEL_SLAB = ModRegistry.register(Registries.ITEM, "aerogel_slab",
        key -> new BlockItem(ModBlocks.AEROGEL_SLAB.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AEROGEL_STAIRS = ModRegistry.register(Registries.ITEM, "aerogel_stairs",
        key -> new BlockItem(ModBlocks.AEROGEL_STAIRS.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AEROGEL_WALL = ModRegistry.register(Registries.ITEM, "aerogel_wall",
        key -> new BlockItem(ModBlocks.AEROGEL_WALL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> GLOWING_AEROGEL = ModRegistry.register(Registries.ITEM, "glowing_aerogel",
        key -> new BlockItem(ModBlocks.GLOWING_AEROGEL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AVALI_LAMP = ModRegistry.register(Registries.ITEM, "avali_lamp",
        key -> new BlockItem(ModBlocks.AVALI_LAMP.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AVALI_LIGHT_STRIP = ModRegistry.register(Registries.ITEM, "avali_light_strip",
        key -> new BlockItem(ModBlocks.AVALI_LIGHT_STRIP.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> CRYSTAL_POT = ModRegistry.register(Registries.ITEM, "crystal_pot",
        key -> new BlockItem(ModBlocks.CRYSTAL_POT.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> HEATER = ModRegistry.register(Registries.ITEM, "heater",
        key -> new BlockItem(ModBlocks.HEATER.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AVALI_LOOM = ModRegistry.register(Registries.ITEM, "avali_loom",
        key -> new BlockItem(ModBlocks.AVALI_LOOM.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> TERMINAL = ModRegistry.register(Registries.ITEM, "terminal",
        key -> new BlockItem(ModBlocks.TERMINAL.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> AVALI_BED = ModRegistry.register(Registries.ITEM, "avali_bed",
        key -> new BlockItem(ModBlocks.AVALI_BED.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix().stacksTo(1)));

    public static final RegistryEntry<BlockItem> AVALI_CARPET = ModRegistry.register(Registries.ITEM, "avali_carpet",
        key -> new BlockItem(ModBlocks.AVALI_CARPET.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()
            .component(DataComponents.EQUIPPABLE, llamaSwag("avali_carpet"))
            .cookingFuel(ContextIntProviders.COOKING_TIME_WOOL_CARPETS)));

    public static final RegistryEntry<AvaliCushionItem> AVALI_CUSHION = ModRegistry.register(Registries.ITEM, "avali_cushion",
        key -> new AvaliCushionItem(new Item.Properties().setId(key)));

    private ModItems() {
    }

    // Same as Equippable.llamaSwag, which only knows the vanilla carpet colors
    private static Equippable llamaSwag(String asset) {
        return Equippable.builder(EquipmentSlot.BODY)
            .setEquipSound(SoundEvents.LLAMA_SWAG)
            .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, AvaliNexus.id(asset)))
            .setAllowedEntities(HolderSet.direct(EntityTypes.LLAMA.builtInRegistryHolder(), EntityTypes.TRADER_LLAMA.builtInRegistryHolder()))
            .setCanBeSheared(true)
            .setShearingSound(SoundEvents.LLAMA_CARPET_UNEQUIP)
            .build();
    }

    public static void init() {
    }
}
