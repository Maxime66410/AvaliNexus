/**
 * File: PressCategory.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-01
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
import org.furranystudio.avalinexus.block.press.GraphenePressBlockEntity;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;

public class PressCategory extends AbstractRecipeCategory<PressCategory.Recipe> {

    public static final IRecipeType<Recipe> TYPE = IRecipeType.create(AvaliNexus.id("graphene_press"), Recipe.class);

    public record Recipe() {
    }

    public PressCategory(IGuiHelper gui) {
        super(TYPE, ModBlocks.GRAPHENE_PRESS.get().getName(), gui.createDrawableItemLike(ModItems.GRAPHENE_PRESS.get()), 120, 62);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 1).setStandardSlotBackground().add(new ItemStack(ModItems.NEXITE_SHARD.get()));
        builder.addInputSlot(1, 23).setStandardSlotBackground().addItemStacks(AvaliJeiPlugin.coals(GraphenePressBlockEntity.COAL_COST));
        AvaliJeiPlugin.fluid(builder.addInputSlot(1, 45).setStandardSlotBackground(), ModFluids.source(AvaliFluidKind.FUEL), GraphenePressBlockEntity.FUEL_COST);
        builder.addOutputSlot(95, 23).setOutputSlotBackground().add(new ItemStack(ModItems.GRAPHENE.get(), GraphenePressBlockEntity.GRAPHENE_COUNT));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(GraphenePressBlockEntity.DURATION).setPosition(55, 22);
        builder.addText(AvaliJeiPlugin.seconds(GraphenePressBlockEntity.DURATION), 40, 10).setPosition(55, 42);
    }
}
