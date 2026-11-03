/**
 * File: RefineryCategory.java
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
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryBlockEntity;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;

public class RefineryCategory extends AbstractRecipeCategory<AvaliRecipes.Refining> {

    public static final IRecipeType<AvaliRecipes.Refining> TYPE = IRecipeType.create(AvaliNexus.id("ammonia_refinery"), AvaliRecipes.Refining.class);

    public RefineryCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.AMMONIA_REFINERY.get().getName(), gui.createDrawableItemLike(ModItems.AMMONIA_REFINERY.get()), 120, 62);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AvaliRecipes.Refining recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 1).setStandardSlotBackground().addItemStacks(recipe.ingredient());
        AvaliJeiPlugin.fluid(builder.addInputSlot(1, 23).setStandardSlotBackground(), ModFluids.source(AvaliFluidKind.AMMONIA), AvaliLiquids.BUCKET);
        AvaliJeiPlugin.fluid(builder.addInputSlot(1, 45).setStandardSlotBackground(), ModFluids.source(AvaliFluidKind.FUEL), AmmoniaRefineryBlockEntity.FUEL_COST)
            .addItemStacks(AvaliRecipes.coals(1));
        AvaliJeiPlugin.fluid(builder.addOutputSlot(95, 23).setOutputSlotBackground(), recipe.product(), recipe.amount());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AvaliRecipes.Refining recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlameWidget(AmmoniaRefineryBlockEntity.DURATION).setPosition(22, 45);
        builder.addAnimatedRecipeArrowWidget(AmmoniaRefineryBlockEntity.DURATION).setPosition(55, 22);
        builder.addText(AvaliRecipes.seconds(AmmoniaRefineryBlockEntity.DURATION), 40, 10).setPosition(55, 42);
    }
}
