/**
 * File: AvaliAppearance.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AvaliAppearance {

    private static final Identifier SKINS = AvaliNexus.id("avali/appearance/skins.json");
    private static final Identifier PACKS = AvaliNexus.id("avali/appearance/packs.json");
    private static final int WHITE = 0xFFFFFF;

    public record Rare(String name, Identifier texture, float chance) {
    }

    private record Data(float femaleChance, List<Rare> rares, Map<String, Integer> colors, float hueVariation, float brightnessVariation) {
    }

    private static final Data FALLBACK = new Data(0.5F, List.of(), Map.of(), 0.0F, 0.0F);

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliAppearance() {
    }

    public static boolean rollFemale(MinecraftServer server, RandomSource random) {
        return random.nextFloat() < get(server).femaleChance();
    }

    public static String rollRare(MinecraftServer server, RandomSource random) {
        float roll = random.nextFloat();
        for (Rare rare : get(server).rares()) {
            if (roll < rare.chance()) {
                return rare.texture().toString();
            }
            roll -= rare.chance();
        }
        return "";
    }

    public static int packColor(MinecraftServer server, String pack) {
        return get(server).colors().getOrDefault(pack, WHITE);
    }

    public static int rollColor(MinecraftServer server, String pack, RandomSource random) {
        Data loaded = get(server);
        int base = loaded.colors().getOrDefault(pack, WHITE);
        float red = (base >> 16 & 0xFF) / 255.0F;
        float green = (base >> 8 & 0xFF) / 255.0F;
        float blue = (base & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float delta = max - Math.min(red, Math.min(green, blue));
        float hue = delta == 0.0F ? 0.0F
            : max == red ? (green - blue) / delta / 6.0F
            : max == green ? ((blue - red) / delta + 2.0F) / 6.0F
            : ((red - green) / delta + 4.0F) / 6.0F;
        float saturation = max == 0.0F ? 0.0F : delta / max;
        hue += (random.nextFloat() * 2.0F - 1.0F) * loaded.hueVariation() / 360.0F;
        float value = Mth.clamp(max * (1.0F + (random.nextFloat() * 2.0F - 1.0F) * loaded.brightnessVariation()), 0.0F, 1.0F);
        return Mth.hsvToRgb(hue - Mth.floor(hue), saturation, value) & 0xFFFFFF;
    }

    private static Data get(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            data = load(manager);
        }
        return data;
    }

    public static void reload(MinecraftServer server) {
        loadedFrom = null;
        get(server);
    }

    private static Data load(ResourceManager manager) {
        float femaleChance = FALLBACK.femaleChance();
        List<Rare> rares = new ArrayList<>();
        Map<String, Integer> colors = new HashMap<>();
        float hueVariation = 0.0F;
        float brightnessVariation = 0.0F;
        JsonObject skins = read(manager, SKINS);
        if (skins != null) {
            femaleChance = skins.get("female_chance").getAsFloat();
            for (JsonElement element : skins.getAsJsonArray("rare")) {
                JsonObject rare = element.getAsJsonObject();
                rares.add(new Rare(rare.get("name").getAsString(), Identifier.parse(rare.get("texture").getAsString()), rare.get("chance").getAsFloat()));
            }
        }
        JsonObject packs = read(manager, PACKS);
        if (packs != null) {
            hueVariation = packs.get("hue_variation").getAsFloat();
            brightnessVariation = packs.get("brightness_variation").getAsFloat();
            for (Map.Entry<String, JsonElement> entry : packs.getAsJsonObject("colors").entrySet()) {
                colors.put(entry.getKey(), Integer.parseInt(entry.getValue().getAsString().replace("#", ""), 16));
            }
        }
        return new Data(femaleChance, rares, colors, hueVariation, brightnessVariation);
    }

    private static JsonObject read(ResourceManager manager, Identifier file) {
        Optional<Resource> resource = manager.getResource(file);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing appearance file {}", file);
            return null;
        }
        try (Reader reader = resource.get().openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read appearance file {}", file, e);
            return null;
        }
    }
}
