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
        new ModuleInfo("orbitcubes", "Orbit Cubes", "Transparent theme-colored cubes orbit and rotate around your player", "Render", true),
        new ModuleInfo("hitboxes", "Hitbox ESP", "Draw accent-colored 3D outlines around nearby players, mobs, and dropped items", "Render", true),
        new ModuleInfo("targetbox", "Target Box", "Draw a bright pink 3D outline around the current combat target", "Render", true),
        new ModuleInfo("targethalo", "Target Halo", "Two animated rings of tiny cubes orbit the current target", "Render", true),
        new ModuleInfo("playertrail", "Player Trail", "Leave a sparkling end-rod particle trail as you move", "Render", true),
        new ModuleInfo("blockhighlight", "Block Highlight", "Draw a crisp theme-colored outline around the block under your crosshair", "Render", true),
        new ModuleInfo("orbitsparks", "Orbit Sparks", "Two glowing particles spiral around your player", "Render", true),
        new ModuleInfo("customcrosshair", "Custom Crosshair", "Draws a compact accent-colored crosshair overlay", "Render", true),
        new ModuleInfo("playeresp", "Player ESP", "Local glowing outline around other players", "Render", true),
        new ModuleInfo("mobesp", "Mob ESP", "Local glowing outlines around nearby mobs", "Render", true),
        new ModuleInfo("itemesp", "Item ESP", "Local glowing outlines around dropped items", "Render", true),
        new ModuleInfo("tracers", "Tracers", "Draw screen-space lines toward nearby loaded entities", "Render", true),
        new ModuleInfo("xray", "X-Ray", "Cached ore outlines in nearby loaded blocks; does not hide terrain", "Render", true),
        new ModuleInfo("nametags", "Nametags", "Screen-projected labels with entity names and distances", "Render", true),
        new ModuleInfo("zoom", "Zoom", "Adjustable camera FOV zoom", "Render", true),
        new ModuleInfo("autosprint", "Auto Sprint", "Sprint while moving forward", "Movement", true),
        new ModuleInfo("sprint", "Sprint", "Maintains sprint while moving forward", "Movement", true),
        new ModuleInfo("noslow", "No Slow", "Reduce local movement slowdown while using an item", "Movement", true),
        new ModuleInfo("speed", "Speed", "Modestly boosts horizontal movement speed locally", "Movement", true),
        new ModuleInfo("safewalk", "Safe Walk", "Uses sneak protection near block edges", "Movement", true),
        new ModuleInfo("eagle", "Eagle", "Automatically sneaks when standing over a ledge", "Movement", true),
        new ModuleInfo("inventorymove", "Inventory Move", "Move with inventory open", "Movement", true),
        new ModuleInfo("autoeat", "Auto Eat", "Automatically eats hotbar food at the configured hunger threshold", "Inventory", true),
        new ModuleInfo("fly", "Fly", "Client-side flight: jump to rise, sneak to descend", "Movement", true),
        new ModuleInfo("autowalk", "Auto Walk", "Automatically holds forward while in the world", "Movement", true),
        new ModuleInfo("aimassist", "Aim Assist", "Gently guides aim toward a visible nearby target while attacking", "Combat", true),
        new ModuleInfo("autoclicker", "Auto Clicker", "Repeats ordinary crosshair attacks while holding the attack button, respecting vanilla cooldown", "Combat", true),
        new ModuleInfo("criticals", "Criticals", "Only allows Astra combat attacks when vanilla critical-hit conditions are met", "Combat", true),
        new ModuleInfo("reach", "Reach Info", "Displays target distance; attacks remain capped at vanilla melee reach", "Combat", true),
        new ModuleInfo("triggerbot", "TriggerBot", "Attacks a valid target under your crosshair when the vanilla attack cooldown is ready", "Combat", true),
        new ModuleInfo("killaura", "Kill Aura", "Automatically attacks nearby living targets in front of you", "Combat", true),
        new ModuleInfo("autototem", "Auto Totem", "Moves an available Totem of Undying to the offhand", "Combat", true),
        new ModuleInfo("antiknockback", "Anti Knockback", "Reduces local horizontal velocity during the hurt animation", "Combat", true),
        new ModuleInfo("targethud", "Target HUD", "Show target name and distance", "HUD", true),
        new ModuleInfo("damageindicators", "Damage Indicators", "Show a short red indicator when your health drops", "Player", true),
        new ModuleInfo("cps", "CPS Counter", "Count left and right mouse clicks per second", "HUD", true),
        new ModuleInfo("worldinfo", "World Information", "Show dimension and in-game day", "HUD", true),
        new ModuleInfo("serverinfo", "Server Information", "Show the current server/world name and connection ping", "HUD", true),
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
        new ModuleInfo("noweather", "No Weather", "Hide local rain and snow rendering", "World", true),
        new ModuleInfo("timechanger", "Time Changer", "Locally hold the world at day, noon, sunset, or night", "World", true),
        new ModuleInfo("autotool", "Auto Tool", "Select the fastest hotbar tool for the targeted block", "World", true),
        new ModuleInfo("blockfinder", "Block Finder", "Find nearby diamond, ancient debris, emerald, gold, iron, coal, copper, lapis, redstone, or quartz", "World", true),
        new ModuleInfo("autoweapon", "Auto Weapon", "Selects a sword or axe while aiming at a living target", "Combat", true),
        new ModuleInfo("fastplace", "Fast Place", "Removes the client item-use cooldown before placement", "World", true),
        new ModuleInfo("fastbreak", "Fast Break", "Removes the client-side block-breaking cooldown", "World", true),
        new ModuleInfo("scaffold", "Scaffold", "Places held blocks beside existing support while bridging", "World", true),
        new ModuleInfo("nopumpkinoverlay", "No Pumpkin Overlay", "Hide the pumpkin blur when wearing a carved pumpkin", "Render", true),
        new ModuleInfo("nofireoverlay", "No Fire Overlay", "Hide the first-person flames when on fire", "Render", true),
        new ModuleInfo("noportaloverlay", "No Portal Overlay", "Hide the purple portal distortion overlay", "Render", true),
        new ModuleInfo("nospyglassoverlay", "No Spyglass Overlay", "Hide the circular spyglass mask while zooming", "Render", true),
        new ModuleInfo("nopowdersnowoverlay", "No Powder Snow Overlay", "Hide the powder-snow screen frost", "Render", true),
        new ModuleInfo("nonauseaoverlay", "No Nausea Overlay", "Hide the wobbly view effect from nausea", "Render", true),
        new ModuleInfo("novignette", "No Vignette", "Remove the dark border around the screen", "Render", true),
        new ModuleInfo("noscoreboard", "No Scoreboard", "Hide the right-side scoreboard panel", "Render", true)
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
    private boolean draggingListScrollbar;
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
        // Keep the complete window on-screen, including compact mobile and split-screen sizes.
        return Math.max(1, Math.min(Math.round(760 * AstraClient.guiScale() / 100.0f), width - 24));
    }

    private int panelHeight() {
        // Avoid the old minimum-size overflow when the scaled game viewport is short.
        return Math.max(1, Math.min(Math.round(470 * AstraClient.guiScale() / 100.0f), height - 24));
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

        // Ambient nebula grid and tiny drifting particles. These are drawn before the
        // opaque panel, so effects stay in the margins and never reduce text contrast.
        int ambientGrid = Math.max(30, Math.min(48, width / 26));
        int ambientDrift = (int) ((now / 72L) % ambientGrid);
        for (int gx = -ambientGrid + ambientDrift; gx < width; gx += ambientGrid) {
            if (gx >= 0 && gx < width) {
                ctx.fill(gx, 0, Math.min(width, gx + 1), height, 0x0C4AD9F5);
            }
        }
        for (int gy = ambientDrift; gy < height; gy += ambientGrid) {
            if (gy >= 0 && gy < height) {
                ctx.fill(0, gy, width, Math.min(height, gy + 1), 0x0C8175E8);
            }
        }
        // Small, deterministic particles avoid per-frame object creation and random jitter.
        for (int mote = 0; mote < 28; mote++) {
            int mx = Math.floorMod(mote * 83 + (int) (now / (51L + mote % 6 * 13L)), Math.max(1, width));
            int my = Math.floorMod(mote * 47 + (int) (now / (67L + mote % 5 * 17L)), Math.max(1, height));
            int size = mote % 7 == 0 ? 2 : 1;
            int moteColor = mote % 4 == 0 ? 0x9A54E8FF : 0x657C70D9;
            ctx.fill(mx, my, Math.min(width, mx + size), Math.min(height, my + size), moteColor);
        }

        // Delicate edge rails reinforce the Astra instrument-panel look without competing
        // with the module cards. Skip them on narrow screens where margins are too small.
        if (width >= panelW + 100 && height >= 220) {
            int railY = Math.max(16, Math.min(height - 36, top + panelH - 56));
            int railColor = (0x42 << 24) | (accent & 0x00FFFFFF);
            ctx.fill(14, 22, 16, Math.min(height - 14, 88), 0x554AD9F5);
            ctx.fill(width - 16, Math.max(14, height - 88), width - 14, height - 22, 0x556D54D9);
            ctx.fill(18, railY, Math.min(width / 2 - 12, 64), railY + 1, railColor);
            int meterStartX = Math.max(width / 2 + 12, width - 64);
            ctx.fill(meterStartX, railY + 2, Math.max(meterStartX + 1, width - 18), railY + 3, railColor);
            for (int meter = 0; meter < 9; meter++) {
                int meterHeight = 2 + Math.floorMod(meter * 5 + (int) (now / 170L), 10);
                int meterX = width - 30 - meter * 7;
                if (meterX >= 0) {
                    ctx.fill(meterX, railY - meterHeight - 3, meterX + 3, railY - 3,
                        meter % 3 == 0 ? 0xAA54E8FF : 0x8A8175E8);
                }
            }
        }

        // Shell, glow rails and title bar
        ctx.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF32284A);
        ctx.fill(left, top, left + panelW, top + panelH, BG);
        // Subtle technical texture: etched grid, diagonal data rails, and corner ticks.
        for (int gx = left + 12; gx < left + panelW; gx += 24) {
            ctx.fill(gx, top + 59, gx + 1, top + panelH - 1, 0x182F3B55);
        }
        for (int gy = top + 68; gy < top + panelH - 8; gy += 24) {
            ctx.fill(left + 2, gy, left + panelW - 2, gy + 1, 0x142F3B55);
        }
        int textureBand = Math.max(44, panelW / 7);
        ctx.fill(left + panelW - textureBand, top + 59, left + panelW - 2, top + panelH - 2, 0x0D7C62FF);
        for (int tick = 0; tick < 5; tick++) {
            int tx = left + panelW - 18 - tick * 9;
            ctx.fill(tx, top + 66, tx + 3, top + 68, 0x553F4B65);
            ctx.fill(tx, top + panelH - 13, tx + 3, top + panelH - 11, 0x553F4B65);
        }
        // Fine frame marks make the panel feel like a purpose-built control surface.
        ctx.fill(left + 7, top + 62, left + 19, top + 63, 0xFF34334C);
        ctx.fill(left + 7, top + 62, left + 8, top + 74, 0xFF34334C);
        ctx.fill(left + panelW - 19, top + panelH - 7, left + panelW - 7, top + panelH - 6, 0xFF34334C);
        ctx.fill(left + panelW - 8, top + panelH - 19, left + panelW - 7, top + panelH - 6, 0xFF34334C);
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
        // Keep the status badge in the narrow top strip; the search field occupies the row below.
        int liveY = top + 5;
        int livePulse = 150 + (int) (70 * (0.5 + 0.5 * Math.sin(now / 230.0)));
        ctx.fill(liveX, liveY, liveX + 35, liveY + 11, 0xFF172D2B);
        ctx.fill(liveX + 5, liveY + 4, liveX + 9, liveY + 8,
            (livePulse << 24) | 0x0069E6B8);
        ctx.drawText(textRenderer, "LIVE", liveX + 13, liveY + 2, 0xFF69E6B8, true);
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
            drawCategoryIcon(ctx, nav, left + 14,
                categoryY + Math.max(1, (rowHeight - 12) / 2), selected ? accent : MUTED);
            ctx.drawText(textRenderer, nav, left + 33,
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
                        // Subtle animated active-state glow; simple fills keep this renderer
                        // compatible with vanilla, Sodium, and mobile OpenGL backends.
                        int glowAlpha = 18 + (int) (14 * (0.5 + 0.5 * Math.sin(now / 260.0 + i * 0.7)));
                        int activeGlow = (glowAlpha << 24) | (accent & 0x00FFFFFF);
                        ctx.fill(x + 4, y + 2, x + cardW - 2, y + 3, 0x553FCEE9);
                        ctx.fill(x + 4, y + 3, x + cardW - 2, y + 7, activeGlow);
                        // A moving highlight gives enabled cards a restrained energy pulse.
                        int shineWidth = Math.max(8, cardW / 5);
                        int shineTravel = Math.max(1, cardW - shineWidth - 8);
                        int cardShineX = x + 4 + (int) ((now / 11L + i * 13L) % shineTravel);
                        ctx.fill(cardShineX, y + 2, Math.min(x + cardW - 2, cardShineX + shineWidth), y + 3,
                            0xBFFFFFFF);
                    }
                    // Small corner cuts provide a crisp, technical card frame without
                    // rounded-rectangle or custom-shader dependencies.
                    int cornerColor = hover ? accent : (enabled ? 0xFF3E6477 : 0xFF30394A);
                    ctx.fill(x + cardW - 8, y + 1, x + cardW - 2, y + 2, cornerColor);
                    ctx.fill(x + cardW - 2, y + 1, x + cardW - 1, y + 7, cornerColor);
                    ctx.fill(x + 1, y + cardH - 2, x + 7, y + cardH - 1, cornerColor);
                    ctx.fill(x + 1, y + cardH - 7, x + 2, y + cardH - 2, cornerColor);
                    boolean iconHover = mouseX >= x + 7 && mouseX <= x + 32
                        && mouseY >= y + 7 && mouseY <= y + 32;
                    ctx.fill(x + 10, y + 10, x + 29, y + 29,
                        iconHover ? 0xFF463765 : (enabled ? 0xFF302747 : 0xFF252B37));
                    int iconColor = module.implemented ? accent : 0xFF727B8D;
                    if (iconHover) {
                        ctx.drawText(textRenderer, "⚙", x + 15, y + 15, WHITE, true);
                    } else {
                        drawModuleIcon(ctx, module.id, x + 10, y + 10, iconColor);
                    }

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
                // Slim draggable scrollbar makes the module list's scroll position obvious.
                if (maxScroll > 0) {
                    int trackX = contentX + contentW - 5;
                    int trackTop = listTop + 2;
                    int trackBottom = listBottom - 2;
                    int trackH = Math.max(1, trackBottom - trackTop);
                    int thumbH = Math.max(24, (int) ((long) trackH * viewportH / (rows * (long) stride)));
                    thumbH = Math.min(trackH, thumbH);
                    int thumbTravel = Math.max(0, trackH - thumbH);
                    int thumbY = trackTop + (int) Math.round((double) scrollOffset / maxScroll * thumbTravel);
                    ctx.fill(trackX, trackTop, trackX + 3, trackBottom, 0xFF252D3D);
                    ctx.fill(trackX - 1, thumbY, trackX + 4, thumbY + thumbH,
                        mouseX >= trackX - 4 && mouseX <= trackX + 7 ? 0xFFB7A0FF : accent);
                    ctx.fill(trackX, thumbY + 2, trackX + 2, thumbY + thumbH - 2, 0xFF54E8FF);
                }
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

    /** Draw compact, font-independent glyphs for the nine workspace categories. */
    private void drawCategoryIcon(DrawContext ctx, String category, int x, int y, int color) {
        switch (category) {
            case "Combat" -> {
                ctx.fill(x + 4, y, x + 8, y + 1, color);
                ctx.fill(x + 4, y + 11, x + 8, y + 12, color);
                ctx.fill(x, y + 4, x + 1, y + 8, color);
                ctx.fill(x + 11, y + 4, x + 12, y + 8, color);
                ctx.fill(x + 3, y + 3, x + 4, y + 4, color);
                ctx.fill(x + 8, y + 3, x + 9, y + 4, color);
                ctx.fill(x + 3, y + 8, x + 4, y + 9, color);
                ctx.fill(x + 8, y + 8, x + 9, y + 9, color);
                ctx.fill(x + 5, y + 5, x + 7, y + 7, color);
            }
            case "Render" -> {
                ctx.fill(x + 1, y + 5, x + 11, y + 7, color);
                ctx.fill(x + 2, y + 4, x + 4, y + 8, color);
                ctx.fill(x + 8, y + 4, x + 10, y + 8, color);
                ctx.fill(x + 5, y + 3, x + 7, y + 9, color);
            }
            case "Movement" -> {
                ctx.fill(x + 1, y + 5, x + 8, y + 7, color);
                ctx.fill(x + 6, y + 2, x + 8, y + 10, color);
                ctx.fill(x + 8, y + 1, x + 10, y + 4, color);
                ctx.fill(x + 8, y + 8, x + 10, y + 11, color);
                ctx.fill(x + 10, y + 3, x + 12, y + 5, color);
                ctx.fill(x + 10, y + 7, x + 12, y + 9, color);
            }
            case "Player" -> {
                ctx.fill(x + 4, y + 1, x + 8, y + 5, color);
                ctx.fill(x + 2, y + 6, x + 10, y + 10, color);
                ctx.fill(x + 4, y + 10, x + 5, y + 12, color);
                ctx.fill(x + 7, y + 10, x + 8, y + 12, color);
            }
            case "Inventory" -> {
                ctx.fill(x, y, x + 5, y + 5, color);
                ctx.fill(x + 7, y, x + 12, y + 5, color);
                ctx.fill(x, y + 7, x + 5, y + 12, color);
                ctx.fill(x + 7, y + 7, x + 12, y + 12, color);
                ctx.fill(x + 2, y + 2, x + 3, y + 3, 0xFF111621);
                ctx.fill(x + 9, y + 9, x + 10, y + 10, 0xFF111621);
            }
            case "World" -> {
                ctx.fill(x + 5, y, x + 7, y + 12, color);
                ctx.fill(x, y + 5, x + 12, y + 7, color);
                ctx.fill(x + 2, y + 2, x + 4, y + 4, color);
                ctx.fill(x + 8, y + 8, x + 10, y + 10, color);
            }
            case "Utility" -> {
                ctx.fill(x + 5, y + 1, x + 7, y + 11, color);
                ctx.fill(x + 1, y + 5, x + 11, y + 7, color);
                ctx.fill(x + 2, y + 2, x + 4, y + 4, color);
                ctx.fill(x + 8, y + 8, x + 10, y + 10, color);
            }
            case "HUD" -> {
                ctx.fill(x + 1, y + 2, x + 11, y + 4, color);
                ctx.fill(x + 1, y + 5, x + 8, y + 7, color);
                ctx.fill(x + 1, y + 8, x + 10, y + 10, color);
            }
            case "Settings" -> {
                ctx.fill(x + 4, y, x + 8, y + 2, color);
                ctx.fill(x + 4, y + 10, x + 8, y + 12, color);
                ctx.fill(x, y + 4, x + 2, y + 8, color);
                ctx.fill(x + 10, y + 4, x + 12, y + 8, color);
                ctx.fill(x + 3, y + 3, x + 9, y + 9, color);
                ctx.fill(x + 5, y + 5, x + 7, y + 7, 0xFF111621);
            }
            default -> ctx.fill(x + 4, y + 4, x + 8, y + 8, color);
        }
    }

    /**
     * Small vector-style module glyphs drawn from solid rectangles rather than font symbols.
     * This avoids missing-glyph boxes on different Minecraft fonts and mobile renderers.
     */
    private void drawModuleIcon(DrawContext ctx, String id, int x, int y, int color) {
        switch (id) {
            case "fullbright" -> {
                ctx.fill(x + 7, y + 7, x + 12, y + 12, color);
                ctx.fill(x + 8, y + 1, x + 11, y + 5, color);
                ctx.fill(x + 8, y + 14, x + 11, y + 18, color);
                ctx.fill(x + 1, y + 8, x + 5, y + 11, color);
                ctx.fill(x + 14, y + 8, x + 18, y + 11, color);
                ctx.fill(x + 3, y + 3, x + 6, y + 6, color);
                ctx.fill(x + 13, y + 3, x + 16, y + 6, color);
                ctx.fill(x + 3, y + 13, x + 6, y + 16, color);
                ctx.fill(x + 13, y + 13, x + 16, y + 16, color);
            }
            case "killaura", "triggerbot", "aimassist", "autoweapon", "targethud",
                 "targetbox", "targethalo", "hitboxes", "playeresp", "mobesp", "itemesp",
                 "nametags", "tracers" -> {
                ctx.fill(x + 2, y + 2, x + 7, y + 3, color);
                ctx.fill(x + 2, y + 2, x + 3, y + 7, color);
                ctx.fill(x + 12, y + 2, x + 17, y + 3, color);
                ctx.fill(x + 16, y + 2, x + 17, y + 7, color);
                ctx.fill(x + 2, y + 12, x + 3, y + 17, color);
                ctx.fill(x + 2, y + 16, x + 7, y + 17, color);
                ctx.fill(x + 16, y + 12, x + 17, y + 17, color);
                ctx.fill(x + 12, y + 16, x + 17, y + 17, color);
                ctx.fill(x + 8, y + 8, x + 11, y + 11, color);
            }
            case "orbitcubes", "orbitsparks", "playertrail" -> {
                ctx.fill(x + 2, y + 3, x + 7, y + 8, color);
                ctx.fill(x + 3, y + 4, x + 6, y + 7, 0xFF111621);
                ctx.fill(x + 12, y + 11, x + 17, y + 16, color);
                ctx.fill(x + 13, y + 12, x + 16, y + 15, 0xFF111621);
                ctx.fill(x + 7, y + 5, x + 13, y + 6, color);
                ctx.fill(x + 10, y + 6, x + 11, y + 12, color);
            }
            case "fps", "fpsgraph", "frametimehud", "ping", "memory",
                 "speedometer", "cps", "sessionstats", "arraylist" -> {
                ctx.fill(x + 2, y + 15, x + 17, y + 17, color);
                ctx.fill(x + 3, y + 10, x + 6, y + 15, color);
                ctx.fill(x + 8, y + 6, x + 11, y + 15, color);
                ctx.fill(x + 13, y + 2, x + 16, y + 15, color);
            }
            case "coordinates", "direction", "compasshud", "waypoint",
                 "breadcrumbs", "blockfinder", "storagefinder" -> {
                ctx.fill(x + 8, y + 1, x + 10, y + 18, color);
                ctx.fill(x + 1, y + 8, x + 18, y + 10, color);
                ctx.fill(x + 5, y + 4, x + 7, y + 6, color);
                ctx.fill(x + 12, y + 12, x + 14, y + 14, color);
                ctx.fill(x + 11, y + 2, x + 15, y + 6, color);
            }
            case "armorhud", "durabilityhud", "totemcounter", "lowhealthalert" -> {
                ctx.fill(x + 4, y + 2, x + 15, y + 4, color);
                ctx.fill(x + 3, y + 4, x + 5, y + 11, color);
                ctx.fill(x + 14, y + 4, x + 16, y + 11, color);
                ctx.fill(x + 5, y + 11, x + 7, y + 14, color);
                ctx.fill(x + 12, y + 11, x + 14, y + 14, color);
                ctx.fill(x + 7, y + 14, x + 12, y + 17, color);
            }
            case "potionhud", "potioncounter", "effectalert" -> {
                ctx.fill(x + 7, y + 1, x + 12, y + 3, color);
                ctx.fill(x + 8, y + 3, x + 11, y + 7, color);
                ctx.fill(x + 5, y + 7, x + 14, y + 15, color);
                ctx.fill(x + 6, y + 8, x + 13, y + 14, 0xFF111621);
                ctx.fill(x + 6, y + 12, x + 13, y + 14, color);
                ctx.fill(x + 7, y + 16, x + 12, y + 18, color);
            }
            case "healthhud", "hungerhud", "damageindicators", "goldencounter" -> {
                ctx.fill(x + 2, y + 4, x + 8, y + 9, color);
                ctx.fill(x + 11, y + 4, x + 17, y + 9, color);
                ctx.fill(x + 4, y + 8, x + 15, y + 12, color);
                ctx.fill(x + 6, y + 11, x + 13, y + 14, color);
                ctx.fill(x + 8, y + 13, x + 11, y + 17, color);
            }
            case "keystrokes", "inventorymove", "inventoryhud", "pearlcounter", "arrowcounter" -> {
                ctx.fill(x + 6, y + 1, x + 12, y + 6, color);
                ctx.fill(x + 1, y + 7, x + 7, y + 12, color);
                ctx.fill(x + 11, y + 7, x + 17, y + 12, color);
                ctx.fill(x + 6, y + 13, x + 12, y + 18, color);
                ctx.fill(x + 7, y + 2, x + 11, y + 5, 0xFF111621);
            }
            case "autosprint", "sprint", "fly", "autowalk", "safewalk",
                 "eagle", "speed", "zoom" -> {
                ctx.fill(x + 2, y + 8, x + 9, y + 11, color);
                ctx.fill(x + 8, y + 5, x + 11, y + 14, color);
                ctx.fill(x + 11, y + 2, x + 14, y + 6, color);
                ctx.fill(x + 11, y + 13, x + 14, y + 17, color);
                ctx.fill(x + 14, y + 5, x + 17, y + 8, color);
                ctx.fill(x + 14, y + 11, x + 17, y + 14, color);
            }
            case "blockhighlight", "autotool", "timechanger", "worldinfo",
                 "biome", "serverinfo", "noweather", "customcrosshair" -> {
                ctx.fill(x + 2, y + 2, x + 17, y + 4, color);
                ctx.fill(x + 2, y + 7, x + 17, y + 9, color);
                ctx.fill(x + 2, y + 12, x + 17, y + 14, color);
                ctx.fill(x + 2, y + 2, x + 4, y + 17, color);
                ctx.fill(x + 8, y + 2, x + 10, y + 17, color);
                ctx.fill(x + 15, y + 2, x + 17, y + 17, color);
            }
            default -> {
                ctx.fill(x + 8, y + 1, x + 11, y + 4, color);
                ctx.fill(x + 4, y + 5, x + 15, y + 7, color);
                ctx.fill(x + 2, y + 8, x + 17, y + 11, color);
                ctx.fill(x + 4, y + 12, x + 15, y + 14, color);
                ctx.fill(x + 8, y + 15, x + 11, y + 18, color);
            }
        }
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
        int rgbButtonX = contentX + contentW - 53;
        int rgbButtonY = top + 101 - settingsScrollOffset;
        boolean rgbButtonHover = mouseX >= rgbButtonX && mouseX <= rgbButtonX + 43
            && mouseY >= rgbButtonY && mouseY <= rgbButtonY + 24;
        ctx.fill(rgbButtonX, rgbButtonY, rgbButtonX + 43, rgbButtonY + 24, rgbButtonHover ? PANEL_HOVER : PANEL);
        ctx.fill(rgbButtonX, rgbButtonY, rgbButtonX + 2, rgbButtonY + 24, 0xFF54E8FF);
        ctx.drawCenteredTextWithShadow(textRenderer, "RGB", rgbButtonX + 23, rgbButtonY + 8, WHITE);
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
            "Config manager (5 save slots)", "OPEN", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 454 - settingsScrollOffset, contentW,
            "Import config backup", "IMPORT", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 490 - settingsScrollOffset, contentW,
            "Quick module profiles", "OPEN", accent);
        renderSettingRow(ctx, mouseX, mouseY, contentX, top + 526 - settingsScrollOffset, contentW,
            "Animated menu background", AstraClient.isCustomMenuBackgroundEnabled() ? "ON" : "OFF", accent);
    }

    private void renderSettingRow(
        DrawContext ctx, int mouseX, int mouseY, int x, int y, int w,
        String label, String action, int accent
    ) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 43;
        ctx.fill(x, y, x + w, y + 34, hover ? PANEL_HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + 34, accent);
        ctx.drawText(textRenderer, label, x + 12, y + 9, WHITE, true);
        int actionRightPadding = label.equals("Accent theme") ? 68 : 12;
        ctx.drawText(textRenderer, action, x + w - textRenderer.getWidth(action) - actionRightPadding,
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
                if (mouseX >= contentX + contentW - 56) {
                    client.setScreen(new AstraThemeEditorScreen(this));
                } else {
                    AstraClient.toggleAccent();
                }
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
                client.setScreen(new AstraConfigScreen(this));
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 454 - settingsScrollOffset, contentW, 34)) {
                AstraClient.importConfigBackup();
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 490 - settingsScrollOffset, contentW, 34)) {
                client.setScreen(new AstraProfileScreen(this));
                return true;
            }
            if (inside(mouseX, mouseY, contentX, top + 526 - settingsScrollOffset, contentW, 34)) {
                AstraClient.toggleCustomMenuBackground();
                return true;
            }
            return true;
        }

        int listTop = top + 105, listBottom = top + panelH - 43;
        if (mouseY < listTop || mouseY > listBottom) return true;
        List<ModuleInfo> visible = getVisibleModules();
        int viewportH = Math.max(1, listBottom - listTop);
        int rows = (visible.size() + 1) / 2;
        int maxScroll = Math.max(0, rows * 76 - viewportH);
        int scrollbarX = contentX + contentW - 7;
        if (maxScroll > 0 && mouseX >= scrollbarX && mouseX <= contentX + contentW + 1) {
            draggingListScrollbar = true;
            updateListScrollFromMouse(mouseY, listTop, listBottom, rows, maxScroll);
            return true;
        }
        int gap = 10, cardW = Math.max(70, (contentW - gap) / 2);
        int stride = 76, cardH = 65;
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

    private void updateListScrollFromMouse(double mouseY, int listTop, int listBottom, int rows, int maxScroll) {
        int trackTop = listTop + 2;
        int trackBottom = listBottom - 2;
        int trackH = Math.max(1, trackBottom - trackTop);
        int viewportH = Math.max(1, listBottom - listTop);
        int thumbH = Math.max(24, (int) ((long) trackH * viewportH / (rows * 76L)));
        thumbH = Math.min(trackH, thumbH);
        int thumbTravel = Math.max(1, trackH - thumbH);
        double progress = Math.max(0.0, Math.min(1.0,
            (mouseY - trackTop - thumbH / 2.0) / thumbTravel));
        scrollOffset = (int) Math.round(progress * maxScroll);
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
        actions.add("themeeditor");
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
            actions.add("auraFov");
            actions.add("criticalOnly");
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
            case "themeeditor" -> "Open custom RGB theme editor...";
            case "hudopacity" -> "HUD opacity: " + AstraClient.hudOpacity() + "%";
            case "toggle" -> module != null && AstraClient.isEnabled(module.id) ? "Disable module" : "Enable module";
            case "bind" -> "Change keybind...";
            case "resetbind" -> "Clear keybind";
            case "crosshairSize" -> "Arm length: " + AstraClient.crosshairSize();
            case "crosshairGap" -> "Center gap: " + AstraClient.crosshairGap();
            case "crosshairThickness" -> "Line thickness: " + AstraClient.crosshairThickness();
            case "auraFov" -> "FOV: " + AstraClient.killAuraFov() + " degrees (scroll to adjust)";
            case "criticalOnly" -> "Critical-only: " + (AstraClient.killAuraCriticalOnly() ? "ON" : "OFF");
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
                client.setScreen(new AstraModuleSettingsScreen(this, module.id, module.name, module.description, module.category, module.implemented));
            }
            case "accent" -> AstraClient.toggleAccent();
            case "sounds" -> AstraClient.toggleClickSounds();
            case "backgrounds" -> AstraClient.toggleHudBackgrounds();
            case "themeeditor" -> {
                contextMenuModuleId = null;
                client.setScreen(new AstraThemeEditorScreen(this));
            }
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
            case "auraFov" -> AstraClient.setKillAuraFov(AstraClient.killAuraFov() >= 180 ? 60 : Math.min(180, AstraClient.killAuraFov() + 15));
            case "criticalOnly" -> AstraClient.toggleKillAuraCriticalOnly();
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
        if (button == 0 && draggingListScrollbar) {
            int panelH = panelHeight();
            int top = panelTop(panelH);
            int listTop = top + 105, listBottom = top + panelH - 43;
            List<ModuleInfo> visible = getVisibleModules();
            int rows = (visible.size() + 1) / 2;
            int maxScroll = Math.max(0, rows * 76 - Math.max(1, listBottom - listTop));
            if (maxScroll > 0) updateListScrollFromMouse(mouseY, listTop, listBottom, rows, maxScroll);
            return true;
        }
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
        draggingListScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Scroll over the FOV row in Kill Aura's right-click menu for five-degree steps.
        if (contextMenuModuleId != null && contextMenuModuleId.equals("killaura") && verticalAmount != 0.0D) {
            ModuleInfo module = findModule(contextMenuModuleId);
            List<String> actions = contextMenuActions();
            int menuW = Math.min(202, Math.max(174, width - 12));
            int menuH = 28 + actions.size() * 21 + 5;
            int menuX = Math.max(6, Math.min(width - menuW - 6, contextMenuX));
            int menuY = Math.max(6, Math.min(height - menuH - 6, contextMenuY));
            if (module != null && inside(mouseX, mouseY, menuX, menuY, menuW, menuH)) {
                int row = (int) ((mouseY - (menuY + 27)) / 21);
                if (row >= 0 && row < actions.size() && actions.get(row).equals("auraFov")) {
                    AstraClient.setKillAuraFov(AstraClient.killAuraFov() + (verticalAmount > 0 ? 5 : -5));
                    return true;
                }
            }
        }
        if (category.equals("Settings")) {
            int panelH = panelHeight();
            int maxSettingsScroll = Math.max(0, 564 - Math.max(90, panelH - 130));
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
