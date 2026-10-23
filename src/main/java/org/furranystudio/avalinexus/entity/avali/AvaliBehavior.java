/**
 * File: AvaliBehavior.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliFace;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AvaliBehavior {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/behavior.json");

    public record Pack(double maxDistance, double babyDistance, double babyRunDistance, double parentDistance, double regroupRadius,
                       int heaterSearchRadius, double heaterRadius, double eggJoinRadius) {
    }

    public record Life(int bedSearchRadius, double bedMaxPackDistance, double pickupRadius, double giveRange,
                       float eatBelowHealth, float healPerNutrition, double eatMouthForward, double eatMouthHeight) {
    }

    public record Music(double startChance, int eveningStart, int eveningEnd, double eveningMultiplier, double zoneRadius,
                        int restSeconds, int silenceMinSeconds, int silenceMaxSeconds, double joinRadius, int gatherSeconds,
                        float volume, boolean particles, List<AvaliFace> faces) {
    }

    public record Camp(double anchorRadius, int explorersMin, int explorersMax, double explorerRadius, float exploreChance) {
    }

    public record Village(Camp settlement, double workshopRadius, double watchDistance, int fieldCheckSeconds, double fieldRadius) {
    }

    public record Data(Pack pack, Life life, Music music, Camp camp, Village village) {
    }

    private static final Data FALLBACK = new Data(new Pack(100, 6, 12, 16, 8, 32, 6, 64), new Life(16, 100, 8, 16, 0.5F, 1.0F, 0.3, -0.15),
        new Music(0.002, 12000, 13800, 5.0, 32, 300, 120, 300, 32, 15, 1.0F, true, List.of(AvaliFace.ADORABLE, AvaliFace.EXCITED, AvaliFace.SMUG)),
        new Camp(32, 1, 2, 96, 0.05F), new Village(new Camp(100, 2, 4, 160, 0.05F), 6, 16, 30, 48));

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliBehavior() {
    }

    public static Data get(MinecraftServer server) {
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
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing behavior file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject pack = root.getAsJsonObject("pack");
            JsonObject life = root.getAsJsonObject("life");
            JsonObject music = root.getAsJsonObject("music");
            JsonObject camp = root.getAsJsonObject("camp");
            JsonObject village = root.getAsJsonObject("village");
            List<AvaliFace> faces = new ArrayList<>();
            for (JsonElement face : music.getAsJsonArray("faces")) {
                faces.add(AvaliFace.valueOf(face.getAsString().toUpperCase(Locale.ROOT)));
            }
            return new Data(
                new Pack(pack.get("max_distance").getAsDouble(), pack.get("baby_distance").getAsDouble(),
                    pack.get("baby_run_distance").getAsDouble(),
                    pack.get("parent_distance").getAsDouble(), pack.get("regroup_radius").getAsDouble(),
                    pack.get("heater_search_radius").getAsInt(), pack.get("heater_radius").getAsDouble(),
                    pack.get("egg_join_radius").getAsDouble()),
                new Life(life.get("bed_search_radius").getAsInt(), life.get("bed_max_pack_distance").getAsDouble(),
                    life.get("pickup_radius").getAsDouble(), life.get("give_range").getAsDouble(),
                    life.get("eat_below_health").getAsFloat(), life.get("heal_per_nutrition").getAsFloat(),
                    life.get("eat_mouth_forward").getAsDouble(), life.get("eat_mouth_height").getAsDouble()),
                new Music(music.get("start_chance").getAsDouble(), music.get("evening_start").getAsInt(),
                    music.get("evening_end").getAsInt(), music.get("evening_multiplier").getAsDouble(),
                    music.get("zone_radius").getAsDouble(), music.get("rest_seconds").getAsInt(),
                    music.get("silence_min_seconds").getAsInt(), music.get("silence_max_seconds").getAsInt(),
                    music.get("join_radius").getAsDouble(), music.get("gather_seconds").getAsInt(),
                    music.get("volume").getAsFloat(), music.get("particles").getAsBoolean(), List.copyOf(faces)),
                settlement(camp),
                new Village(settlement(village), village.get("workshop_radius").getAsDouble(), village.get("watch_distance").getAsDouble(),
                    village.has("field_check_seconds") ? village.get("field_check_seconds").getAsInt() : 30,
                    village.has("field_radius") ? village.get("field_radius").getAsDouble() : 48));
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read behavior file {}", FILE, e);
            return FALLBACK;
        }
    }

    private static Camp settlement(JsonObject json) {
        return new Camp(json.get("anchor_radius").getAsDouble(), json.get("explorers_min").getAsInt(), json.get("explorers_max").getAsInt(),
            json.get("explorer_radius").getAsDouble(), json.get("explore_chance").getAsFloat());
    }
}
