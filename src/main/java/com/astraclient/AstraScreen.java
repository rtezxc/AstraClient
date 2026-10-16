package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AstraScreen extends Screen {
    private static final int BG = 0xF20A0D16;
    private static final int SIDEBAR = 0xFF111622;
    private static final int PANEL = 0xFF171D2A;
    private static final int PANEL_HOVER = 0xFF222B3D;
    private static final int WHITE = 0xFFF3F5FF;
    private static final int MUTED = 0xFF929BB0;
    private static final int LINE = 0xFF2A3142;

    private static final String[] CATEGORIES = {
        "Combat", "Render", "Movement", "Player", "World", "HUD", "Settings"
    };

    private static final List<ModuleInfo> MODULES = List.of(
        new ModuleInfo("fullbright", "FullBright", "Clearer vision in dark areas", "Render", true),
        new ModuleInfo("playeresp", "Player ESP", "Highlight other players through blocks", "Render", false),
        new ModuleInfo("itemesp", "Item ESP", "Highlight dropped items", "Render", false),
        new ModuleInfo("xray", "X-Ray", "Reveal selected block types", "Render", false),
        new ModuleInfo("autosprint", "Auto Sprint", "Sprint while moving forward", "Movement", true),
        new ModuleInfo("noslow", "No Slow", "Reduce item-use movement slowdown", "Movement", false),
        new ModuleInfo("speed", "Speed", "Adjust movement speed", "Movement", false),
        new ModuleInfo("fly", "Fly", "Custom flight movement", "Movement", false),
        new ModuleInfo("aimassist", "Aim Assist", "Adjust aim toward valid targets", "Combat", false),
        new ModuleInfo("triggerbot", "TriggerBot", "Attack when a target is under the crosshair", "Combat", false),
        new ModuleInfo("killaura", "Kill Aura", "Automatic nearby-target attacks", "Combat", false),
        new ModuleInfo("autototem", "Auto Totem", "Automatically manage the offhand totem", "Player", false),
        new ModuleInfo("antiknockback", "Anti Knockback", "Reduce received knockback", "Player", false),
        new ModuleInfo("freecam", "Freecam", "Move a camera independently", "World", false),
        new ModuleInfo("noweather", "No Weather", "Hide rain and snow", "World", false),
        new ModuleInfo("fps", "FPS Counter", "Show the current frame rate", "HUD", true),
        new ModuleInfo("coordinates", "Coordinates", "Show your block coordinates", "HUD", true),
        new ModuleInfo("direction", "Direction", "Show the direction you face", "HUD", true),
        new ModuleInfo("keystrokes", "Keystrokes", "Show movement keys on screen", "HUD", true)
    );

    private String category = "Render";
    private String searchText = "";
    private boolean searchFocused;
    private int scrollOffset;
    private int settingsActionY;

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
        super(Text.literal("Astra Client"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int panelW = Math.min(760, width - 24);
        int panelH = Math.min(470, height - 24);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int sideW = Math.min(154, Math.max(112, panelW / 4));
        int accent = AstraClient.accentColor();
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int listTop = top + 91;
        int listBottom = top + panelH - 43;
        int viewportH = Math.max(0, listBottom - listTop);

        ctx.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0x884D36A8);
        ctx.fill(left, top, left + panelW, top + panelH, BG);
        ctx.fill(left, top, left + panelW, top + 3, accent);
        ctx.fill(left, top, left + sideW, top + panelH, SIDEBAR);
        ctx.fill(left + sideW, top + 57, left + panelW, top + 58, LINE);

        // Brand block
        ctx.fill(left + 14, top + 15, left + 40, top + 41, accent);
        ctx.drawText(textRenderer, "A", left + 22, top + 21, 0xFF10131C, true);
        ctx.drawText(textRenderer, "ASTRA", left + 48, top + 16, WHITE, true);
        ctx.drawText(textRenderer, "CLIENT  /  1.21.4", left + 48, top + 33, MUTED, false);

        // Search field
        int searchW = Math.min(190, contentW);
        int searchX = left + panelW - searchW - 14;
        int searchY = top + 17;
        ctx.fill(searchX, searchY, searchX + searchW, searchY + 25,
            searchFocused ? 0xFF252D40 : PANEL);
        ctx.fill(searchX, searchY + 24, searchX + searchW, searchY + 25, searchFocused ? accent : LINE);
        String searchLabel = searchText.isEmpty() ? "Search modules..." : searchText;
        ctx.drawText(textRenderer, searchLabel, searchX + 8, searchY + 8,
            searchText.isEmpty() ? MUTED : WHITE, false);

        ctx.drawText(textRenderer, category.toUpperCase(Locale.ROOT),
            contentX, top + 17, WHITE, true);
        ctx.drawText(textRenderer, "MODULE CONTROL CENTER",
            contentX, top + 36, accent, false);

        // Sidebar categories
        int catY = top + 76;
        for (String name : CATEGORIES) {
            boolean selected = category.equals(name);
            int rowH = 29;
            if (selected) {
                ctx.fill(left + 8, catY, left + sideW - 8, catY + rowH, 0xFF252237);
                ctx.fill(left + 8, catY, left + 11, catY + rowH, accent);
            }
            ctx.drawText(textRenderer, selected ? "◆" : "·",
                left + 18, catY + 10, selected ? accent : MUTED, false);
            ctx.drawText(textRenderer, name, left + 32, catY + 10,
                selected ? WHITE : MUTED, selected);
            catY += 35;
        }

        ctx.drawText(textRenderer, "RIGHT SHIFT", left + 13, top + panelH - 32, accent, false);
        ctx.drawText(textRenderer, "TOGGLE MENU", left + 13, top + panelH - 19, MUTED, false);

        if (category.equals("Settings")) {
            renderSettings(ctx, mouseX, mouseY, left, top, contentX, contentW, panelH, accent);
        } else {
            List<ModuleInfo> visible = getVisibleModules();
            if (visible.isEmpty()) {
                ctx.drawText(textRenderer, searchText.isEmpty()
                    ? "No modules in this category yet."
                    : "No matching modules found.", contentX, listTop + 10, MUTED, false);
            } else {
                int stride = 76;
                int cardH = 65;
                int gap = 10;
                int cardW = (contentW - gap) / 2;
                int rows = (visible.size() + 1) / 2;
                int maxScroll = Math.max(0, rows * stride - viewportH);
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

                ctx.enableScissor(contentX, listTop, contentX + contentW, listBottom);
                for (int i = 0; i < visible.size(); i++) {
                    ModuleInfo module = visible.get(i);
                    int col = i % 2;
                    int row = i / 2;
                    int cardX = contentX + col * (cardW + gap);
                    int cardY = listTop + row * stride - scrollOffset;
                    boolean hovered = mouseX >= cardX && mouseX <= cardX + cardW
                        && mouseY >= cardY && mouseY <= cardY + cardH;
                    boolean enabled = module.implemented && AstraClient.isEnabled(module.id);

                    ctx.fill(cardX, cardY, cardX + cardW, cardY + cardH,
                        hovered ? PANEL_HOVER : PANEL);
                    ctx.fill(cardX, cardY, cardX + 2, cardY + cardH,
                        enabled ? accent : (module.implemented ? LINE : 0xFF3A4050));
                    ctx.drawText(textRenderer, module.name, cardX + 11, cardY + 10,
                        module.implemented ? WHITE : MUTED, true);

                    String status = module.implemented
                        ? (enabled ? "ON" : "OFF") : "COMING SOON";
                    int statusColor = !module.implemented ? MUTED : (enabled ? accent : 0xFFB4BBCB);
                    int statusW = textRenderer.getWidth(status);
                    ctx.drawText(textRenderer, status, cardX + cardW - statusW - 9,
                        cardY + 11, statusColor, true);

                    String description = module.description;
                    int maxDescWidth = Math.max(35, cardW - 20);
                    while (textRenderer.getWidth(description) > maxDescWidth && description.length() > 4) {
                        description = description.substring(0, description.length() - 4) + "...";
                    }
                    ctx.drawText(textRenderer, description, cardX + 11, cardY + 31, MUTED, false);
                    if (module.implemented) {
                        ctx.fill(cardX + 11, cardY + 49, cardX + 36, cardY + 57,
                            enabled ? accent : 0xFF30384A);
                        ctx.fill(cardX + 37, cardY + 49, cardX + 47, cardY + 57,
                            enabled ? 0xFFB9A0FF : 0xFF30384A);
                    } else {
                        ctx.drawText(textRenderer, "NOT AVAILABLE IN THIS BUILD",
                            cardX + 11, cardY + 50, 0xFF70798E, false);
                    }
                }
                ctx.disableScissor();

                ctx.drawText(textRenderer,
                    visible.size() + " MODULES  ·  " + visible.stream().filter(m -> m.implemented).count()
                        + " IMPLEMENTED",
                    contentX, top + panelH - 27, MUTED, false);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
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
        DrawContext ctx, int mouseX, int mouseY, int left, int top,
        int contentX, int contentW, int panelH, int accent
    ) {
        ctx.drawText(textRenderer, "CLIENT SETTINGS", contentX, top + 88, WHITE, true);
        ctx.drawText(textRenderer, "Settings are saved to config/astraclient.properties.",
            contentX, top + 108, MUTED, false);

        int y = top + 144;
        int rowH = 43;
        settingsActionY = y;
        renderSettingRow(ctx, mouseX, mouseY, contentX, y, contentW,
            "Accent color", AstraClient.isAlternateAccent() ? "CYAN" : "PURPLE", accent);
        y += rowH + 8;
        renderSettingRow(ctx, mouseX, mouseY, contentX, y, contentW,
            "Reset module toggles", "RESET", accent);
        y += rowH + 8;
        renderSettingRow(ctx, mouseX, mouseY, contentX, y, contentW,
            "Save configuration", "SAVE NOW", accent);

        ctx.drawText(textRenderer, "Only modules marked implemented can be enabled.",
            contentX, top + panelH - 55, MUTED, false);
    }

    private void renderSettingRow(
        DrawContext ctx, int mouseX, int mouseY, int x, int y, int w,
        String label, String action, int accent
    ) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 43;
        ctx.fill(x, y, x + w, y + 43, hovered ? PANEL_HOVER : PANEL);
        ctx.fill(x, y, x + 2, y + 43, accent);
        ctx.drawText(textRenderer, label, x + 12, y + 10, WHITE, true);
        int actionW = textRenderer.getWidth(action);
        ctx.drawText(textRenderer, action, x + w - actionW - 12, y + 10, accent, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int panelW = Math.min(760, width - 24);
        int panelH = Math.min(470, height - 24);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int sideW = Math.min(154, Math.max(112, panelW / 4));
        int contentX = left + sideW + 18;
        int contentW = panelW - sideW - 34;
        int searchW = Math.min(190, contentW);
        int searchX = left + panelW - searchW - 14;
        int searchY = top + 17;

        if (mouseX >= searchX && mouseX <= searchX + searchW
            && mouseY >= searchY && mouseY <= searchY + 25) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        int catY = top + 76;
        for (String name : CATEGORIES) {
            if (mouseX >= left + 8 && mouseX <= left + sideW - 8
                && mouseY >= catY && mouseY <= catY + 29) {
                category = name;
                scrollOffset = 0;
                searchText = "";
                return true;
            }
            catY += 35;
        }

        if (category.equals("Settings")) {
            int y = top + 144;
            if (mouseX >= contentX && mouseX <= contentX + contentW
                && mouseY >= y && mouseY <= y + 43) {
                AstraClient.toggleAccent();
                return true;
            }
            y += 51;
            if (mouseX >= contentX && mouseX <= contentX + contentW
                && mouseY >= y && mouseY <= y + 43) {
                AstraClient.resetModuleDefaults();
                return true;
            }
            y += 51;
            if (mouseX >= contentX && mouseX <= contentX + contentW
                && mouseY >= y && mouseY <= y + 43) {
                AstraClient.saveConfig();
                return true;
            }
            return true;
        }

        int listTop = top + 91;
        int listBottom = top + panelH - 43;
        if (mouseY < listTop || mouseY > listBottom) return true;

        List<ModuleInfo> visible = getVisibleModules();
        int gap = 10;
        int cardW = (contentW - gap) / 2;
        int cardH = 65;
        int stride = 76;
        int viewportH = Math.max(0, listBottom - listTop);
        int rows = (visible.size() + 1) / 2;
        int maxScroll = Math.max(0, rows * stride - viewportH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < visible.size(); i++) {
            ModuleInfo module = visible.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = contentX + col * (cardW + gap);
            int y = listTop + row * stride - scrollOffset;
            if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH) {
                if (module.implemented) AstraClient.toggle(module.id);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!category.equals("Settings")) {
            int panelW = Math.min(760, width - 24);
            int panelH = Math.min(470, height - 24);
            int left = (width - panelW) / 2;
            int top = (height - panelH) / 2;
            int sideW = Math.min(154, Math.max(112, panelW / 4));
            int contentX = left + sideW + 18;
            int contentW = panelW - sideW - 34;
            int viewportH = Math.max(0, (top + panelH - 43) - (top + 91));
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (searchFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                scrollOffset = 0;
            }
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
