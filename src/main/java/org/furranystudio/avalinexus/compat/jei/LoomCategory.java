/**
 * File: LoomCategory.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-01
 */
package org.furranystudio.avalinexus.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.block.loom.AvaliLoomMenu;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.item.TapestryItems;

import java.util.ArrayList;
import java.util.List;

public class LoomCategory extends AbstractRecipeCategory<LoomCategory.Recipe> {

    public static final IRecipeType<Recipe> TYPE = IRecipeType.create(AvaliNexus.id("avali_loom"), Recipe.class);

    public record Recipe(List<ItemStack> canvas, boolean fibre, List<ItemStack> dyes, List<ItemStack> outputs) {
    }

    public LoomCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.AVALI_LOOM.get().getName(), gui.createDrawableItemLike(ModItems.AVALI_LOOM.get()), 120, 62);
    }

    public static List<Recipe> recipes() {
        List<ItemStack> dyes = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            dyes.add(dye(color));
        }
        List<Recipe> recipes = new ArrayList<>();
        for (TapestryPattern pattern : TapestryPattern.values()) {
            List<ItemStack> canvas = new ArrayList<>();
            List<ItemStack> woven = new ArrayList<>();
            for (DyeColor color : DyeColor.values()) {
                canvas.add(new ItemStack(NanocanvasBlocks.item(NanocanvasBlocks.Shape.BLOCK, color)));
                woven.add(new ItemStack(TapestryItems.get(pattern, color), AvaliLoomMenu.CANVAS_YIELD));
            }
            recipes.add(new Recipe(canvas, true, dyes, woven));
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
                recipes.add(new Recipe(tapestries, false, dyes, rewoven));
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder canvas = builder.addInputSlot(1, 1).setStandardSlotBackground().addItemStacks(recipe.canvas());
        if (recipe.fibre()) {
            builder.addInputSlot(1, 23).setStandardSlotBackground().add(new ItemStack(ModItems.NANOFIBRE.get()));
        }
        IRecipeSlotBuilder dye = builder.addInputSlot(1, 45).setStandardSlotBackground().addItemStacks(recipe.dyes());
        IRecipeSlotBuilder output = builder.addOutputSlot(95, 23).setOutputSlotBackground().addItemStacks(recipe.outputs());
        builder.createFocusLink(canvas, dye, output);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(55, 22);
    }
}
