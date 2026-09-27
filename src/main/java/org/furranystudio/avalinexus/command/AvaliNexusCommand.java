/**
 * File: AvaliNexusCommand.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.furranystudio.avalinexus.settings.SettingsRegistry;

// Command tree is pure Brigadier/vanilla - each loader's bootstrap calls register(dispatcher)
// from its own command-registration hook (Forge/NeoForge: RegisterCommandsEvent; Fabric: CommandRegistrationCallback).
public final class AvaliNexusCommand {

    private AvaliNexusCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("avalinexus")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("settings")
                    .executes(AvaliNexusCommand::runSettingsList)
                    .then(Commands.argument("parameter", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SettingsRegistry.names(), builder))
                        .then(Commands.argument("value", StringArgumentType.word())
                            .executes(AvaliNexusCommand::runSettings))))
        );
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
