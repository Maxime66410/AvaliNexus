/**
 * File: GraphenePressScreen.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.client.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.furranystudio.avalinexus.block.press.GraphenePressMenu;

public class GraphenePressScreen extends MachineScreen<GraphenePressMenu> {

    public GraphenePressScreen(GraphenePressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected int barLeft() {
        return GraphenePressMenu.INGREDIENT_X + 24;
    }

    @Override
    protected int barRight() {
        return GraphenePressMenu.OUTPUT_X - 8;
    }

    @Override
    protected int barY() {
        return GraphenePressMenu.OUTPUT_Y + 4;
    }
}
