/**
 * File: DialogueHud.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-11-09
 */
package org.furranystudio.avalinexus.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
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
    private static final int LABEL_OFFSET = ICON_SIZE + 8;
    private static final int EDGE = 4;
    private static final int MARKER_SPACE = 12;
    private static final String ELLIPSIS = "...";

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
        int width = minecraft.getWindow().getGuiScaledWidth();
        int choiceWidth = choiceWidth(minecraft.font, width);
        int left = choicesLeft(width, choiceWidth);
        int top = choicesTop(minecraft.font, width, minecraft.getWindow().getGuiScaledHeight(), left);
        if (mouseX < left || mouseX > left + choiceWidth || mouseY < top) {
            return -1;
        }
        int index = (int) ((mouseY - top) / CHOICE_HEIGHT);
        return index < ClientDialogue.choices().length ? index : -1;
    }

    private static void renderSubtitle(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        int panelWidth = subtitleWidth(width);
        List<FormattedCharSequence> lines = subtitleLines(font, panelWidth, ClientDialogue.visibleText());
        int panelHeight = subtitleHeight(font, lines.size());
        int left = (width - panelWidth) / 2;
        int top = height - SUBTITLE_BOTTOM - panelHeight;

        AvaliUi.panel(graphics, left, top, panelWidth, panelHeight, AvaliUi.BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(ClientDialogue.speakerTitle()),
            left + PADDING, top + PADDING, AvaliUi.TEXT_SECONDARY);
        for (int i = 0; i < lines.size(); i++) {
            AvaliUi.shadowedText(graphics, font, lines.get(i), left + PADDING, top + PADDING + 12 + i * font.lineHeight, AvaliUi.TEXT_PRIMARY);
        }
    }

    private static void renderChoices(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        DialogueChoice[] choices = ClientDialogue.choices();
        int choiceWidth = choiceWidth(font, width);
        int left = choicesLeft(width, choiceWidth);
        int top = choicesTop(font, width, height, left);
        for (int i = 0; i < choices.length; i++) {
            int y = top + i * CHOICE_HEIGHT;
            boolean selected = i == ClientDialogue.selected();
            if (selected) {
                AvaliUi.panel(graphics, left, y, choiceWidth, CHOICE_HEIGHT - 2, AvaliUi.BACKDROP);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AvaliUi.MARKER, left - 10, y + 4, 8, 8);
            }
            if (choices[i].icon() != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, choices[i].icon(), left + 4, y + 2, ICON_SIZE, ICON_SIZE);
            }
            boolean current = choices[i].ordinal() == ClientDialogue.highlight();
            AvaliUi.shadowedText(graphics, font, fit(font, label(i, choices[i]), choiceWidth - LABEL_OFFSET - EDGE),
                left + LABEL_OFFSET, y + 4, selected || current ? AvaliUi.ORANGE_GLOW : AvaliUi.TEXT_PRIMARY);
        }
        List<FormattedCharSequence> lines = hintLines(font, width, left);
        int hintTop = top + choices.length * CHOICE_HEIGHT + 4;
        for (int i = 0; i < lines.size(); i++) {
            AvaliUi.shadowedText(graphics, font, lines.get(i), left, hintTop + i * font.lineHeight, AvaliUi.TEXT_DISABLED);
        }
    }

    private static List<FormattedCharSequence> hintLines(Font font, int width, int left) {
        Component hint = Component.translatable("avalinexus.dialogue.hint", choiceKeys(ClientDialogue.choices().length),
            keyName(DialogueKeys.PICK), keyName(DialogueKeys.CURSOR));
        return font.split(hint, width - left - EDGE);
    }

    private static int subtitleWidth(int width) {
        return Math.min(SUBTITLE_MAX_WIDTH, width - 40);
    }

    private static List<FormattedCharSequence> subtitleLines(Font font, int panelWidth, String text) {
        return font.split(AvaliUi.reading(text), panelWidth - PADDING * 2);
    }

    private static int subtitleHeight(Font font, int lines) {
        return PADDING * 2 + 12 + Math.max(1, lines) * font.lineHeight;
    }

    private static Component label(int index, DialogueChoice choice) {
        return AvaliUi.styled(Component.literal((index + 1) + ". ").append(choice.label()));
    }

    private static int choiceWidth(Font font, int width) {
        DialogueChoice[] choices = ClientDialogue.choices();
        int widest = 0;
        for (int i = 0; i < choices.length; i++) {
            widest = Math.max(widest, font.width(label(i, choices[i])));
        }
        int wanted = Math.max(CHOICE_WIDTH, widest + LABEL_OFFSET + EDGE);
        return Math.max(LABEL_OFFSET + EDGE, Math.min(wanted, width - MARKER_SPACE - EDGE));
    }

    private static FormattedCharSequence fit(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text.getVisualOrderText();
        }
        Component ellipsis = AvaliUi.styled(ELLIPSIS);
        FormattedText cut = font.substrByWidth(text, Math.max(0, maxWidth - font.width(ellipsis)));
        return Language.getInstance().getVisualOrder(FormattedText.composite(cut, ellipsis));
    }

    private static Component choiceKeys(int count) {
        boolean digits = true;
        for (int i = 0; i < count; i++) {
            digits &= digit(DialogueKeys.CHOICES[i]) == i + 1;
        }
        if (digits) {
            return Component.literal("1-" + count);
        }
        MutableComponent keys = Component.empty();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                keys.append("/");
            }
            keys.append(keyName(DialogueKeys.CHOICES[i]));
        }
        return keys;
    }

    private static Component keyName(KeyMapping key) {
        int digit = digit(key);
        return digit >= 0 ? Component.literal(String.valueOf(digit)) : key.getTranslatedKeyMessage();
    }

    private static int digit(KeyMapping key) {
        InputConstants.Key bound = InputConstants.getKey(key.saveString());
        int value = bound.getValue();
        if (bound.getType() != InputConstants.Type.KEYBOARD
            || !key.getTranslatedKeyMessage().getString().equals(bound.getDisplayName().getString())) {
            return -1;
        }
        if (value == InputConstants.KEY_0) {
            return 0;
        }
        return value >= InputConstants.KEY_1 && value <= InputConstants.KEY_9 ? value - InputConstants.KEY_1 + 1 : -1;
    }

    private static int choicesLeft(int width, int choiceWidth) {
        return Math.max(MARKER_SPACE, Math.min(width / 2 + CHOICE_OFFSET_X, width - choiceWidth - EDGE));
    }

    private static int choicesTop(Font font, int width, int height, int left) {
        int choicesHeight = ClientDialogue.choices().length * CHOICE_HEIGHT;
        int blockHeight = choicesHeight + 4 + hintLines(font, width, left).size() * font.lineHeight;
        int subtitleLines = subtitleLines(font, subtitleWidth(width), ClientDialogue.fullText()).size();
        int subtitleTop = height - SUBTITLE_BOTTOM - subtitleHeight(font, subtitleLines);
        return Math.max(EDGE, Math.min(height / 2 - choicesHeight / 2, subtitleTop - EDGE - blockHeight));
    }
}
