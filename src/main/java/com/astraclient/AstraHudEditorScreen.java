package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** A live drag editor for the HUD positions persisted by AstraClient. */
public final class AstraHudEditorScreen extends Screen {
    private static final int PANEL = 0xE90B101A;
    private static final int CARD = 0xE91A2231;
    private static final int TEXT = 0xFFF2F5FC;
    private static final int MUTED = 0xFF8996AD;

    // Every HUD element registered by AstraClient is represented here so none
    // are left impossible to reposition from the editor. Two columns keep the
    // preview usable at common laptop and phone-scaled resolutions.
    private static final List<Widget> WIDGETS = List.of(
        // Defaults intentionally mirror renderHud() in AstraClient so the
        // editor preview starts where each widget is actually drawn in-game.
        new Widget("fps", "FPS Counter", 8, 8),
        new Widget("coordinates", "Coordinates", 8, 29),
        new Widget("direction", "Direction", 8, 50),
        new Widget("ping", "Ping", 8, 71),
        new Widget("memory", "Memory", 8, 92),
        new Widget("speedometer", "Speedometer", 8, 113),
        new Widget("clock", "Clock", 8, 134),
        new Widget("totemcounter", "Totem Counter", 8, 218),
        new Widget("pearlcounter", "Ender Pearl Counter", 8, 239),
        new Widget("arrowcounter", "Arrow Counter", 8, 260),
        new Widget("goldencounter", "Golden Apples", 8, 281),
        new Widget("armorhud", "Armor HUD", 8, 302),
        new Widget("potionhud", "Potion HUD", 150, 260),
        new Widget("targethud", "Target HUD", 150, -43),
        new Widget("damageindicators", "Damage Indicators", 150, -190),
        new Widget("blockfinder", "Block Finder", 150, -60),
        new Widget("breadcrumbs", "Breadcrumb Trail", 500, 180),
        new Widget("waypoint", "Waypoint", 8, -80),
        new Widget("storagefinder", "Storage Finder", 8, -100),
        new Widget("sessionstats", "Session Stats", 8, 449),
        new Widget("arraylist", "Active Modules", 150, 8),
        new Widget("watermark", "Astra Watermark", 8, 160),
        new Widget("fpsgraph", "FPS Graph", 150, 70),
        new Widget("compasshud", "Compass HUD", 150, 178),
        new Widget("keystrokes", "Keystrokes", 8, -110),
        new Widget("healthhud", "Health HUD", 8, -42),
        new Widget("hungerhud", "Hunger HUD", 8, -21),
        new Widget("blockreachhud", "Block Reach HUD", 150, -84),
        new Widget("frametimehud", "Frame Time HUD", 150, -63),
        new Widget("biome", "Biome Display", 8, 365),
        new Widget("cps", "CPS Counter", 8, 386),
        new Widget("worldinfo", "World Information", 8, 407),
        new Widget("serverinfo", "Server Information", 8, -122),
        new Widget("potioncounter", "Potion Counter", 8, 428),
        new Widget("durabilityhud", "Durability HUD", 8, 323),
        new Widget("inventoryhud", "Inventory HUD", 8, 344)
    );

    private final List<Box> renderedBoxes = new ArrayList<>();
    private final Screen parent;
    private String draggedId;
    private int dragOffsetX, dragOffsetY;
    private final long openedAt = System.currentTimeMillis();

    public AstraHudEditorScreen() {
        this(null);
    }

    public AstraHudEditorScreen(Screen parent) {
        super(Text.literal("Astra HUD Editor"));
        this.parent = parent;
    }

    private record Widget(String id, String label, int defaultX, int defaultY) { }
    private record Box(String id, String label, int x, int y, int width, int height, boolean enabled) { }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float progress = Math.min(1.0f, Math.max(0.0f, (now - openedAt) / 240.0f));
        float eased = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
        int shade = Math.round(8 + 3 * eased);
        context.fill(0, 0, width, height, 0xFF000000 | (shade << 16) | (shade << 8) | (shade + 4));
        int accent = AstraClient.accentColor();

