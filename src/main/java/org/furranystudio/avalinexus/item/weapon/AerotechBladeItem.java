/**
 * File: AerotechBladeItem.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item.weapon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.furranystudio.avalinexus.sound.ModSounds;

public class AerotechBladeItem extends Item {

    public AerotechBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        wear(stack, attacker, 1);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool != null && tool.damagePerBlock() > 0 && state.getDestroySpeed(level, pos) != 0.0F) {
            wear(stack, miner, tool.damagePerBlock());
        }
        return true;
    }

    private static void wear(ItemStack stack, LivingEntity user, int amount) {
        if (!(user.level() instanceof ServerLevel level) || user instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        int damage = stack.getDamageValue() + EnchantmentHelper.processDurabilityChange(level, stack, amount);
        if (damage < stack.getMaxDamage()) {
            stack.setDamageValue(damage);
            return;
        }
        if (user.getMainHandItem() == stack) {
            user.setItemInHand(InteractionHand.MAIN_HAND, shatter(stack));
            ModSounds.play(level, user, ModSounds.AEROTECH_SHATTER.get(), 1.0F, 0.06F);
        }
    }

    static ItemStack shatter(ItemStack blade) {
        ItemStack hilt = blade.transmuteCopy(AerotechWeapons.AEROTECH_HILT.get());
        hilt.remove(DataComponents.DAMAGE);
        hilt.set(AerotechWeapons.BLADE.get(), BuiltInRegistries.ITEM.getKey(blade.getItem()));
        hilt.set(AerotechWeapons.REFORGE.get(), 0);
        return hilt;
    }
}
