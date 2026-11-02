/**
 * File: AvaliRecipes.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerBlockEntity;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerMenu;
import org.furranystudio.avalinexus.block.loom.AvaliLoomMenu;
import org.furranystudio.avalinexus.block.press.GraphenePressMenu;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryBlockEntity;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryMenu;
import org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchMenu;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.item.TapestryItems;

import java.util.ArrayList;
import java.util.List;

public final class AvaliRecipes {

    public static final int BAR_HEIGHT = 8;
    public static final Bar REFINERY_BAR = new Bar(AmmoniaRefineryMenu.INGREDIENT_X + 24, AmmoniaRefineryMenu.INGREDIENT_Y + 4, AmmoniaRefineryMenu.OUTPUT_X - 8);
    public static final Bar FREEZER_BAR = new Bar(CryoFreezerMenu.INPUT_X + 24, CryoFreezerMenu.ITEM_Y + 4, CryoFreezerMenu.OUTPUT_X - 8);
    public static final Bar PRESS_BAR = new Bar(GraphenePressMenu.INGREDIENT_X + 24, GraphenePressMenu.OUTPUT_Y + 4, GraphenePressMenu.OUTPUT_X - 8);
    public static final Bar WORKBENCH_BAR = new Bar(WeaponWorkbenchMenu.GRID_X, WeaponWorkbenchMenu.BAR_Y, WeaponWorkbenchMenu.OUTPUT_X + 16);

    public record Bar(int left, int y, int right) {

        public int width() {
            return right - left;
        }
    }

    public record Refining(List<ItemStack> ingredient, Fluid product, int amount) {
    }

    public record Freezing(ItemStack input, Fluid liquid, ItemStack output) {
    }

    public record Weaving(List<ItemStack> canvas, boolean fibre, List<ItemStack> dyes, List<ItemStack> outputs) {
    }

    private AvaliRecipes() {
    }

    public static List<Refining> refining() {
        return List.of(
            new Refining(List.of(new ItemStack(ModItems.GRAPHENE.get())), ModFluids.source(AvaliFluidKind.COOLANT), AvaliLiquids.BUCKET),
            new Refining(coals(1), ModFluids.source(AvaliFluidKind.FUEL), AmmoniaRefineryBlockEntity.COAL_FUEL));
    }

    public static List<Freezing> freezing() {
        List<Freezing> recipes = new ArrayList<>();
        recipes.add(new Freezing(ItemStack.EMPTY, Fluids.WATER, new ItemStack(Items.ICE)));
        recipes.add(new Freezing(ItemStack.EMPTY, ModFluids.source(AvaliFluidKind.AMMONIA), new ItemStack(ModItems.AMMONIA_ICE.get())));
        recipes.add(new Freezing(new ItemStack(Items.BUCKET), Fluids.WATER, new ItemStack(Items.POWDER_SNOW_BUCKET)));
        for (ItemStack ice : List.of(new ItemStack(Items.ICE), new ItemStack(Items.PACKED_ICE), new ItemStack(ModItems.AMMONIA_ICE.get()))) {
            recipes.add(new Freezing(ice, null, new ItemStack(CryoFreezerBlockEntity.packed(ice))));
        }
        return recipes;
    }

    public static List<Weaving> weaving() {
        List<ItemStack> dyes = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            dyes.add(dye(color));
        }
        List<Weaving> recipes = new ArrayList<>();
        for (TapestryPattern pattern : TapestryPattern.values()) {
            List<ItemStack> canvas = new ArrayList<>();
            List<ItemStack> woven = new ArrayList<>();
            for (DyeColor color : DyeColor.values()) {
                canvas.add(new ItemStack(NanocanvasBlocks.item(NanocanvasBlocks.Shape.BLOCK, color)));
                woven.add(new ItemStack(TapestryItems.get(pattern, color), AvaliLoomMenu.CANVAS_YIELD));
            }
            recipes.add(new Weaving(canvas, true, dyes, woven));
            for (TapestryPattern other : TapestryPattern.values()) {
                if (other == pattern) {
                    continue;
                }
                List<ItemStack> tapestries = new ArrayList<>();
                List<ItemStack> rewoven = new ArrayList<>();
                for (DyeColor color : DyeColor.values()) {
                    tapestries.add(new ItemStack(TapestryItems.get(other, color)));
                    rewoven.add(new ItemStack(TapestryItems.get(pattern, color)));
                }
                recipes.add(new Weaving(tapestries, false, dyes, rewoven));
            }
        }
        return recipes;
    }

    private static ItemStack dye(DyeColor color) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.get(DataComponents.DYE) == color) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> coals(int count) {
        List<ItemStack> coals = new ArrayList<>();
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.COALS)) {
            coals.add(new ItemStack(item, count));
        }
        return coals;
    }

    public static Component seconds(int ticks) {
        return Component.translatable("gui.avalinexus.jei.time", ticks / 20);
    }
}
