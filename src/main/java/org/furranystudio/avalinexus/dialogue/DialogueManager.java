/**
 * File: DialogueManager.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.trade.ShopManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DialogueManager {

    public static final double MAX_DISTANCE = 6.0;
    private static final int GESTURE_TICKS = 60;
    private static final long REOPEN_COOLDOWN_TICKS = 20;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> CLOSED_AT = new HashMap<>();

    private DialogueManager() {
    }

    public static void open(ServerPlayer player, AvaliEntity avali) {
        // Stops the click used to pick "Leave" from opening the dialogue again right away
        Long closedAt = CLOSED_AT.get(player.getUUID());
        if (closedAt != null && player.level().getGameTime() - closedAt < REOPEN_COOLDOWN_TICKS) {
            return;
        }
        ServerPlayer current = avali.getTalkingTo();
        if (current != null && current != player) {
            player.sendSystemMessage(Component.translatable("avalinexus.dialogue.busy"), true);
            return;
        }
        Session previous = SESSIONS.get(player.getUUID());
        if (previous != null && previous.avali == avali) {
            return;
        }
        if (previous != null) {
            close(player, true);
        }

        avali.startTalking(player);
        DialogueLine line = DialogueData.pick(player.level().getServer(), "greeting", avali, null);
        SESSIONS.put(player.getUUID(), new Session(avali, line));
        say(avali, line);
        ModNetworking.sendToPlayer(player, new OpenDialoguePayload(avali.getId(), line.text()));
    }

    public static void onChoice(ServerPlayer player, int entityId, int choiceIndex) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.avali.getId() != entityId || choiceIndex < 0 || choiceIndex >= DialogueChoice.values().length) {
            return;
        }
        AvaliEntity avali = session.avali;
        if (!avali.isAlive() || avali.distanceTo(player) > MAX_DISTANCE) {
            close(player, true);
            return;
        }

        DialogueChoice choice = DialogueChoice.values()[choiceIndex];
        String section = switch (choice) {
            case TALK -> "talk";
            case TRADE -> "trade";
            case LEAVE -> "farewell";
        };
        DialogueLine line = DialogueData.pick(player.level().getServer(), section, avali, session.lastLine);
        session.lastLine = line;
        say(avali, line);

        boolean leaving = choice == DialogueChoice.LEAVE;
        ModNetworking.sendToPlayer(player, new DialogueLinePayload(entityId, line.text(), leaving));
        if (choice == DialogueChoice.TRADE) {
            ShopManager.open(player, avali);
        }
        if (leaving) {
            SESSIONS.remove(player.getUUID());
            CLOSED_AT.put(player.getUUID(), player.level().getGameTime());
            avali.stopTalking();
        }
    }

    // Trades only go through while the player is still talking to that Avali, in range
    public static void onShopTrade(ServerPlayer player, int entityId, int offer) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.avali.getId() != entityId || !session.avali.isAlive()
                || session.avali.distanceTo(player) > MAX_DISTANCE) {
            return;
        }
        ShopManager.trade(player, session.avali, offer);
    }

    public static void close(ServerPlayer player, boolean notifyClient) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) {
            return;
        }
        session.avali.stopTalking();
        CLOSED_AT.put(player.getUUID(), player.level().getGameTime());
        if (notifyClient) {
            ModNetworking.sendToPlayer(player, new CloseDialoguePayload(session.avali.getId()));
        }
    }

    private static void say(AvaliEntity avali, DialogueLine line) {
        avali.setDialogueFace(line.face());
        if (line.gesture() != null) {
            avali.playGesture(line.gesture(), GESTURE_TICKS);
        }
        avali.playNoise();
    }

    private static final class Session {

        private final AvaliEntity avali;
        private DialogueLine lastLine;

        private Session(AvaliEntity avali, DialogueLine lastLine) {
            this.avali = avali;
            this.lastLine = lastLine;
        }
    }
}
