package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Explicit quick presets for common play styles. Applying a preset replaces module toggles only. */
public final class AstraProfileScreen extends Screen {
    private final Screen parent;
    private static final int BG = 0xFF080B12;
    private static final int PANEL = 0xFF171D2A;
    private static final int HOVER = 0xFF252D3F;
    private static final int WHITE = 0xFFF3F5FF;
    private static final int MUTED = 0xFF929BB0;

    private record Profile(String id, String title, String subtitle, String details, int color) { }
    private static final Profile[] PROFILES = {
        new Profile("performance", "PERFORMANCE", "Minimal overlay, less visual clutter",
            "Keeps FPS and the Astra watermark enabled. Disables other module toggles.", 0xFF69E6B8),
        new Profile("pvp", "PVP HUD", "Combat information, not combat automation",
            "Enables player outlines, tracers, target HUD, CPS, armor, potion, compass, keystrokes and low-health alerts. Does not enable Kill Aura or TriggerBot.", 0xFFB58CFF),
        new Profile("exploration", "EXPLORATION", "Navigation and world awareness",
            "Enables coordinates, direction, biome, waypoint, breadcrumbs, storage/block finder, world info, health, hunger, armor and session stats.", 0xFF54E8FF)
    };

    public AstraProfileScreen(Screen parent) {
        super(Text.literal("Astra Quick Profiles"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG);
        int accent = AstraClient.accentColor();
        AstraUiEffects.renderAmbientBackdrop(ctx, width, height, System.currentTimeMillis(), accent);
        int panelW = Math.max(1, Math.min(680, width - 28));
        int panelH = Math.max(1, Math.min(430, height - 24));
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        ctx.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0x774C34A3);
        ctx.fill(left, top, left + panelW, top + panelH, 0xFF0E131E);
        ctx.fill(left, top, left + panelW, top + 3, accent);
        long now = System.currentTimeMillis();
        int shineW = Math.max(20, panelW / 8);
        int shineX = left + (int)((now / 5L) % (panelW + shineW)) - shineW;
        if (shineX < left + panelW) ctx.fill(Math.max(left, shineX), top, Math.min(left + panelW, shineX + shineW), top + 2, 0xBFFFFFFF);
        ctx.drawText(textRenderer, "ASTRA  /  QUICK PROFILES", left + 20, top + 19, WHITE, true);
        ctx.drawText(textRenderer, "Choose a preset. Your choice saves automatically; you can still toggle every module afterwards.",
            left + 20, top + 39, MUTED, false);

        int gap = 12;
        int cardW = (panelW - 40 - gap * 2) / 3;
        int cardY = top + 76;
        int cardH = panelH - 130;
        for (int i = 0; i < PROFILES.length; i++) {
            Profile p = PROFILES[i];
            int x = left + 20 + i * (cardW + gap);
            boolean hover = mouseX >= x && mouseX <= x + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
            ctx.fill(x, cardY, x + cardW, cardY + cardH, hover ? HOVER : PANEL);
            ctx.fill(x, cardY, x + cardW, cardY + 3, p.color);
            ctx.fill(x, cardY + 3, x + 3, cardY + cardH, p.color);

            // Each preset gets its own geometric mark instead of a generic text glyph.
            int ix = x + cardW - 34;
            int iy = cardY + 13;
            ctx.fill(ix - 5, iy - 3, ix + 23, iy + 21, 0xFF131925);
            ctx.fill(ix - 5, iy - 3, ix + 23, iy - 1, p.color);
            switch (p.id) {
                case "performance" -> {
                    ctx.fill(ix + 1, iy + 11, ix + 5, iy + 17, p.color);
                    ctx.fill(ix + 8, iy + 6, ix + 12, iy + 17, p.color);
                    ctx.fill(ix + 15, iy + 1, ix + 19, iy + 17, p.color);
                }
                case "pvp" -> {
                    ctx.fill(ix + 2, iy + 1, ix + 7, iy + 2, p.color);
                    ctx.fill(ix + 2, iy + 1, ix + 3, iy + 6, p.color);
                    ctx.fill(ix + 13, iy + 1, ix + 18, iy + 2, p.color);
                    ctx.fill(ix + 17, iy + 1, ix + 18, iy + 6, p.color);
                    ctx.fill(ix + 2, iy + 12, ix + 3, iy + 17, p.color);
                    ctx.fill(ix + 2, iy + 16, ix + 7, iy + 17, p.color);
                    ctx.fill(ix + 17, iy + 12, ix + 18, iy + 17, p.color);
                    ctx.fill(ix + 13, iy + 16, ix + 18, iy + 17, p.color);
                    ctx.fill(ix + 9, iy + 8, ix + 12, iy + 11, p.color);
                }
                default -> {
                    ctx.fill(ix + 9, iy + 1, ix + 11, iy + 17, p.color);
                    ctx.fill(ix + 2, iy + 8, ix + 18, iy + 10, p.color);
                    ctx.fill(ix + 5, iy + 4, ix + 8, iy + 7, p.color);
                    ctx.fill(ix + 12, iy + 11, ix + 15, iy + 14, p.color);
                }
            }
            ctx.drawText(textRenderer, p.title, x + 12, cardY + 17, p.color, true);
            ctx.drawText(textRenderer, p.subtitle, x + 12, cardY + 39, WHITE, false);
            drawWrapped(ctx, p.details, x + 12, cardY + 63, cardW - 24, 12, 9, MUTED);
            int buttonY = cardY + cardH - 37;
            ctx.fill(x + 12, buttonY, x + cardW - 12, buttonY + 25, hover ? 0xFF343D50 : 0xFF222A39);
            ctx.fill(x + 12, buttonY, x + 14, buttonY + 25, p.color);
            ctx.drawCenteredTextWithShadow(textRenderer, "APPLY", x + cardW / 2, buttonY + 8, WHITE);
        }

        int footerY = top + panelH - 37;
        ctx.drawText(textRenderer, "Presets only change module on/off states. Keybinds, themes and HUD positions stay untouched.",
            left + 20, footerY + 7, MUTED, false);
        ctx.fill(left + panelW - 90, footerY, left + panelW - 16, footerY + 25, 0xFF222A39);
        ctx.drawCenteredTextWithShadow(textRenderer, "BACK", left + panelW - 53, footerY + 8, WHITE);
    }

