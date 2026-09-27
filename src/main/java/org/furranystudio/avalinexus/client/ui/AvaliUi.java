/**
 * File: AvaliUi.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.furranystudio.avalinexus.AvaliNexus;

// Avali theme shared by every mod UI
public final class AvaliUi {

    public static final int PRIMARY_ORANGE = 0xFFFF8C1A;
    public static final int SECONDARY_ORANGE = 0xFFFFA640;
    public static final int ORANGE_GLOW = 0xFFFFA640;
    public static final int PRIMARY_BORDER = 0xFF2B1810;
    public static final int SECONDARY_BORDER = 0xFF3C2216;
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFFF8C1A;
    public static final int TEXT_DISABLED = 0xFF6B6B6B;
    public static final int SUCCESS = 0xFF4CD964;
    public static final int DANGER = 0xFFE63946;
    public static final int BACKDROP = 0x992B1810;
    public static final int WINDOW_BACKDROP = 0xE62B1810;

    public static final FontDescription FONT = new FontDescription.Resource(AvaliNexus.id("avali"));
    public static final Identifier PANEL = AvaliNexus.id("dialogue/panel");
    public static final Identifier MARKER = AvaliNexus.id("dialogue/marker");

    private AvaliUi() {
    }

    public static MutableComponent styled(String text) {
        return Component.literal(text).withStyle(style -> style.withFont(FONT));
    }

    public static MutableComponent styled(Component text) {
        return styled(text.getString());
    }

    public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int backdrop) {
        graphics.fill(x, y, x + width, y + height, backdrop);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, x, y, width, height);
    }

    // Text shadow in the dark border color instead of the vanilla darkened one
    public static void shadowedText(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.text(font, text, x + 1, y + 1, PRIMARY_BORDER, false);
        graphics.text(font, text, x, y, color, false);
    }

    public static void shadowedText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        shadowedText(graphics, font, text.getVisualOrderText(), x, y, color);
    }
}
