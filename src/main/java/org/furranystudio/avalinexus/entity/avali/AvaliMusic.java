/**
 * File: AvaliMusic.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AvaliMusic {

    private static final String DIRECTORY = "avali/music";
    private static final String EXTENSION = ".json";
    private static final List<String> NAMES = List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final List<String> FLATS = List.of("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B");
    private static final int LOWEST = NAMES.indexOf("F#") + 3 * 12;
    private static final int RANGE = 24;
    private static final String REST = "-";
    private static final float NOTE_COLORS = 24.0F;

    public record Part(NoteBlockInstrument instrument, Map<Integer, int[]> notes) {
    }

    public record Song(Identifier id, int length, List<Part> parts) {

        public boolean isGroup() {
            return parts.size() > 1;
        }
    }

    private static ResourceManager loadedFrom;
    private static List<Song> songs = List.of();

    private AvaliMusic() {
    }

    public static List<Song> get(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            songs = load(manager);
        }
        return songs;
    }

    public static void reload(MinecraftServer server) {
        loadedFrom = null;
        get(server);
    }

    public static Song find(MinecraftServer server, Identifier id) {
        return get(server).stream().filter(song -> song.id().equals(id)).findFirst().orElse(null);
    }

    public static void playNotes(ServerLevel level, Vec3 sound, Vec3 particle, Part part, int[] notes, AvaliBehavior.Music music) {
        for (int note : notes) {
            level.playSound(null, sound.x, sound.y, sound.z, part.instrument().getSoundEvent().value(), SoundSource.RECORDS,
                music.volume(), part.instrument().isTunable() ? NoteBlock.getPitchFromNote(note) : 1.0F);
            if (music.particles()) {
                level.sendParticles(ParticleTypes.NOTE, particle.x, particle.y, particle.z, 0, note / NOTE_COLORS, 0.0, 0.0, 1.0);
            }
        }
    }

    private static List<Song> load(ResourceManager manager) {
        List<Song> result = new ArrayList<>();
        Map<Identifier, Resource> files = manager.listResources(DIRECTORY, id -> id.getPath().endsWith(EXTENSION));
        files.forEach((file, resource) -> {
            String path = file.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(file.getNamespace(),
                path.substring(DIRECTORY.length() + 1, path.length() - EXTENSION.length()));
            try (Reader reader = resource.openAsReader()) {
                result.add(parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception e) {
                AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read song {}", file, e);
            }
        });
        return List.copyOf(result);
    }

    private static Song parse(Identifier id, JsonObject root) {
        int tempo = root.get("tempo").getAsInt();
        List<Part> parts = new ArrayList<>();
        int length = 0;
        for (JsonElement element : root.getAsJsonArray("parts")) {
            JsonObject part = element.getAsJsonObject();
            String name = part.get("instrument").getAsString();
            NoteBlockInstrument instrument = Arrays.stream(NoteBlockInstrument.values())
                .filter(value -> value.getSerializedName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument " + name));
            Map<Integer, int[]> notes = new HashMap<>();
            int step = 0;
            for (JsonElement token : part.getAsJsonArray("notes")) {
                String[] split = token.getAsString().split(":");
                int steps = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                if (!split[0].equals(REST)) {
                    notes.put(step * tempo, Arrays.stream(split[0].split("\\+")).mapToInt(AvaliMusic::note).toArray());
                }
                step += steps;
            }
            length = Math.max(length, step * tempo);
            parts.add(new Part(instrument, notes));
        }
        return new Song(id, length, List.copyOf(parts));
    }

    private static int note(String name) {
        if (Character.isDigit(name.charAt(0))) {
            return check(Integer.parseInt(name), name);
        }
        int split = name.length() > 2 && (name.charAt(1) == '#' || name.charAt(1) == 'b') ? 2 : 1;
        String letter = name.substring(0, split);
        int index = NAMES.contains(letter) ? NAMES.indexOf(letter) : FLATS.indexOf(letter);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown note " + name);
        }
        return check(index + Integer.parseInt(name.substring(split)) * 12 - LOWEST, name);
    }

    private static int check(int note, String name) {
        if (note < 0 || note > RANGE) {
            throw new IllegalArgumentException("Note " + name + " is outside F#3 to F#5");
        }
        return note;
    }
}
