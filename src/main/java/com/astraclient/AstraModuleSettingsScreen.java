package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Per-module detail page: status, keybind editing, and implemented module-specific options. */
public final class AstraModuleSettingsScreen extends Screen {
    private static final int BG = 0xF20A0D16;
    private static final int PANEL = 0xFF171D2A;
    private static final int HOVER = 0xFF252D3F;
    private static final int WHITE = 0xFFF3F5FC;
    private static final int MUTED = 0xFF929BB0;
    private final Screen parent;
    private final String moduleId;
    private final String moduleName;
    private final String description;
    private final boolean implemented;
    private boolean capturingKey;
    private boolean draggingZoomSlider;
    private boolean draggingEspSlider;
    private boolean draggingCombatSlider;

    public AstraModuleSettingsScreen(Screen parent, String moduleId, String moduleName,
                                     String description, boolean implemented) {
        super(Text.literal("Astra Module Settings"));
        this.parent = parent;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.description = description;
        this.implemented = implemented;
    }

    private int accent() { return AstraClient.accentColor(); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xFF080B12);
        ctx.fill(0, 0, width, height, 0xAF080B12);

        int panelW = Math.min(600, width - 24);
        int panelH = Math.min(390, height - 24);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int a = accent();

        ctx.fill(x - 2, y - 2, x + panelW + 2, y + panelH + 2, 0x774C34A3);
        ctx.fill(x, y, x + panelW, y + panelH, BG);
        ctx.fill(x, y, x + panelW, y + 3, a);
        ctx.fill(x, y + 3, x + panelW, y + 56, 0xFF111621);

        ctx.fill(x + 15, y + 13, x + 45, y + 43, 0xFF29213F);
        ctx.fill(x + 15, y + 13, x + 18, y + 43, a);
        ctx.drawCenteredTextWithShadow(textRenderer, "A", x + 30, y + 24, WHITE);
        ctx.drawText(textRenderer, moduleName, x + 55, y + 14, WHITE, true);
        ctx.drawText(textRenderer, "ASTRA  /  MODULE SETTINGS", x + 55, y + 33, a, false);

        ctx.drawText(textRenderer, "DESCRIPTION", x + 20, y + 72, MUTED, false);
        drawWrapped(ctx, description, x + 20, y + 89, panelW - 40, 12, 3, WHITE);

        int statusY = y + 133;
        ctx.fill(x + 20, statusY, x + panelW - 20, statusY + 37, PANEL);
        ctx.fill(x + 20, statusY, x + 22, statusY + 37, implemented ? a : 0xFF667184);
        String status = implemented
            ? (AstraClient.isEnabled(moduleId) ? "ENABLED" : "DISABLED")
            : "NOT IMPLEMENTED IN THIS BUILD";
        ctx.drawText(textRenderer, "STATUS", x + 32, statusY + 7, MUTED, false);
        ctx.drawText(textRenderer, status, x + 32, statusY + 21,
            !implemented ? 0xFF7C8699 : (AstraClient.isEnabled(moduleId) ? 0xFF69E6B8 : WHITE), true);

        int bindY = statusY + 48;
        String bind = capturingKey ? "PRESS ANY KEY  (ESC CLEARS)" : "KEYBIND  •  " + AstraClient.keyName(AstraClient.moduleKeybind(moduleId));
        drawRow(ctx, x + 20, bindY, panelW - 40, 36, "Module hotkey", bind, mouseX, mouseY, a);

