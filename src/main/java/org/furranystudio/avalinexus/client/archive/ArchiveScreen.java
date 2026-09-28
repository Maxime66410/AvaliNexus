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
import org.furranystudio.avalinexus.archive.ArchiveCategory;
import org.furranystudio.avalinexus.archive.ArchiveEntry;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

// Avali archives shown by the terminal: a tab per category, entries on the left, the chosen one on the right
// Both the list and the text get a scrollbar once they overflow, the wheel or a drag on the bar moves them
public class ArchiveScreen extends Screen {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 220;
    private static final int PADDING = 8;
    private static final int HEADER = 24;
    private static final int TAB_WIDTH = 72;
    private static final int TAB_HEIGHT = 14;
    private static final int CONTENT_TOP = HEADER + TAB_HEIGHT + 6;
    private static final int LIST_WIDTH = 112;
    private static final int ROW_HEIGHT = 20;
    private static final int BAR_WIDTH = 4;
    private static final int CLOSE_SIZE = 12;
    private static final int TEXT_TOP = 19;

    private final List<ArchiveCategory> categories;
    private final List<ArchiveEntry> allEntries;
    private List<ArchiveEntry> entries;
    private int category;
    private int selected;
    private int listScroll;
    private int textScroll;
    // Which scrollbar the mouse is holding, if any
    private Scrollbar dragging;
    private int left;
    private int top;

    private enum Scrollbar {
        LIST,
        TEXT
    }

    public ArchiveScreen(List<ArchiveCategory> categories, List<ArchiveEntry> entries) {
        super(Component.translatable("avalinexus.archive.title"));
        this.categories = categories;
        this.allEntries = entries;
        this.entries = entriesOf(0);
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

        renderTabs(graphics, mouseX, mouseY);
        if (entries.isEmpty()) {
            graphics.text(font, AvaliUi.reading(Component.translatable("avalinexus.archive.empty")), left + PADDING, contentTop() + 4,
                AvaliUi.TEXT_DISABLED, false);
            return;
        }
        renderList(graphics, mouseX, mouseY);
        renderText(graphics, mouseX, mouseY);
    }

