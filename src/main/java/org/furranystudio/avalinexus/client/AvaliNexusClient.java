/**
 * File: AvaliNexusClient.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client;

import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.network.packet.PongPayload;

public final class AvaliNexusClient {

    private AvaliNexusClient() {
    }

    public static void init() {
        ModNetworking.setClientHandler(PingPayload.TYPE, ping -> ModNetworking.sendToServer(new PongPayload(ping.sentAt())));
        ModNetworking.setClientHandler(OpenDialoguePayload.TYPE, open -> ClientDialogue.open(open.entityId(), open.line()));
        ModNetworking.setClientHandler(DialogueLinePayload.TYPE, line -> ClientDialogue.showLine(line.entityId(), line.line(), line.closing()));
        ModNetworking.setClientHandler(CloseDialoguePayload.TYPE, close -> ClientDialogue.close(close.entityId()));
    }
}
