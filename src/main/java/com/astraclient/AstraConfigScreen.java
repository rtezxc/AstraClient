package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Friendly named config snapshots. Each slot keeps a separate full properties-file snapshot. */
public final class AstraConfigScreen extends Screen {
    private final Screen parent;
    private static final int BG = 0xFF080B12;
    private static final int PANEL = 0xFF171D2A;
    private static final int HOVER = 0xFF252D3F;
    private static final int WHITE = 0xFFF3F5FF;
    private static final int MUTED = 0xFF929BB0;
    private static final int CYAN = 0xFF54E8FF;
    private static final int PURPLE = 0xFFB58CFF;

    public AstraConfigScreen(Screen parent) {
        super(Text.literal("Astra Config Manager"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG);
        AstraUiEffects.renderAmbientBackdrop(ctx, width, height, System.currentTimeMillis(), AstraClient.accentColor());
        int panelW = Math.max(1, Math.min(620, width - 24));
        int panelH = Math.max(1, Math.min(430, height - 20));
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        ctx.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0x774C34A3);
        ctx.fill(left, top, left + panelW, top + panelH, 0xFF0E131E);
        int accent = AstraClient.accentColor();
        ctx.fill(left, top, left + panelW, top + 3, accent);
        int badgeX = left + panelW - 116;
        ctx.fill(badgeX, top + 15, badgeX + 96, top + 36, 0xFF152A2D);
        ctx.fill(badgeX, top + 15, badgeX + 2, top + 36, CYAN);
        ctx.drawText(textRenderer, "LOCAL  /  5 SLOTS", badgeX + 8, top + 22, CYAN, true);
        ctx.drawText(textRenderer, "ASTRA  /  CONFIG MANAGER", left + 20, top + 17, WHITE, true);
        ctx.drawText(textRenderer, "Save five independent snapshots and switch setups whenever you want.",
            left + 20, top + 38, MUTED, false);
        ctx.drawText(textRenderer, "SAVE = overwrite slot with your current setup     LOAD = replace current setup",
            left + 20, top + 57, MUTED, false);

        int rowY = top + 84;
        int rowH = 48;
        for (int slot = 1; slot <= 5; slot++) {
            int y = rowY + (slot - 1) * (rowH + 8);
            boolean exists = AstraClient.hasNamedConfig(slot);
            boolean hover = mouseX >= left + 18 && mouseX <= left + panelW - 18
                && mouseY >= y && mouseY <= y + rowH;
            ctx.fill(left + 18, y, left + panelW - 18, y + rowH, hover ? HOVER : PANEL);
            ctx.fill(left + 18, y, left + 21, y + rowH, exists ? CYAN : 0xFF3A4254);

            // Tiny local-file glyph makes each save slot read like a separate profile card.
            int fileX = left + 31;
            int fileY = y + 12;
            int fileColor = exists ? CYAN : 0xFF657084;
            ctx.fill(fileX, fileY, fileX + 15, fileY + 23, exists ? 0xFF17313B : 0xFF202633);
            ctx.fill(fileX + 4, fileY + 5, fileX + 12, fileY + 6, fileColor);
            ctx.fill(fileX + 4, fileY + 9, fileX + 12, fileY + 10, fileColor);
            ctx.fill(fileX + 4, fileY + 13, fileX + 10, fileY + 14, fileColor);
            ctx.fill(fileX + 4, fileY + 17, fileX + 12, fileY + 18, fileColor);
            ctx.drawText(textRenderer, "SLOT " + slot, left + 54, y + 8, WHITE, true);
            ctx.drawText(textRenderer, exists ? "Saved snapshot available" : "Empty — save your current setup here",
                left + 54, y + 26, exists ? CYAN : MUTED, false);
            int loadX = left + panelW - 158;
            int saveX = left + panelW - 82;
            drawButton(ctx, mouseX, mouseY, loadX, y + 10, 66, 28, "LOAD", PURPLE, exists);
            drawButton(ctx, mouseX, mouseY, saveX, y + 10, 66, 28, "SAVE", CYAN, true);
        }

        int footerY = top + panelH - 38;
        ctx.drawText(textRenderer, "Configs stay on this device in the Minecraft config folder.",
            left + 20, footerY + 8, MUTED, false);
        drawButton(ctx, mouseX, mouseY, left + panelW - 91, footerY, 73, 27, "BACK", WHITE, true);
    }

    private void drawButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h,
                            String label, int color, boolean enabled) {
        boolean hover = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, enabled ? (hover ? 0xFF343D50 : 0xFF222A39) : 0xFF121722);
        ctx.fill(x, y, x + 2, y + h, enabled ? color : 0xFF3A4254);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - 8) / 2, enabled ? WHITE : MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();
        int panelW = Math.min(620, width - 24);
        int panelH = Math.min(430, height - 20);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int rowY = top + 84;
        int rowH = 48;
        for (int slot = 1; slot <= 5; slot++) {
            int y = rowY + (slot - 1) * (rowH + 8);
            int loadX = left + panelW - 158;
            int saveX = left + panelW - 82;
            if (mouseY >= y + 10 && mouseY <= y + 38) {
                if (mouseX >= saveX && mouseX <= saveX + 66) {
                    AstraClient.saveNamedConfig(slot);
                    return true;
                }
                if (mouseX >= loadX && mouseX <= loadX + 66) {
                    if (AstraClient.hasNamedConfig(slot)) {
                        AstraClient.loadNamedConfig(slot);
                        client.setScreen(parent);
                    } else {
                        AstraClient.loadNamedConfig(slot);
                    }
                    return true;
                }
            }
        }
        int footerY = top + panelH - 38;
        if (mouseX >= left + panelW - 91 && mouseX <= left + panelW - 18
            && mouseY >= footerY && mouseY <= footerY + 27) {
            client.setScreen(parent);
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
