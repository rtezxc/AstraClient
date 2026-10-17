package com.astraclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Lightweight, animated Astra title/world-browser backdrop.
 * Draws through DrawContext only: no texture assets, shader assumptions, or GPU-specific code.
 */
public final class AstraBackgroundRenderer {
    private AstraBackgroundRenderer() {}

    public static void render(DrawContext context) {
        int width = Math.max(1, context.getScaledWindowWidth());
        int height = Math.max(1, context.getScaledWindowHeight());
        long time = System.currentTimeMillis();

        context.fill(0, 0, width, height, 0xFF070A14);

        // Layered night-sky gradient, kept deliberately subtle behind the controls.
        for (int y = 0; y < height; y += 4) {
            float progress = y / (float) height;
            int blue = (int) (18 + progress * 18);
            int green = (int) (10 + progress * 5);
            context.fill(0, y, width, Math.min(height, y + 4),
                0xFF000000 | (8 << 16) | (green << 8) | blue);
        }

        int gridStep = Math.max(24, Math.min(42, width / 28));
        int drift = (int) ((time / 45L) % gridStep);
        for (int x = -gridStep + drift; x < width; x += gridStep) {
            int left = Math.max(0, x);
            int right = Math.min(width, x + 1);
            if (right > left) context.fill(left, 0, right, height, 0x183C6A9A);
        }
        for (int y = drift; y < height; y += gridStep) {
            int top = Math.max(0, y);
            int bottom = Math.min(height, y + 1);
            if (bottom > top) context.fill(0, top, width, bottom, 0x183C6A9A);
        }

        // Large geometric emblem to the right; the main menu remains unobstructed.
        int cx = width * 3 / 4;
        int cy = height / 2;
        int pulse = 90 + (int) (35 * (0.5 + 0.5 * Math.sin(time / 520.0)));
        int glow = (pulse << 24) | 0x003F43C8;
        for (int ring = 0; ring < 4; ring++) {
            int radius = Math.max(12, Math.min(width, height) / 5 + ring * 13);
            int inset = (int) (Math.sin(time / 1100.0 + ring) * 8.0);
            int top = cy - radius + inset;
            int bottom = cy + radius + inset;
            int left = cx - radius;
            int right = cx + radius;
            int color = ring % 2 == 0 ? glow : 0x454AD9F5;
            if (top >= 0 && top < height) context.fill(Math.max(0, left), top, Math.min(width, right), top + 1, color);
            if (bottom >= 0 && bottom < height) context.fill(Math.max(0, left), bottom, Math.min(width, right), bottom + 1, color);
            if (left >= 0 && left < width) context.fill(left, Math.max(0, top), left + 1, Math.min(height, bottom), color);
            if (right - 1 >= 0 && right - 1 < width) context.fill(right - 1, Math.max(0, top), right, Math.min(height, bottom), color);
        }

        // Sparse moving particles, calculated from time so no per-frame collections are allocated.
        for (int i = 0; i < 44; i++) {
            int sx = Math.floorMod(i * 97 + (int) (time / (19L + i % 5)), width);
            int sy = Math.floorMod(i * 53 + (int) (time / (31L + i % 7)), height);
            int size = i % 7 == 0 ? 3 : 1;
            context.fill(sx, sy, Math.min(width, sx + size), Math.min(height, sy + size),
                i % 3 == 0 ? 0xFF55E7FF : 0xFF7770D8);
        }

        // Corner brackets adapt to small resolutions instead of producing inverted rectangles.
        int rail = Math.min(152, Math.max(0, width - 36));
        int leftRailEnd = Math.min(width, 18 + rail);
        if (width >= 36 && height >= 20) {
            context.fill(18, 18, leftRailEnd, 20, 0xFF4AD9F5);
            context.fill(18, 18, 20, Math.min(height, 64), 0xFF4AD9F5);
            context.fill(Math.max(18, width - 18 - rail), height - 22, width - 18, height - 20, 0xFF6D54D9);
            context.fill(width - 20, Math.max(18, height - 64), width - 18, height - 20, 0xFF6D54D9);
        }

        var client = MinecraftClient.getInstance();
        if (client.textRenderer != null && width > 160 && height > 55) {
            context.drawText(client.textRenderer, "ASTRA  /  FABRIC CLIENT", 28, 31, 0xFFB9C8E8, false);
            context.drawText(client.textRenderer, "NEBULA INTERFACE  •  1.21.4", 28, 44, 0xFF687DAB, false);
            String footer = "ASTRA SYSTEMS  //  READY";
            int footerWidth = client.textRenderer.getWidth(footer);
            if (width > footerWidth + 32 && height > 30) {
                context.drawText(client.textRenderer, footer, width - footerWidth - 24, height - 31, 0xFF687DAB, false);
            }
        }
    }
}
