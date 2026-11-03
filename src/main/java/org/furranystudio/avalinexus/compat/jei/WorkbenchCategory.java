/**
 * File: WorkbenchCategory.java
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
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.workbench.WeaponRecipe;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;

import java.util.List;

public class WorkbenchCategory extends AbstractRecipeCategory<WeaponRecipe> {

    public static final IRecipeType<WeaponRecipe> TYPE = IRecipeType.create(AvaliNexus.id("weapon_workbench"), WeaponRecipe.class);

    public WorkbenchCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.WEAPON_WORKBENCH.get().getName(), gui.createDrawableItemLike(ModItems.WEAPON_WORKBENCH.get()), 120, 62);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WeaponRecipe recipe, IFocusGroup focuses) {
        List<WeaponRecipe.Cost> costs = recipe.costs();
        for (int i = 0; i < costs.size(); i++) {
            WeaponRecipe.Cost cost = costs.get(i);
            builder.addInputSlot(1 + i * 18, 1).setStandardSlotBackground().add(new ItemStack(cost.item().get(), cost.count()));
        }
        AvaliJeiPlugin.fluid(builder.addInputSlot(1, 45).setStandardSlotBackground(), ModFluids.source(AvaliFluidKind.FUEL), recipe.fuel());
        builder.addOutputSlot(95, 23).setOutputSlotBackground().add(recipe.output());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WeaponRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(recipe.duration()).setPosition(62, 22);
        builder.addText(AvaliRecipes.seconds(recipe.duration()), 30, 10).setPosition(62, 42);
    }
}
