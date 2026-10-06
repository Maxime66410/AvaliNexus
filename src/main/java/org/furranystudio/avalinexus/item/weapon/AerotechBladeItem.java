/**
 * File: AerotechBladeItem.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.furranystudio.avalinexus.sound.ModSounds;

// Wears down one point per hit, Unbreaking included, and turns into the hilt when the crystal is spent
public class AerotechBladeItem extends Item {

    public AerotechBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level) || attacker instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        int damage = stack.getDamageValue() + EnchantmentHelper.processDurabilityChange(level, stack, 1);
        if (damage < stack.getMaxDamage()) {
            stack.setDamageValue(damage);
            return;
        }
        if (attacker.getMainHandItem() == stack) {
            attacker.setItemInHand(InteractionHand.MAIN_HAND, shatter(stack));
            ModSounds.play(level, attacker, ModSounds.AEROTECH_SHATTER.get(), 1.0F, 0.06F);
        }
    }

    // Keeps enchantments and name, the hilt remembers which blade to print back
    static ItemStack shatter(ItemStack blade) {
        ItemStack hilt = blade.transmuteCopy(AerotechWeapons.AEROTECH_HILT.get());
        hilt.remove(DataComponents.DAMAGE);
        hilt.set(AerotechWeapons.BLADE.get(), BuiltInRegistries.ITEM.getKey(blade.getItem()));
        hilt.set(AerotechWeapons.REFORGE.get(), 0);
        return hilt;
    }
}
