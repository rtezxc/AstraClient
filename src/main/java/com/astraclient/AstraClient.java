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
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private static KeyBinding openGuiKey;
    private static boolean alternateAccent;
    private static int guiScale = 100;
    private static long sessionStartedAt = System.currentTimeMillis();

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

    public static Map<String, Boolean> moduleSnapshot() {
        return Map.copyOf(MODULES);
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
        alternateAccent = false;
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
        properties.setProperty("gui.scale", Integer.toString(guiScale));
        for (Map.Entry<String, Integer> entry : MODULE_KEYBINDS.entrySet()) {
            properties.setProperty("keybind." + entry.getKey(), Integer.toString(entry.getValue()));
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

            if (client.player == null || client.world == null) return;

            if (isEnabled("autosprint")
                && client.player.forwardSpeed > 0.0F
                && !client.player.isSneaking()
                && !client.player.horizontalCollision) {
                client.player.setSprinting(true);
            }

            // Refresh the visual aid while enabled. Disabling stops refreshes immediately.
            if (isEnabled("fullbright") && client.player.age % 100 == 0) {
                client.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false)
                );
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
            renderHud(MinecraftClient.getInstance(), drawContext));

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }

    private static void renderHud(MinecraftClient client, DrawContext context) {
        if (client.player == null || client.world == null) return;

        int x = 8;
        int y = 8;
        int accent = accentColor();
        int screenHeight = context.getScaledWindowHeight();

        if (isEnabled("fps")) {
            y = drawHudLine(client, context, x, y, "FPS",
                Integer.toString(client.getCurrentFps()), accent);
        }
        if (isEnabled("coordinates")) {
            var pos = client.player.getBlockPos();
            y = drawHudLine(client, context, x, y, "XYZ",
                pos.getX() + "  " + pos.getY() + "  " + pos.getZ(), accent);
        }
        if (isEnabled("direction")) {
            y = drawHudLine(client, context, x, y, "FACING",
                client.player.getHorizontalFacing().asString().toUpperCase(Locale.ROOT), accent);
        }
        if (isEnabled("ping") && client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) y = drawHudLine(client, context, x, y, "PING",
                entry.getLatency() + " ms", accent);
        }
        if (isEnabled("memory")) {
            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long maxMb = runtime.maxMemory() / (1024L * 1024L);
            y = drawHudLine(client, context, x, y, "RAM", usedMb + " / " + maxMb + " MB", accent);
        }
        if (isEnabled("speedometer")) {
            var velocity = client.player.getVelocity();
            double blocksPerSecond = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
            y = drawHudLine(client, context, x, y, "SPEED",
                String.format(Locale.ROOT, "%.2f b/s", blocksPerSecond), accent);
        }
        if (isEnabled("clock")) {
            y = drawHudLine(client, context, x, y, "LOCAL TIME",
                LocalTime.now().format(CLOCK_FORMAT), accent);
        }
        if (isEnabled("totemcounter")) {
            y = drawHudLine(client, context, x, y, "TOTEMS",
                Integer.toString(countItem(client, Items.TOTEM_OF_UNDYING)), accent);
        }
        if (isEnabled("pearlcounter")) {
            y = drawHudLine(client, context, x, y, "PEARLS",
                Integer.toString(countItem(client, Items.ENDER_PEARL)), accent);
        }
        if (isEnabled("arrowcounter")) {
            int arrows = countItem(client, Items.ARROW) + countItem(client, Items.SPECTRAL_ARROW)
                + countItem(client, Items.TIPPED_ARROW);
            y = drawHudLine(client, context, x, y, "ARROWS", Integer.toString(arrows), accent);
        }
        if (isEnabled("goldencounter")) {
            int apples = countItem(client, Items.GOLDEN_APPLE)
                + countItem(client, Items.ENCHANTED_GOLDEN_APPLE);
            y = drawHudLine(client, context, x, y, "GOLDEN APPLES", Integer.toString(apples), accent);
        }
        if (isEnabled("armorhud")) {
            String armor = armorSummary(client);
            y = drawHudLine(client, context, x, y, "ARMOR", armor, accent);
        }
        if (isEnabled("potionhud")) {
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                if (y + 18 > screenHeight - (isEnabled("keystrokes") ? 100 : 20)) break;
                String name = effect.getEffectType().value().getName().getString();
                String duration = effect.getDuration() < 0 ? "∞" : formatDuration(effect.getDuration());
                y = drawHudLine(client, context, x, y, "EFFECT", name + " " + duration, accent);
            }
        }
        if (isEnabled("targethud") && client.crosshairTarget instanceof EntityHitResult hit) {
            Entity entity = hit.getEntity();
            String targetInfo = entity.getName().getString() + "  "
                + String.format(Locale.ROOT, "%.1f m", client.player.distanceTo(entity));
            if (y + 18 < screenHeight - (isEnabled("keystrokes") ? 100 : 20)) {
                y = drawHudLine(client, context, x, y, "TARGET", targetInfo, accent);
            }
        }
        if (isEnabled("sessionstats")) {
            long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
            String session = String.format(Locale.ROOT, "%02d:%02d:%02d",
                elapsed / 3600, (elapsed / 60) % 60, elapsed % 60);
            if (y + 18 < screenHeight - (isEnabled("keystrokes") ? 100 : 20)) {
                y = drawHudLine(client, context, x, y, "SESSION", session, accent);
            }
        }
        if (isEnabled("keystrokes")) {
            int keyX = 8;
            int keyY = screenHeight - 79;
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

    private static int drawHudLine(
        MinecraftClient client, DrawContext context, int x, int y,
        String label, String value, int accent
    ) {
        String content = label + "  " + value;
        int maxWidth = Math.max(80, Math.min(340, context.getScaledWindowWidth() - x - 8));
        String shown = content;
        while (client.textRenderer.getWidth(shown) > maxWidth - 18 && shown.length() > 5) {
            shown = shown.substring(0, shown.length() - 4) + "...";
        }
        int width = Math.min(maxWidth, client.textRenderer.getWidth(shown) + 16);
        context.fill(x, y, x + width, y + 18, 0xD9101420);
        context.fill(x, y, x + 2, y + 18, accent);
        context.drawText(client.textRenderer, shown, x + 8, y + 5, 0xFFF3F5FF, false);
        return y + 21;
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
