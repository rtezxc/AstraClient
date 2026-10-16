package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Responsive, searchable Astra module browser. Unimplemented modules are visible but locked. */
public final class AstraScreen extends Screen {
    private static final int BG = 0xFF0A0D16;
    private static final int SIDEBAR = 0xFF101520;
    private static final int PANEL = 0xFF171D2A;
    private static final int PANEL_HOVER = 0xFF252D3F;
    private static final int WHITE = 0xFFF3F5FF;
    private static final int MUTED = 0xFF929BB0;
    private static final int LINE = 0xFF2A3142;
    private static final int GREEN = 0xFF69E6B8;
    private static final int AMBER = 0xFFFFC66E;

    private static final String[] CATEGORIES = {
        "Combat", "Render", "Movement", "Player", "Inventory", "World", "Utility", "HUD", "Settings"
    };

    private static final List<ModuleInfo> MODULES = List.of(
        new ModuleInfo("fullbright", "FullBright", "Night vision for dark areas", "Render", true),
        new ModuleInfo("customcrosshair", "Custom Crosshair", "Draws a compact accent-colored crosshair overlay", "Render", true),
        new ModuleInfo("playeresp", "Player ESP", "Local glowing outline around other players", "Render", true),
        new ModuleInfo("mobesp", "Mob ESP", "Local glowing outlines around nearby mobs", "Render", true),
        new ModuleInfo("itemesp", "Item ESP", "Local glowing outlines around dropped items", "Render", true),
        new ModuleInfo("xray", "X-Ray", "Highlight selected blocks", "Render", false),
        new ModuleInfo("nametags", "Nametags", "Enhanced entity labels", "Render", false),
        new ModuleInfo("zoom", "Zoom", "Adjustable camera FOV zoom", "Render", true),
        new ModuleInfo("autosprint", "Auto Sprint", "Sprint while moving forward", "Movement", true),
        new ModuleInfo("sprint", "Sprint", "Maintains sprint while moving forward", "Movement", true),
        new ModuleInfo("noslow", "No Slow", "Reduce action slowdown", "Movement", false),
        new ModuleInfo("speed", "Speed", "Movement speed adjustment", "Movement", false),
        new ModuleInfo("safewalk", "Safe Walk", "Uses sneak protection near block edges", "Movement", true),
        new ModuleInfo("eagle", "Eagle", "Automatically sneaks when standing over a ledge", "Movement", true),
        new ModuleInfo("inventorymove", "Inventory Move", "Move with inventory open", "Movement", true),
        new ModuleInfo("autoeat", "Auto Eat", "Automatically eats hotbar food when hunger is 16 or lower", "Inventory", true),
        new ModuleInfo("fly", "Fly", "Client-side flight: jump to rise, sneak to descend", "Movement", true),
        new ModuleInfo("autowalk", "Auto Walk", "Automatically holds forward while in the world", "Movement", true),
        new ModuleInfo("aimassist", "Aim Assist", "Aim assistance", "Combat", false),
        new ModuleInfo("triggerbot", "Critical TriggerBot", "Attacks the crosshair target only during a valid critical hit", "Combat", true),
        new ModuleInfo("killaura", "Kill Aura", "Automatically attacks nearby living targets in front of you", "Combat", true),
        new ModuleInfo("autototem", "Auto Totem", "Automatic offhand management", "Combat", false),
        new ModuleInfo("antiknockback", "Anti Knockback", "Reduce received knockback", "Combat", false),
        new ModuleInfo("targethud", "Target HUD", "Show target name and distance", "HUD", true),
        new ModuleInfo("cps", "CPS Counter", "Count left and right mouse clicks per second", "HUD", true),
        new ModuleInfo("worldinfo", "World Information", "Show dimension and in-game day", "HUD", true),
        new ModuleInfo("potioncounter", "Potion Counter", "Count potions in your inventory", "Inventory", true),
        new ModuleInfo("durabilityhud", "Durability HUD", "Show the lowest durability item in your inventory", "HUD", true),
        new ModuleInfo("inventoryhud", "Inventory HUD", "Show the held item's name and stack count", "HUD", true),
        new ModuleInfo("healthhud", "Health HUD", "Show current and maximum health", "HUD", true),
        new ModuleInfo("hungerhud", "Hunger HUD", "Show food level and saturation", "HUD", true),
        new ModuleInfo("blockreachhud", "Block Reach HUD", "Show distance to the targeted block", "HUD", true),
        new ModuleInfo("frametimehud", "Frame Time HUD", "Estimate frame time from the current FPS", "HUD", true),
        new ModuleInfo("biome", "Biome Display", "Show the current biome name", "HUD", true),
        new ModuleInfo("fps", "FPS Counter", "Show live frame rate", "HUD", true),
        new ModuleInfo("coordinates", "Coordinates", "Show XYZ coordinates", "HUD", true),
        new ModuleInfo("direction", "Direction", "Show facing direction", "HUD", true),
        new ModuleInfo("keystrokes", "Keystrokes", "Show movement-key states", "HUD", true),
        new ModuleInfo("ping", "Ping Display", "Show server latency", "HUD", true),
        new ModuleInfo("memory", "Memory Display", "Show JVM memory use", "HUD", true),
        new ModuleInfo("speedometer", "Speedometer", "Show horizontal movement speed", "HUD", true),
        new ModuleInfo("armorhud", "Armor HUD", "Show armor durability summary", "HUD", true),
        new ModuleInfo("potionhud", "Potion HUD", "Show active effects and durations", "HUD", true),
        new ModuleInfo("clock", "Clock", "Show local clock", "HUD", true),
        new ModuleInfo("totemcounter", "Totem Counter", "Count inventory totems", "Inventory", true),
        new ModuleInfo("pearlcounter", "Ender Pearl Counter", "Count inventory pearls", "Inventory", true),
        new ModuleInfo("arrowcounter", "Arrow Counter", "Count inventory arrows", "Inventory", true),
        new ModuleInfo("goldencounter", "Golden Apple Counter", "Count golden apples", "Inventory", true),
        new ModuleInfo("sessionstats", "Session Stats", "Show time since client start", "Utility", true),
        new ModuleInfo("noweather", "No Weather", "Hide weather effects", "World", false),
        new ModuleInfo("autotool", "Auto Tool", "Select the fastest hotbar tool for the targeted block", "World", true),
        new ModuleInfo("autoweapon", "Auto Weapon", "Selects a sword or axe while aiming at a living target", "Combat", true),
        new ModuleInfo("fastplace", "Fast Place", "Placement assistance", "World", false),
        new ModuleInfo("fastbreak", "Fast Break", "Breaking assistance", "World", false),
        new ModuleInfo("scaffold", "Scaffold", "Automated block placement", "World", false)
    );

