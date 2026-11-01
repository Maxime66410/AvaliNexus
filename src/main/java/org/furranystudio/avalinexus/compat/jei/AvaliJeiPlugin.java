/**
 * File: AvaliJeiPlugin.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-01
 */
package org.furranystudio.avalinexus.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerMenu;
import org.furranystudio.avalinexus.block.press.GraphenePressMenu;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryMenu;
import org.furranystudio.avalinexus.block.workbench.WeaponRecipe;
import org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchMenu;
import org.furranystudio.avalinexus.client.machine.AmmoniaRefineryScreen;
import org.furranystudio.avalinexus.client.machine.CryoFreezerScreen;
import org.furranystudio.avalinexus.client.machine.GraphenePressScreen;
import org.furranystudio.avalinexus.client.machine.WeaponWorkbenchScreen;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.item.ModItems;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AvaliJeiPlugin implements IModPlugin {

    private static final int BAR_HEIGHT = 8;

    private static long bucketVolume = AvaliLiquids.BUCKET;

    @Override
    public Identifier getPluginUid() {
        return AvaliNexus.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        bucketVolume = registration.getJeiHelpers().getPlatformFluidHelper().bucketVolume();
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new RefineryCategory(gui), new FreezerCategory(gui), new PressCategory(gui),
            new WorkbenchCategory(gui), new LoomCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RefineryCategory.TYPE, RefineryCategory.recipes());
        registration.addRecipes(FreezerCategory.TYPE, FreezerCategory.recipes());
        registration.addRecipes(PressCategory.TYPE, List.of(new PressCategory.Recipe()));
        registration.addRecipes(WorkbenchCategory.TYPE, WeaponRecipe.ALL);
        registration.addRecipes(LoomCategory.TYPE, LoomCategory.recipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RefineryCategory.TYPE, ModItems.AMMONIA_REFINERY.get());
        registration.addCraftingStation(FreezerCategory.TYPE, ModItems.CRYO_FREEZER.get());
        registration.addCraftingStation(PressCategory.TYPE, ModItems.GRAPHENE_PRESS.get());
        registration.addCraftingStation(WorkbenchCategory.TYPE, ModItems.WEAPON_WORKBENCH.get());
        registration.addCraftingStation(LoomCategory.TYPE, ModItems.AVALI_LOOM.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AmmoniaRefineryScreen.class, AmmoniaRefineryMenu.INGREDIENT_X + 24, AmmoniaRefineryMenu.INGREDIENT_Y + 4,
            AmmoniaRefineryMenu.OUTPUT_X - 8 - (AmmoniaRefineryMenu.INGREDIENT_X + 24), BAR_HEIGHT, RefineryCategory.TYPE);
        registration.addRecipeClickArea(CryoFreezerScreen.class, CryoFreezerMenu.INPUT_X + 24, CryoFreezerMenu.ITEM_Y + 4,
            CryoFreezerMenu.OUTPUT_X - 8 - (CryoFreezerMenu.INPUT_X + 24), BAR_HEIGHT, FreezerCategory.TYPE);
        registration.addRecipeClickArea(GraphenePressScreen.class, GraphenePressMenu.INGREDIENT_X + 24, GraphenePressMenu.OUTPUT_Y + 4,
            GraphenePressMenu.OUTPUT_X - 8 - (GraphenePressMenu.INGREDIENT_X + 24), BAR_HEIGHT, PressCategory.TYPE);
        registration.addRecipeClickArea(WeaponWorkbenchScreen.class, WeaponWorkbenchMenu.GRID_X, WeaponWorkbenchMenu.BAR_Y,
            WeaponWorkbenchMenu.OUTPUT_X + 16 - WeaponWorkbenchMenu.GRID_X, BAR_HEIGHT, WorkbenchCategory.TYPE);
    }

    static IRecipeSlotBuilder fluid(IRecipeSlotBuilder slot, Fluid fluid, int amount) {
        return slot.add(fluid, amount * bucketVolume / AvaliLiquids.BUCKET)
            .setFluidRenderer(bucketVolume * 2, false, 16, 16);
    }

    static List<ItemStack> coals(int count) {
        List<ItemStack> coals = new ArrayList<>();
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.COALS)) {
            coals.add(new ItemStack(item, count));
        }
        return coals;
    }

    static Component seconds(int ticks) {
        return Component.translatable("gui.avalinexus.jei.time", ticks / 20);
    }
}
