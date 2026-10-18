/**
 * File: RailGunClient.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.client.weapon;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.client.ModKeyCategory;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;
import org.furranystudio.avalinexus.item.weapon.RailStats;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.RailEjectPayload;
import org.furranystudio.avalinexus.network.packet.RailFirePayload;
import org.furranystudio.avalinexus.network.packet.RailReloadPayload;

public final class RailGunClient {

    public static final KeyMapping RELOAD = new KeyMapping("key.avalinexus.reload", InputConstants.KEY_R, ModKeyCategory.AVALI_NEXUS);

    private static final int EJECT_HOLD_TICKS = 10;

    private static int nextShot;
    private static int reloadHeld = -1;
    private static boolean ejected;

    private RailGunClient() {
    }

    // Runs before vanilla reads the keys, so a held gun eats the attack clicks
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null || !(player.getMainHandItem().getItem() instanceof RailGunItem gun)) {
            reloadHeld = -1;
            return;
        }
        int clicks = 0;
        while (minecraft.options.keyAttack.consumeClick()) {
            clicks++;
        }
        handleReloadKey(gun);
        RailStats stats = gun.stats();
        boolean trigger = clicks > 0 || stats.automatic() && minecraft.options.keyAttack.isDown();
        if (!trigger) {
            return;
        }
        if (RailGunItem.isReloading(player.getMainHandItem())) {
            if (clicks > 0 && gun.shellByShell()) {
                ModNetworking.sendToServer(RailFirePayload.INSTANCE);
            }
            return;
        }
        if (player.tickCount < nextShot) {
            return;
        }
        nextShot = player.tickCount + stats.interval();
        ModNetworking.sendToServer(RailFirePayload.INSTANCE);
        if (RailGunItem.ammo(player.getMainHandItem()) > 0 || player.hasInfiniteMaterials()) {
            float recoil = RailGunItem.isAiming(player) ? stats.recoil() * 0.5F : stats.recoil();
            player.setXRot(player.getXRot() - recoil);
        }
    }

    // A tap can only be told from a hold once the key is released
    private static void handleReloadKey(RailGunItem gun) {
        int presses = 0;
        while (RELOAD.consumeClick()) {
            presses++;
        }
        if (RELOAD.isDown()) {
            if (reloadHeld < 0) {
                reloadHeld = 0;
                ejected = false;
            } else if (++reloadHeld >= EJECT_HOLD_TICKS && !ejected) {
                ejected = true;
                if (gun.usesMagazines()) {
                    ModNetworking.sendToServer(RailEjectPayload.INSTANCE);
                }
            }
            return;
        }
        if ((reloadHeld >= 0 || presses > 0) && !ejected) {
            ModNetworking.sendToServer(RailReloadPayload.INSTANCE);
        }
        reloadHeld = -1;
        ejected = false;
    }

    public static boolean blocksAttack() {
        Player player = Minecraft.getInstance().player;
        return player != null && player.getMainHandItem().getItem() instanceof RailGunItem;
    }

    public static float fovModifier(Player player, float modifier) {
        if (RailGunItem.isAiming(player) && player.getUseItem().getItem() instanceof RailGunItem gun) {
            return modifier * gun.stats().zoom();
        }
        return modifier;
    }

    public static double aimSensitivity() {
        Player player = Minecraft.getInstance().player;
        if (player != null && RailGunItem.isAiming(player) && player.getUseItem().getItem() instanceof RailGunItem gun) {
            return gun.stats().zoom();
        }
        return 1.0;
    }

    public static ItemStack heldGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof RailGunItem ? stack : ItemStack.EMPTY;
    }
}