    private void drawWrapped(DrawContext ctx, String value, int x, int y, int maxWidth, int lineHeight, int maxLines, int color) {
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : value.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (textRenderer.getWidth(candidate) > maxWidth && line.length() > 0) {
                ctx.drawText(textRenderer, line.toString(), x, y + lines * lineHeight, color, false);
                lines++;
                line = new StringBuilder(word);
                if (lines >= maxLines) return;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0 && lines < maxLines) ctx.drawText(textRenderer, line.toString(), x, y + lines * lineHeight, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();
        int panelW = Math.min(680, width - 28);
        int panelH = Math.min(430, height - 24);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int gap = 12;
        int cardW = (panelW - 40 - gap * 2) / 3;
        int cardY = top + 76;
        int cardH = panelH - 130;
        for (int i = 0; i < PROFILES.length; i++) {
            int x = left + 20 + i * (cardW + gap);
            int buttonY = cardY + cardH - 37;
            if (mouseX >= x + 12 && mouseX <= x + cardW - 12
                && mouseY >= buttonY && mouseY <= buttonY + 25) {
                AstraClient.applyQuickProfile(PROFILES[i].id);
                client.setScreen(parent);
                return true;
            }
        }
        int footerY = top + panelH - 37;
        if (mouseX >= left + panelW - 90 && mouseX <= left + panelW - 16
            && mouseY >= footerY && mouseY <= footerY + 25) {
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
