/**
 * File: AvaliCushion.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.entity.cushion;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import org.furranystudio.avalinexus.item.ModItems;

// Vanilla cushion with the Avali colors, it drops and picks its own item instead of a dyed one
public class AvaliCushion extends Cushion {

    private static final int LIGHTNING_DROP_INVULNERABLE_TICKS = 20;

    public AvaliCushion(EntityType<Cushion> type, Level level) {
        super(type, level);
        // Orange keeps the vanilla hit particles in the right color
        setColor(DyeColor.ORANGE);
    }

    @Override
    public void dropItem(ServerLevel level, Entity causedBy) {
        playSound(SoundEvents.CUSHION_BREAK, 1.0F, 1.0F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WOOL.pick(DyeColor.ORANGE).defaultBlockState()),
            getX(), getY(0.6666666666666666), getZ(), 10, getBbWidth() / 4.0F, getBbHeight() / 4.0F, getBbWidth() / 4.0F, 0.05);
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }
        if (causedBy instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        ItemEntity drop = spawnAtLocation(level, getPickResult());
        if (drop != null && causedBy instanceof LightningBolt) {
            drop.setInvulnerableTime(LIGHTNING_DROP_INVULNERABLE_TICKS);
        }
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(ModItems.AVALI_CUSHION.get());
        stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        return stack;
    }
}
