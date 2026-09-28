/**
 * File: AvaliNames.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Builds Avali names out of the syllables in data/avalinexus/names/avali.json, and picks pack names from it
public final class AvaliNames {

    private static final Identifier FILE = AvaliNexus.id("names/avali.json");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 10;
    private static final int TRIES = 8;

    private record Syllables(List<String> first, List<String> middle, List<String> last, float middleChance,
                             float apostropheChance, List<String> packs) {
    }

    private static final Syllables FALLBACK = new Syllables(List.of("Ki"), List.of(), List.of("ri"), 0.0F, 0.0F, List.of("frost"));

    // Reloaded when the resource manager changes, so /reload picks up edited names
    private static ResourceManager loadedFrom;
    private static Syllables syllables = FALLBACK;

    private AvaliNames() {
    }

    public static String name(MinecraftServer server, RandomSource random) {
        Syllables data = syllables(server);
        String name = build(data, random);
        for (int i = 0; i < TRIES && (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH); i++) {
            name = build(data, random);
        }
        return name;
    }

    public static String pack(MinecraftServer server, RandomSource random) {
        List<String> packs = syllables(server).packs();
        return packs.get(random.nextInt(packs.size()));
    }

    private static String build(Syllables data, RandomSource random) {
        StringBuilder name = new StringBuilder(pick(data.first(), random));
        if (!data.middle().isEmpty() && random.nextFloat() < data.middleChance()) {
            name.append(pick(data.middle(), random));
        }
        String last = pick(data.last(), random);
        // The apostrophe only splits two real syllables
        if (!last.isEmpty() && last.length() > 1 && random.nextFloat() < data.apostropheChance()) {
            name.append('\'');
        }
        name.append(last);
        String result = name.toString().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    private static String pick(List<String> list, RandomSource random) {
        return list.isEmpty() ? "" : list.get(random.nextInt(list.size()));
    }

    private static Syllables syllables(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            syllables = load(manager);
        }
        return syllables;
    }

    private static Syllables load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing names file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Syllables loaded = new Syllables(strings(root.getAsJsonArray("first")), strings(root.getAsJsonArray("middle")),
                strings(root.getAsJsonArray("last")), root.get("middle_chance").getAsFloat(),
                root.get("apostrophe_chance").getAsFloat(), strings(root.getAsJsonArray("packs")));
            return loaded.first().isEmpty() || loaded.packs().isEmpty() ? FALLBACK : loaded;
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read names file {}", FILE, e);
            return FALLBACK;
        }
    }

    private static List<String> strings(JsonArray array) {
        List<String> result = new ArrayList<>();
        array.forEach(element -> result.add(element.getAsString()));
        return result;
    }
}
