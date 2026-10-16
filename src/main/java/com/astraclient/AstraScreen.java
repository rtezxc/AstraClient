package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AstraScreen extends Screen {

    private static final int BACKGROUND = 0xF20B0D16;
    private static final int PANEL = 0xFF121622;
    private static final int CARD = 0xFF191F2D;
    private static final int PURPLE = 0xFF9B5CFF;
    private static final int CYAN = 0xFF45E6FF;
    private static final int WHITE = 0xFFF2F4FF;
    private static final int MUTED = 0xFF8992A8;

    private final List<ModuleButton> modules = new ArrayList<>();
    private String category = "Combat";
    private int scroll = 0;

    private static class ModuleButton {
        String name;
        String description;
        boolean enabled;

        ModuleButton(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public AstraScreen() {
        super(Text.literal("Astra Client"));
        addModules();
    }

    private void addModules() {
        modules.add(new ModuleButton("FullBright",
                "Improves visibility in dark areas"));
        modules.add(new ModuleButton("Sprint",
                "Automatically maintains sprint"));
        modules.add(new ModuleButton("Player ESP",
                "Highlights player entities"));
        modules.add(new ModuleButton("Entity ESP",
                "Highlights nearby entities"));
        modules.add(new ModuleButton("Zoom",
                "Zooms the camera"));
        modules.add(new ModuleButton("Coordinates",
                "Displays your current position"));
        modules.add(new ModuleButton("FPS Counter",
                "Displays frames per second"));
        modules.add(new ModuleButton("Keystrokes",
                "Displays movement keys"));
    }

    @Override
    protected void init() {
        super.init();
        clearChildren();

        int panelWidth = Math.min(650, width - 24);
        int panelHeight = Math.min(390, height - 24);

        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;

        int sidebar = Math.min(145, panelWidth / 3);

        String[] categories = {
                "Combat", "Render", "Movement",
                "Player", "World", "HUD", "Settings"
        };

        int categoryY = top + 80;

        for (String name : categories) {
            final String selected = name;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal((category.equals(name) ? "◆  " : "   ") + name),
                    button -> {
                        category = selected;
                        scroll = 0;
                        clearAndInit();
                    }
            ).dimensions(
                    left + 10,
                    categoryY,
                    sidebar - 20,
                    25
            ).build());

            categoryY += 30;
        }

        int contentX = left + sidebar + 14;
        int contentWidth = panelWidth - sidebar - 28;

        int y = top + 105;

        for (ModuleButton module : modules) {
            if (y > top + panelHeight - 48) break;

            final ModuleButton selected = module;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(
                            module.name + "     " +
                            (module.enabled ? "ON" : "OFF")
                    ),
                    button -> {
                        selected.enabled = !selected.enabled;
                        clearAndInit();
                    }
            ).dimensions(
                    contentX,
                    y,
                    contentWidth,
                    28
            ).build());

            y += 42;
        }

        addDrawableChild(ButtonWidget.builder(
                Text.literal("CLOSE"),
                button -> close()
        ).dimensions(
                left + panelWidth - 85,
                top + panelHeight - 34,
                72,
                22
        ).build());
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelWidth = Math.min(650, width - 24);
        int panelHeight = Math.min(390, height - 24);

        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;

        int sidebar = Math.min(145, panelWidth / 3);

        context.fill(
                left - 3, top - 3,
                left + panelWidth + 3,
                top + panelHeight + 3,
                0x664B38A0
        );

        context.fill(
                left, top,
                left + panelWidth,
                top + panelHeight,
                BACKGROUND
        );

        context.fill(
                left, top,
                left + panelWidth,
                top + 3,
                PURPLE
        );

        context.fill(
                left, top,
                left + sidebar,
                top + panelHeight,
                PANEL
        );

        context.fill(
                left + sidebar, top + 50,
                left + panelWidth,
                top + 51,
                0xFF292E40
        );

        context.drawText(
                textRenderer,
                "ASTRA",
                left + 16,
                top + 18,
                PURPLE,
                true
        );

        context.drawText(
                textRenderer,
                "CLIENT  /  1.21.4",
                left + 16,
                top + 36,
                MUTED,
                false
        );

        context.drawText(
                textRenderer,
                category.toUpperCase(),
                left + sidebar + 14,
                top + 20,
                WHITE,
                true
        );

        context.drawText(
                textRenderer,
                "MODULE CONTROL CENTER",
                left + sidebar + 14,
                top + 36,
                CYAN,
                false
        );

        context.drawText(
                textRenderer,
                "ASTRA CLIENT  •  FABRIC",
                left + 16,
                top + panelHeight - 22,
                MUTED,
                false
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
