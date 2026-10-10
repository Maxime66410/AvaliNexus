/**
 * File: RailMagazineItem.java
 * Author: Maxime66410
 * Created: 2026-10-10
 * Last Modified: 2026-10-10
 */
package org.furranystudio.avalinexus.item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.function.Consumer;

public class RailMagazineItem extends Item {

    private static final int BAR_COLOR = 0xFF8C1A;

    private final int capacity;

    public RailMagazineItem(int capacity, Properties properties) {
        super(properties);
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public static ItemStack withAmmo(Item magazine, int ammo) {
        ItemStack stack = new ItemStack(magazine);
        stack.set(RailWeapons.AMMO.get(), ammo);
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack magazine = player.getItemInHand(hand);
        int ammo = RailGunItem.ammo(magazine);
        int missing = capacity - ammo;
        if (missing <= 0) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int loaded = player.hasInfiniteMaterials() ? missing : RailGunItem.takeQuills(player.getInventory(), missing);
        if (loaded == 0) {
            player.sendOverlayMessage(Component.translatable("item.avalinexus.rail_gun.no_quills"));
            return InteractionResult.FAIL;
        }
        magazine.set(RailWeapons.AMMO.get(), ammo + loaded);
        ModSounds.play(level, player, ModSounds.RAIL_SHELL_INSERT.get(), 0.7F, 0.06F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack magazine) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack magazine) {
        return Math.round(13.0F * RailGunItem.ammo(magazine) / capacity);
    }

    @Override
    public int getBarColor(ItemStack magazine) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack magazine, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.avalinexus.rail_magazine.ammo", RailGunItem.ammo(magazine), capacity).withStyle(style -> style.withColor(0xFFA640)));
        tooltip.accept(Component.translatable("item.avalinexus.rail_magazine.fill").withStyle(style -> style.withColor(0xAAAAAA)));
    }
}
