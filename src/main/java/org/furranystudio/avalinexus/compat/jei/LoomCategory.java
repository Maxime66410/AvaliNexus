/**
 * File: LoomCategory.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.item.ModItems;

public class LoomCategory extends AbstractRecipeCategory<AvaliRecipes.Weaving> {

    public static final IRecipeType<AvaliRecipes.Weaving> TYPE = IRecipeType.create(AvaliNexus.id("avali_loom"), AvaliRecipes.Weaving.class);

    public LoomCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.AVALI_LOOM.get().getName(), gui.createDrawableItemLike(ModItems.AVALI_LOOM.get()), 120, 62);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AvaliRecipes.Weaving recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder canvas = builder.addInputSlot(1, 1).setStandardSlotBackground().addItemStacks(recipe.canvas());
        if (recipe.fibre()) {
            builder.addInputSlot(1, 23).setStandardSlotBackground().add(new ItemStack(ModItems.NANOFIBRE.get()));
        }
        IRecipeSlotBuilder dye = builder.addInputSlot(1, 45).setStandardSlotBackground().addItemStacks(recipe.dyes());
        IRecipeSlotBuilder output = builder.addOutputSlot(95, 23).setOutputSlotBackground().addItemStacks(recipe.outputs());
        builder.createFocusLink(canvas, dye, output);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AvaliRecipes.Weaving recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(55, 22);
    }
}
