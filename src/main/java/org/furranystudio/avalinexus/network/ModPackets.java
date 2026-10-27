/**
 * File: ModPackets.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.dialogue.DialogueManager;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;
import org.furranystudio.avalinexus.network.packet.ChangeSettingPayload;
import org.furranystudio.avalinexus.network.packet.CloseDialoguePayload;
import org.furranystudio.avalinexus.network.packet.DialogueChoicePayload;
import org.furranystudio.avalinexus.network.packet.DialogueLinePayload;
import org.furranystudio.avalinexus.network.packet.OpenArchivesPayload;
import org.furranystudio.avalinexus.network.packet.OpenDialoguePayload;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.network.packet.OpenShopPayload;
import org.furranystudio.avalinexus.network.packet.PongPayload;
import org.furranystudio.avalinexus.network.packet.RailEjectPayload;
import org.furranystudio.avalinexus.network.packet.RailFirePayload;
import org.furranystudio.avalinexus.network.packet.RailReloadPayload;
import org.furranystudio.avalinexus.network.packet.ShopTradePayload;
import org.furranystudio.avalinexus.network.packet.ReputationPayload;
import org.furranystudio.avalinexus.network.packet.RequestReputationPayload;
import org.furranystudio.avalinexus.network.packet.RequestSettingsPayload;
import org.furranystudio.avalinexus.network.packet.SettingsPayload;
import org.furranystudio.avalinexus.network.packet.ShopUpdatePayload;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.settings.SettingsRegistry;

import java.util.HashMap;
import java.util.Map;

public final class ModPackets {

    private ModPackets() {
    }

    public static void init() {
        ModNetworking.clientbound(PingPayload.TYPE, PingPayload.STREAM_CODEC);
        ModNetworking.serverbound(PongPayload.TYPE, PongPayload.STREAM_CODEC, ModPackets::onPong);

        ModNetworking.serverbound(RequestSettingsPayload.TYPE, RequestSettingsPayload.STREAM_CODEC, (payload, player) -> sendSettings(player));
        ModNetworking.serverbound(ChangeSettingPayload.TYPE, ChangeSettingPayload.STREAM_CODEC, ModPackets::onChangeSetting);
        ModNetworking.clientbound(SettingsPayload.TYPE, SettingsPayload.STREAM_CODEC);

        ModNetworking.clientbound(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC);
        ModNetworking.clientbound(DialogueLinePayload.TYPE, DialogueLinePayload.STREAM_CODEC);
        ModNetworking.clientbound(CloseDialoguePayload.TYPE, CloseDialoguePayload.STREAM_CODEC);
        ModNetworking.serverbound(DialogueChoicePayload.TYPE, DialogueChoicePayload.STREAM_CODEC,
            (payload, player) -> DialogueManager.onChoice(player, payload.entityId(), payload.choice()));

        ModNetworking.clientbound(OpenShopPayload.TYPE, OpenShopPayload.STREAM_CODEC);
        ModNetworking.clientbound(ReputationPayload.TYPE, ReputationPayload.STREAM_CODEC);
        ModNetworking.serverbound(RequestReputationPayload.TYPE, RequestReputationPayload.STREAM_CODEC,
            (payload, player) -> AvaliReputation.sendScreen(player));
        ModNetworking.clientbound(OpenArchivesPayload.TYPE, OpenArchivesPayload.STREAM_CODEC);
        ModNetworking.clientbound(ShopUpdatePayload.TYPE, ShopUpdatePayload.STREAM_CODEC);
        ModNetworking.serverbound(ShopTradePayload.TYPE, ShopTradePayload.STREAM_CODEC,
            (payload, player) -> DialogueManager.onShopTrade(player, payload.entityId(), payload.offer()));

        ModNetworking.serverbound(RailFirePayload.TYPE, RailFirePayload.STREAM_CODEC, (payload, player) -> {
            if (player.getMainHandItem().getItem() instanceof RailGunItem gun) {
                gun.fire(player, player.getMainHandItem());
            }
        });
        ModNetworking.serverbound(RailEjectPayload.TYPE, RailEjectPayload.STREAM_CODEC, (payload, player) -> {
            if (player.getMainHandItem().getItem() instanceof RailGunItem gun) {
                gun.ejectMagazine(player, player.getMainHandItem());
            }
        });
        ModNetworking.serverbound(RailReloadPayload.TYPE, RailReloadPayload.STREAM_CODEC, (payload, player) -> {
            if (player.getMainHandItem().getItem() instanceof RailGunItem gun) {
                gun.startReload(player, player.getMainHandItem());
            }
        });
    }

    private static void sendSettings(ServerPlayer player) {
        Map<String, String> values = new HashMap<>();
        for (String key : Config.SERVER_SETTINGS) {
            values.put(key, SettingsRegistry.get(key).get());
        }
        ModNetworking.sendToPlayer(player, new SettingsPayload(values, canEditSettings(player)));
    }

    private static void onChangeSetting(ChangeSettingPayload payload, ServerPlayer player) {
        if (canEditSettings(player) && Config.SERVER_SETTINGS.contains(payload.key())
            && SettingsRegistry.get(payload.key()).trySet(payload.value()) == null) {
            AvaliNexus.LOGGER.info("[AvaliNexus] {} set {} to {}", player.getName().getString(), payload.key(), payload.value());
        }
        sendSettings(player);
    }

    private static boolean canEditSettings(ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static void onPong(PongPayload payload, ServerPlayer player) {
        long roundTrip = System.currentTimeMillis() - payload.sentAt();
        player.sendSystemMessage(Component.translatable("avalinexus.ping.result", roundTrip));
    }
}
