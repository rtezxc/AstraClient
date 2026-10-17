package com.astraclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Astra's animated title and world-browser backdrop.
 *
 * The scene is intentionally built from Minecraft's DrawContext primitives rather than
 * depending on shaders, framebuffers, or renderer-specific OpenGL behavior. That keeps it
 * visually rich while remaining compatible with vanilla rendering and mobile launchers.
 */
public final class AstraBackgroundRenderer {
    private AstraBackgroundRenderer() { }

    public static void render(DrawContext context) {
        int width = Math.max(1, context.getScaledWindowWidth());
        int height = Math.max(1, context.getScaledWindowHeight());
        long time = System.currentTimeMillis();

        // Deep-space base and broad vertical color falloff.
        context.fill(0, 0, width, height, 0xFF050711);
        for (int y = 0; y < height; y += 3) {
            float p = y / (float) height;
            int red = (int) (5 + 5 * p);
            int green = (int) (7 + 5 * p);
            int blue = (int) (18 + 20 * p);
            context.fill(0, y, width, Math.min(height, y + 3),
                0xFF000000 | (red << 16) | (green << 8) | blue);
        }

        // Large layered nebula clouds, rasterized as horizontal soft-edged bands.
        // The asymmetrical placement keeps the central-left menu controls readable.
        drawNebula(context, width, height, width * 3 / 4, height * 2 / 5,
            Math.max(44, width / 3), Math.max(28, height / 3), time, 0x003A35C9);
        drawNebula(context, width, height, width * 5 / 8, height * 3 / 4,
            Math.max(35, width / 4), Math.max(24, height / 4), time + 1700L, 0x00134C76);
        drawNebula(context, width, height, width / 5, height / 4,
            Math.max(24, width / 7), Math.max(18, height / 5), time + 3100L, 0x002D176F);

        // Technical grid, with a slow drift so the backdrop feels alive.
        int gridStep = Math.max(25, Math.min(44, width / 27));
        int drift = (int) ((time / 54L) % gridStep);
        for (int x = -gridStep + drift; x < width; x += gridStep) {
            int left = Math.max(0, x);
            int right = Math.min(width, x + 1);
            if (right > left) context.fill(left, 0, right, height, 0x153C6A9A);
        }
        for (int y = drift; y < height; y += gridStep) {
            int top = Math.max(0, y);
            int bottom = Math.min(height, y + 1);
            if (bottom > top) context.fill(0, top, width, bottom, 0x143C6A9A);
        }

        // A few broad, translucent light beams cross the sky at different speeds.
        // Their number is fixed, so frame cost does not grow with elapsed time.
        for (int beam = 0; beam < 5; beam++) {
            int travel = Math.max(1, width + height);
            int offset = Math.floorMod((int) (time / (65L + beam * 23L))
                + beam * (width / 5), travel);
            for (int step = 0; step < height; step += 8) {
                int x = offset - step;
                if (x >= 0 && x < width) {
                    int beamWidth = beam % 2 == 0 ? 2 : 4;
                    int color = beam % 2 == 0 ? 0x104AD9F5 : 0x0E8175E8;
                    context.fill(x, step, Math.min(width, x + beamWidth),
                        Math.min(height, step + 8), color);
                }
            }
        }

        // Main Astra orbital engine: three segmented elliptical rings and a fine inner ring.
        int cx = width * 3 / 4;
        int cy = height / 2;
        int baseRadius = Math.max(22, Math.min(width, height) / 5);
        for (int ring = 0; ring < 4; ring++) {
            double rotation = time / (1450.0 + ring * 370.0) + ring * 1.15;
            int radiusX = baseRadius + ring * 13;
            int radiusY = Math.max(12, (int) (radiusX * 0.72));
            int samples = ring == 3 ? 72 : 96;
            for (int point = 0; point < samples; point++) {
                if ((point + ring * 3) % (ring == 1 ? 4 : 3) == 0) continue;
                double angle = point * (Math.PI * 2.0 / samples) + rotation;
                int px = cx + (int) Math.round(Math.cos(angle) * radiusX);
                int py = cy + (int) Math.round(Math.sin(angle) * radiusY);
                if (px < 0 || px >= width || py < 0 || py >= height) continue;
                int color = ring % 2 == 0 ? 0xC04AD9F5 : 0xB68175E8;
                int dotSize = ring == 0 ? 3 : 2;
                context.fill(px, py, Math.min(width, px + dotSize),
                    Math.min(height, py + dotSize), color);
            }
        }

        // Four radial spokes and bright orbital markers make the rings read as a machine,
        // rather than as plain decorative circles.
        for (int spoke = 0; spoke < 4; spoke++) {
            double angle = time / 1800.0 + spoke * Math.PI / 2.0;
            int endX = cx + (int) (Math.cos(angle) * baseRadius * 1.28);
            int endY = cy + (int) (Math.sin(angle) * baseRadius * 0.92);
            drawLine(context, cx, cy, endX, endY, 0x374AD9F5, width, height, 3);
            int markerX = cx + (int) (Math.cos(angle) * (baseRadius + 24));
            int markerY = cy + (int) (Math.sin(angle) * (baseRadius * 0.72 + 18));
            if (markerX >= 0 && markerX < width && markerY >= 0 && markerY < height) {
                context.fill(markerX - 2, markerY - 2, markerX + 3, markerY + 3, 0xFF4AD9F5);
                context.fill(markerX - 4, markerY, markerX + 5, markerY + 1, 0x6654E8FF);
                context.fill(markerX, markerY - 4, markerX + 1, markerY + 5, 0x6654E8FF);
            }
        }

        // Angular Astra A emblem, with a layered core and animated crossbar.
        int emblemSize = Math.max(24, Math.min(84, Math.min(width, height) / 8));
        int corePulse = 165 + (int) (55 * (0.5 + 0.5 * Math.sin(time / 330.0)));
        int coreColor = (corePulse << 24) | 0x004AD9F5;
        for (int step = 0; step <= emblemSize / 2; step += 2) {
            int py = cy + emblemSize / 2 - step;
            int leftX = cx - emblemSize / 2 + step;
            int rightX = cx + emblemSize / 2 - step;
            if (leftX >= 0 && leftX < width && py >= 0 && py < height) {
                context.fill(leftX, py, Math.min(width, leftX + 2), Math.min(height, py + 2), coreColor);
            }
            if (rightX - 1 >= 0 && rightX < width && py >= 0 && py < height) {
                context.fill(rightX - 1, py, rightX + 1, Math.min(height, py + 2), 0xD68175E8);
            }
        }
        int crossY = cy + emblemSize / 6;
        int crossHalf = Math.max(4, emblemSize / 4);
        context.fill(Math.max(0, cx - crossHalf), crossY,
            Math.min(width, cx + crossHalf), Math.min(height, crossY + 2), 0xF04AD9F5);
        context.fill(Math.max(0, cx - 3), Math.max(0, cy - 3),
            Math.min(width, cx + 4), Math.min(height, cy + 4), 0xFFDAFCFF);

        // Rich star field with different sizes, twinkle, and slow vertical drift.
        for (int i = 0; i < 88; i++) {
            int starX = Math.floorMod(i * 97 + (int) (time / (31L + i % 7 * 13L)), width);
            int starY = Math.floorMod(i * 53 + (int) (time / (43L + i % 9 * 11L)), height);
            int size = i % 19 == 0 ? 3 : (i % 5 == 0 ? 2 : 1);
            int alpha = 105 + Math.floorMod(i * 29 + (int) (time / (80L + i % 4 * 27L)), 145);
            int rgb = i % 4 == 0 ? 0x0054E8FF : (i % 3 == 0 ? 0x00B58CFF : 0x00D8E5FF);
            context.fill(starX, starY, Math.min(width, starX + size), Math.min(height, starY + size),
                (alpha << 24) | rgb);
            // Cross-shaped glints on a few bright stars.
            if (i % 19 == 0) {
                context.fill(Math.max(0, starX - 2), starY + 1, Math.min(width, starX + size + 2),
                    Math.min(height, starY + 2), 0x6654E8FF);
                context.fill(starX + 1, Math.max(0, starY - 2), Math.min(width, starX + 2),
                    Math.min(height, starY + size + 2), 0x6654E8FF);
            }
        }

        // Floating constellation paths on the upper and lower edges.
        drawConstellation(context, width, height, time, 0.10, 0.20, 0x8054E8FF);
        drawConstellation(context, width, height, time + 2100L, 0.56, 0.82, 0x777D70E8);

        // Left-side telemetry frame and bottom status rail.
        int panelWidth = Math.min(270, Math.max(0, width / 3));
        int panelTop = Math.max(58, height / 5);
        int panelBottom = Math.min(height - 42, height * 4 / 5);
        if (panelWidth > 48 && panelBottom > panelTop) {
            int panelLeft = Math.max(18, width / 12);
            int panelRight = Math.min(width - 18, panelLeft + panelWidth);
            context.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0x604AD9F5);
            context.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0x426D54D9);
            context.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0x384AD9F5);
            context.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0x386D54D9);
            context.fill(panelLeft + 8, panelTop + 10, panelLeft + 64, panelTop + 12, 0x9954E8FF);
            context.fill(panelLeft + 8, panelTop + 16, panelLeft + 42, panelTop + 17, 0x557D70E8);
            for (int mark = 0; mark < 18; mark++) {
                int markX = panelLeft + 10 + mark * Math.max(3, (panelRight - panelLeft - 20) / 18);
                int markHeight = 3 + Math.floorMod(mark * 7 + (int) (time / 180L), 15);
                context.fill(markX, panelBottom - 8 - markHeight,
                    Math.min(panelRight - 5, markX + 2), panelBottom - 8,
                    mark % 3 == 0 ? 0xFF4AD9F5 : 0xFF655CC4);
            }
        }

        // Thin scanline and corner brackets complete the interface frame.
        int scanY = Math.floorMod((int) (time / 28L), height);
        context.fill(0, scanY, width, Math.min(height, scanY + 1), 0x184AD9F5);
        int rail = Math.min(168, Math.max(0, width - 36));
        if (width >= 36 && height >= 20) {
            context.fill(18, 18, Math.min(width, 18 + rail), 20, 0xFF4AD9F5);
            context.fill(18, 18, 20, Math.min(height, 64), 0xFF4AD9F5);
            context.fill(Math.max(18, width - 18 - rail), height - 22, width - 18, height - 20, 0xFF6D54D9);
            context.fill(width - 20, Math.max(18, height - 64), width - 18, height - 20, 0xFF6D54D9);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer != null && width > 160 && height > 55) {
            context.drawText(client.textRenderer, "ASTRA  /  FABRIC CLIENT", 28, 31, 0xFFB9C8E8, false);
            context.drawText(client.textRenderer, "NEBULA INTERFACE  •  1.21.4", 28, 44, 0xFF687DAB, false);
            String footer = "ASTRA SYSTEMS  //  READY";
            int footerWidth = client.textRenderer.getWidth(footer);
            if (width > footerWidth + 32 && height > 30) {
                context.drawText(client.textRenderer, footer, width - footerWidth - 24, height - 31, 0xFF9AA9D4, false);
            }
        }
    }

    private static void drawNebula(DrawContext context, int width, int height, int cx, int cy,
                                   int radiusX, int radiusY, long time, int baseRgb) {
        if (radiusX <= 0 || radiusY <= 0) return;
        double driftX = Math.sin(time / 2200.0) * Math.min(14, radiusX / 8);
        double driftY = Math.cos(time / 2700.0) * Math.min(9, radiusY / 8);
        int centerX = cx + (int) driftX;
        int centerY = cy + (int) driftY;
        for (int y = -radiusY; y <= radiusY; y += 3) {
            double normalizedY = y / (double) radiusY;
            double inside = 1.0 - normalizedY * normalizedY;
            if (inside <= 0) continue;
            int halfWidth = (int) (radiusX * Math.sqrt(inside));
            int left = Math.max(0, centerX - halfWidth);
            int right = Math.min(width, centerX + halfWidth);
            int drawY = centerY + y;
            if (drawY < 0 || drawY >= height || right <= left) continue;
            int alpha = (int) (5 + 16 * inside);
            int variation = (int) (4 * Math.sin((y + time / 31.0) / 11.0));
            int r = Math.max(0, Math.min(255, ((baseRgb >> 16) & 255) + variation));
            int g = Math.max(0, Math.min(255, ((baseRgb >> 8) & 255) + variation));
            int b = Math.max(0, Math.min(255, (baseRgb & 255) + variation));
            context.fill(left, drawY, right, drawY + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private static void drawConstellation(DrawContext context, int width, int height, long time,
                                          double xFraction, double yFraction, int color) {
        int offset = (int) (Math.sin(time / 1800.0) * 8);
        int x0 = (int) (width * xFraction) + offset;
        int y0 = (int) (height * yFraction);
        int x1 = x0 + Math.max(20, width / 14);
        int y1 = y0 + Math.max(10, height / 12);
        int x2 = x1 + Math.max(16, width / 18);
        int y2 = y0 - Math.max(8, height / 16);
        drawLine(context, x0, y0, x1, y1, color, width, height, 2);
        drawLine(context, x1, y1, x2, y2, color, width, height, 2);
        drawLine(context, x0, y0, x2, y2, color, width, height, 2);
        drawStar(context, x0, y0, width, height);
        drawStar(context, x1, y1, width, height);
        drawStar(context, x2, y2, width, height);
    }

    private static void drawStar(DrawContext context, int x, int y, int width, int height) {
        if (x < 2 || x >= width - 2 || y < 2 || y >= height - 2) return;
        context.fill(x - 2, y, x + 3, y + 1, 0xAA54E8FF);
        context.fill(x, y - 2, x + 1, y + 3, 0xAA54E8FF);
        context.fill(x - 1, y - 1, x + 2, y + 2, 0xFFDAFCFF);
    }

    private static void drawLine(DrawContext context, int x0, int y0, int x1, int y1,
                                 int color, int width, int height, int thickness) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i += 2) {
            double t = i / (double) steps;
            int x = (int) Math.round(x0 + (x1 - x0) * t);
            int y = (int) Math.round(y0 + (y1 - y0) * t);
            if (x < 0 || x >= width || y < 0 || y >= height) continue;
            context.fill(x, y, Math.min(width, x + thickness), Math.min(height, y + thickness), color);
        }
    }
}
