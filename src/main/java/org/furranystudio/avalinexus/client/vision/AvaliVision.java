/**
 * File: AvaliVision.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.client.vision;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.client.ModKeyCategory;

public final class AvaliVision {

    public static final Identifier EFFECT = AvaliNexus.id("avali_vision");
    public static final KeyMapping TOGGLE = new KeyMapping("key.avalinexus.avali_vision", InputConstants.UNKNOWN.getValue(), ModKeyCategory.AVALI_NEXUS);
    private static final Identifier PARAMS = AvaliNexus.id("textures/effect/avali_vision.png");
    private static final int CHUNK = 16;
    private static final int DEPTH_FAR_PER_BLOCK = 4;
    private static final int MAX_ENCODED = 0xFFFF;

    private static DynamicTexture params;
    private static int uploadedDistance = -1;
    private static int uploadedFar = -1;
    private static int uploadedStrength = -1;

    private AvaliVision() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                boolean enabled = !Config.AVALI_VISION.get();
                Config.AVALI_VISION.set(enabled);
                minecraft.player.sendOverlayMessage(Component.translatable(enabled ? "message.avalinexus.avali_vision.on" : "message.avalinexus.avali_vision.off"));
            }
        }
    }

    public static boolean prepare() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!Config.AVALI_VISION.get() || minecraft.level == null) {
            return false;
        }
        if (params == null) {
            params = new DynamicTexture(() -> "Avali vision params", 1, 1, true);
            minecraft.getTextureManager().register(PARAMS, params);
        }
        int distance = Config.AVALI_VISION_DISTANCE.get();
        int strength = Config.AVALI_VISION_STRENGTH.get();
        int far = Math.min(MAX_ENCODED, Math.max(minecraft.options.getEffectiveRenderDistance() * CHUNK * DEPTH_FAR_PER_BLOCK,
            minecraft.options.cloudRange().get() * CHUNK));
        if (distance != uploadedDistance || far != uploadedFar || strength != uploadedStrength) {
            params.getPixels().setPixel(0, 0, ARGB.color(strength, distance, far >> 8, far & 0xFF));
            params.upload();
            uploadedDistance = distance;
            uploadedFar = far;
            uploadedStrength = strength;
        }
        return true;
    }
}
