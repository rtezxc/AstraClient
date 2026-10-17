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
        if (AstraClient.isReducedMotionEnabled()) time = 0L;

        // Two drifting, soft-edged aurora clouds add depth to secondary Astra screens.
        // The effect is DrawContext-only: no framebuffer, shader, texture, or per-frame object allocation.
        drawAurora(context, width, height, width * 3 / 4, height * 2 / 5,
            Math.max(48, width / 3), Math.max(28, height / 3), time, 0x00472CB5);
        drawAurora(context, width, height, width / 3, height * 3 / 4,
            Math.max(38, width / 4), Math.max(24, height / 4), time + 1900L, 0x00145E8B);

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

    /** Draws a slowly drifting, feathered color cloud using a bounded number of fills. */
    private static void drawAurora(DrawContext context, int width, int height,
                                   int cx, int cy, int radiusX, int radiusY,
                                   long time, int baseRgb) {
        if (radiusX <= 0 || radiusY <= 0 || width <= 0 || height <= 0) return;
        int centerX = cx + (int) (Math.sin(time / 3100.0) * Math.min(22, radiusX / 9));
        int centerY = cy + (int) (Math.cos(time / 2700.0) * Math.min(10, radiusY / 8));
        int baseRed = (baseRgb >> 16) & 0xFF;
        int baseGreen = (baseRgb >> 8) & 0xFF;
        int baseBlue = baseRgb & 0xFF;

        // Four-pixel bands are intentionally low-opacity and overlap seamlessly at normal GUI scales.
        for (int row = -radiusY; row <= radiusY; row += 4) {
            double normalizedY = row / (double) radiusY;
            double envelope = 1.0 - normalizedY * normalizedY;
            if (envelope <= 0.0) continue;
            int halfWidth = (int) (radiusX * Math.sqrt(envelope));
            int drawY = centerY + row;
            if (drawY < 0 || drawY >= height) continue;
            int left = Math.max(0, centerX - halfWidth);
            int right = Math.min(width, centerX + halfWidth);
            if (right <= left) continue;

            int variation = (int) (2.0 * Math.sin((row + time / 43.0) / 15.0));
            int red = Math.max(0, Math.min(255, baseRed + variation));
            int green = Math.max(0, Math.min(255, baseGreen + variation));
            int blue = Math.max(0, Math.min(255, baseBlue + variation));
            int alpha = 3 + (int) (12 * envelope);
            context.fill(left, drawY, right, Math.min(height, drawY + 4),
                (alpha << 24) | (red << 16) | (green << 8) | blue);
        }
    }
}
