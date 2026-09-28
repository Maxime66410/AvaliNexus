/**
 * File: AvaliNexusClient.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client;

import net.minecraft.client.Minecraft;
import org.furranystudio.avalinexus.client.archive.ArchiveScreen;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.shop.AvaliShopScreen;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenArchivesPayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.network.packet.OpenShopPayload;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.network.packet.PongPayload;
import org.furranystudio.avalinexus.network.packet.ShopUpdatePayload;

public final class AvaliNexusClient {

    private AvaliNexusClient() {
    }

    public static void init() {
        ModNetworking.setClientHandler(PingPayload.TYPE, ping -> ModNetworking.sendToServer(new PongPayload(ping.sentAt())));
        ModNetworking.setClientHandler(OpenDialoguePayload.TYPE, open -> ClientDialogue.open(open.entityId(), open.line()));
        ModNetworking.setClientHandler(DialogueLinePayload.TYPE, line -> ClientDialogue.showLine(line.entityId(), line.line(), line.closing()));
        ModNetworking.setClientHandler(CloseDialoguePayload.TYPE, close -> {
            ClientDialogue.close(close.entityId());
            if (Minecraft.getInstance().gui.screen() instanceof AvaliShopScreen shop && shop.entityId() == close.entityId()) {
                shop.onClose();
            }
        });
        ModNetworking.setClientHandler(OpenShopPayload.TYPE, open ->
            Minecraft.getInstance().gui.setScreen(new AvaliShopScreen(open.entityId(), open.offers())));
        ModNetworking.setClientHandler(OpenArchivesPayload.TYPE, open ->
            Minecraft.getInstance().gui.setScreen(new ArchiveScreen(open.categories(), open.entries())));
        ModNetworking.setClientHandler(ShopUpdatePayload.TYPE, update -> {
            if (Minecraft.getInstance().gui.screen() instanceof AvaliShopScreen shop && shop.entityId() == update.entityId()) {
                shop.update(update.offer(), update.uses());
            }
        });
    }
}
