package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class AstraClient implements ClientModInitializer {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("astraclient.properties");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static final Map<String, Integer> MODULE_KEYBINDS = new LinkedHashMap<>();
    private static final Map<String, Boolean> KEY_WAS_DOWN = new LinkedHashMap<>();
    private static final Map<String, Integer> HUD_X = new LinkedHashMap<>();
    private static final Map<String, Integer> HUD_Y = new LinkedHashMap<>();
    private static final String[] HUD_WIDGET_IDS = {
        "fps", "coordinates", "direction", "ping", "memory", "speedometer", "clock",
        "totemcounter", "pearlcounter", "arrowcounter", "goldencounter", "armorhud",
        "potionhud", "targethud", "sessionstats", "keystrokes"
    };
    private static KeyBinding openGuiKey;
    private static boolean alternateAccent;
    private static boolean clickSounds = true;
    private static int guiScale = 100;
    private static long sessionStartedAt = System.currentTimeMillis();
    private static String notification = "Astra Client loaded";
    private static long notificationUntil = System.currentTimeMillis() + 2400L;
    private static boolean fullBrightWasEnabled;
    private static boolean appliedNightVision;
    private static boolean hadNightVisionBeforeEnable;

    static {
        MODULES.put("fullbright", false);
        MODULES.put("autosprint", false);
        MODULES.put("fps", true);
        MODULES.put("coordinates", false);
        MODULES.put("direction", false);
        MODULES.put("keystrokes", false);
        MODULES.put("ping", false);
        MODULES.put("memory", false);
        MODULES.put("speedometer", false);
        MODULES.put("targethud", false);
        MODULES.put("armorhud", false);
        MODULES.put("potionhud", false);
        MODULES.put("clock", false);
        MODULES.put("totemcounter", false);
        MODULES.put("pearlcounter", false);
        MODULES.put("arrowcounter", false);
        MODULES.put("goldencounter", false);
        MODULES.put("sessionstats", false);
        for (String id : MODULES.keySet()) {
            MODULE_KEYBINDS.put(id, GLFW.GLFW_KEY_UNKNOWN);
            KEY_WAS_DOWN.put(id, false);
        }
    }

    public static boolean isEnabled(String id) {
        return MODULES.getOrDefault(id, false);
    }

    public static void toggle(String id) {
        if (!MODULES.containsKey(id)) return;
        boolean value = !MODULES.get(id);
        MODULES.put(id, value);
        notifyUser((value ? "Enabled " : "Disabled ") + readableName(id));
        saveConfig();
    }

    public static void setEnabled(String id, boolean enabled) {
        if (!MODULES.containsKey(id)) return;
        MODULES.put(id, enabled);
        notifyUser((enabled ? "Enabled " : "Disabled ") + readableName(id));
        saveConfig();
    }

    public static Map<String, Boolean> moduleSnapshot() {
        return Map.copyOf(MODULES);
    }

    public static int moduleKeybind(String id) {
        return MODULE_KEYBINDS.getOrDefault(id, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static void setModuleKeybind(String id, int keyCode) {
        if (!MODULES.containsKey(id)) return;
        MODULE_KEYBINDS.put(id, keyCode);
        KEY_WAS_DOWN.put(id, false);
        notifyUser("Keybind " + readableName(id) + ": " + keyName(keyCode));
        saveConfig();
    }

    public static int hudX(String id, int fallback) { return HUD_X.getOrDefault(id, fallback); }
    public static int hudY(String id, int fallback) { return HUD_Y.getOrDefault(id, fallback); }

    public static void setHudPosition(String id, int x, int y) {
        previewHudPosition(id, x, y);
        saveConfig();
    }

    public static void previewHudPosition(String id, int x, int y) {
        boolean known = false;
        for (String candidate : HUD_WIDGET_IDS) {
            if (candidate.equals(id)) { known = true; break; }
        }
        if (!known) return;
        HUD_X.put(id, Math.max(0, x));
        HUD_Y.put(id, Math.max(0, y));
    }

    public static void resetHudPositions() {
        HUD_X.clear();
        HUD_Y.clear();
        saveConfig();
        notifyUser("HUD layout reset");
    }

    public static String[] hudWidgetIds() { return HUD_WIDGET_IDS.clone(); }

    public static String keyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) return Character.toString((char) keyCode);
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) return Character.toString((char) keyCode);
        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25)
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            default -> "KEY-" + keyCode;
        };
    }

    private static String readableName(String id) {
        return switch (id) {
            case "fullbright" -> "FullBright";
            case "autosprint" -> "Auto Sprint";
            case "fps" -> "FPS Counter";
            case "coordinates" -> "Coordinates";
            case "direction" -> "Direction";
            case "keystrokes" -> "Keystrokes";
            case "targethud" -> "Target HUD";
            case "armorhud" -> "Armor HUD";
            case "potionhud" -> "Potion HUD";
            case "totemcounter" -> "Totem Counter";
            case "pearlcounter" -> "Pearl Counter";
            case "arrowcounter" -> "Arrow Counter";
            case "goldencounter" -> "Golden Apple Counter";
            case "sessionstats" -> "Session Stats";
            default -> id;
        };
    }

    public static void notifyUser(String message) {
        notification = message == null ? "" : message;
        notificationUntil = System.currentTimeMillis() + 2500L;
    }

    public static boolean isAlternateAccent() {
        return alternateAccent;
    }

    public static boolean areClickSoundsEnabled() {
        return clickSounds;
    }

    public static void toggleClickSounds() {
        clickSounds = !clickSounds;
        notifyUser("Click sounds " + (clickSounds ? "enabled" : "muted"));
        saveConfig();
    }

    public static void playClickSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (clickSounds && client != null && client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.95F));
        }
    }

    public static void toggleAccent() {
        alternateAccent = !alternateAccent;
        saveConfig();
    }

    public static int accentColor() {
        return alternateAccent ? 0xFF54E8FF : 0xFFAA70FF;
    }

    public static int guiScale() {
        return guiScale;
    }

    public static void setGuiScale(int value) {
        previewGuiScale(value);
        saveConfig();
    }

    public static void previewGuiScale(int value) {
        guiScale = Math.max(75, Math.min(125, value));
    }

    public static void resetModuleDefaults() {
        MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("coordinates"));
        MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
        KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
        HUD_X.clear();
        HUD_Y.clear();
        alternateAccent = false;
        clickSounds = true;
        guiScale = 100;
        notifyUser("Settings reset to defaults");
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
            guiScale = Math.max(75, Math.min(125,
                Integer.parseInt(properties.getProperty("gui.scale", "100"))));
            for (String id : MODULE_KEYBINDS.keySet()) {
                MODULE_KEYBINDS.put(id, Integer.parseInt(
                    properties.getProperty("keybind." + id, Integer.toString(GLFW.GLFW_KEY_UNKNOWN))));
            }
            for (String id : HUD_WIDGET_IDS) {
                String x = properties.getProperty("hud." + id + ".x");
                String y = properties.getProperty("hud." + id + ".y");
                if (x != null) HUD_X.put(id, Integer.parseInt(x));
                if (y != null) HUD_Y.put(id, Integer.parseInt(y));
            }
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
        properties.setProperty("ui.clickSounds", Boolean.toString(clickSounds));
        properties.setProperty("gui.scale", Integer.toString(guiScale));
        for (Map.Entry<String, Integer> entry : MODULE_KEYBINDS.entrySet()) {
            properties.setProperty("keybind." + entry.getKey(), Integer.toString(entry.getValue()));
        }
        for (Map.Entry<String, Integer> entry : HUD_X.entrySet()) {
            properties.setProperty("hud." + entry.getKey() + ".x", Integer.toString(entry.getValue()));
        }
        for (Map.Entry<String, Integer> entry : HUD_Y.entrySet()) {
            properties.setProperty("hud." + entry.getKey() + ".y", Integer.toString(entry.getValue()));
        }
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Astra Client settings");
            }
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not save config: " + exception.getMessage());
        }
    }

    // Convenience accessors kept for source compatibility.
    public static boolean isFullBrightEnabled() { return isEnabled("fullbright"); }
    public static boolean isAutoSprintEnabled() { return isEnabled("autosprint"); }
    public static void setFullBright(boolean enabled) { setEnabled("fullbright", enabled); }
    public static void setAutoSprint(boolean enabled) { setEnabled("autosprint", enabled); }

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

            processModuleKeybinds(client);
            if (client.player == null || client.world == null) return;

            boolean fullBrightEnabled = isEnabled("fullbright");
            if (fullBrightEnabled && !fullBrightWasEnabled) {
                hadNightVisionBeforeEnable = client.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
                appliedNightVision = false;
            } else if (!fullBrightEnabled && fullBrightWasEnabled) {
                if (appliedNightVision && !hadNightVisionBeforeEnable) {
                    client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
                appliedNightVision = false;
                hadNightVisionBeforeEnable = false;
            }
            fullBrightWasEnabled = fullBrightEnabled;

            if (isEnabled("autosprint")
                && client.player.forwardSpeed > 0.0F
                && !client.player.isSneaking()
                && !client.player.horizontalCollision) {
                client.player.setSprinting(true);
            }

            // Refresh only the effect Astra itself needs. If the player already had
            // Night Vision before enabling FullBright, leave that external effect alone.
            if (fullBrightEnabled && client.player.age % 100 == 0 && !hadNightVisionBeforeEnable) {
                client.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false)
                );
                appliedNightVision = true;
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
            renderHud(MinecraftClient.getInstance(), drawContext));

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }

    private static void processModuleKeybinds(MinecraftClient client) {
        if (client.getWindow() == null) return;
        long window = client.getWindow().getHandle();
        boolean screenOpen = client.currentScreen != null;
        for (String id : MODULE_KEYBINDS.keySet()) {
            int code = MODULE_KEYBINDS.getOrDefault(id, GLFW.GLFW_KEY_UNKNOWN);
            boolean down = code != GLFW.GLFW_KEY_UNKNOWN && InputUtil.isKeyPressed(window, code);
            boolean wasDown = KEY_WAS_DOWN.getOrDefault(id, false);
            if (!screenOpen && down && !wasDown) toggle(id);
            KEY_WAS_DOWN.put(id, down);
        }
    }

    private static void renderHud(MinecraftClient client, DrawContext context) {
        if (client.player == null || client.world == null) return;

        int accent = accentColor();
        int screenHeight = context.getScaledWindowHeight();

        if (isEnabled("fps")) drawHudLine(client, context, "fps", 8, 8, "FPS",
            Integer.toString(client.getCurrentFps()), accent);
        if (isEnabled("coordinates")) {
            var pos = client.player.getBlockPos();
            drawHudLine(client, context, "coordinates", 8, 29, "XYZ",
                pos.getX() + "  " + pos.getY() + "  " + pos.getZ(), accent);
        }
        if (isEnabled("direction")) drawHudLine(client, context, "direction", 8, 50, "FACING",
            client.player.getHorizontalFacing().asString().toUpperCase(Locale.ROOT), accent);
        if (isEnabled("ping") && client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) drawHudLine(client, context, "ping", 8, 71, "PING",
                entry.getLatency() + " ms", accent);
        }
        if (isEnabled("memory")) {
            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long maxMb = runtime.maxMemory() / (1024L * 1024L);
            drawHudLine(client, context, "memory", 8, 92, "RAM", usedMb + " / " + maxMb + " MB", accent);
        }
        if (isEnabled("speedometer")) {
            var velocity = client.player.getVelocity();
            double bps = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
            drawHudLine(client, context, "speedometer", 8, 113, "SPEED",
                String.format(Locale.ROOT, "%.2f b/s", bps), accent);
        }
        if (isEnabled("clock")) drawHudLine(client, context, "clock", 8, 134, "LOCAL TIME",
            LocalTime.now().format(CLOCK_FORMAT), accent);
        if (isEnabled("totemcounter")) drawHudLine(client, context, "totemcounter", 8, 155, "TOTEMS",
            Integer.toString(countItem(client, Items.TOTEM_OF_UNDYING)), accent);
        if (isEnabled("pearlcounter")) drawHudLine(client, context, "pearlcounter", 8, 176, "PEARLS",
            Integer.toString(countItem(client, Items.ENDER_PEARL)), accent);
        if (isEnabled("arrowcounter")) {
            int arrows = countItem(client, Items.ARROW) + countItem(client, Items.SPECTRAL_ARROW)
                + countItem(client, Items.TIPPED_ARROW);
            drawHudLine(client, context, "arrowcounter", 8, 197, "ARROWS", Integer.toString(arrows), accent);
        }
        if (isEnabled("goldencounter")) {
            int apples = countItem(client, Items.GOLDEN_APPLE)
                + countItem(client, Items.ENCHANTED_GOLDEN_APPLE);
            drawHudLine(client, context, "goldencounter", 8, 218, "GOLDEN APPLES", Integer.toString(apples), accent);
        }
        if (isEnabled("armorhud")) {
            drawHudLine(client, context, "armorhud", 8, 239, "ARMOR", armorSummary(client), accent);
        }
        if (isEnabled("potionhud")) {
            int effectY = hudY("potionhud", 260);
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                if (effectY + 18 > screenHeight - 20) break;
                String name = effect.getEffectType().value().getName().getString();
                String duration = effect.getDuration() < 0 ? "∞" : formatDuration(effect.getDuration());
                drawHudLineAt(client, context, hudX("potionhud", 8), effectY, "EFFECT",
                    name + " " + duration, accent);
                effectY += 21;
            }
        }
        if (isEnabled("targethud") && client.crosshairTarget instanceof EntityHitResult hit) {
            Entity entity = hit.getEntity();
            String targetInfo = entity.getName().getString() + "  "
                + String.format(Locale.ROOT, "%.1f m", client.player.distanceTo(entity));
            drawHudLine(client, context, "targethud", 8, screenHeight - 26, "TARGET", targetInfo, accent);
        }
        if (isEnabled("sessionstats")) {
            long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
            String session = String.format(Locale.ROOT, "%02d:%02d:%02d",
                elapsed / 3600, (elapsed / 60) % 60, elapsed % 60);
            drawHudLine(client, context, "sessionstats", 8, 302, "SESSION", session, accent);
        }

        if (System.currentTimeMillis() < notificationUntil && !notification.isEmpty()) {
            String label = "ASTRA  •  " + notification;
            int width = Math.min(context.getScaledWindowWidth() - 16, client.textRenderer.getWidth(label) + 28);
            int nx = context.getScaledWindowWidth() - width - 8;
            context.fill(nx, 8, nx + width, 30, 0xE80C111D);
            context.fill(nx, 8, nx + 3, 30, accent);
            context.drawText(client.textRenderer, label, nx + 10, 15, 0xFFF4F5FC, false);
        }

        if (isEnabled("keystrokes")) {
            int keyX = hudX("keystrokes", 8);
            int savedY = hudY("keystrokes", -79);
            int keyY = savedY < 0 ? screenHeight + savedY : savedY;
            drawKey(context, client, client.options.forwardKey, keyX + 22, keyY, "W", accent);
            drawKey(context, client, client.options.leftKey, keyX, keyY + 22, "A", accent);
            drawKey(context, client, client.options.backKey, keyX + 22, keyY + 22, "S", accent);
            drawKey(context, client, client.options.rightKey, keyX + 44, keyY + 22, "D", accent);
            drawKey(context, client, client.options.jumpKey, keyX, keyY + 44, "JUMP", accent);
        }
    }

    private static int countItem(MinecraftClient client, net.minecraft.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    private static String armorSummary(MinecraftClient client) {
        int pieces = 0;
        int lowest = 100;
        for (ItemStack stack : client.player.getArmorItems()) {
            if (stack.isEmpty()) continue;
            pieces++;
            if (stack.isDamageable()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                int percent = Math.round(100.0f * remaining / Math.max(1, stack.getMaxDamage()));
                lowest = Math.min(lowest, percent);
            }
        }
        return pieces == 0 ? "None" : pieces + " pieces" + (lowest < 100 ? " · min " + lowest + "%" : "");
    }

    private static String formatDuration(int ticks) {
        int seconds = Math.max(0, ticks) / 20;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    private static void drawHudLine(MinecraftClient client, DrawContext context, String id,
                                     int defaultX, int defaultY, String label, String value, int accent) {
        int x = hudX(id, defaultX);
        int y = hudY(id, defaultY);
        if (y < 0) y = context.getScaledWindowHeight() + y;
        drawHudLineAt(client, context, x, y, label, value, accent);
    }

    private static void drawHudLineAt(MinecraftClient client, DrawContext context, int x, int y,
                                      String label, String value, int accent) {
        x = Math.max(0, Math.min(context.getScaledWindowWidth() - 50, x));
        y = Math.max(0, Math.min(context.getScaledWindowHeight() - 20, y));
        String text = label + "  " + value;
        int maxWidth = Math.max(80, Math.min(340, context.getScaledWindowWidth() - x - 8));
        while (client.textRenderer.getWidth(text) > maxWidth - 18 && text.length() > 5) {
            text = text.substring(0, text.length() - 4) + "...";
        }
        int width = Math.min(maxWidth, client.textRenderer.getWidth(text) + 16);
        context.fill(x, y, x + width, y + 18, 0xD9101420);
        context.fill(x, y, x + 2, y + 18, accent);
        context.drawText(client.textRenderer, text, x + 8, y + 5, 0xFFF3F5FF, false);
    }

    private static void drawKey(
        DrawContext context, MinecraftClient client, KeyBinding binding,
        int x, int y, String label, int accent
    ) {
        int width = label.length() > 1 ? 46 : 20;
        context.fill(x, y, x + width, y + 19,
            binding.isPressed() ? accent : 0xD9101420);
        context.fill(x, y, x + width, y + 1,
            binding.isPressed() ? 0xFFFFFFFF : 0xFF353D50);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label,
            x + (width - textWidth) / 2, y + 6,
            binding.isPressed() ? 0xFF10131C : 0xFFE9ECF6, false);
    }
}
