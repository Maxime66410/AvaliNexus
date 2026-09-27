/**
 * File: DialogueHud.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.dialogue;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;

import java.util.List;

public final class DialogueHud {

    private static final FontDescription FONT = new FontDescription.Resource(AvaliNexus.id("avali"));
    private static final Identifier PANEL = AvaliNexus.id("dialogue/panel");
    private static final Identifier MARKER = AvaliNexus.id("dialogue/marker");

    private static final int ORANGE_GLOW = 0xFFFFA640;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFFF8C1A;
    private static final int TEXT_DISABLED = 0xFF6B6B6B;
    private static final int SHADOW = 0xFF2B1810;
    private static final int BACKDROP = 0x992B1810;

    private static final int SUBTITLE_MAX_WIDTH = 320;
    private static final int SUBTITLE_BOTTOM = 60;
    private static final int PADDING = 8;
    private static final int CHOICE_WIDTH = 120;
    private static final int CHOICE_HEIGHT = 18;
    private static final int CHOICE_OFFSET_X = 50;
    private static final int ICON_SIZE = 12;

    private DialogueHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!ClientDialogue.isActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        renderSubtitle(graphics, font, width, height);
        if (!ClientDialogue.isClosing()) {
            renderChoices(graphics, font, width, height);
        }
    }

    // Returns the choice under the mouse, used by the cursor mode screen
    public static int choiceAt(double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int left = choicesLeft(minecraft.getWindow().getGuiScaledWidth());
        int top = choicesTop(minecraft.getWindow().getGuiScaledHeight());
        if (mouseX < left || mouseX > left + CHOICE_WIDTH || mouseY < top) {
            return -1;
        }
        int index = (int) ((mouseY - top) / CHOICE_HEIGHT);
        return index < ClientDialogue.choices().length ? index : -1;
    }

    private static void renderSubtitle(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        int panelWidth = Math.min(SUBTITLE_MAX_WIDTH, width - 40);
        List<FormattedCharSequence> lines = font.split(styled(ClientDialogue.visibleText()), panelWidth - PADDING * 2);
        int panelHeight = PADDING * 2 + 12 + Math.max(1, lines.size()) * font.lineHeight;
        int left = (width - panelWidth) / 2;
        int top = height - SUBTITLE_BOTTOM - panelHeight;

        graphics.fill(left, top, left + panelWidth, top + panelHeight, BACKDROP);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, left, top, panelWidth, panelHeight);
        shadowedText(graphics, font, styled(Component.translatable("entity.avalinexus.avali").getString()).getVisualOrderText(),
            left + PADDING, top + PADDING, TEXT_SECONDARY);
        for (int i = 0; i < lines.size(); i++) {
            shadowedText(graphics, font, lines.get(i), left + PADDING, top + PADDING + 12 + i * font.lineHeight, TEXT_PRIMARY);
        }
    }

    private static void renderChoices(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        DialogueChoice[] choices = ClientDialogue.choices();
        int left = choicesLeft(width);
        int top = choicesTop(height);
        for (int i = 0; i < choices.length; i++) {
            int y = top + i * CHOICE_HEIGHT;
            boolean selected = i == ClientDialogue.selected();
            if (selected) {
                graphics.fill(left, y, left + CHOICE_WIDTH, y + CHOICE_HEIGHT - 2, BACKDROP);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, left, y, CHOICE_WIDTH, CHOICE_HEIGHT - 2);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MARKER, left - 10, y + 4, 8, 8);
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, choices[i].icon(), left + 4, y + 2, ICON_SIZE, ICON_SIZE);
            MutableComponent label = Component.literal((i + 1) + ". ").append(choices[i].label());
            shadowedText(graphics, font, styled(label.getString()).getVisualOrderText(), left + ICON_SIZE + 8, y + 4,
                selected ? ORANGE_GLOW : TEXT_PRIMARY);
        }
        Component hint = Component.translatable("avalinexus.dialogue.hint");
        graphics.text(font, hint, left, top + choices.length * CHOICE_HEIGHT + 4, TEXT_DISABLED, false);
    }

    private static int choicesLeft(int width) {
        return width / 2 + CHOICE_OFFSET_X;
    }

    private static int choicesTop(int height) {
        return height / 2 - ClientDialogue.choices().length * CHOICE_HEIGHT / 2;
    }

    private static MutableComponent styled(String text) {
        return Component.literal(text).withStyle(style -> style.withFont(FONT));
    }

    // Text shadow in the dark border color instead of the vanilla darkened one
    private static void shadowedText(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.text(font, text, x + 1, y + 1, SHADOW, false);
        graphics.text(font, text, x, y, color, false);
    }
}
