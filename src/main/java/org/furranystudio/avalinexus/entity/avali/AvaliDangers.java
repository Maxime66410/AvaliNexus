/**
 * File: AvaliDangers.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class AvaliDangers {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/dangers.json");

    public record Data(boolean avoidWater, List<Predicate<BlockState>> hotBlocks, int heatDetectRadius, int heatFleeDistance,
                       List<Predicate<Holder<Biome>>> hotBiomes, boolean rainShelter, int shelterSearchRadius) {

        public boolean isHot(BlockState state) {
            if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
                return false;
            }
            return hotBlocks.stream().anyMatch(test -> test.test(state));
        }

        public boolean isHotBiome(Holder<Biome> biome) {
            return hotBiomes.stream().anyMatch(test -> test.test(biome));
        }
    }

    private static final Data FALLBACK = new Data(true, List.of(), 4, 8, List.of(), true, 16);

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliDangers() {
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
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing dangers file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject heat = root.getAsJsonObject("heat");
            JsonObject shelter = root.getAsJsonObject("rain_shelter");
            List<Predicate<BlockState>> blocks = new ArrayList<>();
            for (JsonElement element : heat.getAsJsonArray("blocks")) {
                String entry = element.getAsString();
                if (entry.startsWith("#")) {
                    TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(entry.substring(1)));
                    blocks.add(state -> state.is(tag));
                } else {
                    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.parse(entry));
                    blocks.add(state -> state.is(key));
                }
            }
            List<Predicate<Holder<Biome>>> biomes = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("hot_biomes")) {
                String entry = element.getAsString();
                if (entry.startsWith("#")) {
                    TagKey<Biome> tag = TagKey.create(Registries.BIOME, Identifier.parse(entry.substring(1)));
                    biomes.add(biome -> biome.is(tag));
                } else {
                    ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, Identifier.parse(entry));
                    biomes.add(biome -> biome.is(key));
                }
            }
            return new Data(root.getAsJsonObject("water").get("avoid").getAsBoolean(), blocks,
                heat.get("detect_radius").getAsInt(), heat.get("flee_distance").getAsInt(), biomes,
                shelter.get("enabled").getAsBoolean(), shelter.get("search_radius").getAsInt());
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read dangers file {}", FILE, e);
            return FALLBACK;
        }
    }
}
