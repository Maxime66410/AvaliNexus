/**
 * File: AvaliSettingsScreen.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.ConfigValue;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.ChangeSettingPayload;
import org.furranystudio.avalinexus.network.packet.RequestSettingsPayload;
import org.furranystudio.avalinexus.network.packet.SettingsPayload;
import org.furranystudio.avalinexus.settings.SettingsRegistry;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Client options save to the local config, server options go through the server when connected to one
public class AvaliSettingsScreen extends Screen {

    private sealed interface Entry permits Toggle, Slider {
    }

    private record Toggle(String key, ConfigValue<Boolean> value) implements Entry {
    }

    private record Slider(String key, int min, int max, int step) implements Entry {
    }

    private static final List<Entry> CLIENT = List.of(
        new Toggle("avaliFontUi", Config.AVALI_FONT_UI),
        new Toggle("avaliFontReading", Config.AVALI_FONT_READING));

    private static final List<Entry> SERVER = List.of(
        new Slider("coldSpeed", 0, 500, 5),
        new Slider("liquidColdSpeed", 0, 500, 5));

    private static final int WIDTH = 280;
    private static final int PADDING = 10;
    private static final int HEADER = 26;
    private static final int TAB_WIDTH = 80;
    private static final int TAB_HEIGHT = 16;
    private static final int TAB_GAP = 6;
    private static final int TABS = 22;
    private static final int NOTE = 14;
    private static final int ROW_HEIGHT = 36;
    private static final int CONTROL_WIDTH = 44;
    private static final int SLIDER_WIDTH = 80;
    private static final int CONTROL_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BAR_WIDTH = 4;
    private static final int SCROLL_STEP = 12;
    private static final int SCREEN_MARGIN = 20;

    private final Screen parent;
    private final boolean local;
    private Map<String, String> serverValues;
    private boolean serverEditable;
    private Slider sliding;
    private int slideValue;
    private boolean draggingBar;
    private boolean serverTab;
    private final int[] scrolls = new int[2];
    private int scroll;
    private int left;
    private int top;

    public AvaliSettingsScreen(Screen parent) {
        super(Component.translatable("avalinexus.settings.title"));
        this.parent = parent;
        this.local = Minecraft.getInstance().getConnection() == null;
        if (local) {
            serverEditable = true;
        } else {
            ModNetworking.sendToServer(RequestSettingsPayload.INSTANCE);
        }
    }

    public void receive(SettingsPayload payload) {
        serverValues = new HashMap<>(payload.values());
        serverEditable = payload.editable();
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

        renderTab(graphics, "client", false, mouseX, mouseY);
        renderTab(graphics, "server", true, mouseX, mouseY);
        if (serverTab) {
            Component note = AvaliUi.reading(Component.translatable(serverNote()));
            graphics.centeredText(font, note, left + WIDTH / 2, top + HEADER + TABS + 2, AvaliUi.TEXT_DISABLED);
        }

        scroll = Mth.clamp(scroll, 0, maxScroll());
        boolean overList = inside(mouseX, mouseY, left, listTop(), WIDTH, listHeight());
        graphics.enableScissor(left, listTop(), left + WIDTH, listTop() + listHeight());
        int y = listTop() - scroll;
        for (Entry entry : entries()) {
            switch (entry) {
                case Toggle toggle -> renderToggle(graphics, toggle, y, overList, mouseX, mouseY);
                case Slider slider -> renderSlider(graphics, slider, y, overList, mouseX, mouseY);
            }
            y += ROW_HEIGHT;
        }
        graphics.disableScissor();
        renderScrollbar(graphics, mouseX, mouseY);

        int x = buttonX();
        int buttonY = buttonY();
        boolean hovered = inside(mouseX, mouseY, x, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        graphics.fill(x, buttonY, x + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
        graphics.centeredText(font, AvaliUi.styled(Component.translatable("avalinexus.settings.done")), x + BUTTON_WIDTH / 2, buttonY + 5, AvaliUi.TEXT_PRIMARY);
    }

    private void renderTab(GuiGraphicsExtractor graphics, String key, boolean server, int mouseX, int mouseY) {
        int x = tabX(server);
        int y = tabY();
        boolean active = serverTab == server;
        boolean hovered = inside(mouseX, mouseY, x, y, TAB_WIDTH, TAB_HEIGHT);
        graphics.fill(x - 1, y - 1, x + TAB_WIDTH + 1, y + TAB_HEIGHT + 1, hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER);
        graphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, active ? AvaliUi.PRIMARY_ORANGE : AvaliUi.SECONDARY_BORDER);
        graphics.centeredText(font, AvaliUi.styled(Component.translatable("avalinexus.settings.category." + key)), x + TAB_WIDTH / 2, y + 4,
            active ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED);
    }

    private String serverNote() {
        if (local) {
            return "avalinexus.settings.server.local";
        }
        if (serverValues == null) {
            return "avalinexus.settings.server.loading";
        }
        return serverEditable ? "avalinexus.settings.server.remote" : "avalinexus.settings.server.locked";
    }

    private void renderRow(GuiGraphicsExtractor graphics, String key, int y, int controlWidth, boolean enabled) {
        int x = left + PADDING;
        AvaliUi.panel(graphics, x, y, rowWidth(), ROW_HEIGHT - 4, AvaliUi.BACKDROP);
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(Component.translatable("avalinexus.settings." + key)), x + 6, y + 5,
            enabled ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED);
        List<FormattedCharSequence> lines = font.split(AvaliUi.reading(Component.translatable("avalinexus.settings." + key + ".desc")),
            rowWidth() - controlWidth - 20);
        if (!lines.isEmpty()) {
            graphics.text(font, lines.get(0), x + 6, y + 17, AvaliUi.TEXT_DISABLED, false);
        }
    }

    private void renderToggle(GuiGraphicsExtractor graphics, Toggle toggle, int y, boolean overList, int mouseX, int mouseY) {
        renderRow(graphics, toggle.key(), y, CONTROL_WIDTH, true);
        boolean on = toggle.value().get();
        int x = controlX(CONTROL_WIDTH);
        int controlY = controlY(y);
        boolean hovered = overList && inside(mouseX, mouseY, x, controlY, CONTROL_WIDTH, CONTROL_HEIGHT);
        graphics.fill(x - 1, controlY - 1, x + CONTROL_WIDTH + 1, controlY + CONTROL_HEIGHT + 1, hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER);
        graphics.fill(x, controlY, x + CONTROL_WIDTH, controlY + CONTROL_HEIGHT, on ? AvaliUi.SUCCESS : AvaliUi.SECONDARY_BORDER);
        Component state = AvaliUi.styled(Component.translatable(on ? "avalinexus.settings.on" : "avalinexus.settings.off"));
        graphics.centeredText(font, state, x + CONTROL_WIDTH / 2, controlY + 4, on ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED);
    }

    private void renderSlider(GuiGraphicsExtractor graphics, Slider slider, int y, boolean overList, int mouseX, int mouseY) {
        Integer value = sliding == slider ? Integer.valueOf(slideValue) : sliderValue(slider);
        boolean enabled = serverEditable && value != null;
        renderRow(graphics, slider.key(), y, SLIDER_WIDTH, enabled);
        int x = controlX(SLIDER_WIDTH);
        int controlY = controlY(y);
        boolean hovered = enabled && (sliding == slider || overList && inside(mouseX, mouseY, x, controlY, SLIDER_WIDTH, CONTROL_HEIGHT));
        graphics.fill(x - 1, controlY - 1, x + SLIDER_WIDTH + 1, controlY + CONTROL_HEIGHT + 1, hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_BORDER);
        graphics.fill(x, controlY, x + SLIDER_WIDTH, controlY + CONTROL_HEIGHT, AvaliUi.SECONDARY_BORDER);
        if (value == null) {
            graphics.centeredText(font, "...", x + SLIDER_WIDTH / 2, controlY + 4, AvaliUi.TEXT_DISABLED);
            return;
        }
        int filled = Math.round((float) (value - slider.min()) / (slider.max() - slider.min()) * SLIDER_WIDTH);
        graphics.fill(x, controlY, x + filled, controlY + CONTROL_HEIGHT, enabled ? AvaliUi.PRIMARY_ORANGE : AvaliUi.TEXT_DISABLED);
        graphics.centeredText(font, AvaliUi.styled(value + "%"), x + SLIDER_WIDTH / 2, controlY + 4, enabled ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED);
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (maxScroll() == 0) {
            return;
        }
        int x = scrollbarX();
        int y = listTop();
        graphics.fill(x, y, x + BAR_WIDTH, y + listHeight(), AvaliUi.PRIMARY_BORDER);
        int thumbHeight = thumbHeight();
        int thumbY = y + Math.round((float) scroll / maxScroll() * (listHeight() - thumbHeight));
        boolean hot = draggingBar || inside(mouseX, mouseY, x, y, BAR_WIDTH, listHeight());
        graphics.fill(x, thumbY, x + BAR_WIDTH, thumbY + thumbHeight, hot ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE);
    }

    private Integer sliderValue(Slider slider) {
        String value = local ? SettingsRegistry.get(slider.key()).get() : serverValues == null ? null : serverValues.get(slider.key());
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        for (boolean server : new boolean[] {false, true}) {
            if (serverTab != server && inside(mouseX, mouseY, tabX(server), tabY(), TAB_WIDTH, TAB_HEIGHT)) {
                scrolls[serverTab ? 1 : 0] = scroll;
                serverTab = server;
                scroll = scrolls[serverTab ? 1 : 0];
                playClick();
                return true;
            }
        }
        if (maxScroll() > 0 && inside(mouseX, mouseY, scrollbarX(), listTop(), BAR_WIDTH, listHeight())) {
            draggingBar = true;
            dragBarTo(mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, left, listTop(), WIDTH, listHeight())) {
            int y = listTop() - scroll;
            for (Entry entry : entries()) {
                if (entry instanceof Toggle toggle && inside(mouseX, mouseY, controlX(CONTROL_WIDTH), controlY(y), CONTROL_WIDTH, CONTROL_HEIGHT)) {
                    toggle.value().set(!toggle.value().get());
                    playClick();
                    return true;
                }
                if (entry instanceof Slider slider && serverEditable && sliderValue(slider) != null
                    && inside(mouseX, mouseY, controlX(SLIDER_WIDTH), controlY(y), SLIDER_WIDTH, CONTROL_HEIGHT)) {
                    sliding = slider;
                    slideTo(mouseX);
                    return true;
                }
                y += ROW_HEIGHT;
            }
        }
        if (inside(mouseX, mouseY, buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT)) {
            playClick();
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (sliding != null) {
            slideTo(event.x());
            return true;
        }
        if (draggingBar) {
            dragBarTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (sliding != null) {
            commit(sliding, slideValue);
            sliding = null;
            playClick();
        }
        draggingBar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Mth.clamp(scroll - (int) Math.signum(scrollY) * SCROLL_STEP, 0, maxScroll());
        return true;
    }

    private void slideTo(double mouseX) {
        float progress = Mth.clamp((float) (mouseX - controlX(SLIDER_WIDTH)) / SLIDER_WIDTH, 0.0F, 1.0F);
        int raw = sliding.min() + Math.round(progress * (sliding.max() - sliding.min()) / sliding.step()) * sliding.step();
        slideValue = Mth.clamp(raw, sliding.min(), sliding.max());
    }

    private void commit(Slider slider, int value) {
        String text = String.valueOf(value);
        if (local) {
            SettingsRegistry.get(slider.key()).trySet(text);
            return;
        }
        serverValues.put(slider.key(), text);
        ModNetworking.sendToServer(new ChangeSettingPayload(slider.key(), text));
    }

    private void dragBarTo(double mouseY) {
        float progress = (float) (mouseY - listTop() - thumbHeight() / 2.0) / Math.max(1, listHeight() - thumbHeight());
        scroll = Math.round(Mth.clamp(progress, 0.0F, 1.0F) * maxScroll());
    }

    private List<Entry> entries() {
        return serverTab ? SERVER : CLIENT;
    }

    private int contentHeight() {
        return entries().size() * ROW_HEIGHT;
    }

    private int windowHeight() {
        int rows = Math.max(CLIENT.size(), SERVER.size());
        int wanted = HEADER + TABS + NOTE + rows * ROW_HEIGHT + BUTTON_HEIGHT + PADDING * 2;
        return Math.min(wanted, height - SCREEN_MARGIN * 2);
    }

    private int listTop() {
        return top + HEADER + TABS + (serverTab ? NOTE : 0);
    }

    private int listHeight() {
        return top + windowHeight() - BUTTON_HEIGHT - PADDING * 2 - listTop();
    }

    private int tabX(boolean server) {
        int start = left + (WIDTH - TAB_WIDTH * 2 - TAB_GAP) / 2;
        return server ? start + TAB_WIDTH + TAB_GAP : start;
    }

    private int tabY() {
        return top + HEADER;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - listHeight());
    }

    private int thumbHeight() {
        return Math.max(8, listHeight() * listHeight() / Math.max(1, contentHeight()));
    }

    private int rowWidth() {
        return WIDTH - PADDING * 2 - (maxScroll() > 0 ? BAR_WIDTH + 4 : 0);
    }

    private int scrollbarX() {
        return left + PADDING + rowWidth() + 4;
    }

    private int controlX(int controlWidth) {
        return left + PADDING + rowWidth() - 6 - controlWidth;
    }

    private static int controlY(int rowTop) {
        return rowTop + (ROW_HEIGHT - 4 - CONTROL_HEIGHT) / 2;
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
