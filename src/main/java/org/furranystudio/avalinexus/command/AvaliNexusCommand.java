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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.archive.ArchiveData;
import org.furranystudio.avalinexus.dialogue.DialogueData;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliConcert;
import org.furranystudio.avalinexus.entity.avali.AvaliDangers;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliEquipment;
import org.furranystudio.avalinexus.entity.avali.AvaliMusic;
import org.furranystudio.avalinexus.entity.avali.AvaliMusicPlayer;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.PingPayload;
import org.furranystudio.avalinexus.settings.SettingsRegistry;
import org.furranystudio.avalinexus.trade.TradeData;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class AvaliNexusCommand {

    private static final Map<String, Consumer<MinecraftServer>> CONFIGS = new LinkedHashMap<>();
    private static final double MUSIC_RANGE = 32.0;
    private static final int TICKS_PER_SECOND = 20;
    private static final SuggestionProvider<CommandSourceStack> SONGS = (context, builder) ->
        SharedSuggestionProvider.suggestResource(AvaliMusic.get(context.getSource().getServer()).stream().map(AvaliMusic.Song::id), builder);

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
        CONFIGS.put("music", AvaliMusic::reload);
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
                .then(Commands.literal("music")
                    .then(Commands.literal("list")
                        .executes(AvaliNexusCommand::runMusicList))
                    .then(Commands.literal("play")
                        .then(Commands.argument("song", IdentifierArgument.id())
                            .suggests(SONGS)
                            .executes(AvaliNexusCommand::runMusicPlay)))
                    .then(Commands.literal("avali")
                        .then(Commands.argument("song", IdentifierArgument.id())
                            .suggests(SONGS)
                            .executes(AvaliNexusCommand::runMusicAvali)))
                    .then(Commands.literal("stop")
                        .executes(AvaliNexusCommand::runMusicStop)))
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

    private static int runMusicList(CommandContext<CommandSourceStack> context) {
        List<AvaliMusic.Song> songs = AvaliMusic.get(context.getSource().getServer());
        MutableComponent message = Component.translatable("avalinexus.music.list", songs.size());
        for (AvaliMusic.Song song : songs) {
            message.append(Component.translatable("avalinexus.music.entry", song.id().toString(), song.parts().size(),
                String.format(Locale.ROOT, "%.1f", song.length() / (double) TICKS_PER_SECOND)));
        }
        context.getSource().sendSuccess(() -> message, false);
        return songs.size();
    }

    private static int runMusicPlay(CommandContext<CommandSourceStack> context) {
        AvaliMusic.Song song = findSong(context);
        if (song == null) {
            return 0;
        }
        AvaliMusicPlayer.play(context.getSource().getLevel(), context.getSource().getPosition(), song);
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.music.play", song.id().toString()), false);
        return 1;
    }

    private static int runMusicAvali(CommandContext<CommandSourceStack> context) {
        AvaliMusic.Song song = findSong(context);
        if (song == null) {
            return 0;
        }
        ServerLevel level = context.getSource().getLevel();
        Vec3 position = context.getSource().getPosition();
        AvaliEntity avali = level.getEntitiesOfClass(AvaliEntity.class, AABB.ofSize(position, MUSIC_RANGE * 2, MUSIC_RANGE * 2, MUSIC_RANGE * 2),
                entity -> entity.position().distanceTo(position) <= MUSIC_RANGE)
            .stream().min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(position))).orElse(null);
        if (avali == null) {
            context.getSource().sendFailure(Component.translatable("avalinexus.music.no_avali", (int) MUSIC_RANGE));
            return 0;
        }
        if (avali.getConcert() != null) {
            avali.getConcert().end(level, 0);
        }
        AvaliBehavior.Music music = AvaliBehavior.get(level.getServer()).music();
        AvaliConcert concert = AvaliConcert.start(level, avali, song, song.isGroup() ? avali.getPackCenter() : avali.position(),
            music.gatherSeconds() * TICKS_PER_SECOND);
        if (song.isGroup()) {
            concert.recruit(level, music.joinRadius());
        }
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.music.avali", avali.getName(), song.id().toString()), false);
        return 1;
    }

    private static int runMusicStop(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        Vec3 position = context.getSource().getPosition();
        int stopped = AvaliMusicPlayer.stopNear(level, position, MUSIC_RANGE) + AvaliConcert.stopNear(level, position, MUSIC_RANGE);
        context.getSource().sendSuccess(() -> Component.translatable("avalinexus.music.stop", stopped), false);
        return stopped;
    }

    private static AvaliMusic.Song findSong(CommandContext<CommandSourceStack> context) {
        Identifier id = IdentifierArgument.getId(context, "song");
        AvaliMusic.Song song = AvaliMusic.find(context.getSource().getServer(), id);
        if (song == null) {
            context.getSource().sendFailure(Component.translatable("avalinexus.music.unknown", id.toString()));
        }
        return song;
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
