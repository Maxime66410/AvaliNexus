/**
 * File: AvaliNexusClient.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client;

import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.network.packet.PongPayload;

public final class AvaliNexusClient {

    private AvaliNexusClient() {
    }

    public static void init() {
        ModNetworking.setClientHandler(PingPayload.TYPE, ping -> ModNetworking.sendToServer(new PongPayload(ping.sentAt())));
    }
}
