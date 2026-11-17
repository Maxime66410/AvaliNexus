/**
 * File: NanocanvasDyedRecipe.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-17
 */
package org.furranystudio.avalinexus.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class NanocanvasDyedRecipe extends ShapedRecipe {

    public static final MapCodec<NanocanvasDyedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter((NanocanvasDyedRecipe recipe) -> recipe.commonInfo),
        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter((NanocanvasDyedRecipe recipe) -> recipe.bookInfo),
        ShapedRecipePattern.MAP_CODEC.forGetter((NanocanvasDyedRecipe recipe) -> recipe.layout),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter((NanocanvasDyedRecipe recipe) -> recipe.output)
    ).apply(instance, NanocanvasDyedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NanocanvasDyedRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
        ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.layout,
        ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
        NanocanvasDyedRecipe::new);

    public static final RegistryEntry<RecipeSerializer<NanocanvasDyedRecipe>> SERIALIZER = ModRegistry.register(Registries.RECIPE_SERIALIZER,
        "nanocanvas_dyed", key -> new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC));

    private final ShapedRecipePattern layout;
    private final ItemStackTemplate output;

    public NanocanvasDyedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern layout, ItemStackTemplate output) {
        super(commonInfo, bookInfo, layout, output);
        this.layout = layout;
        this.output = output;
    }

    public static void init() {
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        List<DyeColor> colors = new ArrayList<>();
        for (ItemStack stack : input.items()) {
            DyeColor color = color(stack);
            if (color != null) {
                colors.add(color);
            }
        }
        ItemStack result = output.create();
        return colors.isEmpty() ? result : DyedItemColor.applyDyes(result, colors);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER.get();
    }

    private static DyeColor color(ItemStack stack) {
        for (DyeColor color : DyeColor.values()) {
            if (stack.is(NanocanvasBlocks.item(NanocanvasBlocks.Shape.BLOCK, color))) {
                return color;
            }
        }
        return null;
    }
}
