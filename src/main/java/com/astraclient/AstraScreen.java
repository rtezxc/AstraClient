package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class AstraScreen extends Screen {
    private static final int BG = 0xF20A0D16;
    private static final int SIDEBAR = 0xFF111622;
    private static final int PURPLE = 0xFFAA70FF;
    private static final int CYAN = 0xFF54E8FF;
    private static final int WHITE = 0xFFF3F5FF;
    private static final int MUTED = 0xFF929BB0;

    private final List<ModuleInfo> modules = new ArrayList<>();
    private String category = "Render";

    private static final class ModuleInfo {
        final String name;
        final String description;
        final String category;
        final Runnable toggle;
        final BooleanSupplier enabled;

        ModuleInfo(String name, String description, String category,
                   Runnable toggle, BooleanSupplier enabled) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.toggle = toggle;
            this.enabled = enabled;
        }
    }

    public AstraScreen() {
        super(Text.literal("Astra Client"));
        modules.add(new ModuleInfo("FullBright", "Night vision in dark areas", "Render",
            () -> AstraClient.setFullBright(!AstraClient.isFullBrightEnabled()), AstraClient::isFullBrightEnabled));
        modules.add(new ModuleInfo("Player ESP", "Not implemented yet", "Render", () -> {}, () -> false));
        modules.add(new ModuleInfo("FPS Counter", "HUD display planned", "HUD", () -> {}, () -> false));
        modules.add(new ModuleInfo("Coordinates", "HUD display planned", "HUD", () -> {}, () -> false));
        modules.add(new ModuleInfo("Sprint", "Auto-sprint while moving forward", "Movement",
            () -> AstraClient.setAutoSprint(!AstraClient.isAutoSprintEnabled()), AstraClient::isAutoSprintEnabled));
        modules.add(new ModuleInfo("NoSlow", "Not implemented yet", "Movement", () -> {}, () -> false));
        modules.add(new ModuleInfo("TriggerBot", "Not implemented", "Combat", () -> {}, () -> false));
    }

    @Override
    protected void init() {
        clearChildren();
        int panelW = Math.min(700, width - 20);
        int panelH = Math.min(410, height - 20);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int sideW = Math.min(155, panelW / 3);

        String[] categories = {"Combat", "Render", "Movement", "Player", "World", "HUD", "Settings"};
        int cy = top + 72;
        for (String name : categories) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal((category.equals(name) ? "◆  " : "   ") + name),
                b -> { category = name; clearAndInit(); }
            ).dimensions(left + 9, cy, sideW - 18, 25).build());
            cy += 30;
        }

        int x = left + sideW + 18;
        int w = panelW - sideW - 34;
        int y = top + 78;
        for (ModuleInfo module : modules) {
            if (!module.category.equals(category)) continue;
            final ModuleInfo selected = module;
            boolean active = selected.enabled.getAsBoolean();
            addDrawableChild(ButtonWidget.builder(
                Text.literal((active ? "●  " : "○  ") + selected.name + "     " + (active ? "ON" : "OFF")),
                b -> { selected.toggle.run(); clearAndInit(); }
            ).dimensions(x, y, w, 29).build());
            y += 55;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("CLOSE"),
            b -> close()).dimensions(left + panelW - 88, top + panelH - 32, 76, 22).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int panelW = Math.min(700, width - 20);
        int panelH = Math.min(410, height - 20);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int sideW = Math.min(155, panelW / 3);

        ctx.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0x664D36A8);
        ctx.fill(left, top, left + panelW, top + panelH, BG);
        ctx.fill(left, top, left + panelW, top + 3, PURPLE);
        ctx.fill(left, top, left + sideW, top + panelH, SIDEBAR);
        ctx.fill(left + sideW, top + 57, left + panelW, top + 58, 0xFF2A3142);

        ctx.drawText(textRenderer, "ASTRA", left + 16, top + 16, PURPLE, true);
        ctx.drawText(textRenderer, "CLIENT  /  1.21.4", left + 16, top + 34, MUTED, false);
        ctx.drawText(textRenderer, category.toUpperCase(), left + sideW + 18, top + 18, WHITE, true);
        ctx.drawText(textRenderer, "MODULE CONTROL CENTER", left + sideW + 18, top + 36, CYAN, false);
        ctx.drawText(textRenderer, "RIGHT SHIFT  •  FABRIC", left + 14, top + panelH - 20, MUTED, false);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
