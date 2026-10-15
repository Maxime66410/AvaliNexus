/**
 * File: AmmoniaRefineryScreen.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.client.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryMenu;

public class AmmoniaRefineryScreen extends MachineScreen<AmmoniaRefineryMenu> {

    public AmmoniaRefineryScreen(AmmoniaRefineryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected int barLeft() {
        return AmmoniaRefineryMenu.INGREDIENT_X + 24;
    }

    @Override
    protected int barRight() {
        return AmmoniaRefineryMenu.OUTPUT_X - 8;
    }

    @Override
    protected int barY() {
        return AmmoniaRefineryMenu.INGREDIENT_Y + 4;
    }
}
