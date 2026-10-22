/**
 * File: TradeData.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.trade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.furranystudio.avalinexus.AvaliNexus;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class TradeData {

    private static final Identifier SETTINGS = AvaliNexus.id("avali/trades/professions.json");
    private static final String RANDOM = "random";
    private static final int ROTATION_TRIES = 8;

    public record Settings(List<String> professions, int rotationDays, float inheritChance) {
        private static final Settings EMPTY = new Settings(List.of(), 3, 0.5F);
    }

    private record Template(ShopTab tab, List<Item> items, Optional<TagKey<Item>> accepts, int count, int price,
                            List<Identifier> enchantments, Optional<Identifier> potion) {
    }

    private record Pool(int buyRolls, int sellRolls, int maxUses, Map<ShopSlot, List<Template>> templates) {
        private static final Pool EMPTY = new Pool(0, 0, 0, Map.of());
    }

    private static ResourceManager loadedFrom;
    private static Settings settings = Settings.EMPTY;
    private static final Map<String, Pool> POOLS = new HashMap<>();

    private TradeData() {
    }

    public static Settings settings(MinecraftServer server) {
        refresh(server);
        return settings;
    }

    public static String randomProfession(MinecraftServer server, RandomSource random) {
        List<String> professions = settings(server).professions();
        return professions.isEmpty() ? "" : professions.get(random.nextInt(professions.size()));
    }

    public static AvaliShop rollShop(MinecraftServer server, RandomSource random, String profession, long day) {
        Pool pool = pool(server, profession);
        List<ShopOffer> offers = new ArrayList<>();
        List<ShopSlot> slots = new ArrayList<>();
        for (ShopSlot slot : ShopSlot.values()) {
            for (Template template : pick(pool.templates().getOrDefault(slot, List.of()), rolls(pool, slot), random)) {
                offers.add(roll(server, template, pool.maxUses(), random));
                slots.add(slot);
            }
        }
        return new AvaliShop(profession, offers, slots, day, day);
    }

    public static void rotate(MinecraftServer server, RandomSource random, AvaliShop shop) {
        if (shop.offers().isEmpty()) {
            return;
        }
        Pool pool = pool(server, shop.profession());
        int index = random.nextInt(shop.offers().size());
        ShopSlot slot = shop.slots().get(index);
        List<Template> templates = pool.templates().getOrDefault(slot, List.of());
        if (templates.isEmpty()) {
            return;
        }
        for (int attempt = 0; attempt < ROTATION_TRIES; attempt++) {
            ShopOffer offer = roll(server, templates.get(random.nextInt(templates.size())), pool.maxUses(), random);
            boolean taken = false;
            for (int other = 0; other < shop.offers().size(); other++) {
                taken |= other != index && shop.slots().get(other) == slot
                    && ItemStack.isSameItem(shop.offers().get(other).item(), offer.item());
            }
            if (!taken) {
                shop.set(index, offer);
                return;
            }
        }
    }

    private static int rolls(Pool pool, ShopSlot slot) {
        return switch (slot) {
            case BUY -> pool.buyRolls();
            case SELL -> pool.sellRolls();
            case FRIENDLY, ALLY -> 1;
        };
    }

    private static List<Template> pick(List<Template> templates, int rolls, RandomSource random) {
        List<Template> shuffled = new ArrayList<>(templates);
        Collections.shuffle(shuffled, new Random(random.nextLong()));
        return shuffled.subList(0, Math.min(rolls, shuffled.size()));
    }

    private static ShopOffer roll(MinecraftServer server, Template template, int maxUses, RandomSource random) {
        Item item = template.items().get(random.nextInt(template.items().size()));
        ItemStack stack = new ItemStack(item, template.count());
        if (template.potion().isPresent()) {
            Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION.get(template.potion().get());
            if (potion.isPresent()) {
                stack = PotionContents.createItemStack(item, potion.get());
                stack.setCount(template.count());
            }
        }
        if (!template.enchantments().isEmpty()) {
            stack = enchant(server, stack, template.enchantments(), random);
        }
        return new ShopOffer(template.tab(), stack, template.price(), maxUses, 0, template.accepts());
    }

    private static ItemStack enchant(MinecraftServer server, ItemStack stack, List<Identifier> ids, RandomSource random) {
        HolderLookup.RegistryLookup<Enchantment> registry = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> choices = new ArrayList<>();
        if (ids.size() == 1 && ids.getFirst().getPath().equals(RANDOM)) {
            registry.get(EnchantmentTags.ON_RANDOM_LOOT).ifPresent(tag -> tag.forEach(choices::add));
        } else {
            for (Identifier id : ids) {
                registry.get(ResourceKey.create(Registries.ENCHANTMENT, id)).ifPresent(choices::add);
            }
        }
        if (choices.isEmpty()) {
            return stack;
        }
        Holder<Enchantment> enchantment = choices.get(random.nextInt(choices.size()));
        Enchantment value = enchantment.value();
        int level = value.getMinLevel() + random.nextInt(value.getMaxLevel() - value.getMinLevel() + 1);
        if (stack.is(Items.BOOK)) {
            return EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level));
        }
        stack.enchant(enchantment, level);
        return stack;
    }

    public static void reload(MinecraftServer server) {
        loadedFrom = null;
        refresh(server);
    }

    private static void refresh(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            POOLS.clear();
            settings = loadSettings(manager);
        }
    }

    private static Pool pool(MinecraftServer server, String profession) {
        refresh(server);
        return POOLS.computeIfAbsent(profession, id -> loadPool(loadedFrom, id));
    }

    private static Settings loadSettings(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(SETTINGS);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing trades file {}", SETTINGS);
            return Settings.EMPTY;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            List<String> professions = new ArrayList<>();
            root.getAsJsonArray("professions").forEach(element -> professions.add(element.getAsString()));
            return new Settings(List.copyOf(professions), root.get("rotation_days").getAsInt(), root.get("inherit_chance").getAsFloat());
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read trades file {}", SETTINGS, e);
            return Settings.EMPTY;
        }
    }

    private static Pool loadPool(ResourceManager manager, String profession) {
        Identifier file = AvaliNexus.id("avali/trades/" + profession + ".json");
        Optional<Resource> resource = profession.isEmpty() ? Optional.empty() : manager.getResource(file);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing shop file {}", file);
            return Pool.EMPTY;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<ShopSlot, List<Template>> templates = new EnumMap<>(ShopSlot.class);
            for (ShopSlot slot : ShopSlot.values()) {
                ShopTab tab = slot == ShopSlot.SELL ? ShopTab.SELL : ShopTab.BUY;
                templates.put(slot, templates(root, slot.name().toLowerCase(Locale.ROOT), tab, file));
            }
            return new Pool(root.get("buy_rolls").getAsInt(), root.get("sell_rolls").getAsInt(), root.get("max_uses").getAsInt(), templates);
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read shop file {}", file, e);
            return Pool.EMPTY;
        }
    }

    private static List<Template> templates(JsonObject root, String key, ShopTab tab, Identifier file) {
        List<Template> templates = new ArrayList<>();
        if (!root.has(key)) {
            return templates;
        }
        for (JsonElement element : root.getAsJsonArray(key)) {
            JsonObject json = element.getAsJsonObject();
            List<Item> items = new ArrayList<>();
            Optional<TagKey<Item>> accepts = Optional.empty();
            if (json.has("tag")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(json.get("tag").getAsString().replace("#", "")));
                BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> items.add(holder.value()));
                if (tab == ShopTab.SELL) {
                    accepts = Optional.of(tag);
                }
            } else if (json.has("items")) {
                json.getAsJsonArray("items").forEach(id -> item(id.getAsString(), file).ifPresent(items::add));
            } else {
                item(json.get("item").getAsString(), file).ifPresent(items::add);
            }
            if (items.isEmpty()) {
                AvaliNexus.LOGGER.warn("[AvaliNexus] Empty offer {} in {}", json, file);
                continue;
            }
            List<Identifier> enchantments = new ArrayList<>();
            if (json.has("enchant")) {
                JsonElement enchant = json.get("enchant");
                if (enchant.isJsonArray()) {
                    enchant.getAsJsonArray().forEach(id -> enchantments.add(Identifier.parse(id.getAsString())));
                } else {
                    enchantments.add(Identifier.withDefaultNamespace(enchant.getAsString()));
                }
            }
            Optional<Identifier> potion = json.has("potion") ? Optional.of(Identifier.parse(json.get("potion").getAsString())) : Optional.empty();
            templates.add(new Template(tab, List.copyOf(items), accepts, json.get("count").getAsInt(), json.get("price").getAsInt(),
                List.copyOf(enchantments), potion));
        }
        return templates;
    }

    private static Optional<Item> item(String id, Identifier file) {
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id));
        if (item.isEmpty()) {
            AvaliNexus.LOGGER.warn("[AvaliNexus] Unknown item {} in {}", id, file);
        }
        return item;
    }
}
