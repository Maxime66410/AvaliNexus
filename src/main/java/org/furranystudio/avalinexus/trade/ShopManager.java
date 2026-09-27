/**
 * File: ShopManager.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.trade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.OpenShopPayload;
import org.furranystudio.avalinexus.network.packet.ShopUpdatePayload;

public final class ShopManager {

    private static final long TICKS_PER_DAY = 24000L;
    private static final int GESTURE_TICKS = 40;

    private ShopManager() {
    }

    public static void open(ServerPlayer player, AvaliEntity avali) {
        AvaliShop shop = shopOf(avali);
        ModNetworking.sendToPlayer(player, new OpenShopPayload(avali.getId(), shop.offers()));
    }

    // Called from the dialogue once the player picked an offer, the dialogue already checked the session and distance
    public static void trade(ServerPlayer player, AvaliEntity avali, int index) {
        AvaliShop shop = shopOf(avali);
        if (index < 0 || index >= shop.offers().size()) {
            return;
        }
        ShopOffer offer = shop.offers().get(index);
        if (offer.remaining() <= 0) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemStack nexite = new ItemStack(ModItems.NEXITE_SHARD.get());
        boolean done;
        if (offer.tab() == ShopTab.BUY) {
            done = count(inventory, nexite) >= offer.price();
            if (done) {
                remove(inventory, nexite, offer.price());
                give(player, offer.item().copy());
            }
        } else {
            done = count(inventory, offer.item()) >= offer.item().getCount();
            if (done) {
                remove(inventory, offer.item(), offer.item().getCount());
                give(player, nexite.copyWithCount(offer.price()));
            }
        }
        if (!done) {
            return;
        }

        shop.set(index, offer.used());
        avali.playGesture(AvaliGesture.CONTENT, GESTURE_TICKS);
        avali.playNoise();
        ModNetworking.sendToPlayer(player, new ShopUpdatePayload(avali.getId(), index, shop.offers().get(index).uses()));
    }

    private static AvaliShop shopOf(AvaliEntity avali) {
        ServerLevel level = (ServerLevel) avali.level();
        long day = level.getOverworldClockTime() / TICKS_PER_DAY;
        AvaliShop shop = avali.getShop();
        if (shop == null) {
            shop = TradeData.rollShop(level.getServer(), avali.getRandom(), day);
            avali.setShop(shop);
        }
        shop.restockIfNewDay(day);
        return shop;
    }

    public static int count(Inventory inventory, ItemStack wanted) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void remove(Inventory inventory, ItemStack wanted, int amount) {
        for (int slot = 0; slot < inventory.getContainerSize() && amount > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
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
