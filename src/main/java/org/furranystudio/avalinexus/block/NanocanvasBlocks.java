/**
 * File: NanocanvasBlocks.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// One gray texture tinted with the dye color, every color gets its block, slab, stairs and carpet
public final class NanocanvasBlocks {

    public enum Shape {
        BLOCK(""),
        SLAB("_slab"),
        STAIRS("_stairs"),
        CARPET("_carpet");

        private final String suffix;

        Shape(String suffix) {
            this.suffix = suffix;
        }

        public String name(DyeColor color) {
            return color.getSerializedName() + "_nanocanvas" + suffix;
        }
    }

    private static final Map<Shape, Map<DyeColor, RegistryEntry<Block>>> BLOCKS = new EnumMap<>(Shape.class);
    private static final Map<Shape, Map<DyeColor, RegistryEntry<BlockItem>>> ITEMS = new EnumMap<>(Shape.class);

    static {
        for (Shape shape : Shape.values()) {
            Map<DyeColor, RegistryEntry<Block>> blocks = new EnumMap<>(DyeColor.class);
            Map<DyeColor, RegistryEntry<BlockItem>> items = new EnumMap<>(DyeColor.class);
            for (DyeColor color : DyeColor.values()) {
                RegistryEntry<Block> block = ModRegistry.register(Registries.BLOCK, shape.name(color), key -> create(shape, color, key));
                blocks.put(color, block);
                items.put(color, ModRegistry.register(Registries.ITEM, shape.name(color),
                    key -> new BlockItem(block.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix())));
            }
            BLOCKS.put(shape, blocks);
            ITEMS.put(shape, items);
        }
    }

    private NanocanvasBlocks() {
    }

    public static void init() {
    }

    public static Block get(Shape shape, DyeColor color) {
        return BLOCKS.get(shape).get(color).get();
    }

    public static Item item(Shape shape, DyeColor color) {
        return ITEMS.get(shape).get(color).get();
    }

    public static List<Block> all(DyeColor color) {
        List<Block> blocks = new ArrayList<>();
        for (Shape shape : Shape.values()) {
            blocks.add(get(shape, color));
        }
        return blocks;
    }

    private static Block create(Shape shape, DyeColor color, ResourceKey<Block> key) {
        return switch (shape) {
            case BLOCK -> new Block(wool(color, key));
            case SLAB -> new SlabBlock(wool(color, key));
            // StairBlock keeps its constructor protected
            case STAIRS -> new StairBlock(get(Shape.BLOCK, color).defaultBlockState(), wool(color, key)) {
            };
            case CARPET -> new CarpetBlock(BlockBehaviour.Properties.of()
                .setId(key)
                .mapColor(color.getMapColor())
                .strength(0.1F, 15.0F)
                .sound(SoundType.WOOL));
        };
    }

    // Wool block settings, the graphene makes it tougher, creeper proof and fireproof
    private static BlockBehaviour.Properties wool(DyeColor color, ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(color.getMapColor())
            .instrument(NoteBlockInstrument.GUITAR)
            .strength(1.3F, 15.0F)
            .sound(SoundType.WOOL);
    }
}