        // The same restrained cyan-violet technical grid as the main Astra menu.
        // It is painted first so widget cards and labels always stay crisp above it.
        int gridStep = Math.max(28, Math.min(44, width / 28));
        int gridDrift = (int) ((now / 83L) % gridStep);
        for (int gx = -gridStep + gridDrift; gx < width; gx += gridStep) {
            if (gx >= 0 && gx < width) {
                context.fill(gx, 0, Math.min(width, gx + 1), height, 0x0C4AD9F5);
            }
        }
        for (int gy = gridDrift; gy < height; gy += gridStep) {
            if (gy >= 0 && gy < height) {
                context.fill(0, gy, width, Math.min(height, gy + 1), 0x0B8175E8);
            }
        }
        for (int mote = 0; mote < 20; mote++) {
            int mx = Math.floorMod(mote * 89 + (int) (now / (61L + mote % 5 * 19L)), Math.max(1, width));
            int my = Math.floorMod(mote * 43 + (int) (now / (73L + mote % 4 * 23L)), Math.max(1, height));
            int size = mote % 6 == 0 ? 2 : 1;
            context.fill(mx, my, Math.min(width, mx + size), Math.min(height, my + size),
                mote % 3 == 0 ? 0x9054E8FF : 0x78756BE0);
        }

        int headerY = 10 + Math.round((1.0f - eased) * 9.0f);
        context.fill(10, headerY, width - 10, headerY + 52, PANEL);
        context.fill(10, headerY, width - 10, headerY + 3, accent);
        int shineW = Math.max(14, width / 12);
        int shineX = 10 + (int) ((now / 5L) % Math.max(1, width - 20 + shineW)) - shineW;
        int shineLeft = Math.max(10, shineX);
        int shineRight = Math.min(width - 10, shineX + shineW);
        if (shineRight > shineLeft) context.fill(shineLeft, headerY, shineRight, headerY + 2, 0xBFFFFFFF);
        int pulseAlpha = 45 + (int) (35 * (0.5 + 0.5 * Math.sin(now / 240.0)));
        context.fill(10, headerY - 1, width - 10, headerY, (pulseAlpha << 24) | (accent & 0x00FFFFFF));
        context.drawText(textRenderer, "ASTRA  /  HUD EDITOR", 22, headerY + 12, TEXT, true);
        context.drawText(textRenderer, "Drag to reposition  •  Right-click toggles  •  Chat + "
            + AstraClient.keyName(AstraClient.hudEditKeybindCode()) + " = live drag mode",
            22, headerY + 30, MUTED, false);

        renderedBoxes.clear();
        for (Widget widget : WIDGETS) {
            int defaultX = widget.id.equals("damageindicators") ? width / 2 - 44
                : (widget.id.equals("breadcrumbs") ? width - 124 : widget.defaultX);
            int defaultY = widget.id.equals("damageindicators") ? height / 2 + 26
                : (widget.id.equals("breadcrumbs") ? height / 2 - 56
                : (widget.id.equals("waypoint") ? height - 80
                : (widget.id.equals("storagefinder") ? height - 100
                : (widget.defaultY < 0 ? height + widget.defaultY : widget.defaultY))));
            int x = clamp(AstraClient.hudX(widget.id, defaultX), 2, Math.max(2, width - 82));
            int y = clamp(AstraClient.hudY(widget.id, defaultY), 66, Math.max(66, height - 28));
            boolean enabled = AstraClient.isEnabled(widget.id);
            String display = widget.label + (enabled ? "  •  ON" : "  •  OFF");
            int boxW = Math.max(88, Math.min(205, textRenderer.getWidth(display) + 20));
            int boxH = 21;
            x = clamp(x, 2, Math.max(2, width - boxW - 2));
            y = clamp(y, 66, Math.max(66, height - boxH - 34));
            boolean hover = mouseX >= x && mouseX <= x + boxW && mouseY >= y && mouseY <= y + boxH;
            int background = enabled ? (hover ? 0xF32B2541 : CARD) : (hover ? 0xF22A3040 : 0xC9151A25);
            context.fill(x, y, x + boxW, y + boxH, background);
            if (hover || (draggedId != null && draggedId.equals(widget.id))) {
                int glowAlpha = 55 + (int) (35 * (0.5 + 0.5 * Math.sin(now / 150.0)));
                context.fill(x - 1, y - 1, x + boxW + 1, y, (glowAlpha << 24) | (accent & 0x00FFFFFF));
                context.fill(x - 1, y + boxH, x + boxW + 1, y + boxH + 1, (glowAlpha << 24) | (accent & 0x00FFFFFF));
            }
            context.fill(x, y, x + 3, y + boxH, enabled ? accent : 0xFF5B6578);
            context.drawText(textRenderer, display, x + 9, y + 7, enabled ? TEXT : MUTED, false);
            if (draggedId != null && draggedId.equals(widget.id)) {
                context.fill(x, y + boxH - 2, x + boxW, y + boxH, accent);
            }
            renderedBoxes.add(new Box(widget.id, widget.label, x, y, boxW, boxH, enabled));
        }

