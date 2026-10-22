/**
 * File: AvaliReputation.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.reputation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.ReputationPayload;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public final class AvaliReputation {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/reputation.json");
    private static final long TICKS_PER_DAY = 24000L;

    public enum Change {
        TRADE, GIFT, DEFEND, HIT, KILL_ADULT, KILL_BABY, STEAL, BREAK_MACHINE
    }

    public record Data(int min, int max, int hostile, int wary, int friendly, int ally, int trade, int gift, int defend, int hit,
                       int killAdult, int killBaby, int steal, int breakMachine, int tradeCap, int giftCap, int decayPerDay, float friendlyPrice, float allyPrice) {

        public ReputationTier tier(int value) {
            if (value <= hostile) {
                return ReputationTier.HOSTILE;
            }
            if (value <= wary) {
                return ReputationTier.WARY;
            }
            if (value >= ally) {
                return ReputationTier.ALLY;
            }
            return value >= friendly ? ReputationTier.FRIENDLY : ReputationTier.NEUTRAL;
        }

        public float priceBonus(ReputationTier tier) {
            return switch (tier) {
                case FRIENDLY -> friendlyPrice;
                case ALLY -> allyPrice;
                default -> 0.0F;
            };
        }

        private int amount(Change change) {
            return switch (change) {
                case TRADE -> trade;
                case GIFT -> gift;
                case DEFEND -> defend;
                case HIT -> hit;
                case KILL_ADULT -> killAdult;
                case KILL_BABY -> killBaby;
                case STEAL -> steal;
                case BREAK_MACHINE -> breakMachine;
            };
        }
    }

    private static final Data FALLBACK = new Data(-100, 100, -50, -10, 20, 60, 1, 2, 3, -5, -40, -100, -2, -10, 10, 10, 1, 0.1F, 0.2F);

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliReputation() {
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

    public static ReputationData.Standing standing(ServerPlayer player, String pack) {
        MinecraftServer server = player.level().getServer();
        ReputationData saved = ReputationData.get(server);
        ReputationData.Standing standing = saved.standing(player.getUUID(), pack);
        long day = day(player);
        if (standing.decayDay() < day && standing.value() < 0) {
            int healed = (int) Math.min(-standing.value(), (day - standing.decayDay()) * get(server).decayPerDay());
            standing = new ReputationData.Standing(standing.value() + healed, standing.met(), day, standing.capDay(),
                standing.giftGained(), standing.tradeGained());
            saved.set(player.getUUID(), pack, standing);
        }
        return standing;
    }

    public static int value(ServerPlayer player, String pack) {
        return standing(player, pack).value();
    }

    public static ReputationTier tier(ServerPlayer player, String pack) {
        return get(player.level().getServer()).tier(value(player, pack));
    }

    public static void meet(ServerPlayer player, String pack) {
        ReputationData.Standing standing = standing(player, pack);
        if (!standing.met() && !pack.isEmpty()) {
            ReputationData.get(player.level().getServer()).set(player.getUUID(), pack, new ReputationData.Standing(standing.value(), true,
                standing.decayDay(), standing.capDay(), standing.giftGained(), standing.tradeGained()));
        }
    }

    public static void change(ServerPlayer player, String pack, Change change) {
        if (pack.isEmpty()) {
            return;
        }
        Data config = get(player.level().getServer());
        ReputationData.Standing standing = standing(player, pack);
        long day = day(player);
        int gift = standing.capDay() == day ? standing.giftGained() : 0;
        int trade = standing.capDay() == day ? standing.tradeGained() : 0;
        int amount = config.amount(change);
        if (change == Change.GIFT) {
            amount = Math.min(amount, Math.max(0, config.giftCap() - gift));
            gift += amount;
        } else if (change == Change.TRADE) {
            amount = Math.min(amount, Math.max(0, config.tradeCap() - trade));
            trade += amount;
        }
        apply(player, pack, standing.value() + amount, day, gift, trade);
    }

    public static void set(ServerPlayer player, String pack, int value) {
        ReputationData.Standing standing = standing(player, pack);
        apply(player, pack, value, standing.capDay(), standing.giftGained(), standing.tradeGained());
    }

    private static void apply(ServerPlayer player, String pack, int value, long capDay, int gift, int trade) {
        Data config = get(player.level().getServer());
        ReputationData.Standing standing = standing(player, pack);
        int clamped = Mth.clamp(value, config.min(), config.max());
        ReputationTier before = config.tier(standing.value());
        ReputationData.get(player.level().getServer()).set(player.getUUID(), pack,
            new ReputationData.Standing(clamped, true, Math.max(standing.decayDay(), day(player)), capDay, gift, trade));
        ReputationTier after = config.tier(clamped);
        if (before != after) {
            player.sendSystemMessage(Component.translatable("avalinexus.reputation.changed",
                Component.translatable("avalinexus.pack." + pack), after.label().copy().withColor(after.color())), true);
        }
    }

    public static void sendScreen(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        Data config = get(server);
        List<ReputationPayload.Entry> packs = AvaliNames.packs(server).stream().map(pack -> {
            ReputationData.Standing standing = standing(player, pack);
            return new ReputationPayload.Entry(pack, AvaliAppearance.packColor(server, pack), standing.value(), standing.met());
        }).toList();
        ModNetworking.sendToPlayer(player, new ReputationPayload(packs,
            List.of(config.min(), config.max(), config.hostile(), config.wary(), config.friendly(), config.ally()),
            Math.round(config.friendlyPrice() * 100), Math.round(config.allyPrice() * 100)));
    }

    private static long day(ServerPlayer player) {
        return player.level().getOverworldClockTime() / TICKS_PER_DAY;
    }

    private static Data load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing reputation file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject tiers = root.getAsJsonObject("tiers");
            JsonObject changes = root.getAsJsonObject("changes");
            JsonObject cap = root.getAsJsonObject("daily_cap");
            JsonObject prices = root.getAsJsonObject("prices");
            return new Data(root.get("min").getAsInt(), root.get("max").getAsInt(),
                tiers.get("hostile").getAsInt(), tiers.get("wary").getAsInt(), tiers.get("friendly").getAsInt(), tiers.get("ally").getAsInt(),
                changes.get("trade").getAsInt(), changes.get("gift").getAsInt(), changes.get("defend").getAsInt(),
                changes.get("hit").getAsInt(), changes.get("kill_adult").getAsInt(), changes.get("kill_baby").getAsInt(),
                changes.get("steal").getAsInt(), changes.get("break_machine").getAsInt(),
                cap.get("trade").getAsInt(), cap.get("gift").getAsInt(), root.get("decay_per_day").getAsInt(),
                prices.get("friendly").getAsFloat(), prices.get("ally").getAsFloat());
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read reputation file {}", FILE, e);
            return FALLBACK;
        }
    }
}
