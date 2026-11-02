/**
 * File: AvaliCategory.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.furranystudio.avalinexus.compat.AvaliRecipes;

import java.util.ArrayList;
import java.util.List;

public class AvaliCategory implements DisplayCategory<AvaliDisplay> {

    private final AvaliDisplay.Kind kind;
    private final Block block;

    public AvaliCategory(AvaliDisplay.Kind kind, Block block) {
        this.kind = kind;
        this.block = block;
    }

    @Override
    public CategoryIdentifier<? extends AvaliDisplay> getCategoryIdentifier() {
        return kind.id;
    }

    @Override
    public Component getTitle() {
        return block.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(block);
    }

    @Override
    public int getDisplayWidth(AvaliDisplay display) {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }

    @Override
    public List<Widget> setupDisplay(AvaliDisplay display, Rectangle bounds) {
        int x = bounds.x + 5;
        int y = bounds.y + 5;
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        List<EntryIngredient> inputs = display.getInputEntries();
        if (kind == AvaliDisplay.Kind.WORKBENCH) {
            for (int i = 0; i < inputs.size() - 1; i++) {
                widgets.add(Widgets.createSlot(new Point(x + 1 + i * 18, y + 1)).entries(inputs.get(i)).markInput());
            }
            widgets.add(Widgets.createSlot(new Point(x + 1, y + 45)).entries(inputs.getLast()).markInput());
        } else {
            for (int i = 0; i < inputs.size(); i++) {
                if (!inputs.get(i).isEmpty()) {
                    widgets.add(Widgets.createSlot(new Point(x + 1, y + 1 + i * 22)).entries(inputs.get(i)).markInput());
                }
            }
        }
        if (kind == AvaliDisplay.Kind.REFINERY) {
            widgets.add(Widgets.createBurningFire(new Point(x + 22, y + 45)).animationDurationTicks(display.duration()));
        }
        if (display.duration() > 0) {
            widgets.add(Widgets.createArrow(new Point(x + 55, y + 22)).animationDurationTicks(display.duration()));
            widgets.add(Widgets.createLabel(new Point(x + 67, y + 44), AvaliRecipes.seconds(display.duration())).noShadow().color(0xFF404040, 0xFFBBBBBB).centered());
        } else {
            widgets.add(Widgets.createArrow(new Point(x + 55, y + 22)));
        }
        widgets.add(Widgets.createResultSlotBackground(new Point(x + 95, y + 23)));
        widgets.add(Widgets.createSlot(new Point(x + 95, y + 23)).entries(display.getOutputEntries().getFirst()).disableBackground().markOutput());
        return widgets;
    }
}
