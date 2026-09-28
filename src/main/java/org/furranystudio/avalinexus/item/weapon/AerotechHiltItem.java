/**
 * File: AerotechHiltItem.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.furranystudio.avalinexus.item.ModItems;

import java.util.function.Consumer;

// A spent blade: right click with aerogel to print it back, or wait five minutes and it reforges on its own
public class AerotechHiltItem extends Item {

    private static final int REFORGE_SECONDS = 300;
    private static final int BAR_COLOR = 0xFFA640;

    public AerotechHiltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack hilt = player.getItemInHand(hand);
        if (!player.hasInfiniteMaterials() && !consumeAerogel(player.getInventory())) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            player.setItemInHand(hand, reforge(hilt));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack hilt, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player) || level.getGameTime() % 20 != 0) {
            return;
        }
        int seconds = hilt.getOrDefault(AerotechWeapons.REFORGE.get(), 0) + 1;
        if (seconds < REFORGE_SECONDS) {
            hilt.set(AerotechWeapons.REFORGE.get(), seconds);
            return;
        }
        // The stack can't change its item, so the reforged blade takes its slot
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == hilt) {
                inventory.setItem(i, reforge(hilt));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.4F);
                return;
            }
        }
    }

    // The bar shows how far the blade reforged on its own
    @Override
    public boolean isBarVisible(ItemStack hilt) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack hilt) {
        return Math.round(13.0F * hilt.getOrDefault(AerotechWeapons.REFORGE.get(), 0) / REFORGE_SECONDS);
    }

    @Override
    public int getBarColor(ItemStack hilt) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack hilt, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Item blade = blade(hilt);
        tooltip.accept(Component.translatable("item.avalinexus.aerotech_hilt.blade", blade.getName(blade.getDefaultInstance())).withStyle(style -> style.withColor(0xAAAAAA)));
        tooltip.accept(Component.translatable("item.avalinexus.aerotech_hilt.reforge").withStyle(style -> style.withColor(0xFFA640)));
    }

    static ItemStack reforge(ItemStack hilt) {
        ItemStack blade = hilt.transmuteCopy(blade(hilt));
        blade.remove(AerotechWeapons.BLADE.get());
        blade.remove(AerotechWeapons.REFORGE.get());
        blade.remove(DataComponents.DAMAGE);
        return blade;
    }

    private static Item blade(ItemStack hilt) {
        Identifier id = hilt.get(AerotechWeapons.BLADE.get());
        Item blade = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
        return blade instanceof AerotechBladeItem ? blade : AerotechWeapons.AEROTECH_BLADE.get();
    }

    private static boolean consumeAerogel(Inventory inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.AEROGEL.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