        int nextY = bindY + 45;
        if (moduleId.equals("autosprint")) {
            drawRow(ctx, x + 20, nextY, panelW - 40, 32, "Only sprint on ground",
                AstraClient.autoSprintGroundOnly() ? "ON" : "OFF", mouseX, mouseY, a);
            drawRow(ctx, x + 20, nextY + 37, panelW - 40, 32, "Pause in menus",
                AstraClient.autoSprintDisableInMenus() ? "ON" : "OFF", mouseX, mouseY, a);
        } else if (moduleId.equals("zoom")) {
            ctx.drawText(textRenderer, "ZOOM FIELD OF VIEW", x + 20, nextY + 3, WHITE, true);
            String fovLabel = AstraClient.zoomFov() + "°";
            ctx.drawText(textRenderer, fovLabel, x + panelW - 20 - textRenderer.getWidth(fovLabel),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (60 - AstraClient.zoomFov()) * sliderW / 50;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "10°  •  MORE ZOOM", sliderX, sliderY + 13, MUTED, false);
            String less = "60°";
            ctx.drawText(textRenderer, less, sliderX + sliderW - textRenderer.getWidth(less),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("playeresp") || moduleId.equals("mobesp") || moduleId.equals("itemesp")) {
            ctx.drawText(textRenderer, "MAXIMUM OUTLINE DISTANCE", x + 20, nextY + 3, WHITE, true);
            String rangeLabel = AstraClient.espRange() + " blocks";
            ctx.drawText(textRenderer, rangeLabel, x + panelW - 20 - textRenderer.getWidth(rangeLabel),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.espRange() - 16) * sliderW / 112;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "16", sliderX, sliderY + 13, MUTED, false);
            String maxRange = "128 blocks";
            ctx.drawText(textRenderer, maxRange, sliderX + sliderW - textRenderer.getWidth(maxRange),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("killaura")) {
            ctx.drawText(textRenderer, "MAXIMUM TARGET DISTANCE", x + 20, nextY + 3, WHITE, true);
            String rangeLabel = AstraClient.combatRange() + " blocks";
            ctx.drawText(textRenderer, rangeLabel, x + panelW - 20 - textRenderer.getWidth(rangeLabel),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.combatRange() - 3) * sliderW / 3;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "3 blocks", sliderX, sliderY + 13, MUTED, false);
            ctx.drawText(textRenderer, "6 blocks", sliderX + sliderW - textRenderer.getWidth("6 blocks"),
                sliderY + 13, MUTED, false);
            drawRow(ctx, x + 20, nextY + 54, (panelW - 44) / 2, 27, "Target players",
                AstraClient.targetPlayers() ? "ON" : "OFF", mouseX, mouseY, a);
            drawRow(ctx, x + 24 + (panelW - 44) / 2, nextY + 54, (panelW - 44) / 2, 27, "Hostile mobs",
                AstraClient.targetHostile() ? "ON" : "OFF", mouseX, mouseY, a);
            drawRow(ctx, x + 20, nextY + 84, panelW - 40, 27, "Passive mobs",
                AstraClient.targetPassive() ? "ON" : "OFF", mouseX, mouseY, a);
        } else if (isHudModule(moduleId)) {
            drawRow(ctx, x + 20, nextY, panelW - 40, 32, "HUD layout",
                "OPEN EDITOR", mouseX, mouseY, a);
            drawRow(ctx, x + 20, nextY + 37, panelW - 40, 32, "HUD opacity",
                AstraClient.hudOpacity() + "%  •  CHANGE IN SETTINGS", mouseX, mouseY, a);
        } else {
            ctx.drawText(textRenderer,
                implemented ? "This module has no additional settings yet." : "This module is locked until its logic is implemented.",
                x + 20, nextY + 8, MUTED, false);
        }