    private void renderTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int i = 0; i < categories.size(); i++) {
            int x = tabX(i);
            int y = top + HEADER;
            boolean active = i == category;
            boolean hovered = inside(mouseX, mouseY, x, y, TAB_WIDTH, TAB_HEIGHT);
            graphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT,
                active ? AvaliUi.PRIMARY_ORANGE : hovered ? AvaliUi.PRIMARY_BORDER : AvaliUi.SECONDARY_BORDER);
            graphics.centeredText(font, AvaliUi.styled(categories.get(i).title()), x + TAB_WIDTH / 2, y + 3,
                active ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_SECONDARY);
        }
    }

    private void renderList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        listScroll = Mth.clamp(listScroll, 0, maxListScroll());
        int x = left + PADDING;
        int rowWidth = LIST_WIDTH - BAR_WIDTH - 3;
        for (int row = 0; row < visibleRows(); row++) {
            int index = listScroll + row;
            if (index >= entries.size()) {
                break;
            }
            ArchiveEntry entry = entries.get(index);
            int y = contentTop() + row * ROW_HEIGHT;
            boolean active = index == selected;
            if (active || inside(mouseX, mouseY, x, y, rowWidth, ROW_HEIGHT - 2)) {
                AvaliUi.panel(graphics, x, y, rowWidth, ROW_HEIGHT - 2, AvaliUi.BACKDROP);
            }
            graphics.item(new ItemStack(BuiltInRegistries.ITEM.getValue(entry.icon())), x + 2, y + 1);
            Component name = AvaliUi.styled(font.substrByWidth(AvaliUi.styled(entry.title()), rowWidth - 24).getString());
            graphics.text(font, name, x + 21, y + 5, active ? AvaliUi.ORANGE_GLOW : AvaliUi.TEXT_PRIMARY, false);
        }
        renderScrollbar(graphics, listBarX(), contentTop(), contentHeight(), entries.size(), visibleRows(), listScroll, mouseX, mouseY);
    }

    private void renderText(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ArchiveEntry entry = entries.get(selected);
        int x = textLeft();
        int y = contentTop();
        AvaliUi.panel(graphics, x - 4, y, textWidth() + BAR_WIDTH + 10, contentHeight(), AvaliUi.BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(entry.title()), x, y + 5, AvaliUi.TEXT_SECONDARY);

        List<FormattedCharSequence> lines = lines(entry);
        textScroll = Mth.clamp(textScroll, 0, Math.max(0, lines.size() - visibleLines()));
        int lineY = y + TEXT_TOP;
        for (int i = textScroll; i < Math.min(lines.size(), textScroll + visibleLines()); i++) {
            graphics.text(font, lines.get(i), x, lineY, AvaliUi.TEXT_PRIMARY, false);
            lineY += font.lineHeight;
        }
        renderScrollbar(graphics, textBarX(), y + TEXT_TOP, textBarHeight(), lines.size(), visibleLines(), textScroll, mouseX, mouseY);
    }

    // Dark track with an orange thumb sized to the visible part, hidden when everything already fits
    private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y, int height, int total, int visible, int offset,
                                 int mouseX, int mouseY) {
        if (total <= visible) {
            return;
        }
        graphics.fill(x, y, x + BAR_WIDTH, y + height, AvaliUi.PRIMARY_BORDER);
        int thumbHeight = thumbHeight(height, total, visible);
        int thumbY = y + Math.round((float) offset / (total - visible) * (height - thumbHeight));
        boolean hot = dragging != null || inside(mouseX, mouseY, x, y, BAR_WIDTH, height);
        graphics.fill(x, thumbY, x + BAR_WIDTH, thumbY + thumbHeight, hot ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
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
        for (int i = 0; i < categories.size(); i++) {
            if (i != category && inside(x, y, tabX(i), top + HEADER, TAB_WIDTH, TAB_HEIGHT)) {
                category = i;
                entries = entriesOf(i);
                selected = 0;
                listScroll = 0;
                textScroll = 0;
                playSound(ModSounds.UI_HOVER.get());
                return true;
            }
        }
        if (entries.isEmpty()) {
            return super.mouseClicked(event, doubleClick);
        }
        if (inside(x, y, listBarX(), contentTop(), BAR_WIDTH, contentHeight()) && entries.size() > visibleRows()) {
            dragging = Scrollbar.LIST;
            dragTo(y);
            return true;
        }
        if (inside(x, y, textBarX(), contentTop() + TEXT_TOP, BAR_WIDTH, textBarHeight())
            && lines(entries.get(selected)).size() > visibleLines()) {
            dragging = Scrollbar.TEXT;
            dragTo(y);
            return true;
        }
        for (int row = 0; row < visibleRows(); row++) {
            int index = listScroll + row;
            if (index < entries.size() && index != selected
                && inside(x, y, left + PADDING, contentTop() + row * ROW_HEIGHT, LIST_WIDTH - BAR_WIDTH - 3, ROW_HEIGHT - 2)) {
                selected = index;
                textScroll = 0;
                playSound(ModSounds.UI_HOVER.get());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            dragTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (entries.isEmpty()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int step = (int) Math.signum(scrollY);
        if (mouseX < textLeft() - 4) {
            listScroll = Mth.clamp(listScroll - step, 0, maxListScroll());
        } else {
            textScroll -= step;
        }
        return true;
    }

    // Puts the thumb under the mouse on the held scrollbar
    private void dragTo(double mouseY) {
        if (dragging == Scrollbar.LIST) {
            listScroll = offsetAt(mouseY, contentTop(), contentHeight(), entries.size(), visibleRows());
        } else if (dragging == Scrollbar.TEXT) {
            textScroll = offsetAt(mouseY, contentTop() + TEXT_TOP, textBarHeight(), lines(entries.get(selected)).size(), visibleLines());
        }
    }

    private static int offsetAt(double mouseY, int barY, int height, int total, int visible) {
        int thumbHeight = thumbHeight(height, total, visible);
        float progress = (float) (mouseY - barY - thumbHeight / 2.0) / Math.max(1, height - thumbHeight);
        return Math.round(Mth.clamp(progress, 0.0F, 1.0F) * (total - visible));
    }

    private static int thumbHeight(int height, int total, int visible) {
        return Math.max(8, height * visible / total);
    }

    private List<ArchiveEntry> entriesOf(int index) {
        if (index >= categories.size()) {
            return List.of();
        }
        String id = categories.get(index).id();
        return allEntries.stream().filter(entry -> entry.category().equals(id)).toList();
    }

    private List<FormattedCharSequence> lines(ArchiveEntry entry) {
        return font.split(AvaliUi.reading(entry.text()), textWidth());
    }

    private int contentTop() {
        return top + CONTENT_TOP;
    }

    private int contentHeight() {
        return HEIGHT - CONTENT_TOP - PADDING;
    }

    private int visibleRows() {
        return contentHeight() / ROW_HEIGHT;
    }

    private int maxListScroll() {
        return Math.max(0, entries.size() - visibleRows());
    }

    private int visibleLines() {
        return (contentHeight() - TEXT_TOP - 6) / font.lineHeight;
    }

    private int textBarHeight() {
        return visibleLines() * font.lineHeight;
    }

    private int listBarX() {
        return left + PADDING + LIST_WIDTH - BAR_WIDTH;
    }

    private int textLeft() {
        return left + PADDING + LIST_WIDTH + 10;
    }

    private int textWidth() {
        return left + WIDTH - PADDING - 4 - BAR_WIDTH - 6 - textLeft();
    }

    private int textBarX() {
        return textLeft() + textWidth() + 4;
    }

    private int tabX(int index) {
        return left + PADDING + index * (TAB_WIDTH + 4);
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
