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

        // Slow diagonal light shafts add depth without textures or shader dependencies.
        // Each shaft is only a few dozen tiny fills, keeping the title screen mobile-friendly.
        for (int beam = 0; beam < 3; beam++) {
            int beamOffset = Math.floorMod((int) (time / (70L + beam * 17L)) + beam * width / 3, width + height);
            for (int step = 0; step < height; step += 8) {
                int x = beamOffset - step;
                int y = step;
                if (x >= 0 && x < width) {
                    int beamWidth = beam == 1 ? 3 : 2;
                    int beamColor = beam == 1 ? 0x124AD9F5 : 0x0D7D69E8;
                    context.fill(x, y, Math.min(width, x + beamWidth), Math.min(height, y + 8), beamColor);
                }
            }
        }

        // Dotted circular orbital tracks break up the square grid and frame the Astra emblem.
        // Fixed sample counts avoid allocations and keep rendering cost bounded at every resolution.
        int orbitRadius = Math.max(18, Math.min(width, height) / 6);
        for (int orbit = 0; orbit < 3; orbit++) {
            double rotation = time / (1250.0 + orbit * 310.0) + orbit * 1.7;
            int radius = orbitRadius + orbit * 11;
            for (int point = 0; point < 64; point++) {
                double angle = point * (Math.PI * 2.0 / 64.0) + rotation;
                int px = cx + (int) Math.round(Math.cos(angle) * radius);
                int py = cy + (int) Math.round(Math.sin(angle) * radius * 0.72);
                // Gaps create a segmented, technical orbital-ring look.
                if (point % 4 == 0 && px >= 0 && px < width && py >= 0 && py < height) {
                    int dot = orbit == 1 ? 0xFF4AD9F5 : 0xFF8175E8;
                    context.fill(px, py, Math.min(width, px + 2), Math.min(height, py + 2), dot);
                }
            }
        }

        // A large, angular Astra "A" sits inside the orbital core as a recognizable
        // brand mark. It is rasterized with short fills, not textures or custom shaders.
        int emblemSize = Math.max(20, Math.min(72, Math.min(width, height) / 9));
        int corePulse = 160 + (int) (50 * (0.5 + 0.5 * Math.sin(time / 330.0)));
        int coreColor = (corePulse << 24) | 0x004AD9F5;
        for (int step = 0; step <= emblemSize / 2; step += 2) {
            int py = cy + emblemSize / 2 - step;
            int leftX = cx - emblemSize / 2 + step;
            int rightX = cx + emblemSize / 2 - step;
            context.fill(leftX, py, Math.min(width, leftX + 2), Math.min(height, py + 2), coreColor);
            context.fill(Math.max(0, rightX - 1), py, rightX + 1, Math.min(height, py + 2), 0xB68175E8);
        }
        int crossY = cy + emblemSize / 6;
        int crossHalf = Math.max(4, emblemSize / 4);
        context.fill(cx - crossHalf, crossY, cx + crossHalf, crossY + 2, 0xD04AD9F5);

        // Soft HUD-like framing panels add depth while keeping menu text readable.
        int panelWidth = Math.min(260, Math.max(0, width / 3));
        int panelTop = Math.max(58, height / 5);
        int panelBottom = Math.min(height - 42, height * 4 / 5);
        if (panelWidth > 48 && panelBottom > panelTop) {
            int panelLeft = Math.max(18, width / 12);
            int panelRight = Math.min(width - 18, panelLeft + panelWidth);
            context.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0x304AD9F5);
            context.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0x226D54D9);
            context.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0x224AD9F5);
            context.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0x226D54D9);
            // Small segmented telemetry marks are decorative only; no gameplay state is read.
            for (int mark = 0; mark < 9; mark++) {
                int markX = panelLeft + 10 + mark * Math.max(4, (panelRight - panelLeft - 20) / 9);
                int markHeight = 3 + Math.floorMod(mark * 7 + (int) (time / 240L), 12);
                context.fill(markX, panelBottom - 8 - markHeight, Math.min(panelRight - 5, markX + 2),
                    panelBottom - 8, mark % 3 == 0 ? 0xFF4AD9F5 : 0xFF655CC4);
            }
        }

        // A restrained horizontal scan line slowly travels down the backdrop.
        int scanY = Math.floorMod((int) (time / 28L), Math.max(1, height));
        context.fill(0, scanY, width, Math.min(height, scanY + 1), 0x184AD9F5);

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
