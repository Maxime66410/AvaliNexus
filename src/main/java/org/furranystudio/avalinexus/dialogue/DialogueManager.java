/**
 * File: DialogueManager.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.entity.avali.AvaliCompanions;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.CompanionData;
import org.furranystudio.avalinexus.entity.avali.CompanionRole;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.trade.ShopManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DialogueManager {

    public static final double MAX_DISTANCE = 6.0;
    private static final int GESTURE_TICKS = 60;
    private static final long REOPEN_COOLDOWN_TICKS = 20;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> CLOSED_AT = new HashMap<>();

    private enum Menu {
        MAIN, ORDERS, ROLE
    }

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
        Session session = new Session(avali, line);
        SESSIONS.put(player.getUUID(), session);
        say(avali, line);
        List<DialogueChoice> choices = choices(player, session);
        ModNetworking.sendToPlayer(player, new OpenDialoguePayload(avali.getId(), line.text(), ordinals(choices), highlight(avali, session)));
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
        if (!choices(player, session).contains(choice)) {
            return;
        }
        String section = switch (choice) {
            case TALK -> "talk";
            case TRADE -> "trade";
            case LEAVE -> "farewell";
            case RECRUIT -> recruit(player, avali) ? "recruit" : null;
            case FOLLOW, WAIT -> {
                avali.setWaiting(choice == DialogueChoice.WAIT);
                yield "order";
            }
            case DISMISS -> {
                avali.dismiss();
                yield "dismiss";
            }
            case FIGHTER, GATHERER, HEALER -> {
                avali.setRole(role(choice));
                yield "role";
            }
            case ORDERS, ROLE, BACK -> null;
        };
        session.menu = switch (choice) {
            case ORDERS -> Menu.ORDERS;
            case ROLE -> Menu.ROLE;
            case BACK, RECRUIT -> Menu.MAIN;
            default -> session.menu;
        };

        DialogueLine line = session.lastLine;
        if (section != null) {
            line = DialogueData.pick(player.level().getServer(), section, avali, session.lastLine);
            session.lastLine = line;
            say(avali, line);
        }

        boolean leaving = choice == DialogueChoice.LEAVE || choice == DialogueChoice.DISMISS;
        List<DialogueChoice> choices = leaving ? List.of() : choices(player, session);
        ModNetworking.sendToPlayer(player, new DialogueLinePayload(entityId, line.text(), leaving, ordinals(choices), highlight(avali, session)));
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
        if (session == null || session.avali.getId() != entityId || !session.avali.isAlive() || session.avali.isBaby()
                || session.avali.isOwnedBy(player) || session.avali.distanceTo(player) > MAX_DISTANCE) {
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

    private static List<DialogueChoice> choices(ServerPlayer player, Session session) {
        AvaliEntity avali = session.avali;
        List<DialogueChoice> choices = new ArrayList<>();
        if (avali.isOwnedBy(player)) {
            switch (session.menu) {
                case ORDERS -> {
                    choices.add(avali.isWaiting() ? DialogueChoice.FOLLOW : DialogueChoice.WAIT);
                    choices.add(DialogueChoice.DISMISS);
                    choices.add(DialogueChoice.BACK);
                }
                case ROLE -> {
                    choices.add(DialogueChoice.FIGHTER);
                    choices.add(DialogueChoice.GATHERER);
                    choices.add(DialogueChoice.HEALER);
                    choices.add(DialogueChoice.BACK);
                }
                default -> {
                    choices.add(DialogueChoice.ORDERS);
                    choices.add(DialogueChoice.ROLE);
                    choices.add(DialogueChoice.LEAVE);
                }
            }
            return choices;
        }
        choices.add(DialogueChoice.TALK);
        if (!avali.isBaby()) {
            choices.add(DialogueChoice.TRADE);
        }
        if (canRecruit(player, avali)) {
            choices.add(DialogueChoice.RECRUIT);
        }
        choices.add(DialogueChoice.LEAVE);
        return choices;
    }

    private static int highlight(AvaliEntity avali, Session session) {
        if (session.menu != Menu.ROLE) {
            return -1;
        }
        return switch (avali.getRole()) {
            case FIGHTER -> DialogueChoice.FIGHTER.ordinal();
            case GATHERER -> DialogueChoice.GATHERER.ordinal();
            case HEALER -> DialogueChoice.HEALER.ordinal();
        };
    }

    private static CompanionRole role(DialogueChoice choice) {
        return switch (choice) {
            case GATHERER -> CompanionRole.GATHERER;
            case HEALER -> CompanionRole.HEALER;
            default -> CompanionRole.FIGHTER;
        };
    }

    private static boolean canRecruit(ServerPlayer player, AvaliEntity avali) {
        AvaliCompanions.Data data = AvaliCompanions.get(player.level().getServer());
        return !avali.isBaby() && !avali.isCompanion() && !avali.isBusy()
            && CompanionData.get(player.level().getServer()).count(player.getUUID()) < data.maxCompanions()
            && (player.hasInfiniteMaterials() || count(player.getInventory(), data) >= data.priceCount());
    }

    private static boolean recruit(ServerPlayer player, AvaliEntity avali) {
        if (!canRecruit(player, avali)) {
            return false;
        }
        AvaliCompanions.Data data = AvaliCompanions.get(player.level().getServer());
        if (!player.hasInfiniteMaterials()) {
            int left = data.priceCount();
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.is(data.priceItem())) {
                    int taken = Math.min(left, stack.getCount());
                    stack.shrink(taken);
                    left -= taken;
                }
            }
        }
        avali.recruit(player);
        return true;
    }

    private static int count(Inventory inventory, AvaliCompanions.Data data) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(data.priceItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<Integer> ordinals(List<DialogueChoice> choices) {
        return choices.stream().map(Enum::ordinal).toList();
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
        private Menu menu = Menu.MAIN;

        private Session(AvaliEntity avali, DialogueLine lastLine) {
            this.avali = avali;
            this.lastLine = lastLine;
        }
    }
}
