package com.astraclient;

import net.minecraft.client.gui.DrawContext;

/**
 * Low-cost shared backdrop texture for Astra's secondary screens.
 *
 * Uses DrawContext fills only (no framebuffer, texture, shader, or allocated particle objects),
 * so it stays compatible with vanilla rendering, Sodium, and mobile OpenGL implementations.
 */
public final class AstraUiEffects {
    private AstraUiEffects() { }

    public static void renderAmbientBackdrop(DrawContext context, int width, int height,
                                             long time, int accent) {
        if (width <= 0 || height <= 0) return;

        int gridStep = Math.max(34, Math.min(60, width / 24));
        int drift = (int) ((time / 97L) % gridStep);
        for (int x = -gridStep + drift; x < width; x += gridStep) {
            if (x >= 0) {
                context.fill(x, 0, Math.min(width, x + 1), height, 0x0A4AD9F5);
            }
        }
        for (int y = drift; y < height; y += gridStep) {
            context.fill(0, y, width, Math.min(height, y + 1), 0x0A8175E8);
        }

        // Fixed deterministic motes mean no random-number work or object churn per frame.
        for (int i = 0; i < 18; i++) {
            int x = Math.floorMod(i * 89 + (int) (time / (73L + i % 5 * 17L)), width);
            int y = Math.floorMod(i * 47 + (int) (time / (91L + i % 4 * 19L)), height);
            int size = i % 6 == 0 ? 2 : 1;
            context.fill(x, y, Math.min(width, x + size), Math.min(height, y + size),
                i % 3 == 0 ? 0x8754E8FF : 0x64756BE0);
        }

        int scanY = Math.floorMod((int) (time / 37L), height);
        context.fill(0, scanY, width, Math.min(height, scanY + 1), 0x124AD9F5);

        // Two opposite corner brackets provide depth without surrounding every control.
        if (width >= 36 && height >= 36) {
            int edge = (0x36 << 24) | (accent & 0x00FFFFFF);
            int rail = Math.min(96, width - 28);
            context.fill(14, 14, 14 + rail, 16, edge);
            context.fill(14, 14, 16, Math.min(height, 48), edge);
            context.fill(Math.max(14, width - 14 - rail), height - 16, width - 14, height - 14, edge);
            context.fill(width - 16, Math.max(14, height - 48), width - 14, height - 14, edge);
        }
    }
}
