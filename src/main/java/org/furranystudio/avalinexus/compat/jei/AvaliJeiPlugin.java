/**
 * File: AvaliJeiPlugin.java
 * Author: Maxime66410
 * Created: 2026-11-01
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.workbench.WeaponRecipe;
import org.furranystudio.avalinexus.client.machine.AmmoniaRefineryScreen;
import org.furranystudio.avalinexus.client.machine.CryoFreezerScreen;
import org.furranystudio.avalinexus.client.machine.GraphenePressScreen;
import org.furranystudio.avalinexus.client.machine.WeaponWorkbenchScreen;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.item.ModItems;

import java.util.List;

@JeiPlugin
public class AvaliJeiPlugin implements IModPlugin {

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
        registration.addRecipes(RefineryCategory.TYPE, AvaliRecipes.refining());
        registration.addRecipes(FreezerCategory.TYPE, AvaliRecipes.freezing());
        registration.addRecipes(PressCategory.TYPE, List.of(new PressCategory.Recipe()));
        registration.addRecipes(WorkbenchCategory.TYPE, WeaponRecipe.ALL);
        registration.addRecipes(LoomCategory.TYPE, AvaliRecipes.weaving());
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
        clickArea(registration, AmmoniaRefineryScreen.class, AvaliRecipes.REFINERY_BAR, RefineryCategory.TYPE);
        clickArea(registration, CryoFreezerScreen.class, AvaliRecipes.FREEZER_BAR, FreezerCategory.TYPE);
        clickArea(registration, GraphenePressScreen.class, AvaliRecipes.PRESS_BAR, PressCategory.TYPE);
        clickArea(registration, WeaponWorkbenchScreen.class, AvaliRecipes.WORKBENCH_BAR, WorkbenchCategory.TYPE);
    }

    private static void clickArea(IGuiHandlerRegistration registration, Class<? extends AbstractContainerScreen<?>> screen, AvaliRecipes.Bar bar, IRecipeType<?> type) {
        registration.addRecipeClickArea(screen, bar.left(), bar.y(), bar.width(), AvaliRecipes.BAR_HEIGHT, type);
    }

    static IRecipeSlotBuilder fluid(IRecipeSlotBuilder slot, Fluid fluid, int amount) {
        return slot.add(fluid, amount * bucketVolume / AvaliLiquids.BUCKET)
            .setFluidRenderer(bucketVolume * 2, false, 16, 16);
    }
}
