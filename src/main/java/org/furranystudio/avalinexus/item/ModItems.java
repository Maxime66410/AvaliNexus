/**
 * File: ModItems.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModItems {

    // Only used as the creative tab icon, never shown in any tab
    public static final RegistryEntry<Item> ICON = ModRegistry.register(Registries.ITEM, "icon",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<Item> NEXITE_SHARD = ModRegistry.register(Registries.ITEM, "nexite_shard",
        key -> new Item(new Item.Properties().setId(key)));

    public static final RegistryEntry<BlockItem> NEXITE_ORE = ModRegistry.register(Registries.ITEM, "nexite_ore",
        key -> new BlockItem(ModBlocks.NEXITE_ORE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<BlockItem> DEEPSLATE_NEXITE_ORE = ModRegistry.register(Registries.ITEM, "deepslate_nexite_ore",
        key -> new BlockItem(ModBlocks.DEEPSLATE_NEXITE_ORE.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    // The entity type may not be registered yet when items are, so it gets resolved later
    public static final RegistryEntry<SpawnEggItem> AVALI_SPAWN_EGG = ModRegistry.register(Registries.ITEM, "avali_spawn_egg",
        key -> new SpawnEggItem(new Item.Properties().setId(key).delayedComponent(DataComponents.ENTITY_DATA,
            registries -> TypedEntityData.of(ModEntities.AVALI.get(), new CompoundTag()))));

    public static final RegistryEntry<BlockItem> AVALI_BED = ModRegistry.register(Registries.ITEM, "avali_bed",
        key -> new BlockItem(ModBlocks.AVALI_BED.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix().stacksTo(1)));

    public static final RegistryEntry<BlockItem> AVALI_CARPET = ModRegistry.register(Registries.ITEM, "avali_carpet",
        key -> new BlockItem(ModBlocks.AVALI_CARPET.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));

    public static final RegistryEntry<AvaliCushionItem> AVALI_CUSHION = ModRegistry.register(Registries.ITEM, "avali_cushion",
        key -> new AvaliCushionItem(new Item.Properties().setId(key)));

    private ModItems() {
    }

    public static void init() {
    }
}
