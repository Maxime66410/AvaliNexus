/**
 * File: AvaliReiClientPlugin.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.level.block.Block;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.client.machine.AmmoniaRefineryScreen;
import org.furranystudio.avalinexus.client.machine.CryoFreezerScreen;
import org.furranystudio.avalinexus.client.machine.GraphenePressScreen;
import org.furranystudio.avalinexus.client.machine.WeaponWorkbenchScreen;
import org.furranystudio.avalinexus.compat.AvaliRecipes;

public class AvaliReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        category(registry, AvaliDisplay.Kind.REFINERY, ModBlocks.AMMONIA_REFINERY.get());
        category(registry, AvaliDisplay.Kind.FREEZER, ModBlocks.CRYO_FREEZER.get());
        category(registry, AvaliDisplay.Kind.PRESS, ModBlocks.GRAPHENE_PRESS.get());
        category(registry, AvaliDisplay.Kind.WORKBENCH, ModBlocks.WEAPON_WORKBENCH.get());
        category(registry, AvaliDisplay.Kind.LOOM, ModBlocks.AVALI_LOOM.get());
    }

    private static void category(CategoryRegistry registry, AvaliDisplay.Kind kind, Block block) {
        registry.add(new AvaliCategory(kind, block));
        registry.addWorkstations(kind.id, EntryStacks.of(block));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (AvaliDisplay display : AvaliDisplay.all()) {
            registry.add(display);
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(area(AvaliRecipes.REFINERY_BAR), AmmoniaRefineryScreen.class, AvaliDisplay.Kind.REFINERY.id);
        registry.registerContainerClickArea(area(AvaliRecipes.FREEZER_BAR), CryoFreezerScreen.class, AvaliDisplay.Kind.FREEZER.id);
        registry.registerContainerClickArea(area(AvaliRecipes.PRESS_BAR), GraphenePressScreen.class, AvaliDisplay.Kind.PRESS.id);
        registry.registerContainerClickArea(area(AvaliRecipes.WORKBENCH_BAR), WeaponWorkbenchScreen.class, AvaliDisplay.Kind.WORKBENCH.id);
    }

    private static Rectangle area(AvaliRecipes.Bar bar) {
        return new Rectangle(bar.left(), bar.y(), bar.width(), AvaliRecipes.BAR_HEIGHT);
    }
}
