/**
 * File: GrapheneTankItem.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidContent;

import java.util.function.Consumer;

public class GrapheneTankItem extends BlockItem {

    public GrapheneTankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        FluidContent content = stack.getOrDefault(AvaliLiquids.STORED_FLUID.get(), FluidContent.EMPTY);
        String capacity = GrapheneTankBlock.buckets(GrapheneTankBlockEntity.CAPACITY);
        tooltip.accept((content.isEmpty()
            ? Component.translatable("message.avalinexus.tank.empty", capacity)
            : Component.translatable("message.avalinexus.tank.content", AvaliLiquids.name(content.fluid()), GrapheneTankBlock.buckets(content.amount()), capacity))
            .withStyle(ChatFormatting.GRAY));
    }
}
