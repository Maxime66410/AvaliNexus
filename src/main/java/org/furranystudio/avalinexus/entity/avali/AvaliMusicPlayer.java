/**
 * File: AvaliMusicPlayer.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class AvaliMusicPlayer {

    private static final double PARTICLE_HEIGHT = 2.2;
    private static final List<Playback> PLAYING = new ArrayList<>();

    private record Playback(ServerLevel level, Vec3 position, AvaliMusic.Song song, long start) {
    }

    private AvaliMusicPlayer() {
    }

    public static void play(ServerLevel level, Vec3 position, AvaliMusic.Song song) {
        PLAYING.add(new Playback(level, position, song, level.getGameTime()));
    }

    public static int stopNear(ServerLevel level, Vec3 position, double radius) {
        int before = PLAYING.size();
        PLAYING.removeIf(playback -> playback.level() == level && playback.position().distanceTo(position) <= radius);
        return before - PLAYING.size();
    }

    public static void tick(MinecraftServer server) {
        if (PLAYING.isEmpty()) {
            return;
        }
        AvaliBehavior.Music music = AvaliBehavior.get(server).music();
        PLAYING.removeIf(playback -> {
            long elapsed = playback.level().getGameTime() - playback.start();
            for (AvaliMusic.Part part : playback.song().parts()) {
                int[] notes = part.notes().get((int) elapsed);
                if (notes != null) {
                    AvaliMusic.playNotes(playback.level(), playback.position(), playback.position().add(0.0, PARTICLE_HEIGHT, 0.0),
                        part, notes, music);
                }
            }
            return elapsed >= playback.song().length();
        });
    }
}