        int bottomY = y + panelH - 44;
        drawButton(ctx, x + 20, bottomY, 120, 28, "BACK", mouseX, mouseY, a);
        if (implemented) drawButton(ctx, x + 151, bottomY, 160, 28,
            AstraClient.isEnabled(moduleId) ? "DISABLE MODULE" : "ENABLE MODULE", mouseX, mouseY, a);
        drawButton(ctx, x + panelW - 136, bottomY, 116, 28, "RESET KEY", mouseX, mouseY, a);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawRow(DrawContext ctx, int x, int y, int w, int h, String label, String value,
                         int mouseX, int mouseY, int accent) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + h, accent);
        ctx.drawText(textRenderer, label, x + 11, y + Math.max(6, (h - 9) / 2), WHITE, false);
        int vW = textRenderer.getWidth(value);
        ctx.drawText(textRenderer, value, x + w - vW - 10, y + Math.max(6, (h - 9) / 2),
            accent, true);
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label,
                            int mouseX, int mouseY, int accent) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + h, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - 8) / 2, WHITE);
    }

    private void drawWrapped(DrawContext ctx, String text, int x, int y, int maxWidth,
                             int lineHeight, int maxLines, int color) {
        String line = "";
        int lines = 0;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textRenderer.getWidth(candidate) > maxWidth && !line.isEmpty()) {
                ctx.drawText(textRenderer, line, x, y + lines * lineHeight, color, false);
                lines++;
                if (lines >= maxLines) return;
                line = word;
            } else {
                line = candidate;
            }
        }
        if (!line.isEmpty() && lines < maxLines) ctx.drawText(textRenderer, line, x, y + lines * lineHeight, color, false);
    }

    private boolean isHudModule(String id) {
        for (String candidate : AstraClient.hudWidgetIds()) if (candidate.equals(id)) return true;
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();
        int pw = Math.min(600, width - 24);
        int ph = Math.min(390, height - 24);
        int x = (width - pw) / 2, y = (height - ph) / 2;
        int statusY = y + 133, bindY = statusY + 48, nextY = bindY + 45;
        int bottomY = y + ph - 44;

        if (capturingKey) {
            if (button == 1) return true;
            return true; // Key capture is completed in keyPressed.
        }
        if (mouseY >= bindY && mouseY <= bindY + 36 && mouseX >= x + 20 && mouseX <= x + pw - 20) {
            capturingKey = true;
            AstraClient.notifyUser("Press a key for " + moduleName + " (Esc clears)");
            return true;
        }
        if (moduleId.equals("autosprint")) {
            if (inside(mouseX, mouseY, x + 20, nextY, pw - 40, 32)) {
                AstraClient.toggleAutoSprintGroundOnly();
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 37, pw - 40, 32)) {
                AstraClient.toggleAutoSprintDisableInMenus();
                return true;
            }
        } else if (moduleId.equals("zoom")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingZoomSlider = true;
                updateZoomFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("playeresp") || moduleId.equals("mobesp") || moduleId.equals("itemesp")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingEspSlider = true;
                updateEspRangeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("killaura")) {
            int half = (pw - 44) / 2;
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingCombatSlider = true;
                updateCombatRangeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 54, half, 27)) {
                AstraClient.toggleCombatTarget("players");
                return true;
            }
            if (inside(mouseX, mouseY, x + 24 + half, nextY + 54, half, 27)) {
                AstraClient.toggleCombatTarget("hostile");
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 84, pw - 40, 27)) {
                AstraClient.toggleCombatTarget("passive");
                return true;
            }
        } else if (isHudModule(moduleId)) {
            if (inside(mouseX, mouseY, x + 20, nextY, pw - 40, 32)) {
                client.setScreen(new AstraHudEditorScreen(this));
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 37, pw - 40, 32)) {
                client.setScreen(new AstraScreen("Settings"));
                return true;
            }
        }
        if (inside(mouseX, mouseY, x + 20, bottomY, 120, 28)) {
            client.setScreen(parent);
            return true;
        }
        if (implemented && inside(mouseX, mouseY, x + 151, bottomY, 160, 28)) {
            AstraClient.toggle(moduleId);
            return true;
        }
        if (inside(mouseX, mouseY, x + pw - 136, bottomY, 116, 28)) {
            AstraClient.setModuleKeybind(moduleId, GLFW.GLFW_KEY_UNKNOWN);
            capturingKey = false;
            return true;
        }
        return true;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateEspRangeFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewEspRange(16 + (int) Math.round(progress * 112.0));
    }

    private void updateZoomFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        // Left = stronger zoom (10°), right = lighter zoom (60°).
        AstraClient.previewZoomFov(10 + (int) Math.round(progress * 50.0));
    }

    private void updateCombatRangeFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewCombatRange(3 + (int) Math.round(progress * 3.0));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && (draggingZoomSlider || draggingEspSlider || draggingCombatSlider)) {
            int pw = Math.min(600, width - 24);
            int x = (width - pw) / 2;
            int sliderX = x + 20;
            if (draggingZoomSlider) updateZoomFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingEspSlider) updateEspRangeFromMouse(mouseX, sliderX, pw - 40);
            else updateCombatRangeFromMouse(mouseX, sliderX, pw - 40);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingZoomSlider || draggingEspSlider || draggingCombatSlider) AstraClient.saveConfig();
        draggingZoomSlider = false;
        draggingEspSlider = false;
        draggingCombatSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingKey) {
            AstraClient.setModuleKeybind(moduleId,
                keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            capturingKey = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
