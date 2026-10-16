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
        new ModuleInfo("tracers", "Tracers", "Draw screen-space lines toward nearby loaded entities", "Render", true),
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
        new ModuleInfo("autoeat", "Auto Eat", "Automatically eats hotbar food at the configured hunger threshold", "Inventory", true),
        new ModuleInfo("fly", "Fly", "Client-side flight: jump to rise, sneak to descend", "Movement", true),
        new ModuleInfo("autowalk", "Auto Walk", "Automatically holds forward while in the world", "Movement", true),
        new ModuleInfo("aimassist", "Aim Assist", "Aim assistance", "Combat", false),
        new ModuleInfo("triggerbot", "TriggerBot", "Attacks a valid target under your crosshair when the vanilla attack cooldown is ready", "Combat", true),
        new ModuleInfo("killaura", "Kill Aura", "Automatically attacks nearby living targets in front of you", "Combat", true),
        new ModuleInfo("autototem", "Auto Totem", "Automatic offhand management", "Combat", false),
        new ModuleInfo("antiknockback", "Anti Knockback", "Reduce received knockback", "Combat", false),
        new ModuleInfo("targethud", "Target HUD", "Show target name and distance", "HUD", true),
        new ModuleInfo("damageindicators", "Damage Indicators", "Show a short red indicator when your health drops", "Player", true),
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
        new ModuleInfo("arraylist", "Active Modules", "Show enabled non-HUD modules in a compact neon list", "HUD", true),
        new ModuleInfo("watermark", "Astra Watermark", "Show a movable Astra Client brand plate", "HUD", true),
        new ModuleInfo("fpsgraph", "FPS Graph", "Plot recent frame-rate samples over the last six seconds", "HUD", true),
        new ModuleInfo("lowhealthalert", "Low Health Alert", "Pulse a warning border when health drops to three hearts or less", "Player", true),
        new ModuleInfo("compasshud", "Compass HUD", "Show cardinal direction and heading in a movable compass strip", "HUD", true),
        new ModuleInfo("breadcrumbs", "Breadcrumbs", "Display a small map of your recent path behind you", "HUD", true),
        new ModuleInfo("waypoint", "Waypoint", "Save a location and show its coordinates, distance, and relative direction", "HUD", true),
        new ModuleInfo("storagefinder", "Storage Finder", "Find nearby chests, barrels, ender chests, and shulker boxes", "HUD", true),
        new ModuleInfo("effectalert", "Effect Expiry Alert", "Warn when active potion effects have ten seconds or less remaining", "HUD", true),
        new ModuleInfo("noweather", "No Weather", "Hide weather effects", "World", false),
        new ModuleInfo("autotool", "Auto Tool", "Select the fastest hotbar tool for the targeted block", "World", true),
        new ModuleInfo("blockfinder", "Block Finder", "Find nearby diamond, ancient debris, emerald, gold, iron, coal, copper, lapis, redstone, or quartz", "World", true),
        new ModuleInfo("autoweapon", "Auto Weapon", "Selects a sword or axe while aiming at a living target", "Combat", true),
        new ModuleInfo("fastplace", "Fast Place", "Placement assistance", "World", false),
        new ModuleInfo("fastbreak", "Fast Break", "Breaking assistance", "World", false),
        new ModuleInfo("scaffold", "Scaffold", "Automated block placement", "World", false)
    );

    private String category = "Render";
    private String searchText = "";
    private boolean searchFocused;
    private boolean capturingGuiKey;
    private boolean capturingHudEditKey;
    private int scrollOffset;
    private int settingsScrollOffset;
    private boolean draggingWindow;
    private boolean resizingWindow;
    private boolean draggingScale;
    private boolean draggingOpacity;
    private String captureKeybindId;
    private String contextMenuModuleId;
    private int contextMenuX, contextMenuY;
    private int dragOffsetX, dragOffsetY, resizeStartX, resizeStartScale;
    private int windowOffsetX, windowOffsetY;
    private final long openedAt = System.currentTimeMillis();
    private long closingAt;
    private boolean closing;

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
        long now = System.currentTimeMillis();
        float progress = closing
            ? Math.max(0.0f, 1.0f - (now - closingAt) / 220.0f)
            : Math.min(1.0f, Math.max(0.0f, (now - openedAt) / 260.0f));
        float eased = 1.0f - (float) Math.pow(1.0f - progress, 3.0);
        int shade = Math.round(8 + 3 * eased);
        ctx.fill(0, 0, width, height, 0xFF000000 | (shade << 16) | (shade << 8) | (shade + 4));
        int panelW = panelWidth();
        int panelH = panelHeight();
        int left = panelLeft(panelW);
        int top = panelTop(panelH) + Math.round((1.0f - eased) * 14.0f);
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
        // Animated gloss sweeps across the accent rail; the edge glow gently pulses.
        int shineW = Math.max(12, panelW / 9);
        int shineX = left + (int) ((now / 5L) % Math.max(1, panelW + shineW)) - shineW;
        int shineLeft = Math.max(left, shineX);
        int shineRight = Math.min(left + panelW, shineX + shineW);
        if (shineRight > shineLeft) ctx.fill(shineLeft, top, shineRight, top + 2, 0xBFFFFFFF);
        int pulseAlpha = 45 + (int) (35 * (0.5 + 0.5 * Math.sin(now / 240.0)));
        ctx.fill(left - 1, top - 1, left + panelW + 1, top, (pulseAlpha << 24) | (accent & 0x00FFFFFF));
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
        // Small live-status badge gives the header a polished client-dashboard feel.
        int liveX = left + panelW - 78;
        ctx.fill(liveX, top + 20, liveX + 35, top + 34, 0xFF172D2B);
        ctx.fill(liveX + 5, top + 25, liveX + 9, top + 29, 0xFF69E6B8);
        ctx.drawText(textRenderer, "LIVE", liveX + 13, top + 23, 0xFF69E6B8, true);
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
        ctx.drawText(textRenderer, truncateToWidth(searchLabel, searchW - (searchText.isEmpty() ? 15 : 31)),
            searchX + 8, searchY + 8, searchText.isEmpty() ? MUTED : WHITE, false);
        if (!searchText.isEmpty()) {
            boolean clearHover = mouseX >= searchX + searchW - 23 && mouseX <= searchX + searchW - 4
                && mouseY >= searchY + 3 && mouseY <= searchY + 22;
            ctx.fill(searchX + searchW - 23, searchY + 4, searchX + searchW - 4, searchY + 21,
                clearHover ? 0xFF49314A : 0xFF30283D);
            ctx.drawCenteredTextWithShadow(textRenderer, "×", searchX + searchW - 13,
                searchY + 8, clearHover ? 0xFFFFB6D8 : accent);
        }
        ctx.drawText(textRenderer, category.toUpperCase(Locale.ROOT),
            contentX, top + 17, WHITE, true);
        ctx.drawText(textRenderer, "MODULE CONTROL CENTER  •  v1.1.0  •  "
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
                ctx.fill(left + sideW - 13, categoryY + 4, left + sideW - 9, categoryY + rowHeight - 4, 0xFF54E8FF);
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
        int activeModules = (int) MODULES.stream().filter(m -> m.implemented)
            .filter(m -> AstraClient.isEnabled(m.id)).count();
        ctx.drawText(textRenderer, "RIGHT SHIFT", left + 13, top + panelH - 37, accent, false);
        ctx.drawText(textRenderer, "ASTRA • LOCAL", left + 13, top + panelH - 23, MUTED, false);
        String activeLabel = activeModules + " ACTIVE";
        int badgeW = textRenderer.getWidth(activeLabel) + 12;
        ctx.fill(left + sideW - badgeW - 10, top + panelH - 25,
            left + sideW - 8, top + panelH - 12, 0xFF172D2B);
        ctx.drawText(textRenderer, activeLabel, left + sideW - badgeW - 7,
            top + panelH - 22, 0xFF69E6B8, true);

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
                    // Layered card surfaces, fine borders and a top accent rail create depth
                    // without relying on rounded-rectangle APIs unavailable in older mappings.
                    ctx.fill(x, y, x + cardW, y + cardH, hover ? 0xFF252D40 : PANEL);
                    ctx.fill(x + 1, y + 1, x + cardW - 1, y + cardH - 1,
                        hover ? 0xFF20283A : 0xFF151B27);
                    ctx.fill(x, y, x + cardW, y + 1, enabled ? accent : LINE);
                    ctx.fill(x, y, x + 3, y + cardH, module.implemented
                        ? (enabled ? accent : LINE) : 0xFF3A4050);
                    if (enabled) {
                        ctx.fill(x + 4, y + 2, x + cardW - 2, y + 3, 0x553FCEE9);
                    }
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
                        String keyLabel = "KEY " + AstraClient.keyName(AstraClient.moduleKeybind(module.id));
                        ctx.drawText(textRenderer, truncateToWidth(keyLabel, Math.max(22, cardW - 78)),
                            x + 10, y + 51, 0xFF7D8AA1, false);
                        // Compact toggle switch mirrors the module's current state.
                        int switchX = x + cardW - 34;
                        int switchY = y + 50;
                        ctx.fill(switchX, switchY, switchX + 25, switchY + 11,
                            enabled ? 0xFF254A48 : 0xFF303747);
                        ctx.fill(switchX + (enabled ? 15 : 3), switchY + 2,
                            switchX + (enabled ? 23 : 11), switchY + 9,
                            enabled ? 0xFF69E6B8 : 0xFF8A93A6);
                    } else {
                        ctx.drawText(textRenderer, "NOT IMPLEMENTED", x + 10, y + 51, 0xFF727B8D, false);
                    }
                }
                ctx.disableScissor();
                long ready = visible.stream().filter(m -> m.implemented).count();
                String moduleSummary = visible.size() + " MODULES  •  " + ready + " AVAILABLE";
                ctx.drawText(textRenderer, moduleSummary, contentX, top + panelH - 26, MUTED, false);
                if (!searchText.isEmpty()) {
                    String matchSummary = visible.size() + (visible.size() == 1 ? " MATCH" : " MATCHES");
                    int matchW = textRenderer.getWidth(matchSummary);
                    ctx.drawText(textRenderer, matchSummary,
                        contentX + contentW - matchW, top + panelH - 40, accent, true);
                }
                if (rows * stride > viewportH) {
                    String scrollHint = "SCROLL TO EXPLORE";
                    int hintW = textRenderer.getWidth(scrollHint);
                    ctx.drawText(textRenderer, scrollHint,
                        contentX + contentW - hintW, top + panelH - 26, accent, false);
                }
            }
        }

        renderModuleContextMenu(ctx, mouseX, mouseY);

        // Resize grip in the lower-right corner.
        int gx = left + panelW - 13;
        int gy = top + panelH - 13;
        ctx.fill(gx + 3, gy + 7, gx + 10, gy + 10, accent);
        ctx.fill(gx + 6, gy + 3, gx + 10, gy + 6, accent);
        // Reverse the opening motion for a short, smooth close transition.
        if (closing && now - closingAt >= 220L) {
            if (client != null && client.currentScreen == this) client.setScreen(null);
            return;
        }
        // All Astra controls are drawn manually. Avoid Screen.render here because
        // Minecraft 1.21.4 can render its default blurred background from that path.
    }

    public void requestAnimatedClose() {
        if (!closing) {
            closing = true;
            closingAt = System.currentTimeMillis();
            searchFocused = false;
            contextMenuModuleId = null;
        }
    }

    @Override
    public void close() {
        requestAnimatedClose();
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
        ctx.drawText(textRenderer, "CLIENT SETTINGS", contentX, top + 62 - settingsScrollOffset, WHITE, true);
        ctx.drawText(textRenderer, "Preferences save automatically.", contentX, top + 79 - settingsScrollOffset, MUTED, false);

        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 96 - settingsScrollOffset, contentW,
            "Accent theme", AstraClient.accentName(), accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 132 - settingsScrollOffset, contentW,
            "Interface click sound", AstraClient.areClickSoundsEnabled() ? "ON" : "MUTED", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 168 - settingsScrollOffset, contentW,
            "GUI open key", capturingGuiKey ? "PRESS A KEY  •  ESC = DEFAULT" : AstraClient.keyName(AstraClient.guiKeybindCode()), accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 204 - settingsScrollOffset, contentW,
            "Chat HUD drag key", capturingHudEditKey ? "PRESS A KEY  •  ESC = F6" : AstraClient.keyName(AstraClient.hudEditKeybindCode()), accent);

        ctx.drawText(textRenderer, "GUI SCALE", contentX, top + 243 - settingsScrollOffset, WHITE, true);
        String value = AstraClient.guiScale() + "%";
        ctx.drawText(textRenderer, value, contentX + contentW - textRenderer.getWidth(value),
            top + 243 - settingsScrollOffset, accent, true);
        int trackX = contentX, trackY = top + 262 - settingsScrollOffset, trackW = contentW;
        ctx.fill(trackX, trackY, trackX + trackW, trackY + 6, 0xFF2B3344);
        int fillW = (AstraClient.guiScale() - 75) * trackW / 50;
        ctx.fill(trackX, trackY, trackX + fillW, trackY + 6, accent);
        ctx.fill(trackX + fillW - 4, trackY - 4, trackX + fillW + 4, trackY + 10, WHITE);
        ctx.drawText(textRenderer, "75%", trackX, trackY + 12, MUTED, false);
        String max = "125%";
        ctx.drawText(textRenderer, max, trackX + trackW - textRenderer.getWidth(max),
            trackY + 12, MUTED, false);

        ctx.drawText(textRenderer, "HUD OPACITY", contentX, top + 292 - settingsScrollOffset, WHITE, true);
        String opacity = AstraClient.hudOpacity() + "%";
        ctx.drawText(textRenderer, opacity, contentX + contentW - textRenderer.getWidth(opacity),
            top + 292 - settingsScrollOffset, accent, true);
        int opacityY = top + 311 - settingsScrollOffset;
        ctx.fill(contentX, opacityY, contentX + contentW, opacityY + 6, 0xFF2B3344);
        int opacityFill = (AstraClient.hudOpacity() - 35) * contentW / 65;
        ctx.fill(contentX, opacityY, contentX + opacityFill, opacityY + 6, accent);
        ctx.fill(contentX + opacityFill - 4, opacityY - 4, contentX + opacityFill + 4, opacityY + 10, WHITE);
        ctx.drawText(textRenderer, "35%", contentX, opacityY + 12, MUTED, false);
        String full = "100%";
        ctx.drawText(textRenderer, full, contentX + contentW - textRenderer.getWidth(full),
            opacityY + 12, MUTED, false);

        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 346 - settingsScrollOffset, contentW,
            "HUD backgrounds", AstraClient.areHudBackgroundsEnabled() ? "ON" : "OFF", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 382 - settingsScrollOffset, contentW,
            "Reset module defaults", "RESET", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 418 - settingsScrollOffset, contentW,
            "Export config backup", "EXPORT", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 454 - settingsScrollOffset, contentW,
            "Import config backup", "IMPORT", accent);
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
        if (closing) return true;
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        AstraClient.playClickSound();
        if (contextMenuModuleId != null) {
            if (button == 0 && handleContextMenuClick(mouseX, mouseY)) return true;
            contextMenuModuleId = null;
            if (button == 0) return true;
        }
        if (button == 1) return openModuleContextMenu(mouseX, mouseY);

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
            if (!searchText.isEmpty() && mouseX >= searchX + searchW - 25) {
                searchText = "";
                scrollOffset = 0;
                searchFocused = true;
                return true;
            }
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
                settingsScrollOffset = 0;
                searchText = "";
                searchFocused = false;
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
            if (inside(mouseX, mouseY, contentX, top + 96 - settingsScrollOffset, contentW, 31)) {
                AstraClient.toggleAccent();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 132 - settingsScrollOffset, contentW, 31)) {
                AstraClient.toggleClickSounds();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 168 - settingsScrollOffset, contentW, 34)) {
                capturingGuiKey = true;
                AstraClient.notifyUser("Press a key for the Astra GUI (Esc restores Right Shift)");
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 204 - settingsScrollOffset, contentW, 34)) {
                capturingHudEditKey = true;
                AstraClient.notifyUser("Press a key for chat HUD drag mode (Esc restores F6)");
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 255 - settingsScrollOffset, contentW, 28)) {
                draggingScale = true;
                updateScaleFromMouse(mouseX, contentX, contentW);
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 304 - settingsScrollOffset, contentW, 28)) {
                draggingOpacity = true;
                updateOpacityFromMouse(mouseX, contentX, contentW);
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 346 - settingsScrollOffset, contentW, 34)) {
                AstraClient.toggleHudBackgrounds();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 382 - settingsScrollOffset, contentW, 34)) {
                AstraClient.resetModuleDefaults();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 418 - settingsScrollOffset, contentW, 34)) {
                AstraClient.exportConfigBackup();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 454 - settingsScrollOffset, contentW, 34)) {
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

    private boolean openModuleContextMenu(double mouseX, double mouseY) {
        if (category.equals("Settings")) {
            contextMenuModuleId = null;
            return true;
        }
        int panelW = panelWidth(), panelH = panelHeight();
        int left = panelLeft(panelW), top = panelTop(panelH);
        int sideW = sidebarWidth(panelW);
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int listTop = top + 105, listBottom = top + panelH - 43;
        if (!inside(mouseX, mouseY, contentX, listTop, contentW, Math.max(1, listBottom - listTop))) {
            contextMenuModuleId = null;
            return true;
        }
        List<ModuleInfo> visible = getVisibleModules();
        int cardW = Math.max(70, (contentW - 10) / 2);
        int stride = 76, cardH = 65;
        int viewportH = Math.max(1, listBottom - listTop);
        int maxScroll = Math.max(0, ((visible.size() + 1) / 2) * stride - viewportH);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        for (int i = 0; i < visible.size(); i++) {
            ModuleInfo module = visible.get(i);
            int x = contentX + (i % 2) * (cardW + 10);
            int y = listTop + (i / 2) * stride - scrollOffset;
            if (inside(mouseX, mouseY, x, y, cardW, cardH)) {
                contextMenuModuleId = module.id;
                contextMenuX = (int) mouseX;
                contextMenuY = (int) mouseY;
                searchFocused = false;
                return true;
            }
        }
        contextMenuModuleId = null;
        return true;
    }

    private List<String> contextMenuActions() {
        List<String> actions = new ArrayList<>();
        ModuleInfo module = findModule(contextMenuModuleId);
        if (module == null) return actions;
        actions.add("settings");
        actions.add("accent");
        actions.add("sounds");
        actions.add("backgrounds");
        if (module.implemented) {
            actions.add("toggle");
            actions.add("bind");
            actions.add("resetbind");
        }
        if (module.id.equals("customcrosshair")) {
            actions.add("crosshairSize");
            actions.add("crosshairGap");
            actions.add("crosshairThickness");
        } else if (module.id.equals("killaura")) {
            actions.add("players");
            actions.add("hostile");
            actions.add("passive");
        } else if (module.id.equals("tracers")) {
            actions.add("range");
            actions.add("tracerPlayers");
            actions.add("tracerMobs");
        } else if (module.id.equals("playeresp") || module.id.equals("mobesp") || module.id.equals("itemesp")) {
            actions.add("range");
        } else if (module.id.equals("autosprint")) {
            actions.add("ground");
            actions.add("menus");
        } else if (module.id.equals("zoom")) {
            actions.add("zoom");
        } else if (module.id.equals("autoeat")) {
            actions.add("hunger");
        } else if (module.id.equals("lowhealthalert")) {
            actions.add("healththreshold");
        } else if (module.id.equals("effectalert")) {
            actions.add("effectthreshold");
        } else if (module.id.equals("breadcrumbs")) {
            actions.add("trailLength");
            actions.add("hud");
        } else if (module.id.equals("waypoint")) {
            actions.add("setWaypoint");
            actions.add("clearWaypoint");
            actions.add("hud");
        } else if (module.id.equals("storagefinder")) {
            actions.add("storageRange");
            actions.add("hud");
        } else if (module.id.equals("blockfinder")) {
            actions.add("blocktarget");
            actions.add("blockrange");
            actions.add("hud");
        } else if (isHudModule(module.id)) {
            actions.add("hud");
            actions.add("hudopacity");
        }
        return actions;
    }

    private ModuleInfo findModule(String id) {
        if (id == null) return null;
        for (ModuleInfo module : MODULES) if (module.id.equals(id)) return module;
        return null;
    }

    private String contextActionLabel(String action) {
        ModuleInfo module = findModule(contextMenuModuleId);
        return switch (action) {
            case "settings" -> "Open settings...";
            case "accent" -> "Accent theme: " + AstraClient.accentName();
            case "sounds" -> "UI click sounds: " + (AstraClient.areClickSoundsEnabled() ? "ON" : "OFF");
            case "backgrounds" -> "HUD backgrounds: " + (AstraClient.areHudBackgroundsEnabled() ? "ON" : "OFF");
            case "hudopacity" -> "HUD opacity: " + AstraClient.hudOpacity() + "%";
            case "toggle" -> module != null && AstraClient.isEnabled(module.id) ? "Disable module" : "Enable module";
            case "bind" -> "Change keybind...";
            case "resetbind" -> "Clear keybind";
            case "crosshairSize" -> "Arm length: " + AstraClient.crosshairSize();
            case "crosshairGap" -> "Center gap: " + AstraClient.crosshairGap();
            case "crosshairThickness" -> "Line thickness: " + AstraClient.crosshairThickness();
            case "players" -> "Target players: " + (AstraClient.targetPlayers() ? "ON" : "OFF");
            case "hostile" -> "Target hostile: " + (AstraClient.targetHostile() ? "ON" : "OFF");
            case "passive" -> "Target passive: " + (AstraClient.targetPassive() ? "ON" : "OFF");
            case "range" -> module != null && module.id.equals("tracers")
                ? "Cycle tracer range (" + AstraClient.espRange() + ")"
                : "Cycle outline range (" + AstraClient.espRange() + ")";
            case "tracerPlayers" -> "Trace players: " + (AstraClient.tracerPlayers() ? "ON" : "OFF");
            case "tracerMobs" -> "Trace mobs: " + (AstraClient.tracerMobs() ? "ON" : "OFF");
            case "ground" -> "Ground only: " + (AstraClient.autoSprintGroundOnly() ? "ON" : "OFF");
            case "menus" -> "Pause in menus: " + (AstraClient.autoSprintDisableInMenus() ? "ON" : "OFF");
            case "zoom" -> "Cycle zoom FOV (" + AstraClient.zoomFov() + "°)";
            case "hunger" -> "Food threshold: " + AstraClient.autoEatHungerThreshold();
            case "healththreshold" -> "Health alert: " + AstraClient.lowHealthThreshold() + " HP";
            case "effectthreshold" -> "Effect warning: " + AstraClient.effectAlertThresholdSeconds() + " s";
            case "trailLength" -> "Trail history: " + AstraClient.breadcrumbTrailLength();
            case "setWaypoint" -> "Set waypoint to current position";
            case "clearWaypoint" -> "Clear saved waypoint";
            case "storageRange" -> "Search radius: " + AstraClient.storageFinderRange() + " blocks";
            case "blocktarget" -> "Find: " + AstraClient.blockFinderTarget();
            case "blockrange" -> "Scan radius: " + AstraClient.blockFinderRange() + " blocks";
            case "hud" -> "Open HUD editor...";
            default -> action;
        };
    }

    private void renderModuleContextMenu(DrawContext ctx, int mouseX, int mouseY) {
        ModuleInfo module = findModule(contextMenuModuleId);
        if (module == null) return;
        List<String> actions = contextMenuActions();
        int menuW = Math.min(202, Math.max(174, width - 12));
        int menuH = 28 + actions.size() * 21 + 5;
        int menuX = Math.max(6, Math.min(width - menuW - 6, contextMenuX));
        int menuY = Math.max(6, Math.min(height - menuH - 6, contextMenuY));
        int accent = AstraClient.accentColor();
        ctx.fill(menuX - 2, menuY - 2, menuX + menuW + 2, menuY + menuH + 2, 0xB92C2341);
        ctx.fill(menuX, menuY, menuX + menuW, menuY + menuH, 0xFF0D111B);
        ctx.fill(menuX, menuY, menuX + menuW, menuY + 2, accent);
        ctx.drawText(textRenderer, truncateToWidth(module.name.toUpperCase(Locale.ROOT), menuW - 18),
            menuX + 9, menuY + 9, WHITE, true);
        int rowY = menuY + 27;
        for (String action : actions) {
            boolean hover = mouseX >= menuX + 4 && mouseX <= menuX + menuW - 4
                && mouseY >= rowY && mouseY < rowY + 21;
            if (hover) ctx.fill(menuX + 4, rowY, menuX + menuW - 4, rowY + 20, 0xFF28223A);
            ctx.fill(menuX + 7, rowY + 7, menuX + 9, rowY + 13, action.equals("toggle")
                && module.implemented && AstraClient.isEnabled(module.id) ? 0xFF69E6B8 : accent);
            String label = contextActionLabel(action);
            ctx.drawText(textRenderer, truncateToWidth(label, menuW - 26), menuX + 14,
                rowY + 6, hover ? WHITE : 0xFFD5D9E5, false);
            rowY += 21;
        }
    }

    private boolean isHudModule(String id) {
        if (id == null) return false;
        for (String candidate : AstraClient.hudWidgetIds()) {
            if (candidate.equals(id)) return true;
        }
        return false;
    }

    private boolean handleContextMenuClick(double mouseX, double mouseY) {
        ModuleInfo module = findModule(contextMenuModuleId);
        if (module == null) return false;
        List<String> actions = contextMenuActions();
        int menuW = Math.min(202, Math.max(174, width - 12));
        int menuH = 28 + actions.size() * 21 + 5;
        int menuX = Math.max(6, Math.min(width - menuW - 6, contextMenuX));
        int menuY = Math.max(6, Math.min(height - menuH - 6, contextMenuY));
        if (!inside(mouseX, mouseY, menuX, menuY, menuW, menuH)) return false;
        int index = (int) ((mouseY - (menuY + 27)) / 21);
        if (index < 0 || index >= actions.size()) {
            contextMenuModuleId = null;
            return true;
        }
        String action = actions.get(index);
        switch (action) {
            case "settings" -> {
                contextMenuModuleId = null;
                client.setScreen(new AstraModuleSettingsScreen(this, module.id, module.name, module.description, module.implemented));
            }
            case "accent" -> AstraClient.toggleAccent();
            case "sounds" -> AstraClient.toggleClickSounds();
            case "backgrounds" -> AstraClient.toggleHudBackgrounds();
            case "hudopacity" -> AstraClient.setHudOpacity(AstraClient.hudOpacity() >= 95 ? 50 : AstraClient.hudOpacity() + 15);
            case "resetbind" -> AstraClient.setModuleKeybind(module.id, GLFW.GLFW_KEY_UNKNOWN);
            case "crosshairSize" -> AstraClient.setCrosshairSize(AstraClient.crosshairSize() >= 20 ? 3 : AstraClient.crosshairSize() + 2);
            case "crosshairGap" -> AstraClient.setCrosshairGap(AstraClient.crosshairGap() >= 12 ? 1 : AstraClient.crosshairGap() + 1);
            case "crosshairThickness" -> AstraClient.setCrosshairThickness(AstraClient.crosshairThickness() >= 5 ? 1 : AstraClient.crosshairThickness() + 1);
            case "toggle" -> AstraClient.toggle(module.id);
            case "bind" -> {
                captureKeybindId = module.id;
                AstraClient.notifyUser("Press a key to bind " + module.name + " (Esc clears)");
            }
            case "players" -> AstraClient.toggleCombatTarget("players");
            case "hostile" -> AstraClient.toggleCombatTarget("hostile");
            case "passive" -> AstraClient.toggleCombatTarget("passive");
            case "range" -> AstraClient.setEspRange(AstraClient.espRange() >= 112 ? 16 : AstraClient.espRange() + 16);
            case "tracerPlayers" -> AstraClient.toggleTracerPlayers();
            case "tracerMobs" -> AstraClient.toggleTracerMobs();
            case "ground" -> AstraClient.toggleAutoSprintGroundOnly();
            case "menus" -> AstraClient.toggleAutoSprintDisableInMenus();
            case "zoom" -> AstraClient.setZoomFov(AstraClient.zoomFov() >= 50 ? 10 : AstraClient.zoomFov() + 10);
            case "hunger" -> {
                AstraClient.previewAutoEatHungerThreshold(AstraClient.autoEatHungerThreshold() >= 19
                    ? 10 : AstraClient.autoEatHungerThreshold() + 1);
                AstraClient.saveConfig();
                AstraClient.notifyUser("Auto Eat threshold: " + AstraClient.autoEatHungerThreshold());
            }
            case "healththreshold" -> AstraClient.setLowHealthThreshold(
                AstraClient.lowHealthThreshold() >= 18 ? 2 : AstraClient.lowHealthThreshold() + 2);
            case "effectthreshold" -> AstraClient.setEffectAlertThresholdSeconds(
                AstraClient.effectAlertThresholdSeconds() >= 27 ? 3 : AstraClient.effectAlertThresholdSeconds() + 3);
            case "trailLength" -> AstraClient.setBreadcrumbTrailLength(
                AstraClient.breadcrumbTrailLength() >= 108 ? 12 : AstraClient.breadcrumbTrailLength() + 12);
            case "setWaypoint" -> AstraClient.setWaypointHere();
            case "clearWaypoint" -> AstraClient.clearWaypoint();
            case "storageRange" -> AstraClient.setStorageFinderRange(
                AstraClient.storageFinderRange() >= 28 ? 8 : AstraClient.storageFinderRange() + 4);
            case "blocktarget" -> AstraClient.cycleBlockFinderTarget();
            case "blockrange" -> AstraClient.setBlockFinderRange(
                AstraClient.blockFinderRange() >= 40 ? 8 : AstraClient.blockFinderRange() + 8);
            case "hud" -> {
                contextMenuModuleId = null;
                client.setScreen(new AstraHudEditorScreen(this));
            }
            default -> { }
        }
        if (!action.equals("settings") && !action.equals("hud")) contextMenuModuleId = null;
        return true;
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
        if (category.equals("Settings")) {
            int panelH = panelHeight();
            int maxSettingsScroll = Math.max(0, 492 - Math.max(90, panelH - 130));
            settingsScrollOffset = Math.max(0, Math.min(maxSettingsScroll,
                settingsScrollOffset - (int) Math.round(verticalAmount * 32.0)));
            return true;
        }
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
        if (closing) return true;
        if (capturingGuiKey) {
            AstraClient.setGuiKeybindCode(keyCode == GLFW.GLFW_KEY_ESCAPE
                ? GLFW.GLFW_KEY_RIGHT_SHIFT : keyCode);
            capturingGuiKey = false;
            return true;
        }
        if (capturingHudEditKey) {
            AstraClient.setHudEditKeybindCode(keyCode == GLFW.GLFW_KEY_ESCAPE
                ? GLFW.GLFW_KEY_F6 : keyCode);
            capturingHudEditKey = false;
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
        if (!searchFocused && keyCode == GLFW.GLFW_KEY_SLASH) {
            searchFocused = true;
            return true;
        }
        if (searchFocused && keyCode == GLFW.GLFW_KEY_ENTER) {
            searchFocused = false;
            return true;
        }
        if (searchFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
            scrollOffset = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
