/**
 * File: AvaliNexusCommand.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.furranystudio.avalinexus.archive.ArchiveData;
import org.furranystudio.avalinexus.dialogue.DialogueData;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliDangers;
import org.furranystudio.avalinexus.entity.avali.AvaliEquipment;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.settings.SettingsRegistry;
import org.furranystudio.avalinexus.trade.TradeData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class AvaliNexusCommand {

    private static final Map<String, Consumer<MinecraftServer>> CONFIGS = new LinkedHashMap<>();

    static {
        CONFIGS.put("behavior", AvaliBehavior::reload);
        CONFIGS.put("dangers", AvaliDangers::reload);
        CONFIGS.put("combat", AvaliCombat::reload);
        CONFIGS.put("equipment", AvaliEquipment::reload);
        CONFIGS.put("appearance", AvaliAppearance::reload);
        CONFIGS.put("names", AvaliNames::reload);
        CONFIGS.put("dialogue", DialogueData::reload);
        CONFIGS.put("trade", TradeData::reload);
        CONFIGS.put("archive", ArchiveData::reload);
    }

    private AvaliNexusCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("avalinexus")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("ping")
                    .executes(AvaliNexusCommand::runPing))
                .then(Commands.literal("settings")
                    .executes(AvaliNexusCommand::runSettingsList)
                    .then(Commands.argument("parameter", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SettingsRegistry.names(), builder))
                        .then(Commands.argument("value", StringArgumentType.word())
                            .executes(AvaliNexusCommand::runSettings))))
                .then(Commands.literal("reload")
                    .executes(AvaliNexusCommand::runReloadAll)
                    .then(Commands.argument("config", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(CONFIGS.keySet(), builder))
                        .executes(AvaliNexusCommand::runReload)))
        );
    }

    private static int runPing(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModNetworking.sendToPlayer(context.getSource().getPlayerOrException(), new PingPayload(System.currentTimeMillis()));
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.ping.sent"), false);
        return 1;
    }

    private static int runReloadAll(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CONFIGS.values().forEach(reload -> reload.accept(server));
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.reload.all"), true);
        return CONFIGS.size();
    }

    private static int runReload(CommandContext<CommandSourceStack> context) {
        String config = StringArgumentType.getString(context, "config");
        Consumer<MinecraftServer> reload = CONFIGS.get(config);
        if (reload == null) {
            context.getSource().sendFailure(Component.translatable("avalinexus.reload.unknown", config));
            return 0;
        }
        reload.accept(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.reload.file", config), true);
        return 1;
    }

    private static int runSettingsList(CommandContext<CommandSourceStack> context) {
        MutableComponent message = Component.translatable("avalinexus.settings.header");
        for (String name : SettingsRegistry.names()) {
            message.append(Component.translatable("avalinexus.settings.entry", name, SettingsRegistry.get(name).get()));
        }
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int runSettings(CommandContext<CommandSourceStack> context) {
        String parameter = StringArgumentType.getString(context, "parameter");
        String value = StringArgumentType.getString(context, "value");

        SettingsRegistry.Setting setting = SettingsRegistry.get(parameter);
        if (setting == null) {
            context.getSource().sendFailure(Component.translatable("avalinexus.settings.unknown", parameter));
            return 0;
        }

        String error = setting.trySet(value);
        if (error != null) {
            context.getSource().sendFailure(Component.literal("[AvaliNexus] " + error));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable(
            "avalinexus.settings.set", parameter, setting.get()), true);
        return 1;
    }
}
