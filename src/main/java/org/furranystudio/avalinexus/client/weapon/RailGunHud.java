/**
 * File: RailGunHud.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.client.weapon;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;
import org.furranystudio.avalinexus.item.weapon.RailWeapons;

// Ammo panel in the bottom right corner: magazine, spare Nexite quills and the reload bar
public final class RailGunHud {

    private static final int WIDTH = 86;
    private static final int HEIGHT = 34;
    private static final int MARGIN = 8;
    private static final int PADDING = 6;
    private static final int BAR_HEIGHT = 3;

    private RailGunHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) {
            return;
        }
        ItemStack gun = RailGunClient.heldGun(player);
        if (gun.isEmpty()) {
            return;
        }
        RailGunItem item = (RailGunItem) gun.getItem();
        Font font = minecraft.font;
        int left = minecraft.getWindow().getGuiScaledWidth() - WIDTH - MARGIN;
        int top = minecraft.getWindow().getGuiScaledHeight() - HEIGHT - MARGIN;
        AvaliUi.panel(graphics, left, top, WIDTH, HEIGHT, AvaliUi.BACKDROP);

        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(gun.getHoverName()), left + PADDING, top + PADDING, AvaliUi.TEXT_SECONDARY);

        int ammo = RailGunItem.ammo(gun);
        int magazine = item.stats().magazine();
        int ammoColor = ammo == 0 ? AvaliUi.DANGER : AvaliUi.TEXT_PRIMARY;
        AvaliUi.shadowedText(graphics, font, Component.literal(ammo + " / " + magazine), left + PADDING, top + PADDING + 12, ammoColor);

        String spare = player.hasInfiniteMaterials() ? "∞" : String.valueOf(RailGunItem.countQuills(player.getInventory()));
        Component spareText = Component.literal(spare);
        AvaliUi.shadowedText(graphics, font, spareText, left + WIDTH - PADDING - font.width(spareText), top + PADDING + 12, AvaliUi.TEXT_DISABLED);

        if (RailGunItem.isReloading(gun)) {
            float progress = RailGunItem.reloadProgress(gun, player.level().getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false));
            int barLeft = left + PADDING;
            int barRight = left + WIDTH - PADDING;
            int barTop = top + HEIGHT - PADDING + 1 - BAR_HEIGHT;
            graphics.fill(barLeft, barTop, barRight, barTop + BAR_HEIGHT, AvaliUi.SECONDARY_BORDER);
            graphics.fill(barLeft, barTop, barLeft + Math.round(progress * (barRight - barLeft)), barTop + BAR_HEIGHT, AvaliUi.PRIMARY_ORANGE);
        }
    }
}
