/**
 * File: ReputationScreen.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.client.reputation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;
import org.furranystudio.avalinexus.network.packet.ReputationPayload;
import org.furranystudio.avalinexus.reputation.ReputationTier;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

public class ReputationScreen extends Screen {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 220;
    private static final int PADDING = 8;
    private static final int HEADER = 26;
    private static final int ROW_HEIGHT = 30;
    private static final int BAR_HEIGHT = 5;
    private static final int SWATCH = 8;
    private static final int CLOSE_SIZE = 12;
    private static final int MARK_HEIGHT = 9;
    private static final int BAR_WIDTH = 4;

    private final List<ReputationPayload.Entry> packs;
    private final int min;
    private final int max;
    private final int[] thresholds;
    private final int friendlyPercent;
    private final int allyPercent;
    private int scroll;
    private boolean dragging;
    private int left;
    private int top;

    public ReputationScreen(ReputationPayload payload) {
        super(Component.translatable("avalinexus.reputation.screen"));
        this.packs = payload.packs();
        List<Integer> bounds = payload.bounds();
        this.min = bounds.get(0);
        this.max = bounds.get(1);
        this.thresholds = new int[] {bounds.get(2), bounds.get(3), bounds.get(4), bounds.get(5)};
        this.friendlyPercent = payload.friendlyPercent();
        this.allyPercent = payload.allyPercent();
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
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DialogueChoice.LEAVE.icon(), closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE);

        scroll = Mth.clamp(scroll, 0, maxScroll());
        for (int row = 0; row < visibleRows(); row++) {
            int index = scroll + row;
            if (index >= packs.size()) {
                break;
            }
            renderPack(graphics, packs.get(index), top + HEADER + row * ROW_HEIGHT);
        }
        renderScrollbar(graphics, mouseX, mouseY);
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (packs.size() <= visibleRows()) {
            return;
        }
        int x = scrollbarX();
        int y = listTop();
        graphics.fill(x, y, x + BAR_WIDTH, y + listHeight(), AvaliUi.PRIMARY_BORDER);
        int thumbHeight = thumbHeight();
        int thumbY = y + Math.round((float) scroll / maxScroll() * (listHeight() - thumbHeight));
        boolean hot = dragging || inside(mouseX, mouseY, x, y, BAR_WIDTH, listHeight());
        graphics.fill(x, thumbY, x + BAR_WIDTH, thumbY + thumbHeight, hot ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
    }

    private void renderPack(GuiGraphicsExtractor graphics, ReputationPayload.Entry pack, int y) {
        int x = left + PADDING;
        int rowWidth = rowWidth();
        AvaliUi.panel(graphics, x, y, rowWidth, ROW_HEIGHT - 3, AvaliUi.BACKDROP);
        int barLeft = x + 6;
        int barWidth = rowWidth - 12;
        int barY = y + 17;
        if (!pack.met()) {
            AvaliUi.shadowedText(graphics, font, AvaliUi.styled(Component.translatable("avalinexus.reputation.unknown")), x + 6, y + 5,
                AvaliUi.TEXT_DISABLED);
            graphics.fill(barLeft, barY, barLeft + barWidth, barY + BAR_HEIGHT, AvaliUi.PRIMARY_BORDER);
            return;
        }
        ReputationTier tier = tier(pack.value());
        graphics.fill(x + 6, y + 5, x + 6 + SWATCH, y + 5 + SWATCH, ARGB.opaque(pack.color()));
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(Component.translatable("avalinexus.pack." + pack.pack())), x + 6 + SWATCH + 4, y + 5,
            AvaliUi.TEXT_PRIMARY);
        Component status = Component.translatable("avalinexus.reputation.row", tier.label(), effect(tier));
        int statusWidth = font.width(status);
        AvaliUi.shadowedText(graphics, font, status, x + rowWidth - 6 - statusWidth, y + 5, tier.color());

        graphics.fill(barLeft, barY, barLeft + barWidth, barY + BAR_HEIGHT, AvaliUi.PRIMARY_BORDER);
        int zero = position(0, barLeft, barWidth);
        int value = position(pack.value(), barLeft, barWidth);
        graphics.fill(Math.min(zero, value), barY, Math.max(zero, value) + 1, barY + BAR_HEIGHT, tier.color());
        for (int threshold : thresholds) {
            int mark = position(threshold, barLeft, barWidth);
            graphics.fill(mark, barY - 2, mark + 1, barY - 2 + MARK_HEIGHT, AvaliUi.TEXT_DISABLED);
        }
        graphics.fill(zero, barY - 1, zero + 1, barY + BAR_HEIGHT + 1, AvaliUi.TEXT_PRIMARY);
    }

    private Component effect(ReputationTier tier) {
        return switch (tier) {
            case FRIENDLY -> Component.translatable("avalinexus.reputation.effect.friendly", friendlyPercent);
            case ALLY -> Component.translatable("avalinexus.reputation.effect.ally", allyPercent);
            default -> Component.translatable("avalinexus.reputation.effect." + tier.key());
        };
    }

    private ReputationTier tier(int value) {
        if (value <= thresholds[0]) {
            return ReputationTier.HOSTILE;
        }
        if (value <= thresholds[1]) {
            return ReputationTier.WARY;
        }
        if (value >= thresholds[3]) {
            return ReputationTier.ALLY;
        }
        return value >= thresholds[2] ? ReputationTier.FRIENDLY : ReputationTier.NEUTRAL;
    }

    private int position(int value, int barLeft, int barWidth) {
        return barLeft + Math.round((float) (Mth.clamp(value, min, max) - min) / Math.max(1, max - min) * (barWidth - 1));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (inside(event.x(), event.y(), closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 1.0F));
            onClose();
            return true;
        }
        if (packs.size() > visibleRows() && inside(event.x(), event.y(), scrollbarX(), listTop(), BAR_WIDTH, listHeight())) {
            dragging = true;
            dragTo(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            dragTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    private void dragTo(double mouseY) {
        float progress = (float) (mouseY - listTop() - thumbHeight() / 2.0) / Math.max(1, listHeight() - thumbHeight());
        scroll = Math.round(Mth.clamp(progress, 0.0F, 1.0F) * maxScroll());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    private int thumbHeight() {
        return Math.max(8, listHeight() * visibleRows() / packs.size());
    }

    private int listTop() {
        return top + HEADER;
    }

    private int listHeight() {
        return visibleRows() * ROW_HEIGHT - 3;
    }

    private int rowWidth() {
        return WIDTH - PADDING * 2 - BAR_WIDTH - 4;
    }

    private int scrollbarX() {
        return left + PADDING + rowWidth() + 4;
    }

    private int visibleRows() {
        return (HEIGHT - HEADER - PADDING) / ROW_HEIGHT;
    }

    private int maxScroll() {
        return Math.max(0, packs.size() - visibleRows());
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
}
