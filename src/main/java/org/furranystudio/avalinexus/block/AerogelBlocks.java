/**
 * File: AerogelBlocks.java
 * Author: Maxime66410
 * Created: 2026-10-13
 * Last Modified: 2026-10-13
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WallBlock;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Dyed aerogel in every color
public final class AerogelBlocks {

    public enum Shape {
        BLOCK("_aerogel"),
        PANE("_aerogel_pane"),
        SLAB("_aerogel_slab"),
        STAIRS("_aerogel_stairs"),
        WALL("_aerogel_wall");

        private final String suffix;

        Shape(String suffix) {
            this.suffix = suffix;
        }

        public String name(DyeColor color) {
            return color.getSerializedName() + suffix;
        }
    }

    private static final Map<Shape, Map<DyeColor, RegistryEntry<Block>>> BLOCKS = new EnumMap<>(Shape.class);
    private static final Map<Shape, Map<DyeColor, RegistryEntry<BlockItem>>> ITEMS = new EnumMap<>(Shape.class);

    static {
        for (Shape shape : Shape.values()) {
            Map<DyeColor, RegistryEntry<Block>> blocks = new EnumMap<>(DyeColor.class);
            Map<DyeColor, RegistryEntry<BlockItem>> items = new EnumMap<>(DyeColor.class);
            for (DyeColor color : DyeColor.values()) {
                RegistryEntry<Block> block = ModRegistry.register(Registries.BLOCK, shape.name(color), key -> switch (shape) {
                    case BLOCK -> new TransparentBlock(ModBlocks.aerogel(key).mapColor(color.getMapColor())) {
                    };
                    case PANE -> new IronBarsBlock(ModBlocks.aerogel(key).mapColor(color.getMapColor())) {
                    };
                    case SLAB -> new SlabBlock(ModBlocks.aerogel(key).mapColor(color.getMapColor()));
                    case STAIRS -> new StairBlock(BLOCKS.get(Shape.BLOCK).get(color).get().defaultBlockState(),
                        ModBlocks.aerogel(key).mapColor(color.getMapColor())) {
                    };
                    case WALL -> new WallBlock(ModBlocks.aerogel(key).mapColor(color.getMapColor()));
                });
                blocks.put(color, block);
                items.put(color, ModRegistry.register(Registries.ITEM, shape.name(color),
                    key -> new BlockItem(block.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix())));
            }
            BLOCKS.put(shape, blocks);
            ITEMS.put(shape, items);
        }
    }

    private AerogelBlocks() {
    }

    public static void init() {
    }

    public static Item item(Shape shape, DyeColor color) {
        return ITEMS.get(shape).get(color).get();
    }

    public static List<Block> all(DyeColor color) {
        List<Block> blocks = new ArrayList<>();
        for (Shape shape : Shape.values()) {
            blocks.add(BLOCKS.get(shape).get(color).get());
        }
        return blocks;
    }
}
