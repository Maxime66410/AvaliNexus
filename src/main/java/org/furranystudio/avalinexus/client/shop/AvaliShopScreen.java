/**
 * File: AvaliShopScreen.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.shop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.ui.AvaliUi;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.ShopTradePayload;
import org.furranystudio.avalinexus.sound.ModSounds;
import org.furranystudio.avalinexus.trade.ShopManager;
import org.furranystudio.avalinexus.trade.ShopOffer;
import org.furranystudio.avalinexus.trade.ShopTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AvaliShopScreen extends Screen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 234;
    private static final int TAB_WIDTH = 62;
    private static final int TAB_HEIGHT = 14;
    private static final int LIST_WIDTH = 142;
    private static final int ROW_HEIGHT = 20;
    private static final int DETAIL_WIDTH = 92;
    private static final int DETAIL_HEIGHT = 100;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 16;
    private static final int SLOT = 18;
    private static final int CLOSE_SIZE = 12;

    private final int entityId;
    private final List<ShopOffer> offers;
    private ShopTab tab = ShopTab.BUY;
    private int selected;
    private int left;
    private int top;

    public AvaliShopScreen(int entityId, List<ShopOffer> offers) {
        super(Component.translatable("avalinexus.shop.title"));
        this.entityId = entityId;
        this.offers = new ArrayList<>(offers);
    }

    public int entityId() {
        return entityId;
    }

    public void update(int offer, int uses) {
        if (offer >= 0 && offer < offers.size()) {
            ShopOffer old = offers.get(offer);
            offers.set(offer, new ShopOffer(old.tab(), old.item(), old.price(), old.maxUses(), uses));
        }
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
    public void tick() {
        if (!ClientDialogue.isActive()) {
            onClose();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        AvaliUi.panel(graphics, left, top, WIDTH, HEIGHT, AvaliUi.WINDOW_BACKDROP);
        renderHeader(graphics, mouseX, mouseY);
        renderTabs(graphics);
        List<Integer> visible = visibleOffers();
        renderList(graphics, visible, mouseX, mouseY);
        renderDetail(graphics, visible, mouseX, mouseY);
        renderInventory(graphics, mouseX, mouseY);
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
        for (ShopTab candidate : ShopTab.values()) {
            int tabX = tabX(candidate);
            if (inside(x, y, tabX, top + 24, TAB_WIDTH, TAB_HEIGHT) && candidate != tab) {
                tab = candidate;
                selected = 0;
                playSound(ModSounds.UI_HOVER.get());
                return true;
            }
        }
        List<Integer> visible = visibleOffers();
        for (int row = 0; row < visible.size(); row++) {
            if (inside(x, y, left + 8, listTop() + row * ROW_HEIGHT, LIST_WIDTH, ROW_HEIGHT - 2) && row != selected) {
                selected = row;
                playSound(ModSounds.UI_HOVER.get());
                return true;
            }
        }
        if (!visible.isEmpty() && inside(x, y, buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT)) {
            int index = visible.get(selected);
            if (problem(offers.get(index)) == null) {
                playSound(ModSounds.UI_CLICK.get());
                ModNetworking.sendToServer(new ShopTradePayload(entityId, index));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderHeader(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        AvaliUi.shadowedText(graphics, font, AvaliUi.styled(Component.translatable("entity.avalinexus.avali")),
            left + 8, top + 9, AvaliUi.TEXT_SECONDARY);

        if (inside(mouseX, mouseY, closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE)) {
            AvaliUi.panel(graphics, closeX() - 2, closeY() - 2, CLOSE_SIZE + 4, CLOSE_SIZE + 4, AvaliUi.BACKDROP);
            graphics.setTooltipForNextFrame(Component.translatable("avalinexus.shop.close"), mouseX, mouseY);
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DialogueChoice.LEAVE.icon(), closeX(), closeY(), CLOSE_SIZE, CLOSE_SIZE);

        Component balance = Component.translatable("avalinexus.shop.balance", nexite());
        int textWidth = font.width(balance);
        int x = closeX() - 8 - textWidth;
        AvaliUi.shadowedText(graphics, font, balance, x, top + 9, AvaliUi.TEXT_PRIMARY);
        smallNexite(graphics, x - 11, top + 8);
    }

    private void renderTabs(GuiGraphicsExtractor graphics) {
        for (ShopTab candidate : ShopTab.values()) {
            int x = tabX(candidate);
            boolean active = candidate == tab;
            graphics.fill(x, top + 24, x + TAB_WIDTH, top + 24 + TAB_HEIGHT, active ? AvaliUi.PRIMARY_ORANGE : AvaliUi.SECONDARY_BORDER);
            Component label = AvaliUi.styled(Component.translatable("avalinexus.shop.tab." + candidate.name().toLowerCase(Locale.ROOT)));
            graphics.centeredText(font, label, x + TAB_WIDTH / 2, top + 27, active ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_SECONDARY);
        }
    }

    private void renderList(GuiGraphicsExtractor graphics, List<Integer> visible, int mouseX, int mouseY) {
        if (visible.isEmpty()) {
            graphics.text(font, Component.translatable("avalinexus.shop.empty"), left + 12, listTop() + 4, AvaliUi.TEXT_DISABLED, false);
            return;
        }
        for (int row = 0; row < visible.size(); row++) {
            ShopOffer offer = offers.get(visible.get(row));
            int y = listTop() + row * ROW_HEIGHT;
            if (row == selected) {
                AvaliUi.panel(graphics, left + 8, y, LIST_WIDTH, ROW_HEIGHT - 2, AvaliUi.BACKDROP);
            }
            graphics.item(offer.item(), left + 10, y + 1);
            graphics.itemDecorations(font, offer.item(), left + 10, y + 1);
            int nameColor = offer.remaining() > 0 ? AvaliUi.TEXT_PRIMARY : AvaliUi.TEXT_DISABLED;
            graphics.text(font, font.substrByWidth(offer.item().getHoverName(), 80).getString(), left + 30, y + 5, nameColor, false);
            String price = String.valueOf(offer.price());
            int priceX = left + 8 + LIST_WIDTH - 14 - font.width(price);
            graphics.text(font, price, priceX, y + 5, AvaliUi.ORANGE_GLOW, false);
            smallNexite(graphics, left + 8 + LIST_WIDTH - 12, y + 4);
            if (inside(mouseX, mouseY, left + 10, y + 1, 16, 16)) {
                graphics.setTooltipForNextFrame(font, offer.item(), mouseX, mouseY);
            }
        }
    }

    private void renderDetail(GuiGraphicsExtractor graphics, List<Integer> visible, int mouseX, int mouseY) {
        int x = left + 8 + LIST_WIDTH + 6;
        int y = listTop();
        AvaliUi.panel(graphics, x, y, DETAIL_WIDTH, DETAIL_HEIGHT, AvaliUi.BACKDROP);
        if (visible.isEmpty()) {
            return;
        }
        ShopOffer offer = offers.get(visible.get(Math.min(selected, visible.size() - 1)));

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + DETAIL_WIDTH / 2.0F - 16, y + 6);
        graphics.pose().scale(2.0F, 2.0F);
        graphics.item(offer.item(), 0, 0);
        graphics.pose().popMatrix();
        if (inside(mouseX, mouseY, x + DETAIL_WIDTH / 2 - 16, y + 6, 32, 32)) {
            graphics.setTooltipForNextFrame(font, offer.item(), mouseX, mouseY);
        }

        Component name = Component.literal(offer.item().getCount() + "x ").append(offer.item().getHoverName());
        graphics.centeredText(font, font.substrByWidth(name, DETAIL_WIDTH - 8).getString(), x + DETAIL_WIDTH / 2, y + 42, AvaliUi.TEXT_PRIMARY);
        Component price = Component.translatable("avalinexus.shop.price", offer.price());
        graphics.centeredText(font, price, x + DETAIL_WIDTH / 2, y + 54, AvaliUi.ORANGE_GLOW);
        Component stock = Component.translatable("avalinexus.shop.stock", offer.remaining(), offer.maxUses());
        graphics.centeredText(font, stock, x + DETAIL_WIDTH / 2, y + 64, AvaliUi.TEXT_DISABLED);

        Component problem = problem(offer);
        boolean hovered = inside(mouseX, mouseY, buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        int fill = problem != null ? AvaliUi.SECONDARY_BORDER : hovered ? AvaliUi.ORANGE_GLOW : AvaliUi.PRIMARY_ORANGE;
        graphics.fill(buttonX(), buttonY(), buttonX() + BUTTON_WIDTH, buttonY() + BUTTON_HEIGHT, fill);
        Component action = AvaliUi.styled(Component.translatable(tab == ShopTab.BUY ? "avalinexus.shop.buy" : "avalinexus.shop.sell"));
        graphics.centeredText(font, action, buttonX() + BUTTON_WIDTH / 2, buttonY() + 4,
            problem != null ? AvaliUi.TEXT_DISABLED : AvaliUi.TEXT_PRIMARY);
        if (problem != null && hovered) {
            graphics.setTooltipForNextFrame(problem.copy().withColor(AvaliUi.DANGER), mouseX, mouseY);
        }
    }

    private void renderInventory(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int startX = left + (WIDTH - 9 * SLOT) / 2;
        int startY = top + 152;
        for (int slot = 0; slot < 36; slot++) {
            int column = slot % 9;
            int row = slot < 9 ? 3 : slot / 9 - 1;
            int x = startX + column * SLOT;
            int y = startY + row * SLOT + (slot < 9 ? 4 : 0);
            graphics.fill(x, y, x + 16, y + 16, AvaliUi.SECONDARY_BORDER);
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                graphics.item(stack, x, y);
                graphics.itemDecorations(font, stack, x, y);
                if (inside(mouseX, mouseY, x, y, 16, 16)) {
                    graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                }
            }
        }
    }

    // Null when the trade can go through, otherwise the reason it can't
    private Component problem(ShopOffer offer) {
        if (offer.remaining() <= 0) {
            return Component.translatable("avalinexus.shop.sold_out");
        }
        if (offer.tab() == ShopTab.BUY && nexite() < offer.price()) {
            return Component.translatable("avalinexus.shop.not_enough_nexite");
        }
        if (offer.tab() == ShopTab.SELL && ShopManager.count(Minecraft.getInstance().player.getInventory(), offer.item()) < offer.item().getCount()) {
            return Component.translatable("avalinexus.shop.missing_items");
        }
        return null;
    }

    private List<Integer> visibleOffers() {
        List<Integer> visible = new ArrayList<>();
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i).tab() == tab) {
                visible.add(i);
            }
        }
        if (selected >= visible.size()) {
            selected = Math.max(0, visible.size() - 1);
        }
        return visible;
    }

    private int nexite() {
        return ShopManager.count(Minecraft.getInstance().player.getInventory(), new ItemStack(ModItems.NEXITE_SHARD.get()));
    }

    private void smallNexite(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(0.625F, 0.625F);
        graphics.item(new ItemStack(ModItems.NEXITE_SHARD.get()), 0, 0);
        graphics.pose().popMatrix();
    }

    private int closeX() {
        return left + WIDTH - 8 - CLOSE_SIZE;
    }

    private int closeY() {
        return top + 7;
    }

    private int tabX(ShopTab candidate) {
        return left + 8 + candidate.ordinal() * (TAB_WIDTH + 4);
    }

    private int listTop() {
        return top + 44;
    }

    private int buttonX() {
        return left + 8 + LIST_WIDTH + 6 + (DETAIL_WIDTH - BUTTON_WIDTH) / 2;
    }

    private int buttonY() {
        return listTop() + DETAIL_HEIGHT - BUTTON_HEIGHT - 6;
    }

    private static boolean inside(double x, double y, int areaX, int areaY, int width, int height) {
        return x >= areaX && x < areaX + width && y >= areaY && y < areaY + height;
    }

    private static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
