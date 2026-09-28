/**
 * File: AvaliSettingsScreen.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.ConfigValue;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

// Client options opened from the mod list, every toggle saves to the local config right away
public class AvaliSettingsScreen extends Screen {

    private record Toggle(String key, ConfigValue<Boolean> value) {

        Component label() {
            return Component.translatable("avalinexus.settings." + key);
        }

        Component description() {
            return Component.translatable("avalinexus.settings." + key + ".desc");
        }
    }

    private static final List<Toggle> TOGGLES = List.of(
        new Toggle("avaliFontUi", Config.AVALI_FONT_UI),
        new Toggle("avaliFontReading", Config.AVALI_FONT_READING));

    private static final int WIDTH = 280;
    private static final int PADDING = 10;
    private static final int HEADER = 26;
    private static final int ROW_HEIGHT = 36;
    private static final int SWITCH_WIDTH = 44;
    private static final int SWITCH_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 18;

    private final Screen parent;
    private int left;
    private int top;

    public AvaliSettingsScreen(Screen parent) {
        super(Component.translatable("avalinexus.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - windowHeight()) / 2;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        AvaliUi.panel(graphics, left, top, WIDTH, windowHeight(), AvaliUi.WINDOW_BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(title), left + PADDING, top + 9, AvaliUi.TEXT_SECONDARY);

        for (int i = 0; i < TOGGLES.size(); i++) {
            Toggle toggle = TOGGLES.get(i);
            int y = rowTop(i);
            AvaliUi.panel(graphics, left + PADDING, y, WIDTH - PADDING * 2, ROW_HEIGHT - 4, AvaliUi.BACKDROP);
            AvaliUi.shadowedText(graphics, font, AvaliUi.styled(toggle.label()), left + PADDING + 6, y + 5, AvaliUi.TEXT_PRIMARY);
            List<FormattedCharSequence> lines = font.split(AvaliUi.reading(toggle.description()), WIDTH - PADDING * 2 - SWITCH_WIDTH - 20);
            if (!lines.isEmpty()) {
                graphics.text(font, lines.get(0), left + PADDING + 6, y + 17, AvaliUi.TEXT_DISABLED, false);
            }
            renderSwitch(graphics, toggle.value().get(), switchX(), switchY(i), mouseX, mouseY);
        }

        int x = buttonX();
        int y = buttonY();
        boolean hovered = inside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
        graphics.centeredText(font, AvaliUi.styled(Component.translatable("avalinexus.settings.done")), x + BUTTON_WIDTH / 2, y + 5, AvaliUi.TEXT_PRIMARY);
    }

    private void renderSwitch(GuiGraphicsExtractor graphics, boolean on, int x, int y, int mouseX, int mouseY) {
        int border = inside(mouseX, mouseY, x, y, SWITCH_WIDTH, SWITCH_HEIGHT) ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER;
        graphics.fill(x - 1, y - 1, x + SWITCH_WIDTH + 1, y + SWITCH_HEIGHT + 1, border);
        graphics.fill(x, y, x + SWITCH_WIDTH, y + SWITCH_HEIGHT, on ? AvaliUi.SUCCESS : AvaliUi.SECONDARY_BORDER);
        Component state = AvaliUi.styled(Component.translatable(on ? "avalinexus.settings.on" : "avalinexus.settings.off"));
        graphics.centeredText(font, state, x + SWITCH_WIDTH / 2, y + 4, on ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        for (int i = 0; i < TOGGLES.size(); i++) {
            if (inside(x, y, switchX(), switchY(i), SWITCH_WIDTH, SWITCH_HEIGHT)) {
                ConfigValue<Boolean> value = TOGGLES.get(i).value();
                value.set(!value.get());
                playClick();
                return true;
            }
        }
        if (inside(x, y, buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT)) {
            playClick();
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int windowHeight() {
        return HEADER + TOGGLES.size() * ROW_HEIGHT + BUTTON_HEIGHT + PADDING * 2;
    }

    private int rowTop(int row) {
        return top + HEADER + row * ROW_HEIGHT;
    }

    private int switchX() {
        return left + WIDTH - PADDING - 6 - SWITCH_WIDTH;
    }

    private int switchY(int row) {
        return rowTop(row) + (ROW_HEIGHT - 4 - SWITCH_HEIGHT) / 2;
    }

    private int buttonX() {
        return left + (WIDTH - BUTTON_WIDTH) / 2;
    }

    private int buttonY() {
        return top + windowHeight() - PADDING - BUTTON_HEIGHT;
    }

    private static boolean inside(double x, double y, int areaX, int areaY, int width, int height) {
        return x >= areaX && x < areaX + width && y >= areaY && y < areaY + height;
    }

    private static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 1.0F));
    }
}
