/**
 * File: RailGunClient.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.client.weapon;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;
import org.furranystudio.avalinexus.item.weapon.RailStats;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.RailFirePayload;
import org.furranystudio.avalinexus.network.packet.RailReloadPayload;

// FPS controls: left click fires instead of hitting, holding right click aims, R reloads
public final class RailGunClient {

    public static final KeyMapping RELOAD = new KeyMapping("key.avalinexus.reload", InputConstants.KEY_R, KeyMapping.Category.GAMEPLAY);

    private static int nextShot;

    private RailGunClient() {
    }

    // Runs before vanilla reads the keys, so the attack clicks never reach it while a gun is held
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null || !(player.getMainHandItem().getItem() instanceof RailGunItem gun)) {
            return;
        }
        int clicks = 0;
        while (minecraft.options.keyAttack.consumeClick()) {
            clicks++;
        }
        while (RELOAD.consumeClick()) {
            ModNetworking.sendToServer(RailReloadPayload.INSTANCE);
        }
        RailStats stats = gun.stats();
        boolean trigger = clicks > 0 || stats.automatic() && minecraft.options.keyAttack.isDown();
        if (!trigger) {
            return;
        }
        // A click while loading shells asks the server to stop after the current one
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

    // True while a gun is in the main hand, then left click must not mine or hit
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

    public static ItemStack heldGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof RailGunItem ? stack : ItemStack.EMPTY;
    }
}
