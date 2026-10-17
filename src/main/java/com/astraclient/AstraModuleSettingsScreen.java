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
    private final String category;
    private final boolean implemented;
    private boolean capturingKey;
    private boolean draggingZoomSlider;
    private boolean draggingEspSlider;
    private boolean draggingCombatSlider;
    private boolean draggingAutoEatSlider;
    private boolean draggingLowHealthSlider;
    private boolean draggingEffectAlertSlider;
    private boolean draggingBlockFinderSlider;
    private boolean draggingCrosshairSizeSlider;
    private boolean draggingCrosshairGapSlider;
    private boolean draggingCrosshairThicknessSlider;
    private boolean draggingBreadcrumbSlider;
    private boolean draggingStorageFinderSlider;

    public AstraModuleSettingsScreen(Screen parent, String moduleId, String moduleName,
                                     String description, String category, boolean implemented) {
        super(Text.literal("Astra Module Settings"));
        this.parent = parent;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.description = description;
        this.category = category == null ? "Utility" : category;
        this.implemented = implemented;
    }

    private int accent() { return AstraClient.accentColor(); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xFF080B12);
        ctx.fill(0, 0, width, height, 0xAF080B12);
        AstraUiEffects.renderAmbientBackdrop(ctx, width, height, System.currentTimeMillis(), accent());

        int panelW = Math.max(1, Math.min(600, width - 24));
        int panelH = Math.max(1, Math.min(390, height - 24));
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
        } else if (moduleId.equals("customcrosshair")) {
            int sliderX = x + 20, sliderW = panelW - 40;
            ctx.drawText(textRenderer, "ARM LENGTH", sliderX, nextY + 2, WHITE, true);
            String sizeValue = Integer.toString(AstraClient.crosshairSize());
            ctx.drawText(textRenderer, sizeValue, sliderX + sliderW - textRenderer.getWidth(sizeValue), nextY + 2, accent(), true);
            int sizeY = nextY + 16;
            ctx.fill(sliderX, sizeY, sliderX + sliderW, sizeY + 5, 0xFF2B3344);
            int sizeFill = (AstraClient.crosshairSize() - 3) * sliderW / 17;
            ctx.fill(sliderX, sizeY, sliderX + sizeFill, sizeY + 5, accent());
            ctx.fill(sliderX + sizeFill - 3, sizeY - 3, sliderX + sizeFill + 3, sizeY + 8, WHITE);

            ctx.drawText(textRenderer, "CENTER GAP", sliderX, nextY + 39, WHITE, true);
            String gapValue = Integer.toString(AstraClient.crosshairGap());
            ctx.drawText(textRenderer, gapValue, sliderX + sliderW - textRenderer.getWidth(gapValue), nextY + 39, accent(), true);
            int gapY = nextY + 53;
            ctx.fill(sliderX, gapY, sliderX + sliderW, gapY + 5, 0xFF2B3344);
            int gapFill = (AstraClient.crosshairGap() - 1) * sliderW / 11;
            ctx.fill(sliderX, gapY, sliderX + gapFill, gapY + 5, accent());
            ctx.fill(sliderX + gapFill - 3, gapY - 3, sliderX + gapFill + 3, gapY + 8, WHITE);

            ctx.drawText(textRenderer, "LINE THICKNESS", sliderX, nextY + 76, WHITE, true);
            String thicknessValue = Integer.toString(AstraClient.crosshairThickness());
            ctx.drawText(textRenderer, thicknessValue, sliderX + sliderW - textRenderer.getWidth(thicknessValue), nextY + 76, accent(), true);
            int thicknessY = nextY + 90;
            ctx.fill(sliderX, thicknessY, sliderX + sliderW, thicknessY + 5, 0xFF2B3344);
            int thicknessFill = (AstraClient.crosshairThickness() - 1) * sliderW / 4;
            ctx.fill(sliderX, thicknessY, sliderX + thicknessFill, thicknessY + 5, accent());
            ctx.fill(sliderX + thicknessFill - 3, thicknessY - 3, sliderX + thicknessFill + 3, thicknessY + 8, WHITE);
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
        } else if (moduleId.equals("playeresp") || moduleId.equals("mobesp")
            || moduleId.equals("itemesp") || moduleId.equals("tracers")) {
            String distanceTitle = moduleId.equals("tracers") ? "MAXIMUM TRACER DISTANCE" : "MAXIMUM OUTLINE DISTANCE";
            ctx.drawText(textRenderer, distanceTitle, x + 20, nextY + 3, WHITE, true);
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
            if (moduleId.equals("tracers")) {
                drawRow(ctx, x + 20, nextY + 54, panelW - 40, 27, "Trace players",
                    AstraClient.tracerPlayers() ? "ON" : "OFF", mouseX, mouseY, a);
                drawRow(ctx, x + 20, nextY + 84, panelW - 40, 27, "Trace mobs",
                    AstraClient.tracerMobs() ? "ON" : "OFF", mouseX, mouseY, a);
            }
        } else if (moduleId.equals("storagefinder")) {
            int sliderX = x + 20, sliderW = panelW - 40;
            ctx.drawText(textRenderer, "STORAGE SEARCH RADIUS", sliderX, nextY + 3, WHITE, true);
            String rangeLabel = AstraClient.storageFinderRange() + " blocks";
            ctx.drawText(textRenderer, rangeLabel, sliderX + sliderW - textRenderer.getWidth(rangeLabel),
                nextY + 3, accent(), true);
            int sliderY = nextY + 27;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.storageFinderRange() - 8) * sliderW / 24;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "8 blocks", sliderX, sliderY + 13, MUTED, false);
            String maxRange = "32 blocks";
            ctx.drawText(textRenderer, maxRange, sliderX + sliderW - textRenderer.getWidth(maxRange),
                sliderY + 13, MUTED, false);
            ctx.drawText(textRenderer, "Search is cached and refreshes periodically.", sliderX,
                nextY + 55, MUTED, false);
        } else if (moduleId.equals("waypoint")) {
            drawRow(ctx, x + 20, nextY, panelW - 40, 32, "Save current position", "SET HERE",
                mouseX, mouseY, a);
            drawRow(ctx, x + 20, nextY + 37, panelW - 40, 32, "Saved location",
                AstraClient.isWaypointSet() ? (AstraClient.waypointX() + " " + AstraClient.waypointY() + " " + AstraClient.waypointZ()) : "NOT SET",
                mouseX, mouseY, a);
            drawRow(ctx, x + 20, nextY + 74, panelW - 40, 32, "Clear waypoint", "CLEAR",
                mouseX, mouseY, a);
        } else if (moduleId.equals("breadcrumbs")) {
            int sliderX = x + 20, sliderW = panelW - 40;
            ctx.drawText(textRenderer, "TRAIL HISTORY", sliderX, nextY + 3, WHITE, true);
            String pointCount = AstraClient.breadcrumbTrailLength() + " points";
            ctx.drawText(textRenderer, pointCount, sliderX + sliderW - textRenderer.getWidth(pointCount), nextY + 3, accent(), true);
            int sliderY = nextY + 27;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.breadcrumbTrailLength() - 12) * sliderW / 108;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "12 points", sliderX, sliderY + 13, MUTED, false);
            String maxPoints = "120 points";
            ctx.drawText(textRenderer, maxPoints, sliderX + sliderW - textRenderer.getWidth(maxPoints), sliderY + 13, MUTED, false);
            ctx.drawText(textRenderer, "Samples every 0.25 seconds; view rotates with your heading.",
                sliderX, nextY + 58, MUTED, false);
        } else if (moduleId.equals("blockfinder")) {
            drawRow(ctx, x + 20, nextY, panelW - 40, 32, "Search target",
                AstraClient.blockFinderTarget(), mouseX, mouseY, a);
            ctx.drawText(textRenderer, "SCAN RADIUS", x + 20, nextY + 43, WHITE, true);
            String scanRadius = AstraClient.blockFinderRange() + " blocks";
            ctx.drawText(textRenderer, scanRadius, x + panelW - 20 - textRenderer.getWidth(scanRadius),
                nextY + 43, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 66, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.blockFinderRange() - 8) * sliderW / 40;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "8 blocks", sliderX, sliderY + 13, MUTED, false);
            String maxRange = "48 blocks";
            ctx.drawText(textRenderer, maxRange, sliderX + sliderW - textRenderer.getWidth(maxRange),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("lowhealthalert")) {
            ctx.drawText(textRenderer, "ALERT WHEN HEALTH IS AT OR BELOW", x + 20, nextY + 3, WHITE, true);
            String healthLimit = AstraClient.lowHealthThreshold() + " HP";
            ctx.drawText(textRenderer, healthLimit, x + panelW - 20 - textRenderer.getWidth(healthLimit),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.lowHealthThreshold() - 2) * sliderW / 18;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "2 HP", sliderX, sliderY + 13, MUTED, false);
            String maxHealth = "20 HP";
            ctx.drawText(textRenderer, maxHealth, sliderX + sliderW - textRenderer.getWidth(maxHealth),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("effectalert")) {
            ctx.drawText(textRenderer, "WARN BEFORE AN EFFECT EXPIRES", x + 20, nextY + 3, WHITE, true);
            String expiryLimit = AstraClient.effectAlertThresholdSeconds() + " seconds";
            ctx.drawText(textRenderer, expiryLimit, x + panelW - 20 - textRenderer.getWidth(expiryLimit),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.effectAlertThresholdSeconds() - 3) * sliderW / 27;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "3 s", sliderX, sliderY + 13, MUTED, false);
            String maxSeconds = "30 s";
            ctx.drawText(textRenderer, maxSeconds, sliderX + sliderW - textRenderer.getWidth(maxSeconds),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("autoeat")) {
            ctx.drawText(textRenderer, "EAT WHEN FOOD LEVEL IS AT OR BELOW", x + 20, nextY + 3, WHITE, true);
            String threshold = Integer.toString(AstraClient.autoEatHungerThreshold()) + " / 20";
            ctx.drawText(textRenderer, threshold, x + panelW - 20 - textRenderer.getWidth(threshold),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            int fill = (AstraClient.autoEatHungerThreshold() - 10) * sliderW / 9;
            ctx.fill(sliderX, sliderY, sliderX + fill, sliderY + 6, accent());
            ctx.fill(sliderX + fill - 4, sliderY - 4, sliderX + fill + 4, sliderY + 10, WHITE);
            ctx.drawText(textRenderer, "10 / 20", sliderX, sliderY + 13, MUTED, false);
            String maxHunger = "19 / 20";
            ctx.drawText(textRenderer, maxHunger, sliderX + sliderW - textRenderer.getWidth(maxHunger),
                sliderY + 13, MUTED, false);
        } else if (moduleId.equals("killaura")) {
            // Keep the UI honest: the attack routine intentionally stays within
            // normal vanilla melee reach. Servers may enforce an even shorter range.
            ctx.drawText(textRenderer, "MAXIMUM MELEE REACH", x + 20, nextY + 3, WHITE, true);
            String rangeLabel = "3 blocks";
            ctx.drawText(textRenderer, rangeLabel, x + panelW - 20 - textRenderer.getWidth(rangeLabel),
                nextY + 3, accent(), true);
            int sliderX = x + 20, sliderY = nextY + 27, sliderW = panelW - 40;
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, 0xFF2B3344);
            ctx.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 6, accent());
            ctx.drawText(textRenderer, "Vanilla client reach", sliderX, sliderY + 13, MUTED, false);
            String serverRange = "Server enforced";
            ctx.drawText(textRenderer, serverRange, sliderX + sliderW - textRenderer.getWidth(serverRange),
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
        drawButton(ctx, x + 322, bottomY, 132, 28, "ADVANCED…", mouseX, mouseY, a);
        drawButton(ctx, x + panelW - 136, bottomY, 116, 28, "RESET KEY", mouseX, mouseY, a);
        // Controls are drawn manually; do not invoke the vanilla screen render path,
        // which can re-apply Minecraft menu blur over Astra.
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
        } else if (moduleId.equals("customcrosshair")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 12, pw - 40, 17)) {
                draggingCrosshairSizeSlider = true;
                updateCrosshairSizeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 49, pw - 40, 17)) {
                draggingCrosshairGapSlider = true;
                updateCrosshairGapFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 86, pw - 40, 17)) {
                draggingCrosshairThicknessSlider = true;
                updateCrosshairThicknessFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("zoom")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingZoomSlider = true;
                updateZoomFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("playeresp") || moduleId.equals("mobesp")
            || moduleId.equals("itemesp") || moduleId.equals("tracers")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingEspSlider = true;
                updateEspRangeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
            if (moduleId.equals("tracers") && inside(mouseX, mouseY, x + 20, nextY + 54, pw - 40, 27)) {
                AstraClient.toggleTracerPlayers();
                return true;
            }
            if (moduleId.equals("tracers") && inside(mouseX, mouseY, x + 20, nextY + 84, pw - 40, 27)) {
                AstraClient.toggleTracerMobs();
                return true;
            }
        } else if (moduleId.equals("storagefinder")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingStorageFinderSlider = true;
                updateStorageFinderRangeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("waypoint")) {
            if (inside(mouseX, mouseY, x + 20, nextY, pw - 40, 32)) {
                AstraClient.setWaypointHere();
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 74, pw - 40, 32)) {
                AstraClient.clearWaypoint();
                return true;
            }
        } else if (moduleId.equals("breadcrumbs")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingBreadcrumbSlider = true;
                updateBreadcrumbLengthFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("blockfinder")) {
            if (inside(mouseX, mouseY, x + 20, nextY, pw - 40, 32)) {
                AstraClient.cycleBlockFinderTarget();
                return true;
            }
            if (inside(mouseX, mouseY, x + 20, nextY + 59, pw - 40, 27)) {
                draggingBlockFinderSlider = true;
                updateBlockFinderRangeFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("lowhealthalert")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingLowHealthSlider = true;
                updateLowHealthThresholdFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("effectalert")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingEffectAlertSlider = true;
                updateEffectAlertThresholdFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("autoeat")) {
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                draggingAutoEatSlider = true;
                updateAutoEatThresholdFromMouse(mouseX, x + 20, pw - 40);
                return true;
            }
        } else if (moduleId.equals("killaura")) {
            int half = (pw - 44) / 2;
            if (inside(mouseX, mouseY, x + 20, nextY + 20, pw - 40, 27)) {
                // Reach is fixed to vanilla melee distance; this bar is informational.
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
        if (inside(mouseX, mouseY, x + 322, bottomY, 132, 28)) {
            client.setScreen(new AstraModuleOptionsScreen(this, moduleId, moduleName, category));
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

    private void updateStorageFinderRangeFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewStorageFinderRange(8 + (int) Math.round(progress * 24.0));
    }

    private void updateBreadcrumbLengthFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewBreadcrumbTrailLength(12 + (int) Math.round(progress * 108.0));
    }

    private void updateCrosshairSizeFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewCrosshairSize(3 + (int) Math.round(progress * 17.0));
    }

    private void updateCrosshairGapFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewCrosshairGap(1 + (int) Math.round(progress * 11.0));
    }

    private void updateCrosshairThicknessFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewCrosshairThickness(1 + (int) Math.round(progress * 4.0));
    }

    private void updateBlockFinderRangeFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewBlockFinderRange(8 + (int) Math.round(progress * 40.0));
    }

    private void updateLowHealthThresholdFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewLowHealthThreshold(2 + (int) Math.round(progress * 18.0));
    }

    private void updateEffectAlertThresholdFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewEffectAlertThresholdSeconds(3 + (int) Math.round(progress * 27.0));
    }

    private void updateAutoEatThresholdFromMouse(double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / Math.max(1.0, sliderW)));
        AstraClient.previewAutoEatHungerThreshold(10 + (int) Math.round(progress * 9.0));
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
        if (button == 0 && (draggingZoomSlider || draggingEspSlider || draggingCombatSlider
            || draggingAutoEatSlider || draggingLowHealthSlider || draggingEffectAlertSlider
            || draggingBlockFinderSlider || draggingCrosshairSizeSlider
            || draggingCrosshairGapSlider || draggingCrosshairThicknessSlider
            || draggingBreadcrumbSlider || draggingStorageFinderSlider)) {
            int pw = Math.min(600, width - 24);
            int x = (width - pw) / 2;
            int sliderX = x + 20;
            if (draggingZoomSlider) updateZoomFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingEspSlider) updateEspRangeFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingAutoEatSlider) updateAutoEatThresholdFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingLowHealthSlider) updateLowHealthThresholdFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingEffectAlertSlider) updateEffectAlertThresholdFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingBlockFinderSlider) updateBlockFinderRangeFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingCrosshairSizeSlider) updateCrosshairSizeFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingCrosshairGapSlider) updateCrosshairGapFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingCrosshairThicknessSlider) updateCrosshairThicknessFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingBreadcrumbSlider) updateBreadcrumbLengthFromMouse(mouseX, sliderX, pw - 40);
            else if (draggingStorageFinderSlider) updateStorageFinderRangeFromMouse(mouseX, sliderX, pw - 40);
            else updateCombatRangeFromMouse(mouseX, sliderX, pw - 40);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingZoomSlider || draggingEspSlider || draggingCombatSlider || draggingAutoEatSlider
            || draggingLowHealthSlider || draggingEffectAlertSlider || draggingBlockFinderSlider
            || draggingCrosshairSizeSlider || draggingCrosshairGapSlider || draggingCrosshairThicknessSlider
            || draggingBreadcrumbSlider || draggingStorageFinderSlider) AstraClient.saveConfig();
        draggingZoomSlider = false;
        draggingEspSlider = false;
        draggingCombatSlider = false;
        draggingAutoEatSlider = false;
        draggingLowHealthSlider = false;
        draggingEffectAlertSlider = false;
        draggingBlockFinderSlider = false;
        draggingCrosshairSizeSlider = false;
        draggingCrosshairGapSlider = false;
        draggingCrosshairThicknessSlider = false;
        draggingBreadcrumbSlider = false;
        draggingStorageFinderSlider = false;
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
