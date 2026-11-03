/**
 * File: FreezerCategory.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerBlockEntity;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;

public class FreezerCategory extends AbstractRecipeCategory<AvaliRecipes.Freezing> {

    public static final IRecipeType<AvaliRecipes.Freezing> TYPE = IRecipeType.create(AvaliNexus.id("cryo_freezer"), AvaliRecipes.Freezing.class);

    public FreezerCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.CRYO_FREEZER.get().getName(), gui.createDrawableItemLike(ModItems.CRYO_FREEZER.get()), 120, 62);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AvaliRecipes.Freezing recipe, IFocusGroup focuses) {
        if (!recipe.input().isEmpty()) {
            builder.addInputSlot(1, 1).setStandardSlotBackground().add(recipe.input());
        }
        if (recipe.liquid() != null) {
            AvaliJeiPlugin.fluid(builder.addInputSlot(1, 23).setStandardSlotBackground(), recipe.liquid(), AvaliLiquids.BUCKET);
        }
        AvaliJeiPlugin.fluid(builder.addInputSlot(1, 45).setStandardSlotBackground(), ModFluids.source(AvaliFluidKind.COOLANT), CryoFreezerBlockEntity.COOLANT_COST);
        builder.addOutputSlot(95, 23).setOutputSlotBackground().add(recipe.output());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AvaliRecipes.Freezing recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(CryoFreezerBlockEntity.DURATION).setPosition(55, 22);
        builder.addText(AvaliRecipes.seconds(CryoFreezerBlockEntity.DURATION), 40, 10).setPosition(55, 42);
    }
}
