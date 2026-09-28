/**
 * File: AvaliCushionItem.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.item.CushionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.ModEntities;

import java.lang.reflect.Method;

// Same placement as the vanilla cushion item, which is hardwired to the vanilla entity
public class AvaliCushionItem extends Item {

    // Lets the cushion sit on carpets and slabs like vanilla, the helper is private
    private static final Method RECALCULATE_CONTEXT = findRecalculateContext();

    public AvaliCushionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext original) {
        UseOnContext context = recalculateContext(original);
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext place = new BlockPlaceContext(context);
        BlockPos pos = place.getClickedPos();
        Vec3 spawn = Vec3.atCenterOfWithY(pos, context.getClickLocation().y);
        EntityType<Cushion> type = ModEntities.AVALI_CUSHION.get();
        AABB box = type.getSpawnAABB(spawn);
        if (!Cushion.canBePlacedAt(context.getLevel(), box)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (context.getLevel() instanceof ServerLevel level) {
            if (!level.getEntitiesOfClass(Cushion.class, box).isEmpty()) {
                return InteractionResult.FAIL;
            }
            Cushion cushion = type.create(level, EntityType.createDefaultStackConfig(level, stack, context.getPlayer()), pos,
                EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if (cushion == null) {
                return InteractionResult.FAIL;
            }
            cushion.snapTo(spawn, Direction.fromYRot(place.getRotation()).toYRot(), 0.0F);
            level.addFreshEntity(cushion);
            cushion.destroyIfInFire(level);
            level.playSound(null, cushion.getX(), cushion.getY(), cushion.getZ(), SoundEvents.CUSHION_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            cushion.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
            stack.consume(1, place.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    private static UseOnContext recalculateContext(UseOnContext context) {
        if (RECALCULATE_CONTEXT == null) {
            return context;
        }
        try {
            return (UseOnContext) RECALCULATE_CONTEXT.invoke(null, context);
        } catch (ReflectiveOperationException e) {
            return context;
        }
    }

    private static Method findRecalculateContext() {
        try {
            Method method = CushionItem.class.getDeclaredMethod("recalculateContextForSpecialCollisionShapes", UseOnContext.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            AvaliNexus.LOGGER.warn("[AvaliNexus] Avali cushions will not sit on carpets and slabs", e);
            return null;
        }
    }
}
