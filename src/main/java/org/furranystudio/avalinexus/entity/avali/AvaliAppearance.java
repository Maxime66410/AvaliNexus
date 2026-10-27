/**
 * File: AvaliAppearance.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-25
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

    public record Rare(String name, Identifier texture, float chance) {
    }

    public record Colors(int base, int pattern, int feathers) {

        public static final Colors WHITE = new Colors(0xFFFFFF, 0xFFFFFF, 0xFFFFFF);
    }

    private record Variation(float hue, float brightness) {

        static final Variation NONE = new Variation(0.0F, 0.0F);
    }

    private record Data(float femaleChance, List<Rare> rares, Map<String, Colors> colors, Variation base, Variation pattern, Variation feathers) {
    }

    private static final Data FALLBACK = new Data(0.5F, List.of(), Map.of(), Variation.NONE, Variation.NONE, Variation.NONE);

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
        return get(server).colors().getOrDefault(pack, Colors.WHITE).base();
    }

    public static Colors rollColors(MinecraftServer server, String pack, RandomSource random) {
        Data loaded = get(server);
        Colors colors = loaded.colors().getOrDefault(pack, Colors.WHITE);
        return new Colors(vary(loaded.base(), colors.base(), random), vary(loaded.pattern(), colors.pattern(), random),
            vary(loaded.feathers(), colors.feathers(), random));
    }

    public static Colors mixColors(MinecraftServer server, Colors first, Colors second, RandomSource random) {
        Data loaded = get(server);
        return new Colors(vary(loaded.base(), mix(first.base(), second.base()), random),
            vary(loaded.pattern(), mix(first.pattern(), second.pattern()), random),
            vary(loaded.feathers(), mix(first.feathers(), second.feathers()), random));
    }

    private static int mix(int first, int second) {
        int red = ((first >> 16 & 0xFF) + (second >> 16 & 0xFF)) / 2;
        int green = ((first >> 8 & 0xFF) + (second >> 8 & 0xFF)) / 2;
        int blue = ((first & 0xFF) + (second & 0xFF)) / 2;
        return red << 16 | green << 8 | blue;
    }

    private static int vary(Variation variation, int base, RandomSource random) {
        float[] hsv = hsv(base);
        float hue = hsv[0] + (random.nextFloat() * 2.0F - 1.0F) * variation.hue() / 360.0F;
        float value = Mth.clamp(hsv[2] * (1.0F + (random.nextFloat() * 2.0F - 1.0F) * variation.brightness()), 0.0F, 1.0F);
        return Mth.hsvToRgb(hue - Mth.floor(hue), hsv[1], value) & 0xFFFFFF;
    }

    private static Colors derive(int base) {
        float[] hsv = hsv(base);
        int feathers = Mth.hsvToRgb(hsv[0], hsv[1] > 0.15F ? Mth.clamp(hsv[1], 0.35F, 0.75F) : hsv[1] * 1.5F, 0.98F) & 0xFFFFFF;
        if (hsv[1] < 0.1F) {
            return new Colors(base, Mth.hsvToRgb(hsv[0], hsv[1], 0.5F) & 0xFFFFFF, feathers);
        }
        float hue = hsv[0] + (hsv[0] < 1.0F / 6.0F ? -0.07F : 0.08F);
        int pattern = Mth.hsvToRgb(hue - Mth.floor(hue), Mth.clamp(hsv[1], 0.35F, 0.75F), Mth.clamp(hsv[2], 0.45F, 0.85F)) & 0xFFFFFF;
        return new Colors(base, pattern, feathers);
    }

    private static float[] hsv(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float delta = max - Math.min(red, Math.min(green, blue));
        float hue = delta == 0.0F ? 0.0F
            : max == red ? (green - blue) / delta / 6.0F
            : max == green ? ((blue - red) / delta + 2.0F) / 6.0F
            : ((red - green) / delta + 4.0F) / 6.0F;
        return new float[] {hue - Mth.floor(hue), max == 0.0F ? 0.0F : delta / max, max};
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
        Map<String, Colors> colors = new HashMap<>();
        Variation base = Variation.NONE;
        Variation pattern = Variation.NONE;
        Variation feathers = Variation.NONE;
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
            Variation shared = new Variation(packs.has("hue_variation") ? packs.get("hue_variation").getAsFloat() : 0.0F,
                packs.has("brightness_variation") ? packs.get("brightness_variation").getAsFloat() : 0.0F);
            JsonObject variation = packs.has("variation") ? packs.getAsJsonObject("variation") : new JsonObject();
            base = variation(variation, "base", shared);
            pattern = variation(variation, "pattern", shared);
            feathers = variation(variation, "feathers", shared);
            for (Map.Entry<String, JsonElement> entry : packs.getAsJsonObject("colors").entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonObject()) {
                    JsonObject pack = value.getAsJsonObject();
                    colors.put(entry.getKey(), new Colors(hex(pack.get("base")), hex(pack.get("pattern")), hex(pack.get("feathers"))));
                } else {
                    colors.put(entry.getKey(), derive(hex(value)));
                }
            }
        }
        return new Data(femaleChance, rares, colors, base, pattern, feathers);
    }

    private static Variation variation(JsonObject variation, String part, Variation fallback) {
        if (!variation.has(part)) {
            return fallback;
        }
        JsonObject values = variation.getAsJsonObject(part);
        return new Variation(values.has("hue") ? values.get("hue").getAsFloat() : fallback.hue(),
            values.has("brightness") ? values.get("brightness").getAsFloat() : fallback.brightness());
    }

    private static int hex(JsonElement color) {
        return Integer.parseInt(color.getAsString().replace("#", ""), 16);
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
