/**
 * File: ShopManager.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.trade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.OpenShopPayload;
import org.furranystudio.avalinexus.network.packet.ShopUpdatePayload;
import org.furranystudio.avalinexus.reputation.AvaliReputation;

import java.util.List;
import java.util.function.Predicate;

public final class ShopManager {

    private static final long TICKS_PER_DAY = 24000L;
    private static final int GESTURE_TICKS = 40;

    private ShopManager() {
    }

    public static void open(ServerPlayer player, AvaliEntity avali) {
        AvaliShop shop = shopOf(avali);
        List<ShopOffer> offers = shop.offers().subList(0, shop.visible(unlocked(player, avali))).stream()
            .map(offer -> offer.withPrice(price(player, avali, offer))).toList();
        ModNetworking.sendToPlayer(player, new OpenShopPayload(avali.getId(), offers));
    }

    // Called from the dialogue once the player picked an offer, the dialogue already checked the session and distance
    public static void trade(ServerPlayer player, AvaliEntity avali, int index) {
        AvaliShop shop = shopOf(avali);
        if (index < 0 || index >= shop.visible(unlocked(player, avali))) {
            return;
        }
        ShopOffer offer = shop.offers().get(index);
        if (offer.remaining() <= 0) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemStack nexite = new ItemStack(ModItems.NEXITE_SHARD.get());
        int price = price(player, avali, offer);
        boolean done;
        if (offer.tab() == ShopTab.BUY) {
            done = count(inventory, nexite) >= price;
            if (done) {
                remove(inventory, stack -> ItemStack.isSameItemSameComponents(stack, nexite), price);
                give(player, offer.item().copy());
            }
        } else {
            done = owned(inventory, offer) >= offer.item().getCount();
            if (done) {
                remove(inventory, offer::takes, offer.item().getCount());
                give(player, nexite.copyWithCount(price));
            }
        }
        if (!done) {
            return;
        }
        AvaliReputation.change(player, avali.getPack(), AvaliReputation.Change.TRADE);

        shop.set(index, offer.used());
        avali.playGesture(AvaliGesture.CONTENT, GESTURE_TICKS);
        avali.playNoise();
        ModNetworking.sendToPlayer(player, new ShopUpdatePayload(avali.getId(), index, shop.offers().get(index).uses()));
    }

    private static int price(ServerPlayer player, AvaliEntity avali, ShopOffer offer) {
        AvaliReputation.Data reputation = AvaliReputation.get(player.level().getServer());
        float bonus = reputation.priceBonus(AvaliReputation.tier(player, avali.getPack()));
        if (offer.tab() == ShopTab.BUY) {
            return Math.max(1, Math.round(offer.price() * (1.0F - bonus)));
        }
        return Math.round(offer.price() * (1.0F + bonus));
    }

    private static ShopSlot unlocked(ServerPlayer player, AvaliEntity avali) {
        return switch (AvaliReputation.tier(player, avali.getPack())) {
            case ALLY -> ShopSlot.ALLY;
            case FRIENDLY -> ShopSlot.FRIENDLY;
            default -> ShopSlot.SELL;
        };
    }

    private static AvaliShop shopOf(AvaliEntity avali) {
        ServerLevel level = (ServerLevel) avali.level();
        long day = level.getOverworldClockTime() / TICKS_PER_DAY;
        AvaliShop shop = avali.getShop();
        if (shop == null || !shop.profession().equals(avali.getProfession())) {
            shop = TradeData.rollShop(level.getServer(), avali.getRandom(), avali.getProfession(), day);
            avali.setShop(shop);
        }
        if (avali.isCompanion()) {
            shop.pause(day);
            return shop;
        }
        if (shop.restockIfNewDay(day)) {
            AvaliNexus.debug("{}: restocks its {} shop", avali.debugName(), shop.profession());
        }
        if (shop.shouldRotate(day, TradeData.settings(level.getServer()).rotationDays())) {
            TradeData.rotate(level.getServer(), avali.getRandom(), shop);
            shop.rotated(day);
            AvaliNexus.debug("{}: rotates the offers of its {} shop", avali.debugName(), shop.profession());
        }
        return shop;
    }

    public static int count(Inventory inventory, ItemStack wanted) {
        return count(inventory, stack -> ItemStack.isSameItemSameComponents(stack, wanted));
    }

    public static int owned(Inventory inventory, ShopOffer offer) {
        return count(inventory, offer::takes);
    }

    private static int count(Inventory inventory, Predicate<ItemStack> wanted) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && wanted.test(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void remove(Inventory inventory, Predicate<ItemStack> wanted, int amount) {
        for (int slot = 0; slot < inventory.getContainerSize() && amount > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && wanted.test(stack)) {
                int taken = Math.min(amount, stack.getCount());
                stack.shrink(taken);
                amount -= taken;
            }
        }
        inventory.setChanged();
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.spawnAtLocation((ServerLevel) player.level(), stack);
        }
    }
}
