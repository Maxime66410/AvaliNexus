/**
 * File: BlockExplosionMixin.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.furranystudio.avalinexus.reputation.VillageWatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(BlockBehaviour.class)
public class BlockExplosionMixin {

    @Inject(method = "onExplosionHit", at = @At("HEAD"))
    private void avalinexus$forgetVillageBlock(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                               BiConsumer<ItemStack, BlockPos> drops, CallbackInfo ci) {
        if (explosion.getBlockInteraction() != Explosion.BlockInteraction.KEEP && VillageWatch.isVillageBlock(level, pos)) {
            VillageWatch.forget(level, pos);
        }
    }
}
