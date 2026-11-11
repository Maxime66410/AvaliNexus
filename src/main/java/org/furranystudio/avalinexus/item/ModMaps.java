/**
 * File: ModMaps.java
 * Author: Maxime66410
 * Created: 2026-11-11
 * Last Modified: 2026-11-11
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.Optional;

public final class ModMaps {

    private static final int SEARCH_RADIUS = 100;
    private static final byte ZOOM = 2;

    public static final TagKey<Structure> ON_AVALI_VILLAGE_MAPS = TagKey.create(Registries.STRUCTURE, AvaliNexus.id("on_avali_village_maps"));
    public static final TagKey<Structure> ON_AVALI_CAMP_MAPS = TagKey.create(Registries.STRUCTURE, AvaliNexus.id("on_avali_camp_maps"));

    public static final RegistryEntry<MapDecorationType> AVALI_VILLAGE = ModRegistry.register(Registries.MAP_DECORATION_TYPE, "avali_village",
        key -> new MapDecorationType(key.identifier(), true, false));

    public static final RegistryEntry<MapDecorationType> AVALI_CAMP = ModRegistry.register(Registries.MAP_DECORATION_TYPE, "avali_camp",
        key -> new MapDecorationType(key.identifier(), true, false));

    public static final RegistryEntry<Item> AVALI_VILLAGE_MAP = ModRegistry.register(Registries.ITEM, "avali_village_map",
        key -> new MapItem(new Item.Properties().setId(key).component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)));

    public static final RegistryEntry<Item> AVALI_CAMP_MAP = ModRegistry.register(Registries.ITEM, "avali_camp_map",
        key -> new MapItem(new Item.Properties().setId(key).component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)));

    private ModMaps() {
    }

    public static void init() {
    }

    public static boolean isMap(Item item) {
        return item == AVALI_VILLAGE_MAP.get() || item == AVALI_CAMP_MAP.get();
    }

    public static Optional<ItemStack> create(ServerLevel level, BlockPos origin, Item item) {
        boolean village = item == AVALI_VILLAGE_MAP.get();
        Optional<HolderSet.Named<Structure>> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
            .get(village ? ON_AVALI_VILLAGE_MAPS : ON_AVALI_CAMP_MAPS);
        if (structures.isEmpty()) {
            return Optional.empty();
        }
        StructureManager manager = level.structureManager();
        StructureStart home = manager.getStructureAt(origin, structures.get());
        if (home.isValid() && home.canBeReferenced()) {
            manager.addReference(home);
        }
        BlockPos target = level.findNearestMapStructure(structures.get(), origin, SEARCH_RADIUS, true);
        if (target == null) {
            return Optional.empty();
        }
        Holder<MapDecorationType> decoration = BuiltInRegistries.MAP_DECORATION_TYPE.wrapAsHolder((village ? AVALI_VILLAGE : AVALI_CAMP).get());
        ItemStack stack = new ItemStack(item);
        MapItem.applyNewSavedData(level, stack, target.getX(), target.getZ(), ZOOM, true, true);
        MapItem.renderBiomePreviewMap(level, stack);
        MapItemSavedData.addTargetDecoration(stack, target, "+", decoration);
        AvaliNexus.debug("Created an Avali {} map from {} to {}", village ? "village" : "camp", origin.toShortString(), target.toShortString());
        return Optional.of(stack);
    }
}
