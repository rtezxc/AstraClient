package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Small, self-contained RGB theme editor with live preview and persistent color settings. */
public final class AstraThemeEditorScreen extends Screen {
    private static final int WHITE = 0xFFF3F5FC;
    private static final int MUTED = 0xFF929BB0;
    private static final int PANEL = 0xFF171D2A;
    private static final int HOVER = 0xFF252D3F;
    private final Screen parent;
    private int red, green, blue;
    private boolean draggingRed, draggingGreen, draggingBlue;

    public AstraThemeEditorScreen(Screen parent) {
        super(Text.literal("Astra Theme Editor"));
        this.parent = parent;
        red = AstraClient.customAccentRed();
        green = AstraClient.customAccentGreen();
        blue = AstraClient.customAccentBlue();
    }

    private int currentColor() {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int color = currentColor();
        long now = System.currentTimeMillis();
        ctx.fill(0, 0, width, height, 0xFF080B12);
        AstraUiEffects.renderAmbientBackdrop(ctx, width, height, now, AstraClient.accentColor());
        int panelW = Math.max(1, Math.min(490, width - 20));
        int panelH = Math.max(1, Math.min(360, height - 18));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int accent = AstraClient.accentColor();
        ctx.fill(x - 2, y - 2, x + panelW + 2, y + panelH + 2, 0x774C34A3);
        ctx.fill(x, y, x + panelW, y + panelH, 0xF20A0D16);
        ctx.fill(x, y, x + panelW, y + 3, accent);
        ctx.fill(x, y + 3, x + panelW, y + 57, 0xFF111621);
        ctx.drawText(textRenderer, "ASTRA  /  CUSTOM THEME", x + 20, y + 15, WHITE, true);
        ctx.drawText(textRenderer, "RGB COLOR LAB", x + 20, y + 34, accent, false);

        int previewY = y + 70;
        ctx.fill(x + 20, previewY, x + panelW - 20, previewY + 45, 0xFF101521);
        ctx.fill(x + 20, previewY, x + 25, previewY + 45, color);
        ctx.fill(x + 25, previewY, x + panelW - 20, previewY + 3, color);
        ctx.drawText(textRenderer, "ASTRA", x + 35, previewY + 10, WHITE, true);
        ctx.drawText(textRenderer, "PURPLE / CYAN ACCENT PREVIEW", x + 35, previewY + 25, color, false);
        String hex = "#" + String.format(java.util.Locale.ROOT, "%02X%02X%02X", red, green, blue);
        int hexWidth = textRenderer.getWidth(hex);
        ctx.drawText(textRenderer, hex, x + panelW - 32 - hexWidth, previewY + 16, color, true);

        drawRgbSlider(ctx, x, y + 132, panelW, "RED", red, 0xFFFF6A78, 0, mouseX, mouseY);
        drawRgbSlider(ctx, x, y + 184, panelW, "GREEN", green, 0xFF69E6B8, 1, mouseX, mouseY);
        drawRgbSlider(ctx, x, y + 236, panelW, "BLUE", blue, 0xFF54E8FF, 2, mouseX, mouseY);

        int buttonY = y + panelH - 42;
        drawButton(ctx, x + 20, buttonY, 132, 27, "USE PRESET", mouseX, mouseY, accent);
        drawButton(ctx, x + panelW - 152, buttonY, 132, 27, "BACK", mouseX, mouseY, accent);

        // A moving highlight gives the custom accent a small animated shine.
        int shineWidth = Math.max(14, (panelW - 40) / 6);
        int shineX = x + 20 + (int)((now / 6L) % Math.max(1, panelW - 40 + shineWidth)) - shineWidth;
        int shineLeft = Math.max(x + 26, shineX);
        int shineRight = Math.min(x + panelW - 22, shineX + shineWidth);
        if (shineRight > shineLeft) ctx.fill(shineLeft, previewY + 1, shineRight, previewY + 3, 0xBFFFFFFF);
    }

