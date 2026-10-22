/**
 * File: AvaliBreeding.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.effect.ModEffects;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.reputation.ReputationTier;

import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class AvaliBreeding {

    private static final Identifier FILE = AvaliNexus.id("avali/behavior/breeding.json");
    private static final long TICKS_PER_DAY = 24000L;
    private static final int HEARTS = 7;
    private static final int GROWTH_PARTICLES = 10;

    public record Bond(double chance, double distance, double stayDistance) {
    }

    public record Baby(double chance, double partnerDistance, int foodPerParent, double cooldownDays, int packMax) {
    }

    public record Stuck(double leaveChance, double leaveDistance, double lonerChance, double lonerMinDistance, double lonerMaxDistance) {
    }

    public record Usage(ReputationTier minTier, int loveSeconds, float tonicReduction, double selectRange, float babyGrowth) {
    }

    public record Birth(double messageRadius, int xpMin, int xpMax, float rareInheritChance) {
    }

    public record Egg(int minSeconds, int maxSeconds, double hatchChance) {
    }

    public record Data(int checkSeconds, Bond bond, Baby baby, Stuck stuck, Usage items, Birth birth, Egg egg) {
    }

    private static final Data FALLBACK = new Data(30, new Bond(0.25, 16, 8), new Baby(0.1, 8, 1, 2, 8),
        new Stuck(0.05, 128, 0.05, 16, 24), new Usage(ReputationTier.NEUTRAL, 30, 0.5F, 16, 0.1F), new Birth(32, 1, 7, 0.5F), new Egg(600, 1200, 0.125));

    private static final Map<UUID, UUID> SELECTED = new HashMap<>();

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliBreeding() {
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

    public static void tick(AvaliEntity avali) {
        Data config = get(avali.level().getServer());
        if (avali.getPartnerId() != null) {
            AvaliEntity partner = avali.getPartner();
            if (partner != null && avali.isFemale() && canHaveBaby(avali, partner) && isCalm(avali) && isCalm(partner)
                    && avali.distanceTo(partner) <= config.baby().partnerDistance()
                    && avali.getGear().foodCount() >= config.baby().foodPerParent()
                    && partner.getGear().foodCount() >= config.baby().foodPerParent()
                    && avali.getRandom().nextDouble() < config.baby().chance()) {
                avali.getGear().consumeFood(config.baby().foodPerParent());
                partner.getGear().consumeFood(config.baby().foodPerParent());
                birth(avali, partner, null);
            }
            return;
        }
        List<AvaliEntity> members = avali.packMembers();
        List<AvaliEntity> candidates = members.stream().filter(other -> canBond(avali, other)).toList();
        if (!candidates.isEmpty()) {
            if (!isCalm(avali) || avali.getRandom().nextDouble() >= config.bond().chance()) {
                return;
            }
            candidates.stream()
                .filter(other -> avali.distanceTo(other) <= config.bond().distance() && isCalm(other))
                .min(Comparator.comparingDouble(avali::distanceToSqr))
                .ifPresent(other -> bond(avali, other));
            return;
        }
        double roll = avali.getRandom().nextDouble();
        if (roll < config.stuck().leaveChance()) {
            if (avali.getCamp() == null) {
                leavePack(avali, config);
            }
        } else if (roll < config.stuck().leaveChance() + config.stuck().lonerChance() && members.size() < config.baby().packMax()) {
            avali.spawnLoner(config.stuck().lonerMinDistance(), config.stuck().lonerMaxDistance());
        }
    }

    public static boolean canBond(AvaliEntity first, AvaliEntity second) {
        return first != second && second.isAlive() && !first.isBaby() && !second.isBaby() && !first.isCompanion() && !second.isCompanion()
            && first.isFemale() != second.isFemale() && first.getPackId() != null && first.getPackId().equals(second.getPackId())
            && first.getPartnerId() == null && second.getPartnerId() == null && !related(first, second);
    }

    public static boolean related(AvaliEntity first, AvaliEntity second) {
        List<UUID> firstParents = first.getParents();
        List<UUID> secondParents = second.getParents();
        return firstParents.contains(second.getUUID()) || secondParents.contains(first.getUUID())
            || firstParents.stream().anyMatch(secondParents::contains);
    }

    public static void bond(AvaliEntity first, AvaliEntity second) {
        BondData.get(first.level().getServer()).link(first, second);
        hearts(first);
        hearts(second);
    }

    private static boolean isCalm(AvaliEntity avali) {
        return !avali.isBusy() && AvaliFight.enemies(avali).isEmpty();
    }

    public static boolean canHaveBaby(AvaliEntity avali, AvaliEntity partner) {
        Data config = get(avali.level().getServer());
        return !avali.isCompanion() && !partner.isCompanion() && !avali.isBaby() && !partner.isBaby()
            && avali.getAge() == 0 && partner.getAge() == 0 && !avali.hasGrowingKit() && !partner.hasGrowingKit()
            && avali.packMembers().size() < config.baby().packMax();
    }

    public static void birth(AvaliEntity mother, AvaliEntity father, ServerPlayer cause) {
        ServerLevel level = (ServerLevel) mother.level();
        Data config = get(level.getServer());
        AvaliEntity kit = (AvaliEntity) mother.getBreedOffspring(level, father);
        if (kit == null) {
            return;
        }
        kit.setBaby(true);
        kit.snapTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
        level.addFreshEntityWithPassengers(kit);
        int cooldown = (int) (config.baby().cooldownDays() * TICKS_PER_DAY);
        for (AvaliEntity parent : List.of(mother, father)) {
            parent.setAge(cooldown);
            parent.setKit(kit.getUUID());
            parent.stopLove();
            hearts(parent);
            parent.playNoise();
        }
        if (cause != null) {
            ExperienceOrb.award(level, mother.position(), Mth.nextInt(mother.getRandom(), config.birth().xpMin(), config.birth().xpMax()));
        }
        Set<ServerPlayer> told = new HashSet<>(level.getEntitiesOfClass(ServerPlayer.class,
            mother.getBoundingBox().inflate(config.birth().messageRadius()), player -> player.distanceTo(mother) <= config.birth().messageRadius()));
        if (cause != null) {
            told.add(cause);
        }
        Component message = Component.translatable("avalinexus.breeding.born", Component.translatable("avalinexus.pack." + mother.getPack()));
        told.forEach(player -> player.sendSystemMessage(message, true));
    }

    public static void hearts(AvaliEntity avali) {
        if (avali.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, avali.getX(), avali.getY() + avali.getBbHeight() + 0.3, avali.getZ(), HEARTS,
                0.4, 0.3, 0.4, 0.02);
        }
    }

    private static void leavePack(AvaliEntity avali, Data config) {
        double range = config.stuck().leaveDistance();
        avali.level().getEntitiesOfClass(AvaliEntity.class, avali.getBoundingBox().inflate(range),
                other -> other.getPackId() != null && !other.getPackId().equals(avali.getPackId()) && other.getPartnerId() == null
                    && !other.isBaby() && !other.isCompanion() && other.isFemale() != avali.isFemale() && !related(avali, other)
                    && avali.distanceTo(other) <= range)
            .stream().min(Comparator.comparingDouble(avali::distanceToSqr))
            .ifPresent(avali::joinPackOf);
    }

    public static boolean useItem(AvaliEntity avali, ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Data config = get(player.level().getServer());
        if (avali.isCompanion()
                || AvaliReputation.tier(player, avali.getPack()).ordinal() < config.items().minTier().ordinal()) {
            return false;
        }
        if (avali.isBaby()) {
            return stack.is(ModItems.NEST_RATION.get()) && feedBaby(avali, player, stack, config);
        }
        if (stack.is(ModItems.BOND_FEATHER.get())) {
            return useFeather(avali, player, stack, config);
        }
        if (stack.is(ModItems.NEST_RATION.get())) {
            return useRation(avali, player, stack, config);
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (stack.is(Items.POTION) && contents != null && contents.is(ModEffects.tonicPotion()) && canTakeTonic(avali)) {
            avali.removeEffect(ModEffects.tonic());
            contents.forEachEffect(effect -> avali.addEffect(effect, player), 1.0F);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            return true;
        }
        return false;
    }

    private static boolean useFeather(AvaliEntity avali, ServerPlayer player, ItemStack stack, Data config) {
        if (avali.getPartnerId() != null) {
            return false;
        }
        Entity picked = Optional.ofNullable(SELECTED.get(player.getUUID())).map(id -> ((ServerLevel) avali.level()).getEntity(id)).orElse(null);
        if (picked instanceof AvaliEntity first && first.distanceTo(player) <= config.items().selectRange() && canBond(first, avali)
                && AvaliReputation.tier(player, first.getPack()).ordinal() >= config.items().minTier().ordinal()) {
            SELECTED.remove(player.getUUID());
            bond(first, avali);
            stack.consume(1, player);
            player.sendSystemMessage(Component.translatable("avalinexus.breeding.bonded", first.getName(), avali.getName()), true);
            return true;
        }
        SELECTED.put(player.getUUID(), avali.getUUID());
        hearts(avali);
        player.sendSystemMessage(Component.translatable("avalinexus.breeding.selected", avali.getName()), true);
        return true;
    }

    private static boolean useRation(AvaliEntity avali, ServerPlayer player, ItemStack stack, Data config) {
        AvaliEntity partner = avali.getPartner();
        if (partner == null || avali.isInLove() || !canHaveBaby(avali, partner)) {
            return false;
        }
        stack.consume(1, player);
        avali.startLove(player, config.items().loveSeconds() * 20);
        hearts(avali);
        if (partner.isInLove() && avali.distanceTo(partner) <= config.baby().partnerDistance()) {
            birth(avali.isFemale() ? avali : partner, avali.isFemale() ? partner : avali, player);
        }
        return true;
    }

    private static boolean feedBaby(AvaliEntity avali, ServerPlayer player, ItemStack stack, Data config) {
        int seconds = (int) (-avali.getAge() / 20 * config.items().babyGrowth());
        if (seconds <= 0) {
            return false;
        }
        stack.consume(1, player);
        avali.ageUp(seconds, true);
        ((ServerLevel) avali.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, avali.getX(), avali.getY() + avali.getBbHeight() * 0.5,
            avali.getZ(), GROWTH_PARTICLES, 0.3, 0.3, 0.3, 0.0);
        return true;
    }

    public static void tickLove(AvaliEntity avali) {
        AvaliEntity partner = avali.getPartner();
        hearts(avali);
        if (partner != null && partner.isInLove() && avali.isFemale()
                && avali.distanceTo(partner) <= get(avali.level().getServer()).baby().partnerDistance() && canHaveBaby(avali, partner)) {
            ServerPlayer cause = avali.getLoveCause();
            birth(avali, partner, cause != null ? cause : partner.getLoveCause());
        }
    }

    public static boolean canTakeTonic(AvaliEntity avali) {
        return !avali.isBaby() && !avali.isCompanion() && avali.getPartnerId() != null && avali.getAge() > 0;
    }

    public static void applyTonic(AvaliEntity avali) {
        float keep = 1.0F - get(avali.level().getServer()).items().tonicReduction();
        avali.setAge((int) (avali.getAge() * keep));
        AvaliEntity partner = avali.getPartner();
        if (partner != null && partner.getAge() > 0) {
            partner.setAge((int) (partner.getAge() * keep));
        }
    }

    public static void onTonic(AvaliEntity avali, Entity source) {
        Entity thrower = source instanceof Projectile projectile ? projectile.getOwner() : source;
        if (!canTakeTonic(avali) || thrower instanceof ServerPlayer player
                && AvaliReputation.tier(player, avali.getPack()).ordinal() < get(player.level().getServer()).items().minTier().ordinal()) {
            return;
        }
        applyTonic(avali);
    }

    private static Data load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing breeding file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject bond = root.getAsJsonObject("bond");
            JsonObject baby = root.getAsJsonObject("baby");
            JsonObject stuck = root.getAsJsonObject("stuck");
            JsonObject items = root.getAsJsonObject("items");
            JsonObject birth = root.getAsJsonObject("birth");
            JsonObject egg = root.getAsJsonObject("egg");
            return new Data(root.get("check_seconds").getAsInt(),
                new Bond(bond.get("chance").getAsDouble(), bond.get("distance").getAsDouble(), bond.get("stay_distance").getAsDouble()),
                new Baby(baby.get("chance").getAsDouble(), baby.get("partner_distance").getAsDouble(), baby.get("food_per_parent").getAsInt(),
                    baby.get("cooldown_days").getAsDouble(), baby.get("pack_max").getAsInt()),
                new Stuck(stuck.get("leave_chance").getAsDouble(), stuck.get("leave_distance").getAsDouble(),
                    stuck.get("loner_chance").getAsDouble(), stuck.get("loner_min_distance").getAsDouble(),
                    stuck.get("loner_max_distance").getAsDouble()),
                new Usage(ReputationTier.valueOf(items.get("min_tier").getAsString().toUpperCase(Locale.ROOT)),
                    items.get("love_seconds").getAsInt(), items.get("tonic_reduction").getAsFloat(), items.get("select_range").getAsDouble(),
                    items.get("baby_growth").getAsFloat()),
                new Birth(birth.get("message_radius").getAsDouble(), birth.get("xp_min").getAsInt(), birth.get("xp_max").getAsInt(),
                    birth.get("rare_inherit_chance").getAsFloat()),
                egg == null ? FALLBACK.egg() : new Egg(egg.get("min_seconds").getAsInt(), egg.get("max_seconds").getAsInt(),
                    egg.get("hatch_chance").getAsDouble()));
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read breeding file {}", FILE, e);
            return FALLBACK;
        }
    }
}
