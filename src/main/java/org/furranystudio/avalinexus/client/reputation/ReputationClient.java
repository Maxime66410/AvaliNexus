/**
 * File: ReputationClient.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.client.reputation;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.furranystudio.avalinexus.client.ModKeyCategory;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.RequestReputationPayload;

public final class ReputationClient {

    public static final KeyMapping OPEN = new KeyMapping("key.avalinexus.reputation", InputConstants.KEY_J, ModKeyCategory.AVALI_NEXUS);

    private ReputationClient() {
    }

    public static void tick() {
        while (OPEN.consumeClick()) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().gui.screen() == null) {
                request();
            }
        }
    }

    public static void request() {
        ModNetworking.sendToServer(RequestReputationPayload.INSTANCE);
    }
}