        // While moving a widget, show alignment guides and a live coordinate chip.
        // This gives precise placement feedback without changing the saved coordinates.
        if (draggedId != null) {
            for (Box box : renderedBoxes) {
                if (!box.id().equals(draggedId)) continue;
                int centerX = box.x() + box.width() / 2;
                int centerY = box.y() + box.height() / 2;
                int guide = 0x3254E8FF;
                context.fill(centerX, headerY + 55, centerX + 1, Math.max(headerY + 56, height - 36), guide);
                context.fill(10, centerY, Math.max(11, width - 10), centerY + 1, guide);
                String position = "X " + box.x() + "   Y " + box.y();
                int chipW = textRenderer.getWidth(position) + 12;
                int chipX = clamp(box.x() + box.width() + 7, 6, Math.max(6, width - chipW - 6));
                int chipY = clamp(box.y() - 18, headerY + 56, Math.max(headerY + 56, height - 49));
                context.fill(chipX, chipY, chipX + chipW, chipY + 15, 0xF20B101A);
                context.fill(chipX, chipY, chipX + 2, chipY + 15, accent);
                context.drawText(textRenderer, position, chipX + 6, chipY + 4, TEXT, false);
                break;
            }
        }

        int buttonY = height - 31;
        drawButton(context, 12, buttonY, 128, 22, "RESET LAYOUT",
            mouseX >= 12 && mouseX <= 140 && mouseY >= buttonY && mouseY <= buttonY + 22, 0xFFFF829A);
        drawButton(context, width - 98, buttonY, 86, 22, "DONE",
            mouseX >= width - 98 && mouseX <= width - 12 && mouseY >= buttonY && mouseY <= buttonY + 22, accent);
        context.drawText(textRenderer, "All HUD widgets are editable • Drag to place them exactly where you want",
            151, height - 24, MUTED, false);
        // This screen uses manual drawing only. Skip the vanilla renderer to keep it crisp.
    }

    private void drawButton(DrawContext context, int x, int y, int w, int h, String label, boolean hover, int color) {
        context.fill(x, y, x + w, y + h, hover ? 0xFF2D3546 : 0xFF1A202D);
        context.fill(x, y, x + 2, y + h, color);
        context.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 7, TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();

        int buttonY = height - 31;
        if (mouseY >= buttonY && mouseY <= buttonY + 22 && mouseX >= 12 && mouseX <= 140) {
            AstraClient.resetHudPositions();
            return true;
        }
        if (mouseY >= buttonY && mouseY <= buttonY + 22 && mouseX >= width - 98 && mouseX <= width - 12) {
            close();
            return true;
        }
        for (Box box : renderedBoxes) {
            if (mouseX < box.x || mouseX > box.x + box.width || mouseY < box.y || mouseY > box.y + box.height) continue;
            if (button == 1) {
                AstraClient.toggle(box.id);
                return true;
            }
            draggedId = box.id;
            dragOffsetX = (int) mouseX - box.x;
            dragOffsetY = (int) mouseY - box.y;
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggedId != null) {
            Widget widget = WIDGETS.stream().filter(item -> item.id.equals(draggedId)).findFirst().orElse(null);
            if (widget == null) return true;
            Box rendered = renderedBoxes.stream().filter(item -> item.id.equals(draggedId)).findFirst().orElse(null);
            int boxW = rendered == null ? 130 : rendered.width;
            int boxH = rendered == null ? 21 : rendered.height;
            int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, width - boxW));
            int y = clamp((int) mouseY - dragOffsetY, 66, Math.max(66, height - boxH - 34));
            AstraClient.previewHudPosition(draggedId, x, y);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedId != null) AstraClient.saveConfig();
        draggedId = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
