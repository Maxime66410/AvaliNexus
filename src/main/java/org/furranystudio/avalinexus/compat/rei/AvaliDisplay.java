/**
 * File: AvaliDisplay.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerBlockEntity;
import org.furranystudio.avalinexus.block.press.GraphenePressBlockEntity;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryBlockEntity;
import org.furranystudio.avalinexus.block.workbench.WeaponRecipe;
import org.furranystudio.avalinexus.compat.AvaliRecipes;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AvaliDisplay extends BasicDisplay {

    public static final DisplaySerializer<AvaliDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("kind").forGetter(display -> display.kind.name()),
            Codec.INT.fieldOf("index").forGetter(display -> display.index)
        ).apply(instance, (kind, index) -> create(Kind.valueOf(kind), index))),
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, display -> display.kind.name(),
            ByteBufCodecs.VAR_INT, display -> display.index,
            (kind, index) -> create(Kind.valueOf(kind), index)));

    public enum Kind {
        REFINERY("ammonia_refinery"),
        FREEZER("cryo_freezer"),
        PRESS("graphene_press"),
        WORKBENCH("weapon_workbench"),
        LOOM("avali_loom");

        public final CategoryIdentifier<AvaliDisplay> id;

        Kind(String name) {
            id = CategoryIdentifier.of(AvaliNexus.id(name));
        }
    }

    private final Kind kind;
    private final int index;
    private final int duration;

    private AvaliDisplay(Kind kind, int index, int duration, List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs, Optional.empty());
        this.kind = kind;
        this.index = index;
        this.duration = duration;
    }

    public static List<AvaliDisplay> all() {
        List<AvaliDisplay> displays = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            int count = switch (kind) {
                case REFINERY -> AvaliRecipes.refining().size();
                case FREEZER -> AvaliRecipes.freezing().size();
                case PRESS -> 1;
                case WORKBENCH -> WeaponRecipe.ALL.size();
                case LOOM -> AvaliRecipes.weaving().size();
            };
            for (int i = 0; i < count; i++) {
                displays.add(create(kind, i));
            }
        }
        return displays;
    }

    private static AvaliDisplay create(Kind kind, int index) {
        return switch (kind) {
            case REFINERY -> {
                AvaliRecipes.Refining recipe = AvaliRecipes.refining().get(index);
                EntryIngredient heat = EntryIngredient.builder()
                    .add(fluidStack(ModFluids.source(AvaliFluidKind.FUEL), AmmoniaRefineryBlockEntity.FUEL_COST))
                    .addAll(EntryIngredients.ofItemStacks(AvaliRecipes.coals(1)))
                    .build();
                yield new AvaliDisplay(kind, index, AmmoniaRefineryBlockEntity.DURATION,
                    List.of(EntryIngredients.ofItemStacks(recipe.ingredient()), fluid(ModFluids.source(AvaliFluidKind.AMMONIA), AvaliLiquids.BUCKET), heat),
                    List.of(fluid(recipe.product(), recipe.amount())));
            }
            case FREEZER -> {
                AvaliRecipes.Freezing recipe = AvaliRecipes.freezing().get(index);
                yield new AvaliDisplay(kind, index, CryoFreezerBlockEntity.DURATION,
                    List.of(recipe.input().isEmpty() ? EntryIngredient.empty() : EntryIngredients.of(recipe.input()),
                        recipe.liquid() == null ? EntryIngredient.empty() : fluid(recipe.liquid(), AvaliLiquids.BUCKET),
                        fluid(ModFluids.source(AvaliFluidKind.COOLANT), CryoFreezerBlockEntity.COOLANT_COST)),
                    List.of(EntryIngredients.of(recipe.output())));
            }
            case PRESS -> new AvaliDisplay(kind, index, GraphenePressBlockEntity.DURATION,
                List.of(EntryIngredients.of(ModItems.NEXITE_SHARD.get()),
                    EntryIngredients.ofItemStacks(AvaliRecipes.coals(GraphenePressBlockEntity.COAL_COST)),
                    fluid(ModFluids.source(AvaliFluidKind.FUEL), GraphenePressBlockEntity.FUEL_COST)),
                List.of(EntryIngredients.of(ModItems.GRAPHENE.get(), GraphenePressBlockEntity.GRAPHENE_COUNT)));
            case WORKBENCH -> {
                WeaponRecipe recipe = WeaponRecipe.ALL.get(index);
                List<EntryIngredient> inputs = new ArrayList<>();
                for (WeaponRecipe.Cost cost : recipe.costs()) {
                    inputs.add(EntryIngredients.of(new ItemStack(cost.item().get(), cost.count())));
                }
                inputs.add(fluid(ModFluids.source(AvaliFluidKind.FUEL), recipe.fuel()));
                yield new AvaliDisplay(kind, index, recipe.duration(), inputs, List.of(EntryIngredients.of(recipe.output())));
            }
            case LOOM -> {
                AvaliRecipes.Weaving recipe = AvaliRecipes.weaving().get(index);
                yield new AvaliDisplay(kind, index, 0,
                    List.of(EntryIngredients.ofItemStacks(recipe.canvas()),
                        recipe.fibre() ? EntryIngredients.of(ModItems.NANOFIBRE.get()) : EntryIngredient.empty(),
                        EntryIngredients.ofItemStacks(recipe.dyes())),
                    List.of(EntryIngredients.ofItemStacks(recipe.outputs())));
            }
        };
    }

    private static EntryIngredient fluid(Fluid fluid, int amount) {
        return EntryIngredient.of(fluidStack(fluid, amount));
    }

    private static EntryStack<FluidStack> fluidStack(Fluid fluid, int amount) {
        return EntryStacks.of(fluid, amount * FluidStack.bucketAmount() / AvaliLiquids.BUCKET);
    }

    public Kind kind() {
        return kind;
    }

    public int duration() {
        return duration;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return kind.id;
    }

    @Override
    public DisplaySerializer<? extends AvaliDisplay> getSerializer() {
        return SERIALIZER;
    }
}