    private String category = "Render";
    private String searchText = "";
    private boolean searchFocused;
    private boolean capturingGuiKey;
    private int scrollOffset;
    private boolean draggingWindow;
    private boolean resizingWindow;
    private boolean draggingScale;
    private boolean draggingOpacity;
    private String captureKeybindId;
    private int dragOffsetX, dragOffsetY, resizeStartX, resizeStartScale;
    private int windowOffsetX, windowOffsetY;
    private final long openedAt = System.currentTimeMillis();

    private static final class ModuleInfo {
        final String id;
        final String name;
        final String description;
        final String category;
        final boolean implemented;

        ModuleInfo(String id, String name, String description, String category, boolean implemented) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.implemented = implemented;
        }
    }

    public AstraScreen() {
        this("Render");
    }

    public AstraScreen(String initialCategory) {
        super(Text.literal("Astra Client"));
        for (String item : CATEGORIES) {
            if (item.equals(initialCategory)) {
                category = item;
                break;
            }
        }
    }

    private int panelWidth() {
        return Math.max(220, Math.min(Math.round(760 * AstraClient.guiScale() / 100.0f), width - 24));
    }

    private int panelHeight() {
        return Math.max(210, Math.min(Math.round(470 * AstraClient.guiScale() / 100.0f), height - 24));
    }

    private int panelLeft(int panelW) {
        int centered = (width - panelW) / 2 + windowOffsetX;
        return Math.max(0, Math.min(width - panelW, centered));
    }

    private int panelTop(int panelH) {
        int centered = (height - panelH) / 2 + windowOffsetY;
        return Math.max(0, Math.min(height - panelH, centered));
    }

    private int sidebarWidth(int panelW) {
        return Math.min(154, Math.max(100, panelW / 4));
    }

    private int categoryStep(int panelH) {
        return Math.max(16, Math.min(35, (panelH - 150) / CATEGORIES.length));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Fully opaque canvas: never expose Minecraft's blurred world behind Astra.
        // This also avoids translucent compositing on mobile OpenGL renderers.
        ctx.fill(0, 0, width, height, 0xFF080B12);
        int panelW = panelWidth();
        int panelH = panelHeight();
        int left = panelLeft(panelW);
        int top = panelTop(panelH);
        int sideW = sidebarWidth(panelW);
        int accent = AstraClient.accentColor();
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int listTop = top + 105;
        int listBottom = top + panelH - 43;
        int viewportH = Math.max(1, listBottom - listTop);


        // Shell, glow rails and title bar
        ctx.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF32284A);
        ctx.fill(left, top, left + panelW, top + panelH, BG);
        ctx.fill(left, top, left + panelW, top + 3, accent);
        ctx.fill(left, top + 3, left + 1, top + panelH, 0xFF32284A);
        ctx.fill(left + panelW - 1, top + 3, left + panelW, top + panelH, 0xFF242B3B);
        ctx.fill(left, top + 3, left + panelW, top + 58, 0xFF111621);

        // Custom Astra mark
        ctx.fill(left + 14, top + 14, left + 42, top + 42, 0xFF29213F);
        ctx.fill(left + 14, top + 14, left + 17, top + 42, accent);
        ctx.fill(left + 22, top + 20, left + 34, top + 23, accent);
        ctx.fill(left + 22, top + 20, left + 25, top + 35, accent);
        ctx.fill(left + 22, top + 32, left + 34, top + 35, 0xFF54E8FF);
        ctx.drawText(textRenderer, "ASTRA", left + 50, top + 14, WHITE, true);
        ctx.drawText(textRenderer, "CLIENT  /  FABRIC 1.21.4", left + 50, top + 32, MUTED, false);
        ctx.fill(left + panelW - 31, top + 17, left + panelW - 11, top + 37, 0xFF242B3B);
        ctx.drawText(textRenderer, "×", left + panelW - 25, top + 22, WHITE, true);

        // Search field and active category title
        int searchW = Math.min(190, Math.max(100, contentW - 10));
        int searchX = left + panelW - searchW - 42;
        int searchY = top + 17;
        ctx.fill(searchX, searchY, searchX + searchW, searchY + 25,
            searchFocused ? 0xFF28213F : PANEL);
        ctx.fill(searchX, searchY + 24, searchX + searchW, searchY + 25,
            searchFocused ? accent : LINE);
        String searchLabel = searchText.isEmpty() ? "Search modules..." : searchText;
        ctx.drawText(textRenderer, truncateToWidth(searchLabel, searchW - 15),
            searchX + 8, searchY + 8, searchText.isEmpty() ? MUTED : WHITE, false);
        ctx.drawText(textRenderer, category.toUpperCase(Locale.ROOT),
            contentX, top + 17, WHITE, true);
        ctx.drawText(textRenderer, "MODULE CONTROL CENTER  •  v1.2.0  •  "
            + net.minecraft.client.MinecraftClient.getInstance().getCurrentFps() + " FPS",
            contentX, top + 36, accent, false);
        if (category.equals("HUD")) {
            int ex = left + panelW - 121, ey = top + 66;
            boolean hover = mouseX >= ex && mouseX <= ex + 108 && mouseY >= ey && mouseY <= ey + 24;
            ctx.fill(ex, ey, ex + 108, ey + 24, hover ? PANEL_HOVER : PANEL);
            ctx.fill(ex, ey, ex + 3, ey + 24, accent);
            ctx.drawCenteredTextWithShadow(textRenderer, "EDIT HUD", ex + 54, ey + 8, WHITE);
        }

        // Sidebar navigation
        ctx.fill(left, top + 58, left + sideW, top + panelH, SIDEBAR);
        ctx.drawText(textRenderer, "WORKSPACE", left + 12, top + 65, 0xFF717D92, false);
        int step = categoryStep(panelH);
        int rowHeight = step - 6;
        int categoryY = top + 76;
        for (String nav : CATEGORIES) {
            boolean selected = category.equals(nav);
            boolean hover = mouseX >= left + 7 && mouseX <= left + sideW - 7
                && mouseY >= categoryY && mouseY <= categoryY + rowHeight;
            if (selected) {
                ctx.fill(left + 7, categoryY, left + sideW - 7, categoryY + rowHeight, 0xFF29233F);
                ctx.fill(left + 7, categoryY, left + 10, categoryY + rowHeight, accent);
            } else if (hover) {
                ctx.fill(left + 7, categoryY, left + sideW - 7, categoryY + rowHeight, 0xFF1A202D);
            }
            ctx.drawText(textRenderer, selected ? "◆" : "·", left + 16,
                categoryY + Math.max(1, (rowHeight - 9) / 2), selected ? accent : MUTED, false);
            ctx.drawText(textRenderer, nav, left + 31,
                categoryY + Math.max(1, (rowHeight - 9) / 2), selected ? WHITE : MUTED, selected);
            categoryY += step;
        }
        ctx.fill(left + 12, top + panelH - 47, left + sideW - 12, top + panelH - 46, LINE);
        ctx.drawText(textRenderer, "RIGHT SHIFT", left + 13, top + panelH - 37, accent, false);
        ctx.drawText(textRenderer, "ASTRA • LOCAL", left + 13, top + panelH - 23, MUTED, false);

        if (category.equals("Settings")) {
            renderSettings(ctx, mouseX, mouseY, top, contentX, contentW, panelH, accent);
        } else {
            List<ModuleInfo> visible = getVisibleModules();
            if (visible.isEmpty()) {
                ctx.drawText(textRenderer, searchText.isEmpty()
                    ? "No modules in this category yet."
                    : "No matching modules found.", contentX, listTop + 10, MUTED, false);
            } else {
                int gap = 10;
                int cardW = Math.max(70, (contentW - gap) / 2);
                int cardH = 65;
                int stride = 76;
                int rows = (visible.size() + 1) / 2;
                int maxScroll = Math.max(0, rows * stride - viewportH);
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                ctx.enableScissor(contentX, listTop, contentX + contentW, listBottom);

                for (int i = 0; i < visible.size(); i++) {
                    ModuleInfo module = visible.get(i);
                    int col = i % 2, row = i / 2;
                    int x = contentX + col * (cardW + gap);
                    int y = listTop + row * stride - scrollOffset;
                    if (y + cardH < listTop || y > listBottom) continue;
                    boolean hover = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
                    boolean enabled = module.implemented && AstraClient.isEnabled(module.id);
                    ctx.fill(x, y, x + cardW, y + cardH, hover ? PANEL_HOVER : PANEL);
                    ctx.fill(x, y, x + 3, y + cardH, module.implemented
                        ? (enabled ? accent : LINE) : 0xFF3A4050);
                    boolean iconHover = mouseX >= x + 7 && mouseX <= x + 32
                        && mouseY >= y + 7 && mouseY <= y + 32;
                    ctx.fill(x + 10, y + 10, x + 29, y + 29,
                        iconHover ? 0xFF463765 : (enabled ? 0xFF302747 : 0xFF252B37));
                    ctx.drawText(textRenderer, iconHover ? "⚙" : iconFor(module.name), x + 15, y + 15,
                        module.implemented ? accent : 0xFF727B8D, true);

                    String status = captureKeybindId != null && captureKeybindId.equals(module.id)
                        ? "PRESS KEY" : (module.implemented ? (enabled ? "ON" : "OFF") : "LOCKED");
                    int statusColor = captureKeybindId != null && captureKeybindId.equals(module.id)
                        ? AMBER : (!module.implemented ? 0xFF777F90 : (enabled ? GREEN : MUTED));
                    int statusW = textRenderer.getWidth(status);
                    ctx.drawText(textRenderer, status, x + cardW - statusW - 8, y + 11, statusColor, true);

                    ctx.drawText(textRenderer, truncateToWidth(module.name, Math.max(8, cardW - 82)),
                        x + 36, y + 9, WHITE, true);
                    ctx.drawText(textRenderer, truncateToWidth(module.description, Math.max(12, cardW - 22)),
                        x + 10, y + 34, MUTED, false);
                    if (module.implemented) {
                        ctx.fill(x + 10, y + 51, x + 39, y + 58, enabled ? accent : 0xFF30384A);
                        ctx.fill(x + 42, y + 51, x + 49, y + 58, enabled ? 0xFFBDA7FF : 0xFF30384A);
                        String keyLabel = "KEY " + AstraClient.keyName(AstraClient.moduleKeybind(module.id));
                        ctx.drawText(textRenderer, truncateToWidth(keyLabel, Math.max(22, cardW - 66)),
                            x + 56, y + 51, 0xFF7D8AA1, false);
                    } else {
                        ctx.drawText(textRenderer, "NOT IMPLEMENTED", x + 10, y + 51, 0xFF727B8D, false);
                    }
                }
                ctx.disableScissor();
                long ready = visible.stream().filter(m -> m.implemented).count();
                ctx.drawText(textRenderer, visible.size() + " MODULES  •  " + ready + " AVAILABLE",
                    contentX, top + panelH - 26, MUTED, false);
            }
        }

        // Resize grip in the lower-right corner.
        int gx = left + panelW - 13;
        int gy = top + panelH - 13;
        ctx.fill(gx + 3, gy + 7, gx + 10, gy + 10, accent);
        ctx.fill(gx + 6, gy + 3, gx + 10, gy + 6, accent);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String iconFor(String name) {
        return switch (name) {
            case "FullBright" -> "☼";
            case "Player ESP", "Mob ESP", "Item ESP" -> "◎";
            case "Auto Sprint" -> "➤";
            case "FPS Counter", "Memory Display", "Ping Display" -> "▥";
            case "Coordinates", "Direction" -> "⌖";
            case "Keystrokes" -> "⌨";
            case "Target HUD" -> "◈";
            case "Armor HUD", "Totem Counter" -> "◇";
            case "Potion HUD" -> "✚";
            case "Clock" -> "◷";
            default -> "✦";
        };
    }

    private String truncateToWidth(String value, int maxWidth) {
        if (maxWidth <= 0) return "";
        String result = value;
        while (!result.isEmpty() && textRenderer.getWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        if (!result.equals(value) && maxWidth >= textRenderer.getWidth("…")) return result + "…";
        return result;
    }

    private List<ModuleInfo> getVisibleModules() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        List<ModuleInfo> result = new ArrayList<>();
        for (ModuleInfo module : MODULES) {
            if (!module.category.equals(category)) continue;
            if (query.isEmpty()
                || module.name.toLowerCase(Locale.ROOT).contains(query)
                || module.description.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(module);
            }
        }
        return result;
    }

    private void renderSettings(
        DrawContext ctx, int mouseX, int mouseY, int top,
        int contentX, int contentW, int panelH, int accent
    ) {
        ctx.drawText(textRenderer, "CLIENT SETTINGS", contentX, top + 62, WHITE, true);
        ctx.drawText(textRenderer, "Preferences save automatically.", contentX, top + 79, MUTED, false);

        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 96, contentW,
            "Accent theme", AstraClient.accentName(), accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 132, contentW,
            "Interface click sound", AstraClient.areClickSoundsEnabled() ? "ON" : "MUTED", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 168, contentW,
            "GUI open key", capturingGuiKey ? "PRESS A KEY  •  ESC = DEFAULT" : AstraClient.keyName(AstraClient.guiKeybindCode()), accent);

        ctx.drawText(textRenderer, "GUI SCALE", contentX, top + 207, WHITE, true);
        String value = AstraClient.guiScale() + "%";
        ctx.drawText(textRenderer, value, contentX + contentW - textRenderer.getWidth(value),
            top + 207, accent, true);
        int trackX = contentX, trackY = top + 226, trackW = contentW;
        ctx.fill(trackX, trackY, trackX + trackW, trackY + 6, 0xFF2B3344);
        int fillW = (AstraClient.guiScale() - 75) * trackW / 50;
        ctx.fill(trackX, trackY, trackX + fillW, trackY + 6, accent);
        ctx.fill(trackX + fillW - 4, trackY - 4, trackX + fillW + 4, trackY + 10, WHITE);
        ctx.drawText(textRenderer, "75%", trackX, trackY + 12, MUTED, false);
        String max = "125%";
        ctx.drawText(textRenderer, max, trackX + trackW - textRenderer.getWidth(max),
            trackY + 12, MUTED, false);

        ctx.drawText(textRenderer, "HUD OPACITY", contentX, top + 256, WHITE, true);
        String opacity = AstraClient.hudOpacity() + "%";
        ctx.drawText(textRenderer, opacity, contentX + contentW - textRenderer.getWidth(opacity),
            top + 256, accent, true);
        int opacityY = top + 275;
        ctx.fill(contentX, opacityY, contentX + contentW, opacityY + 6, 0xFF2B3344);
        int opacityFill = (AstraClient.hudOpacity() - 35) * contentW / 65;
        ctx.fill(contentX, opacityY, contentX + opacityFill, opacityY + 6, accent);
        ctx.fill(contentX + opacityFill - 4, opacityY - 4, contentX + opacityFill + 4, opacityY + 10, WHITE);
        ctx.drawText(textRenderer, "35%", contentX, opacityY + 12, MUTED, false);
        String full = "100%";
        ctx.drawText(textRenderer, full, contentX + contentW - textRenderer.getWidth(full),
            opacityY + 12, MUTED, false);

        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 310, contentW,
            "Reset module defaults", "RESET", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 346, contentW,
            "Export config backup", "EXPORT", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 382, contentW,
            "Import config backup", "IMPORT", accent);
        if (panelH >= 390) {
            ctx.drawText(textRenderer, "HUD positions are saved from the HUD Editor.", contentX,
                top + panelH - 50, MUTED, false);
        }
    }

    private void renderSettingRow(
        DrawContext ctx, int mouseX, int mouseY, int x, int y, int w,
        String label, String action, int accent
    ) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 43;
        ctx.fill(x, y, x + w, y + 34, hover ? PANEL_HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + 34, accent);
        ctx.drawText(textRenderer, label, x + 12, y + 9, WHITE, true);
        ctx.drawText(textRenderer, action, x + w - textRenderer.getWidth(action) - 12,
            y + 9, accent, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();
        if (button == 1) return beginModuleKeybindCapture(mouseX, mouseY);

        int panelW = panelWidth(), panelH = panelHeight();
        int left = panelLeft(panelW), top = panelTop(panelH);
        int sideW = sidebarWidth(panelW);
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int searchW = Math.min(190, Math.max(100, contentW - 10));
        int searchX = left + panelW - searchW - 42;
        int searchY = top + 17;

        if (mouseX >= left + panelW - 34 && mouseX <= left + panelW - 9
            && mouseY >= top + 13 && mouseY <= top + 40) {
            close();
            return true;
        }
        if (mouseX >= left + panelW - 18 && mouseY >= top + panelH - 18) {
            resizingWindow = true;
            resizeStartX = (int) mouseX;
            resizeStartScale = AstraClient.guiScale();
            return true;
        }
        if (mouseX >= searchX && mouseX <= searchX + searchW
            && mouseY >= searchY && mouseY <= searchY + 25) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (mouseY >= top + 3 && mouseY < top + 57
            && mouseX >= left && mouseX <= left + panelW) {
            draggingWindow = true;
            dragOffsetX = (int) mouseX - left;
            dragOffsetY = (int) mouseY - top;
            return true;
        }

        int step = categoryStep(panelH), rowHeight = step - 6;
        int categoryY = top + 76;
        for (String nav : CATEGORIES) {
            if (mouseX >= left + 7 && mouseX <= left + sideW - 7
                && mouseY >= categoryY && mouseY <= categoryY + rowHeight) {
                category = nav;
                scrollOffset = 0;
                searchText = "";
                return true;
            }
            categoryY += step;
        }

        if (category.equals("HUD")
            && inside(mouseX, mouseY, left + panelW - 121, top + 66, 108, 24)) {
            client.setScreen(new AstraHudEditorScreen());
            return true;
        }

        if (category.equals("Settings")) {
            if (inside(mouseX, mouseY, contentX, top + 96, contentW, 31)) {
                AstraClient.toggleAccent();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 132, contentW, 31)) {
                AstraClient.toggleClickSounds();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 168, contentW, 34)) {
                capturingGuiKey = true;
                AstraClient.notifyUser("Press a key for the Astra GUI (Esc restores Right Shift)");
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 219, contentW, 28)) {
                draggingScale = true;
                updateScaleFromMouse(mouseX, contentX, contentW);
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 268, contentW, 28)) {
                draggingOpacity = true;
                updateOpacityFromMouse(mouseX, contentX, contentW);
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 310, contentW, 34)) {
                AstraClient.resetModuleDefaults();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 346, contentW, 34)) {
                AstraClient.exportConfigBackup();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 382, contentW, 34)) {
                AstraClient.importConfigBackup();
                return true;
            }
            return true;
        }

        int listTop = top + 105, listBottom = top + panelH - 43;
        if (mouseY < listTop || mouseY > listBottom) return true;
        List<ModuleInfo> visible = getVisibleModules();
        int gap = 10, cardW = Math.max(70, (contentW - gap) / 2);
        int stride = 76, cardH = 65;
        int viewportH = Math.max(1, listBottom - listTop);
        int rows = (visible.size() + 1) / 2;
        int maxScroll = Math.max(0, rows * stride - viewportH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        for (int i = 0; i < visible.size(); i++) {
            ModuleInfo module = visible.get(i);
            int x = contentX + (i % 2) * (cardW + gap);
            int y = listTop + (i / 2) * stride - scrollOffset;
            if (inside(mouseX, mouseY, x, y, cardW, cardH)) {
                // Tap the icon to open module settings; tap the key label to bind on touchscreens.
                // Right-click anywhere on a card also starts key capture.
                if (module.implemented && mouseX >= x + 7 && mouseX <= x + 32
                    && mouseY >= y + 7 && mouseY <= y + 32) {
                    client.setScreen(new AstraModuleSettingsScreen(this, module.id,
                        module.name, module.description, true));
                } else if (module.implemented && mouseY >= y + 49
                    && mouseX >= x + Math.min(54, cardW - 10)) {
                    captureKeybindId = module.id;
                    searchFocused = false;
                    AstraClient.notifyUser("Press a key to bind " + module.name + " (Esc clears)");
                } else if (module.implemented) {
                    AstraClient.toggle(module.id);
                } else if (mouseX >= x + 7 && mouseX <= x + 32 && mouseY >= y + 7 && mouseY <= y + 32) {
                    client.setScreen(new AstraModuleSettingsScreen(this, module.id,
                        module.name, module.description, false));
                }
                return true;
            }
        }
        return true;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateScaleFromMouse(double mouseX, int x, int trackW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - x) / Math.max(1.0, trackW)));
        AstraClient.previewGuiScale(75 + (int) Math.round(progress * 50.0));
    }

    private void updateOpacityFromMouse(double mouseX, int x, int trackW) {
        double progress = Math.max(0.0, Math.min(1.0, (mouseX - x) / Math.max(1.0, trackW)));
        AstraClient.previewHudOpacity(35 + (int) Math.round(progress * 65.0));
    }

    private boolean beginModuleKeybindCapture(double mouseX, double mouseY) {
        if (category.equals("Settings")) return super.mouseClicked(mouseX, mouseY, 1);
        int panelW = panelWidth(), panelH = panelHeight();
        int left = panelLeft(panelW), top = panelTop(panelH);
        int sideW = sidebarWidth(panelW);
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int listTop = top + 105, listBottom = top + panelH - 43;
        if (mouseY < listTop || mouseY > listBottom) return true;
        List<ModuleInfo> visible = getVisibleModules();
        int cardW = Math.max(70, (contentW - 10) / 2);
        int stride = 76, cardH = 65;
        int viewportH = Math.max(1, listBottom - listTop);
        int maxScroll = Math.max(0, ((visible.size() + 1) / 2) * stride - viewportH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        for (int i = 0; i < visible.size(); i++) {
            ModuleInfo module = visible.get(i);
            int x = contentX + (i % 2) * (cardW + 10);
            int y = listTop + (i / 2) * stride - scrollOffset;
            if (inside(mouseX, mouseY, x, y, cardW, cardH)) {
                if (module.implemented) {
                    captureKeybindId = module.id;
                    searchFocused = false;
                    AstraClient.notifyUser("Press a key to bind " + module.name + " (Esc clears)");
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && (draggingScale || draggingOpacity)) {
            int panelW = panelWidth(), sideW = sidebarWidth(panelW);
            int x = panelLeft(panelW) + sideW + 18;
            int w = panelW - sideW - 34;
            if (draggingScale) updateScaleFromMouse(mouseX, x, w);
            else updateOpacityFromMouse(mouseX, x, w);
            return true;
        }
        if (button == 0 && resizingWindow) {
            AstraClient.previewGuiScale(resizeStartScale + (int) Math.round((mouseX - resizeStartX) / 4.0));
            return true;
        }
        if (button == 0 && draggingWindow) {
            int left = (width - panelWidth()) / 2;
            int top = (height - panelHeight()) / 2;
            windowOffsetX = (int) mouseX - dragOffsetX - left;
            windowOffsetY = (int) mouseY - dragOffsetY - top;
            windowOffsetX = Math.max(-left, Math.min(width - panelWidth() - left, windowOffsetX));
            windowOffsetY = Math.max(-top, Math.min(height - panelHeight() - top, windowOffsetY));
            return true;
        }
        if (button == 0 && searchFocused && category.equals("Settings")) {
            int panelW = panelWidth(), sideW = sidebarWidth(panelW);
            int x = panelLeft(panelW) + sideW + 18;
            int w = panelW - sideW - 34;
            updateScaleFromMouse(mouseX, x, w);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (resizingWindow || draggingScale || draggingOpacity) AstraClient.saveConfig();
        draggingWindow = false;
        resizingWindow = false;
        draggingScale = false;
        draggingOpacity = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!category.equals("Settings")) {
            int panelW = panelWidth(), panelH = panelHeight();
            int left = panelLeft(panelW), top = panelTop(panelH);
            int sideW = sidebarWidth(panelW), contentX = left + sideW + 18;
            int contentW = panelW - sideW - 34;
            int listTop = top + 105, listBottom = top + panelH - 43;
            if (!inside(mouseX, mouseY, contentX, listTop, contentW, Math.max(1, listBottom - listTop))) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
            int viewportH = Math.max(1, listBottom - listTop);
            List<ModuleInfo> visible = getVisibleModules();
            int rows = (visible.size() + 1) / 2;
            int maxScroll = Math.max(0, rows * 76 - viewportH);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 38)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && !Character.isISOControl(chr) && searchText.length() < 30) {
            searchText += chr;
            scrollOffset = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingGuiKey) {
            AstraClient.setGuiKeybindCode(keyCode == GLFW.GLFW_KEY_ESCAPE
                ? GLFW.GLFW_KEY_RIGHT_SHIFT : keyCode);
            capturingGuiKey = false;
            return true;
        }
        if (captureKeybindId != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                AstraClient.setModuleKeybind(captureKeybindId, GLFW.GLFW_KEY_UNKNOWN);
            } else {
                AstraClient.setModuleKeybind(captureKeybindId, keyCode);
            }
            captureKeybindId = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
            close();
            return true;
        }
        if (searchFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
            scrollOffset = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
