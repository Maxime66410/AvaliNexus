/**
 * File: TradeData.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.trade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class TradeData {

    private static final Identifier AVALI_TRADES = AvaliNexus.id("trades/avali.json");

    private static ResourceManager loadedFrom;
    private static Pools pools = Pools.EMPTY;

    private TradeData() {
    }

    public static AvaliShop rollShop(MinecraftServer server, RandomSource random, long day) {
        Pools data = pools(server);
        List<ShopOffer> offers = new ArrayList<>();
        offers.addAll(roll(data.buy, data.buyRolls, random));
        offers.addAll(roll(data.sell, data.sellRolls, random));
        return new AvaliShop(offers, day);
    }

    private static List<ShopOffer> roll(List<ShopOffer> pool, int rolls, RandomSource random) {
        List<ShopOffer> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random(random.nextLong()));
        return shuffled.subList(0, Math.min(rolls, shuffled.size()));
    }

    private static Pools pools(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            pools = load(manager);
        }
        return pools;
    }

    private static Pools load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(AVALI_TRADES);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing trades file {}", AVALI_TRADES);
            return Pools.EMPTY;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            int maxUses = root.get("max_uses").getAsInt();
            return new Pools(
                offers(root, "buy", ShopTab.BUY, maxUses), root.get("buy_rolls").getAsInt(),
                offers(root, "sell", ShopTab.SELL, maxUses), root.get("sell_rolls").getAsInt());
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read trades file {}", AVALI_TRADES, e);
            return Pools.EMPTY;
        }
    }

    private static List<ShopOffer> offers(JsonObject root, String key, ShopTab tab, int maxUses) {
        List<ShopOffer> offers = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray(key)) {
            JsonObject json = element.getAsJsonObject();
            Identifier id = Identifier.parse(json.get("item").getAsString());
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isEmpty()) {
                AvaliNexus.LOGGER.warn("[AvaliNexus] Unknown item {} in {}", id, AVALI_TRADES);
                continue;
            }
            ItemStack stack = new ItemStack(item.get(), json.get("count").getAsInt());
            offers.add(new ShopOffer(tab, stack, json.get("price").getAsInt(), maxUses, 0));
        }
        return offers;
    }

    private record Pools(List<ShopOffer> buy, int buyRolls, List<ShopOffer> sell, int sellRolls) {

        private static final Pools EMPTY = new Pools(List.of(), 0, List.of(), 0);
    }
}
