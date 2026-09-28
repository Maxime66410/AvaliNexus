/**
 * File: TapestryItem.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.furranystudio.avalinexus.entity.tapestry.AvaliTapestry;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;

// Hangs a tapestry on the clicked face, like the vanilla item frame item
public class TapestryItem extends Item {

    private final TapestryPattern pattern;
    private final DyeColor color;

    public TapestryItem(TapestryPattern pattern, DyeColor color, Properties properties) {
        super(properties);
        this.pattern = pattern;
        this.color = color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(pos, direction, stack)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        AvaliTapestry tapestry = new AvaliTapestry(level, pos, direction, pattern, color);
        if (!tapestry.survives() || isTaken(level, tapestry)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            tapestry.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, tapestry.position());
            level.addFreshEntity(tapestry);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    // One tapestry per face, blocks and other decorations are fine
    private static boolean isTaken(Level level, AvaliTapestry tapestry) {
        return !level.getEntitiesOfClass(AvaliTapestry.class, tapestry.getBoundingBox(),
            other -> other.getPos().equals(tapestry.getPos()) && other.getDirection() == tapestry.getDirection()).isEmpty();
    }
}
