/**
 * File: AvaliLoomMenu.java
 * Author: Maxime66410
 * Created: 2026-10-11
 * Last Modified: 2026-10-12
 */
package org.furranystudio.avalinexus.block.loom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.inventory.ModMenus;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.item.TapestryItem;
import org.furranystudio.avalinexus.item.TapestryItems;

public class AvaliLoomMenu extends AbstractContainerMenu {

    public static final int CANVAS_SLOT = 0;
    public static final int FIBRE_SLOT = 1;
    public static final int DYE_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int INPUT_X = 20;
    public static final int RESULT_X = 143;
    public static final int RESULT_Y = 35;
    public static final int INVENTORY_Y = 84;
    private static final int INVENTORY_START = 4;
    private static final int INVENTORY_END = 40;
    private static final int CANVAS_YIELD = 2;

    private final ContainerLevelAccess access;
    private final DataSlot selected = DataSlot.standalone();
    private final ResultContainer result = new ResultContainer();
    private final Container inputs = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private long lastSoundTime;

    public AvaliLoomMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public AvaliLoomMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.AVALI_LOOM.get(), id);
        this.access = access;
        selected.set(-1);
        addSlot(new Slot(inputs, CANVAS_SLOT, INPUT_X, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isCanvas(stack);
            }
        });
        addSlot(new Slot(inputs, FIBRE_SLOT, INPUT_X, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isFibre(stack);
            }
        });
        addSlot(new Slot(inputs, DYE_SLOT, INPUT_X, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isDye(stack);
            }
        });
        addSlot(new Slot(result, 0, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                consumeInputs();
                access.execute((level, pos) -> {
                    long time = level.getGameTime();
                    if (lastSoundTime != time) {
                        level.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        lastSoundTime = time;
                    }
                });
                super.onTake(player, stack);
            }
        });
        addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        addDataSlot(selected);
    }

    public static boolean isFibre(ItemStack stack) {
        return stack.is(ModItems.NANOFIBRE.get());
    }

    public static boolean isDye(ItemStack stack) {
        return stack.has(DataComponents.DYE);
    }

    public static boolean isCanvas(ItemStack stack) {
        return canvasColor(stack) != null || stack.getItem() instanceof TapestryItem;
    }

    private static DyeColor canvasColor(ItemStack stack) {
        for (DyeColor color : DyeColor.values()) {
            if (stack.is(NanocanvasBlocks.item(NanocanvasBlocks.Shape.BLOCK, color))) {
                return color;
            }
        }
        return null;
    }

    public int selectedPattern() {
        return selected.get();
    }

    public DyeColor previewColor() {
        ItemStack dye = inputs.getItem(DYE_SLOT);
        if (isDye(dye)) {
            return dye.get(DataComponents.DYE);
        }
        ItemStack canvas = inputs.getItem(CANVAS_SLOT);
        if (canvas.getItem() instanceof TapestryItem tapestry) {
            return tapestry.color();
        }
        DyeColor color = canvasColor(canvas);
        return color != null ? color : DyeColor.WHITE;
    }

    public boolean hasCanvas() {
        return !inputs.getItem(CANVAS_SLOT).isEmpty();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= TapestryPattern.values().length) {
            return false;
        }
        selected.set(id);
        updateResult();
        access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.BLOCKS, 1.0F, 1.0F));
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        updateResult();
    }

    private void updateResult() {
        ItemStack woven = weave();
        if (!ItemStack.matches(woven, result.getItem(0))) {
            result.setItem(0, woven);
        }
        broadcastChanges();
    }

    private ItemStack weave() {
        int pattern = selected.get();
        ItemStack canvas = inputs.getItem(CANVAS_SLOT);
        if (pattern < 0 || canvas.isEmpty()) {
            return ItemStack.EMPTY;
        }
        TapestryPattern target = TapestryPattern.byId(pattern);
        DyeColor color = previewColor();
        if (canvas.getItem() instanceof TapestryItem tapestry) {
            if (tapestry.pattern() == target && tapestry.color() == color) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(TapestryItems.get(target, color));
        }
        if (!isFibre(inputs.getItem(FIBRE_SLOT))) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(TapestryItems.get(target, color), CANVAS_YIELD);
    }

    private void consumeInputs() {
        ItemStack canvas = inputs.getItem(CANVAS_SLOT);
        boolean fresh = !(canvas.getItem() instanceof TapestryItem);
        canvas.shrink(1);
        if (fresh) {
            inputs.getItem(FIBRE_SLOT).shrink(1);
        }
        inputs.getItem(DYE_SLOT).shrink(1);
        inputs.setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // Hmmm, maybe use a switch ? TODO: for later maybe
        if (index < INVENTORY_START) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isCanvas(stack)) {
            if (!moveItemStackTo(stack, CANVAS_SLOT, CANVAS_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isFibre(stack)) {
            if (!moveItemStackTo(stack, FIBRE_SLOT, FIBRE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isDye(stack)) {
            if (!moveItemStackTo(stack, DYE_SLOT, DYE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        result.removeItemNoUpdate(0);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.AVALI_LOOM.get());
    }
}
