/**
 * File: ArchiveScreen.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.archive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.archive.ArchiveEntry;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

// Avali archives shown by the terminal: entries on the left, the chosen one on the right, the wheel scrolls long texts
public class ArchiveScreen extends Screen {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private static final int LIST_WIDTH = 104;
    private static final int ROW_HEIGHT = 20;
    private static final int PADDING = 8;
    private static final int HEADER = 24;
    private static final int CLOSE_SIZE = 12;

    private final List<ArchiveEntry> entries;
    private int selected;
    private int scroll;
    private int left;
    private int top;

    public ArchiveScreen(List<ArchiveEntry> entries) {
        super(Component.translatable("avalinexus.archive.title"));
        this.entries = entries;
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        AvaliUi.panel(graphics, left, top, WIDTH, HEIGHT, AvaliUi.WINDOW_BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(title), left + PADDING, top + 9, AvaliUi.TEXT_SECONDARY);
        if (inside(mouseX, mouseY, closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE)) {
            AvaliUi.panel(graphics, closeX() - 2, closeY() - 2, CLOSE_SIZE + 4, CLOSE_SIZE + 4, AvaliUi.BACKDROP);
            graphics.setTooltipForNextFrame(Component.translatable("avalinexus.shop.close"), mouseX, mouseY);
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DialogueChoice.LEAVE.icon(), closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE);

        if (entries.isEmpty()) {
            graphics.text(font, Component.translatable("avalinexus.archive.empty"), left + PADDING, top + HEADER + 4, AvaliUi.TEXT_DISABLED, false);
            return;
        }
        renderList(graphics, mouseX, mouseY);
        renderText(graphics);
    }

    private void renderList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int i = 0; i < entries.size(); i++) {
            ArchiveEntry entry = entries.get(i);
            int x = left + PADDING;
            int y = top + HEADER + i * ROW_HEIGHT;
            boolean active = i == selected;
            if (active || inside(mouseX, mouseY, x, y, LIST_WIDTH, ROW_HEIGHT - 2)) {
                AvaliUi.panel(graphics, x, y, LIST_WIDTH, ROW_HEIGHT - 2, AvaliUi.BACKDROP);
            }
            graphics.item(new ItemStack(BuiltInRegistries.ITEM.getValue(entry.icon())), x + 2, y + 1);
            String title = font.substrByWidth(entry.title(), LIST_WIDTH - 24).getString();
            graphics.text(font, title, x + 21, y + 5, active ? AvaliUi.ORANGE_GLOW : AvaliUi.TEXT_PRIMARY, false);
        }
    }

    private void renderText(GuiGraphicsExtractor graphics) {
        ArchiveEntry entry = entries.get(selected);
        int x = textLeft();
        int y = top + HEADER;
        AvaliUi.panel(graphics, x - 4, y, textWidth() + 8, HEIGHT - HEADER - PADDING, AvaliUi.BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(entry.title()), x, y + 5, AvaliUi.TEXT_SECONDARY);

        List<FormattedCharSequence> lines = font.split(entry.text(), textWidth());
        int first = Mth.clamp(scroll, 0, Math.max(0, lines.size() - visibleLines()));
        scroll = first;
        int lineY = y + 19;
        for (int i = first; i < Math.min(lines.size(), first + visibleLines()); i++) {
            graphics.text(font, lines.get(i), x, lineY, AvaliUi.TEXT_PRIMARY, false);
            lineY += font.lineHeight;
        }
        // Small orange marks tell there is more text above or below
        if (first > 0) {
            graphics.fill(x + textWidth() - 6, y + 16, x + textWidth(), y + 17, AvaliUi.PRIMARY_ORANGE);
        }
        if (first + visibleLines() < lines.size()) {
            int bottom = top + HEIGHT - PADDING - 4;
            graphics.fill(x + textWidth() - 6, bottom, x + textWidth(), bottom + 1, AvaliUi.PRIMARY_ORANGE);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        if (inside(x, y, closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE)) {
            playSound(ModSounds.UI_CLICK.get());
            onClose();
            return true;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (i != selected && inside(x, y, left + PADDING, top + HEADER + i * ROW_HEIGHT, LIST_WIDTH, ROW_HEIGHT - 2)) {
                selected = i;
                scroll = 0;
                playSound(ModSounds.UI_HOVER.get());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!entries.isEmpty() && mouseX >= textLeft()) {
            scroll -= (int) Math.signum(scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int textLeft() {
        return left + PADDING + LIST_WIDTH + 12;
    }

    private int textWidth() {
        return left + WIDTH - PADDING - 4 - textLeft();
    }

    private int visibleLines() {
        return (HEIGHT - HEADER - PADDING - 26) / font.lineHeight;
    }

    private int closeX() {
        return left + WIDTH - 8 - CLOSE_SIZE;
    }

    private int closeY() {
        return top + 7;
    }

    private static boolean inside(double x, double y, int areaX, int areaY, int width, int height) {
        return x >= areaX && x < areaX + width && y >= areaY && y < areaY + height;
    }

    private static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