    private void drawRgbSlider(DrawContext ctx, int panelX, int rowY, int panelW,
                               String label, int value, int barColor, int channel, int mouseX, int mouseY) {
        int trackX = panelX + 24;
        int trackW = Math.max(1, panelW - 48);
        ctx.drawText(textRenderer, label, trackX, rowY, WHITE, true);
        String valueText = Integer.toString(value);
        ctx.drawText(textRenderer, valueText, trackX + trackW - textRenderer.getWidth(valueText), rowY, barColor, true);
        int trackY = rowY + 18;

        // Preview the full channel range using the other two live RGB values.
        // A bounded segment count keeps the gradient smooth enough without hundreds of draw calls.
        int segments = Math.min(36, Math.max(12, trackW / 10));
        for (int segment = 0; segment < segments; segment++) {
            int startX = trackX + segment * trackW / segments;
            int endX = trackX + (segment + 1) * trackW / segments;
            if (endX <= startX) continue;
            int component = segment * 255 / Math.max(1, segments - 1);
            int r = channel == 0 ? component : red;
            int g = channel == 1 ? component : green;
            int b = channel == 2 ? component : blue;
            int segmentColor = 0xFF000000 | (r << 16) | (g << 8) | b;
            ctx.fill(startX, trackY, endX, trackY + 7, segmentColor);
        }

        int fill = value * trackW / 255;
        if (fill > 0) ctx.fill(trackX, trackY, trackX + fill, trackY + 1, 0xBFFFFFFF);
        int knobX = trackX + fill;
        ctx.fill(Math.max(trackX, knobX - 4), trackY - 3,
            Math.min(trackX + trackW, knobX + 4), trackY + 10, WHITE);
        if (mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= trackY - 6 && mouseY <= trackY + 13) {
            ctx.fill(trackX, trackY + 11, trackX + trackW, trackY + 12, 0xAAFFFFFF);
        }
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h,
                            String label, int mouseX, int mouseY, int accent) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + h, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 9, WHITE);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateChannel(double mouseX, int trackX, int trackW, int channel) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - trackX) / Math.max(1.0, trackW)));
        int value = (int)Math.round(progress * 255.0);
        if (channel == 0) red = value;
        else if (channel == 1) green = value;
        else blue = value;
        AstraClient.previewCustomAccentColor(currentColor());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        AstraClient.playClickSound();
        int panelW = Math.min(490, width - 20);
        int panelH = Math.min(360, height - 18);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int trackX = x + 24, trackW = panelW - 48;
        if (inside(mouseX, mouseY, trackX, y + 144, trackW, 20)) {
            draggingRed = true;
            updateChannel(mouseX, trackX, trackW, 0);
            return true;
        }
        if (inside(mouseX, mouseY, trackX, y + 196, trackW, 20)) {
            draggingGreen = true;
            updateChannel(mouseX, trackX, trackW, 1);
            return true;
        }
        if (inside(mouseX, mouseY, trackX, y + 248, trackW, 20)) {
            draggingBlue = true;
            updateChannel(mouseX, trackX, trackW, 2);
            return true;
        }
        int buttonY = y + panelH - 42;
        if (inside(mouseX, mouseY, x + 20, buttonY, 132, 27)) {
            AstraClient.usePresetAccent();
            client.setScreen(parent);
            return true;
        }
        if (inside(mouseX, mouseY, x + panelW - 152, buttonY, 132, 27)) {
            client.setScreen(parent);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && (draggingRed || draggingGreen || draggingBlue)) {
            int trackX = (width - Math.min(490, width - 20)) / 2 + 24;
            int trackW = Math.min(490, width - 20) - 48;
            if (draggingRed) updateChannel(mouseX, trackX, trackW, 0);
            if (draggingGreen) updateChannel(mouseX, trackX, trackW, 1);
            if (draggingBlue) updateChannel(mouseX, trackX, trackW, 2);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingRed || draggingGreen || draggingBlue) AstraClient.setCustomAccentColor(currentColor());
        draggingRed = draggingGreen = draggingBlue = false;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
