/**
 * File: AvaliCompanions.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AvaliCompanions {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/companion.json");

    public record Healer(double range, int feedBelowFood, List<Item> feedItems, int feedCooldownSeconds, float potionBelowHealth,
                         float instantBelowHealth, int potionCooldownSeconds, float specialBelowHealth, int specialCooldownSeconds,
                         int absorptionHearts, int absorptionSeconds, int attackCooldownSeconds) {
    }

    public record Data(int maxCompanions, Item priceItem, int priceCount, double followStartDistance, double followStopDistance,
                       double teleportDistance, double homeTeleportDistance, double followSpeed, double waitRadius, double gatherRadius, Healer healer) {
    }

    private static final Data FALLBACK = new Data(3, Items.AIR, 32, 6, 3, 16, 100, 1.1, 3, 8,
        new Healer(10, 14, List.of(Items.BREAD), 30, 0.6F, 0.35F, 8, 0.25F, 600, 4, 120, 12));

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliCompanions() {
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
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing companion file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject price = root.getAsJsonObject("recruit_price");
            JsonObject healer = root.getAsJsonObject("healer");
            List<Item> food = new ArrayList<>();
            for (JsonElement element : healer.getAsJsonArray("feed_items")) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString()));
                if (item != Items.AIR) {
                    food.add(item);
                }
            }
            return new Data(root.get("max_companions").getAsInt(),
                BuiltInRegistries.ITEM.getValue(Identifier.parse(price.get("item").getAsString())), price.get("count").getAsInt(),
                root.get("follow_start_distance").getAsDouble(), root.get("follow_stop_distance").getAsDouble(),
                root.get("teleport_distance").getAsDouble(), root.get("home_teleport_distance").getAsDouble(),
                root.get("follow_speed").getAsDouble(),
                root.get("wait_radius").getAsDouble(), root.get("gather_radius").getAsDouble(),
                new Healer(healer.get("range").getAsDouble(), healer.get("feed_below_food").getAsInt(), List.copyOf(food),
                    healer.get("feed_cooldown_seconds").getAsInt(), healer.get("potion_below_health").getAsFloat(),
                    healer.get("instant_below_health").getAsFloat(), healer.get("potion_cooldown_seconds").getAsInt(),
                    healer.get("special_below_health").getAsFloat(), healer.get("special_cooldown_seconds").getAsInt(),
                    healer.get("absorption_hearts").getAsInt(), healer.get("absorption_seconds").getAsInt(),
                    healer.get("attack_cooldown_seconds").getAsInt()));
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read companion file {}", FILE, e);
            return FALLBACK;
        }
    }
}
