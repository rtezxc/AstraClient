package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class AstraClient implements ClientModInitializer {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("astraclient.properties");

    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static KeyBinding openGuiKey;
    private static boolean alternateAccent;

    static {
        MODULES.put("fullbright", false);
        MODULES.put("autosprint", false);
        MODULES.put("fps", true);
        MODULES.put("coordinates", false);
        MODULES.put("direction", false);
        MODULES.put("keystrokes", false);
    }

    public static boolean isEnabled(String id) {
        return MODULES.getOrDefault(id, false);
    }

    public static void toggle(String id) {
        if (!MODULES.containsKey(id)) return;
        MODULES.put(id, !MODULES.get(id));
        saveConfig();
    }

    public static void setEnabled(String id, boolean enabled) {
        if (!MODULES.containsKey(id)) return;
        MODULES.put(id, enabled);
        saveConfig();
    }

    public static boolean isAlternateAccent() {
        return alternateAccent;
    }

    public static void toggleAccent() {
        alternateAccent = !alternateAccent;
        saveConfig();
    }

    public static int accentColor() {
        return alternateAccent ? 0xFF54E8FF : 0xFFAA70FF;
    }

    // Kept as convenience accessors for older AstraScreen versions.
    public static boolean isFullBrightEnabled() {
        return isEnabled("fullbright");
    }

    public static boolean isAutoSprintEnabled() {
        return isEnabled("autosprint");
    }

    public static void setFullBright(boolean enabled) {
        setEnabled("fullbright", enabled);
    }

    public static void setAutoSprint(boolean enabled) {
        setEnabled("autosprint", enabled);
    }

    public static void resetModuleDefaults() {
        MODULES.replaceAll((id, ignored) -> id.equals("fps"));
        saveConfig();
    }

    private static void loadConfig() {
        if (!Files.isRegularFile(CONFIG_PATH)) return;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
            for (String id : MODULES.keySet()) {
                MODULES.put(id, Boolean.parseBoolean(
                    properties.getProperty("module." + id, Boolean.toString(MODULES.get(id)))
                ));
            }
            alternateAccent = Boolean.parseBoolean(
                properties.getProperty("theme.alternateAccent", "false")
            );
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not load config: " + exception.getMessage());
        }
    }

    public static void saveConfig() {
        Properties properties = new Properties();
        for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
            properties.setProperty("module." + entry.getKey(), Boolean.toString(entry.getValue()));
        }
        properties.setProperty("theme.alternateAccent", Boolean.toString(alternateAccent));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Astra Client settings");
            }
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not save config: " + exception.getMessage());
        }
    }

    @Override
    public void onInitializeClient() {
        loadConfig();

        openGuiKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.astraclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.astraclient"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new AstraScreen());
                } else if (client.currentScreen instanceof AstraScreen) {
                    client.setScreen(null);
                }
            }

            if (client.player == null || client.world == null) return;

            if (isEnabled("autosprint")
                && client.player.forwardSpeed > 0.0F
                && !client.player.isSneaking()
                && !client.player.horizontalCollision) {
                client.player.setSprinting(true);
            }

            // Refresh Night Vision only while FullBright is enabled.
            if (isEnabled("fullbright") && client.player.age % 100 == 0) {
                client.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false)
                );
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(
            MinecraftClient.getInstance(), drawContext
        ));

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }

    private static void renderHud(MinecraftClient client, DrawContext context) {
        if (client.player == null) return;

        int x = 8;
        int y = 8;
        int accent = accentColor();

        if (isEnabled("fps")) {
            drawHudLine(client, context, x, y, "FPS", Integer.toString(client.getCurrentFps()), accent);
            y += 22;
        }

        if (isEnabled("coordinates")) {
            BlockPos pos = client.player.getBlockPos();
            drawHudLine(client, context, x, y, "XYZ",
                pos.getX() + "  " + pos.getY() + "  " + pos.getZ(), accent);
            y += 22;
        }

        if (isEnabled("direction")) {
            String direction = client.player.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);
            drawHudLine(client, context, x, y, "FACING", direction, accent);
        }

        if (isEnabled("keystrokes")) {
            int keyX = 8;
            int keyY = 82;
            drawKey(context, client, client.options.forwardKey, keyX + 22, keyY, "W", accent);
            drawKey(context, client, client.options.leftKey, keyX, keyY + 22, "A", accent);
            drawKey(context, client, client.options.backKey, keyX + 22, keyY + 22, "S", accent);
            drawKey(context, client, client.options.rightKey, keyX + 44, keyY + 22, "D", accent);
            drawKey(context, client, client.options.jumpKey, keyX, keyY + 44, "JUMP", accent);
        }
    }

    private static void drawHudLine(
        MinecraftClient client, DrawContext context, int x, int y,
        String label, String value, int accent
    ) {
        String content = label + "  " + value;
        int width = client.textRenderer.getWidth(content) + 18;
        context.fill(x, y, x + width, y + 18, 0xD9101420);
        context.fill(x, y, x + 2, y + 18, accent);
        context.drawText(client.textRenderer, label, x + 7, y + 5, accent, false);
        context.drawText(client.textRenderer, value,
            x + 10 + client.textRenderer.getWidth(label), y + 5, 0xFFF3F5FF, false);
    }

    private static void drawKey(
        DrawContext context, MinecraftClient client, KeyBinding binding,
        int x, int y, String label, int accent
    ) {
        int width = label.length() > 1 ? 46 : 20;
        context.fill(x, y, x + width, y + 19, binding.isPressed() ? accent : 0xD9101420);
        context.fill(x, y, x + width, y + 1, binding.isPressed() ? 0xFFFFFFFF : 0xFF353D50);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label,
            x + (width - textWidth) / 2, y + 6,
            binding.isPressed() ? 0xFF10131C : 0xFFE9ECF6, false);
    }
}
