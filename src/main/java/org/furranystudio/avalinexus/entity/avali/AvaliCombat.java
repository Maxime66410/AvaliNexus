/**
 * File: AvaliCombat.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class AvaliCombat {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/combat.json");

    public record Creeper(double detectRadius, float minHealth, int fleeDistance, double fleeSpeed) {
    }

    public record Crit(double jumpDistance, int jumpCooldown, float multiplier) {
    }

    public record Data(float attackHealth, float fleeHealth, int fleeDistance, double fleeSpeed, double chaseSpeed,
                       double allyRadius, double enemyRadius, int tooManyEnemies, double outnumberRatio,
                       int grudgeSeconds, double grudgeDistance, int weaponStowSeconds, double shieldDistance,
                       double powerMargin, double enemyDefaultDamage, double enemyAttacksPerSecond, int corneredSeconds,
                       double parentSafeDistance, Crit crit, Creeper creeper, List<Predicate<EntityType<?>>> hunted) {

        public boolean hunts(EntityType<?> type) {
            return hunted.stream().anyMatch(test -> test.test(type));
        }
    }

    private static final Data FALLBACK = new Data(0.5F, 0.3F, 16, 1.5, 1.2, 16, 10, 3, 2.0, 30, 32, 5, 3.5,
        1.5, 2.0, 1.0, 3, 6, new Crit(4, 40, 1.5F), new Creeper(8, 0.2F, 8, 1.5), List.of());

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliCombat() {
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
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing combat file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject creeper = root.getAsJsonObject("creeper");
            JsonObject crit = root.getAsJsonObject("crit");
            List<Predicate<EntityType<?>>> hunted = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("hunted")) {
                String entry = element.getAsString();
                if (entry.startsWith("#")) {
                    TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(entry.substring(1)));
                    hunted.add(type -> type.builtInRegistryHolder().is(tag));
                } else {
                    Identifier id = Identifier.parse(entry);
                    hunted.add(type -> EntityType.getKey(type).equals(id));
                }
            }
            return new Data(root.get("attack_health").getAsFloat(), root.get("flee_health").getAsFloat(),
                root.get("flee_distance").getAsInt(), root.get("flee_speed").getAsDouble(), root.get("chase_speed").getAsDouble(),
                root.get("ally_radius").getAsDouble(), root.get("enemy_radius").getAsDouble(),
                root.get("too_many_enemies").getAsInt(), root.get("outnumber_ratio").getAsDouble(),
                root.get("grudge_seconds").getAsInt(), root.get("grudge_distance").getAsDouble(),
                root.get("weapon_stow_seconds").getAsInt(), root.get("shield_distance").getAsDouble(),
                root.get("power_margin").getAsDouble(), root.get("enemy_default_damage").getAsDouble(),
                root.get("enemy_attacks_per_second").getAsDouble(), root.get("cornered_seconds").getAsInt(),
                root.get("parent_safe_distance").getAsDouble(),
                new Crit(crit.get("jump_distance").getAsDouble(), crit.get("jump_cooldown").getAsInt(), crit.get("multiplier").getAsFloat()),
                new Creeper(creeper.get("detect_radius").getAsDouble(), creeper.get("min_health").getAsFloat(),
                    creeper.get("flee_distance").getAsInt(), creeper.get("flee_speed").getAsDouble()),
                hunted);
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read combat file {}", FILE, e);
            return FALLBACK;
        }
    }
}
