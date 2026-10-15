/**
 * File: CryoFreezerScreen.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.client.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerMenu;

public class CryoFreezerScreen extends MachineScreen<CryoFreezerMenu> {

    public CryoFreezerScreen(CryoFreezerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected int barLeft() {
        return CryoFreezerMenu.INPUT_X + 24;
    }

    @Override
    protected int barRight() {
        return CryoFreezerMenu.OUTPUT_X - 8;
    }

    @Override
    protected int barY() {
        return CryoFreezerMenu.ITEM_Y + 4;
    }
}
