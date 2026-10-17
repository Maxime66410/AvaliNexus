/**
 * File: AvaliBehavior.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.Optional;

public final class AvaliBehavior {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/behavior.json");

    public record Pack(double maxDistance, double babyDistance, double babyRunDistance, double parentDistance, double regroupRadius,
                       int heaterSearchRadius, double heaterRadius) {
    }

    public record Life(int bedSearchRadius, double bedMaxPackDistance) {
    }

    public record Data(Pack pack, Life life) {
    }

    private static final Data FALLBACK = new Data(new Pack(100, 6, 12, 16, 8, 32, 6), new Life(16, 100));

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
            return new Data(
                new Pack(pack.get("max_distance").getAsDouble(), pack.get("baby_distance").getAsDouble(),
                    pack.get("baby_run_distance").getAsDouble(),
                    pack.get("parent_distance").getAsDouble(), pack.get("regroup_radius").getAsDouble(),
                    pack.get("heater_search_radius").getAsInt(), pack.get("heater_radius").getAsDouble()),
                new Life(life.get("bed_search_radius").getAsInt(), life.get("bed_max_pack_distance").getAsDouble()));
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read behavior file {}", FILE, e);
            return FALLBACK;
        }
    }
}
