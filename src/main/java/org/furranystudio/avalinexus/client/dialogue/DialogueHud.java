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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;

import java.util.List;

public final class DialogueHud {

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
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.gui.screen();
        if (!ClientDialogue.isActive() || (screen != null && !(screen instanceof DialogueCursorScreen))) {
            return;
        }
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
        List<FormattedCharSequence> lines = font.split(AvaliUi.styled(ClientDialogue.visibleText()), panelWidth - PADDING * 2);
        int panelHeight = PADDING * 2 + 12 + Math.max(1, lines.size()) * font.lineHeight;
        int left = (width - panelWidth) / 2;
        int top = height - SUBTITLE_BOTTOM - panelHeight;

        AvaliUi.panel(graphics, left, top, panelWidth, panelHeight, AvaliUi.BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(Component.translatable("entity.avalinexus.avali")),
            left + PADDING, top + PADDING, AvaliUi.TEXT_SECONDARY);
        for (int i = 0; i < lines.size(); i++) {
            AvaliUi.shadowedText(graphics, font, lines.get(i), left + PADDING, top + PADDING + 12 + i * font.lineHeight, AvaliUi.TEXT_PRIMARY);
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
                AvaliUi.panel(graphics, left, y, CHOICE_WIDTH, CHOICE_HEIGHT - 2, AvaliUi.BACKDROP);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AvaliUi.MARKER, left - 10, y + 4, 8, 8);
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, choices[i].icon(), left + 4, y + 2, ICON_SIZE, ICON_SIZE);
            Component label = Component.literal((i + 1) + ". ").append(choices[i].label());
            AvaliUi.shadowedText(graphics, font, AvaliUi.styled(label), left + ICON_SIZE + 8, y + 4,
                selected ? AvaliUi.ORANGE_GLOW : AvaliUi.TEXT_PRIMARY);
        }
        Component hint = Component.translatable("avalinexus.dialogue.hint");
        graphics.text(font, hint, left, top + choices.length * CHOICE_HEIGHT + 4, AvaliUi.TEXT_DISABLED, false);
    }

    private static int choicesLeft(int width) {
        return width / 2 + CHOICE_OFFSET_X;
    }

    private static int choicesTop(int height) {
        return height / 2 - ClientDialogue.choices().length * CHOICE_HEIGHT / 2;
    }
}
