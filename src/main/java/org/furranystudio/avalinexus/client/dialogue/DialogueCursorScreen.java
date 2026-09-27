/**
 * File: DialogueCursorScreen.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.dialogue;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

// Only exists while Alt is held, it frees the cursor so the choices can be clicked, the HUD still draws them
public class DialogueCursorScreen extends Screen {

    public DialogueCursorScreen() {
        super(Component.empty());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (!ClientDialogue.isActive() || ClientDialogue.isClosing() || !DialogueKeys.CURSOR.isDown()) {
            onClose();
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        int choice = DialogueHud.choiceAt(mouseX, mouseY);
        if (choice >= 0) {
            ClientDialogue.select(choice);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int choice = DialogueHud.choiceAt(event.x(), event.y());
        if (choice < 0) {
            return false;
        }
        ClientDialogue.select(choice);
        ClientDialogue.confirm();
        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (DialogueKeys.CURSOR.matches(event)) {
            onClose();
            return true;
        }
        return super.keyReleased(event);
    }
}
