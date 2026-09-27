/**
 * File: ModPackets.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.avalinexus.dialogue.DialogueManager;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueChoicePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.network.packet.PongPayload;

public final class ModPackets {

    private ModPackets() {
    }

    public static void init() {
        ModNetworking.clientbound(PingPayload.TYPE, PingPayload.STREAM_CODEC);
        ModNetworking.serverbound(PongPayload.TYPE, PongPayload.STREAM_CODEC, ModPackets::onPong);

        ModNetworking.clientbound(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC);
        ModNetworking.clientbound(DialogueLinePayload.TYPE, DialogueLinePayload.STREAM_CODEC);
        ModNetworking.clientbound(CloseDialoguePayload.TYPE, CloseDialoguePayload.STREAM_CODEC);
        ModNetworking.serverbound(DialogueChoicePayload.TYPE, DialogueChoicePayload.STREAM_CODEC,
            (payload, player) -> DialogueManager.onChoice(player, payload.entityId(), payload.choice()));
    }

    private static void onPong(PongPayload payload, ServerPlayer player) {
        long roundTrip = System.currentTimeMillis() - payload.sentAt();
        player.sendSystemMessage(Component.translatable("avalinexus.ping.result", roundTrip));
    }
}
