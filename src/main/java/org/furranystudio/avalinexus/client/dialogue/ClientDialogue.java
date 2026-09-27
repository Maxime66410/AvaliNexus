/**
 * File: ClientDialogue.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.dialogue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.furranystudio.avalinexus.dialogue.DialogueChoice;
import org.furranystudio.avalinexus.network.ModNetworking;
import org.furranystudio.avalinexus.network.packet.DialogueChoicePayload;
import org.furranystudio.avalinexus.sound.ModSounds;

public final class ClientDialogue {

    private static final double MAX_DISTANCE = 7.0;
    private static final float CHARS_PER_TICK = 1.5F;
    private static final int CLOSING_TICKS = 50;
    private static final int USE_BLOCK_TICKS = 10;
    private static final DialogueChoice[] CHOICES = DialogueChoice.values();

    private static int entityId = -1;
    private static String text = "";
    private static float revealed;
    private static int selected;
    private static int closingTicks = -1;
    private static int useBlockTicks;

    private ClientDialogue() {
    }

    public static void open(int id, String line) {
        entityId = id;
        selected = 0;
        closingTicks = -1;
        setLine(line);
    }

    public static void showLine(int id, String line, boolean closing) {
        if (id != entityId) {
            return;
        }
        setLine(line);
        if (closing) {
            closingTicks = CLOSING_TICKS;
        }
    }

    public static void close(int id) {
        if (id == entityId) {
            reset();
        }
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        blockUseKey(minecraft.options);
        if (!isActive()) {
            return;
        }
        Entity avali = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        if (minecraft.player == null || avali == null || !avali.isAlive() || avali.distanceTo(minecraft.player) > MAX_DISTANCE) {
            reset();
            return;
        }

        revealed = Math.min(text.length(), revealed + CHARS_PER_TICK);
        if (closingTicks >= 0) {
            if (--closingTicks <= 0) {
                reset();
            }
            return;
        }

        // Runs before vanilla handles its keys, so E, right click and 1-3 don't open the inventory or switch slots
        Options options = minecraft.options;
        while (options.keyInventory.consumeClick()) {
            confirm();
        }
        for (int i = 0; i < CHOICES.length; i++) {
            while (options.keyHotbarSlots[i].consumeClick()) {
                select(i);
                confirm();
            }
        }
        if (minecraft.gui.screen() == null && DialogueKeys.CURSOR.isDown()) {
            minecraft.gui.setScreen(new DialogueCursorScreen());
        }
    }

    // Right click confirms choices, and holding it makes vanilla repeat "use" every few ticks without a click,
    // which would land on the Avali again, so it stays fully blocked during the dialogue and a bit after
    private static void blockUseKey(Options options) {
        if (!isActive() && useBlockTicks <= 0) {
            return;
        }
        if (!isActive()) {
            useBlockTicks--;
        }
        while (options.keyUse.consumeClick()) {
            confirm();
        }
        options.keyUse.setDown(false);
    }

    public static boolean onScroll(double delta) {
        if (!isActive() || isClosing() || delta == 0 || Minecraft.getInstance().gui.screen() != null) {
            return false;
        }
        int step = delta > 0 ? -1 : 1;
        select(Math.floorMod(selected + step, CHOICES.length));
        return true;
    }

    public static void select(int index) {
        if (index != selected && index >= 0 && index < CHOICES.length) {
            selected = index;
            playSound(ModSounds.UI_HOVER.get());
        }
    }

    public static void confirm() {
        if (!isActive() || isClosing()) {
            return;
        }
        // First press finishes the text if it's still being typed
        if (revealed < text.length()) {
            revealed = text.length();
            return;
        }
        playSound(ModSounds.UI_CLICK.get());
        ModNetworking.sendToServer(new DialogueChoicePayload(entityId, selected));
    }

    public static boolean isActive() {
        return entityId >= 0;
    }

    public static boolean isClosing() {
        return closingTicks >= 0;
    }

    public static String visibleText() {
        return text.substring(0, (int) revealed);
    }

    public static int selected() {
        return selected;
    }

    public static DialogueChoice[] choices() {
        return CHOICES;
    }

    private static void setLine(String line) {
        text = Component.translatable(line).getString();
        revealed = 0;
    }

    private static void reset() {
        entityId = -1;
        useBlockTicks = USE_BLOCK_TICKS;
        text = "";
        closingTicks = -1;
    }

    private static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
