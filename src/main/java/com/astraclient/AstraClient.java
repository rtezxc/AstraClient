package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.Hand;
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
    private static final Map<UUID, GlowState> ORIGINAL_ENTITY_GLOW = new HashMap<>();
    private static Object lastGlowWorld;
    private static final Map<String, Integer> HUD_X = new LinkedHashMap<>();
    private static final Map<String, Integer> HUD_Y = new LinkedHashMap<>();
    private static final String[] HUD_WIDGET_IDS = {
        "fps", "coordinates", "direction", "ping", "memory", "speedometer", "clock",
        "totemcounter", "pearlcounter", "arrowcounter", "goldencounter", "armorhud",
        "potionhud", "targethud", "sessionstats", "keystrokes", "biome", "cps", "worldinfo", "potioncounter", "durabilityhud", "inventoryhud", "healthhud", "hungerhud", "blockreachhud", "frametimehud"
    };
    private record GlowState(Entity entity, boolean wasGlowing) { }

    private static KeyBinding openGuiKey;
    private static int accentTheme;
    private static boolean clickSounds = true;
    private static boolean autoSprintGroundOnly = true;
    private static boolean autoSprintDisableInMenus = true;
    private static int guiScale = 100;
    private static int hudOpacity = 84;
    private static long sessionStartedAt = System.currentTimeMillis();
    private static String notification = "Astra Client loaded";
    private static long notificationUntil = System.currentTimeMillis() + 2400L;
    private static boolean fullBrightWasEnabled;
    private static boolean appliedNightVision;
    private static boolean hadNightVisionBeforeEnable;
    private static int zoomFov = 30;
    private static int espRange = 64;
    private static int combatRange = 4;
    private static boolean targetPlayers = true;
    private static boolean targetHostile = true;
    private static boolean targetPassive = false;
    private static int originalFov = 70;
    private static boolean zoomOverrideActive;
    private static boolean forcedSafeWalkSneak;
    private static boolean attackMouseWasDown;
    private static boolean useMouseWasDown;
    private static int autoToolOriginalSlot = -1;
    private static int autoToolSelectedSlot = -1;
    private static PlayerEntity flyPlayer;
    private static boolean flyWasActive;
    private static final Deque<Long> CLICK_TIMESTAMPS = new ArrayDeque<>();

    static {
        MODULES.put("fullbright", false);
        MODULES.put("customcrosshair", false);
        MODULES.put("playeresp", false);
        MODULES.put("mobesp", false);
        MODULES.put("itemesp", false);
        MODULES.put("zoom", false);
        MODULES.put("autosprint", false);
        MODULES.put("sprint", false);
        MODULES.put("fly", false);
        MODULES.put("autowalk", false);
        MODULES.put("safewalk", false);
        MODULES.put("eagle", false);
        MODULES.put("inventorymove", false);
        MODULES.put("killaura", false);
        MODULES.put("triggerbot", false);
        MODULES.put("autotool", false);
        MODULES.put("autoweapon", false);
        MODULES.put("biome", false);
        MODULES.put("cps", false);
        MODULES.put("worldinfo", false);
        MODULES.put("potioncounter", false);
        MODULES.put("durabilityhud", false);
        MODULES.put("inventoryhud", false);
        MODULES.put("healthhud", false);
        MODULES.put("hungerhud", false);
        MODULES.put("blockreachhud", false);
        MODULES.put("frametimehud", false);
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
        if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
            for (Map.Entry<String, Integer> entry : MODULE_KEYBINDS.entrySet()) {
                if (!entry.getKey().equals(id) && entry.getValue() == keyCode) {
                    MODULE_KEYBINDS.put(entry.getKey(), GLFW.GLFW_KEY_UNKNOWN);
                    KEY_WAS_DOWN.put(entry.getKey(), false);
                }
            }
        }
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
            case "customcrosshair" -> "Custom Crosshair";
            case "autosprint" -> "Auto Sprint";
            case "sprint" -> "Sprint";
            case "fly" -> "Fly";
            case "autowalk" -> "Auto Walk";
            case "safewalk" -> "Safe Walk";
            case "eagle" -> "Eagle";
            case "inventorymove" -> "Inventory Move";
            case "killaura" -> "Kill Aura";
            case "triggerbot" -> "Critical TriggerBot";
            case "autotool" -> "Auto Tool";
            case "autoweapon" -> "Auto Weapon";
            case "biome" -> "Biome Display";
            case "cps" -> "CPS Counter";
            case "worldinfo" -> "World Information";
            case "potioncounter" -> "Potion Counter";
            case "durabilityhud" -> "Durability HUD";
            case "inventoryhud" -> "Inventory HUD";
            case "healthhud" -> "Health HUD";
            case "hungerhud" -> "Hunger HUD";
            case "blockreachhud" -> "Block Reach HUD";
            case "frametimehud" -> "Frame Time HUD";
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
        return accentTheme == 1;
    }

    public static String accentName() {
        return switch (accentTheme) {
            case 1 -> "CYAN";
            case 2 -> "AMBER";
            case 3 -> "MAGENTA";
            default -> "VIOLET";
        };
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
        accentTheme = (accentTheme + 1) % 4;
        notifyUser("Theme: " + accentName());
        saveConfig();
    }

    public static int accentColor() {
        return switch (accentTheme) {
            case 1 -> 0xFF54E8FF;
            case 2 -> 0xFFFFC66E;
            case 3 -> 0xFFFF6FAE;
            default -> 0xFFAA70FF;
        };
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

    public static int zoomFov() { return zoomFov; }

    public static void setZoomFov(int value) {
        zoomFov = Math.max(10, Math.min(60, value));
        notifyUser("Zoom FOV: " + zoomFov);
        saveConfig();
    }

    public static void previewZoomFov(int value) {
        zoomFov = Math.max(10, Math.min(60, value));
    }

    public static int espRange() { return espRange; }
    public static int combatRange() { return combatRange; }
    public static boolean targetPlayers() { return targetPlayers; }
    public static boolean targetHostile() { return targetHostile; }
    public static boolean targetPassive() { return targetPassive; }
    public static void previewCombatRange(int value) { combatRange = Math.max(3, Math.min(6, value)); }
    public static void toggleCombatTarget(String kind) {
        switch (kind) {
            case "players" -> targetPlayers = !targetPlayers;
            case "hostile" -> targetHostile = !targetHostile;
            case "passive" -> targetPassive = !targetPassive;
            default -> { return; }
        }
        notifyUser("Kill Aura target filter updated");
        saveConfig();
    }

    public static void setEspRange(int value) {
        espRange = Math.max(16, Math.min(128, value));
        notifyUser("ESP range: " + espRange + " blocks");
        saveConfig();
    }

    public static void previewEspRange(int value) {
        espRange = Math.max(16, Math.min(128, value));
    }

    public static int hudOpacity() {
        return hudOpacity;
    }

    public static void setHudOpacity(int value) {
        hudOpacity = Math.max(35, Math.min(100, value));
        saveConfig();
    }

    public static int previewHudOpacity(int value) {
        hudOpacity = Math.max(35, Math.min(100, value));
        return hudOpacity;
    }

    public static void resetModuleDefaults() {
        MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("coordinates"));
        MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
        KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
        HUD_X.clear();
        HUD_Y.clear();
        accentTheme = 0;
        clickSounds = true;
        autoSprintGroundOnly = true;
        autoSprintDisableInMenus = true;
        guiScale = 100;
        hudOpacity = 84;
        zoomFov = 30;
        espRange = 64;
        combatRange = 4;
        targetPlayers = true;
        targetHostile = true;
        targetPassive = false;
        notifyUser("Settings reset to defaults");
        saveConfig();
    }

    private static void loadConfig() {
        if (!Files.isRegularFile(CONFIG_PATH)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
            clickSounds = Boolean.parseBoolean(properties.getProperty("ui.clickSounds", "true"));
            hudOpacity = Math.max(20, Math.min(100, Integer.parseInt(properties.getProperty("hud.opacity", "90"))));
            autoSprintGroundOnly = Boolean.parseBoolean(properties.getProperty("autosprint.groundOnly", "false"));
            autoSprintDisableInMenus = Boolean.parseBoolean(properties.getProperty("autosprint.disableInMenus", "true"));
            for (String id : MODULES.keySet()) {
                MODULES.put(id, Boolean.parseBoolean(
                    properties.getProperty("module." + id, Boolean.toString(MODULES.get(id)))
                ));
            }
            accentTheme = Math.max(0, Math.min(3, Integer.parseInt(
                properties.getProperty("theme.accentTheme",
                    Boolean.parseBoolean(properties.getProperty("theme.alternateAccent", "false")) ? "1" : "0")
            )));
            guiScale = Math.max(75, Math.min(125,
                Integer.parseInt(properties.getProperty("gui.scale", "100"))));
            zoomFov = Math.max(10, Math.min(60,
                Integer.parseInt(properties.getProperty("zoom.fov", "30"))));
            espRange = Math.max(16, Math.min(128,
                Integer.parseInt(properties.getProperty("esp.range", "64"))));
            combatRange = Math.max(3, Math.min(6,
                Integer.parseInt(properties.getProperty("combat.range", "4"))));
            targetPlayers = Boolean.parseBoolean(properties.getProperty("combat.targetPlayers", "true"));
            targetHostile = Boolean.parseBoolean(properties.getProperty("combat.targetHostile", "true"));
            targetPassive = Boolean.parseBoolean(properties.getProperty("combat.targetPassive", "false"));
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
        properties.setProperty("theme.accentTheme", Integer.toString(accentTheme));
        properties.setProperty("ui.clickSounds", Boolean.toString(clickSounds));
        properties.setProperty("autosprint.groundOnly", Boolean.toString(autoSprintGroundOnly));
        properties.setProperty("autosprint.disableInMenus", Boolean.toString(autoSprintDisableInMenus));
        properties.setProperty("gui.scale", Integer.toString(guiScale));
        properties.setProperty("hud.opacity", Integer.toString(hudOpacity));
        properties.setProperty("zoom.fov", Integer.toString(zoomFov));
        properties.setProperty("esp.range", Integer.toString(espRange));
        properties.setProperty("combat.range", Integer.toString(combatRange));
        properties.setProperty("combat.targetPlayers", Boolean.toString(targetPlayers));
        properties.setProperty("combat.targetHostile", Boolean.toString(targetHostile));
        properties.setProperty("combat.targetPassive", Boolean.toString(targetPassive));
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
    public static boolean autoSprintGroundOnly() { return autoSprintGroundOnly; }
    public static boolean autoSprintDisableInMenus() { return autoSprintDisableInMenus; }
    public static void toggleAutoSprintGroundOnly() {
        autoSprintGroundOnly = !autoSprintGroundOnly;
        notifyUser("Auto Sprint ground-only: " + autoSprintGroundOnly);
        saveConfig();
    }
    public static void toggleAutoSprintDisableInMenus() {
        autoSprintDisableInMenus = !autoSprintDisableInMenus;
        notifyUser("Auto Sprint pauses in menus: " + autoSprintDisableInMenus);
        saveConfig();
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

            processModuleKeybinds(client);
            updateInventoryMove(client);
            updateAutoWalk(client);
            updateCombatModules(client);
            updateAutoWeapon(client);
            updateAutoTool(client);
            updateFly(client);
            long windowHandle = client.getWindow() == null ? 0L : client.getWindow().getHandle();
            if (windowHandle != 0L) {
                long now = System.currentTimeMillis();
                boolean attackDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(windowHandle, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                boolean useDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(windowHandle, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (attackDown && !attackMouseWasDown) CLICK_TIMESTAMPS.addLast(now);
                if (useDown && !useMouseWasDown) CLICK_TIMESTAMPS.addLast(now);
                attackMouseWasDown = attackDown;
                useMouseWasDown = useDown;
                while (!CLICK_TIMESTAMPS.isEmpty() && now - CLICK_TIMESTAMPS.peekFirst() >= 1000L) {
                    CLICK_TIMESTAMPS.removeFirst();
                }
            }
            updateEsp(client);
            updateZoom(client);
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

            boolean movingForward = client.player.forwardSpeed > 0.0F;
            boolean sprintAllowed = movingForward && !client.player.isSneaking()
                && !client.player.horizontalCollision
                && (!autoSprintGroundOnly || client.player.isOnGround())
                && (!autoSprintDisableInMenus || client.currentScreen == null);
            if ((isEnabled("autosprint") || isEnabled("sprint")) && sprintAllowed) {
                client.player.setSprinting(true);
            }

            // Safe Walk: use vanilla sneak-edge protection only while the player is
            // moving over a ledge. Never override the user's own sneak input.
            boolean userSneaking = client.options.sneakKey.isPressed();
            boolean moving = Math.abs(client.player.forwardSpeed) > 0.01F || Math.abs(client.player.sidewaysSpeed) > 0.01F;
            boolean nearEdge = (isEnabled("safewalk") && client.player.isOnGround()
                && moving && client.world.isSpaceEmpty(client.player, client.player.getBoundingBox()
                    .offset(client.player.getVelocity().x, -0.6D, client.player.getVelocity().z)))
                || (isEnabled("eagle") && client.player.isOnGround() && moving
                    && client.world.isSpaceEmpty(client.player, client.player.getBoundingBox().offset(0.0D, -0.1D, 0.0D)));

            if (nearEdge) {
                client.player.setSneaking(true);
                forcedSafeWalkSneak = true;
            } else if (forcedSafeWalkSneak) {
                if (!userSneaking) client.player.setSneaking(false);
                forcedSafeWalkSneak = false;
            }

            // Auto Tool: while mining a block, select the hotbar tool with the
            // highest vanilla mining-speed multiplier for that block.
            if (isEnabled("autotool") && client.options.attackKey.isPressed()
                && client.crosshairTarget instanceof BlockHitResult blockHit) {
                var state = client.world.getBlockState(blockHit.getBlockPos());
                int bestSlot = client.player.getInventory().selectedSlot;
                float bestSpeed = client.player.getInventory().getStack(bestSlot).getMiningSpeedMultiplier(state);
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack stack = client.player.getInventory().getStack(slot);
                    float speed = stack.getMiningSpeedMultiplier(state);
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestSlot = slot;
                    }
                }
                client.player.getInventory().selectedSlot = bestSlot;
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
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> restoreZoom(client));

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }

    /**
     * Basic local combat automation. TriggerBot deliberately takes priority over
     * Kill Aura: while it is enabled, it attacks only the entity under the
     * crosshair and only when vanilla critical-hit conditions are satisfied.
     */
    private static void updateCombatModules(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null
            || client.currentScreen != null || client.player.isSpectator()) return;

        if (isEnabled("triggerbot")) {
            if (client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target
                && isValidCombatTarget(target, client.player)
                && client.player.distanceTo(target) <= combatRange
                && isCriticalHitReady(client.player)) {
                attackTarget(client, target);
            }
            return;
        }

        if (!isEnabled("killaura")) return;
        LivingEntity bestTarget = null;
        double bestDistance = combatRange;
        var look = client.player.getRotationVec(1.0F);
        var eye = client.player.getEyePos();

        for (LivingEntity candidate : client.world.getEntitiesByClass(
            LivingEntity.class, client.player.getBoundingBox().expand(combatRange),
            entity -> isValidCombatTarget(entity, client.player))) {
            double distance = client.player.distanceTo(candidate);
            if (distance > bestDistance) continue;
            var direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            // Avoid attacking entities behind the player; a broad cone keeps this usable.
            if (look.dotProduct(direction) < 0.15D) continue;
            bestTarget = candidate;
            bestDistance = distance;
        }

        if (bestTarget != null) attackTarget(client, bestTarget);
    }

    /**
     * Selects the fastest hotbar item while the player is actively mining a targeted block.
     * Restores the previous slot when mining stops, unless the player changed slots manually.
     */
    private static void updateAutoTool(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null
            || !isEnabled("autotool") || !client.options.attackKey.isPressed()
            || !(client.crosshairTarget instanceof BlockHitResult hit)
            || hit.getType() != HitResult.Type.BLOCK) {
            restoreAutoToolSlot(client);
            return;
        }

        var state = client.world.getBlockState(hit.getBlockPos());
        if (state.isAir()) {
            restoreAutoToolSlot(client);
            return;
        }

        int bestSlot = client.player.getInventory().selectedSlot;
        float bestSpeed = client.player.getInventory().getStack(bestSlot).getMiningSpeedMultiplier(state);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (stack.isSuitableFor(state)) speed += 1000.0F;
            ItemStack current = client.player.getInventory().getStack(bestSlot);
            float currentSpeed = current.getMiningSpeedMultiplier(state)
                + (current.isSuitableFor(state) ? 1000.0F : 0.0F);
            if (speed > currentSpeed) {
                bestSlot = slot;
                bestSpeed = speed;
            }
        }

        if (autoToolOriginalSlot < 0) autoToolOriginalSlot = client.player.getInventory().selectedSlot;
        if (bestSlot != client.player.getInventory().selectedSlot) {
            client.player.getInventory().selectedSlot = bestSlot;
            autoToolSelectedSlot = bestSlot;
        }
    }

    private static void restoreAutoToolSlot(MinecraftClient client) {
        if (autoToolOriginalSlot < 0) return;
        if (client.player != null && autoToolSelectedSlot >= 0
            && client.player.getInventory().selectedSlot == autoToolSelectedSlot) {
            client.player.getInventory().selectedSlot = Math.max(0, Math.min(8, autoToolOriginalSlot));
        }
        autoToolOriginalSlot = -1;
        autoToolSelectedSlot = -1;
    }

    /**
     * Selects the best available sword, then axe, only while aiming at a living target.
     * Does not move items or alter inventory contents.
     */
    private static void updateAutoWeapon(MinecraftClient client) {
        if (!isEnabled("autoweapon") || client.player == null || client.world == null
            || client.currentScreen != null || !(client.crosshairTarget instanceof EntityHitResult hit)
            || !(hit.getEntity() instanceof LivingEntity target) || !target.isAlive()
            || target == client.player) return;

        int bestSlot = -1;
        int bestScore = 0;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            int score = stack.getItem() instanceof SwordItem ? 3
                : stack.getItem() instanceof AxeItem ? 2 : 0;
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        if (bestSlot >= 0) client.player.getInventory().selectedSlot = bestSlot;
    }

    private static boolean isValidCombatTarget(LivingEntity target, PlayerEntity self) {
        if (target == self || !target.isAlive() || target.isSpectator()) return false;
        if (target instanceof PlayerEntity) return targetPlayers;
        return target instanceof HostileEntity ? targetHostile : targetPassive;
    }

    private static boolean isCriticalHitReady(PlayerEntity player) {
        return !player.isOnGround()
            && player.fallDistance > 0.0F
            && player.getVelocity().y < 0.0D
            && !player.isClimbing()
            && !player.isTouchingWater()
            && !player.hasVehicle()
            && !player.isSprinting()
            && !player.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    private static void attackTarget(MinecraftClient client, LivingEntity target) {
        if (client.player == null || client.interactionManager == null
            || client.player.getAttackCooldownProgress(0.0F) < 0.9F) return;
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    /** Lets keyboard movement bindings remain active while the vanilla inventory is open. */
    /** Simple client-side flight: jump rises, sneak descends, neutral input holds altitude. */
    private static boolean autoWalkWasForcing;
    private static void updateAutoWalk(MinecraftClient client) {
        if (client.options == null) return;
        boolean enabled = isEnabled("autowalk") && client.player != null
            && client.world != null && client.currentScreen == null;
        if (enabled) {
            client.options.forwardKey.setPressed(true);
            autoWalkWasForcing = true;
        } else if (autoWalkWasForcing) {
            long window = client.getWindow() == null ? 0L : client.getWindow().getHandle();
            if (window != 0L) setPhysicalKey(client, client.options.forwardKey, window);
            else client.options.forwardKey.setPressed(false);
            autoWalkWasForcing = false;
        }
    }
    private static void updateFly(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) {
            if (flyPlayer != null && flyWasActive) flyPlayer.setNoGravity(false);
            flyPlayer = null;
            flyWasActive = false;
            return;
        }

        boolean enabled = isEnabled("fly") && client.world != null;
        if (flyPlayer != null && flyPlayer != player && flyWasActive) {
            flyPlayer.setNoGravity(false);
        }
        flyPlayer = player;

        if (!enabled) {
            if (flyWasActive) {
                player.setNoGravity(false);
                flyWasActive = false;
            }
            return;
        }

        player.setNoGravity(true);
        flyWasActive = true;
        double vertical = 0.0D;
        if (client.options.jumpKey.isPressed()) vertical = 0.42D;
        else if (client.options.sneakKey.isPressed()) vertical = -0.42D;
        var velocity = player.getVelocity();
        player.setVelocity(velocity.x, vertical, velocity.z);
    }

    private static void updateInventoryMove(MinecraftClient client) {
        if (client.player == null || !isEnabled("inventorymove")
            || !(client.currentScreen instanceof InventoryScreen)) return;
        long window = client.getWindow().getHandle();
        setPhysicalKey(client, client.options.forwardKey, window);
        setPhysicalKey(client, client.options.backKey, window);
        setPhysicalKey(client, client.options.leftKey, window);
        setPhysicalKey(client, client.options.rightKey, window);
        setPhysicalKey(client, client.options.jumpKey, window);
        setPhysicalKey(client, client.options.sneakKey, window);
        setPhysicalKey(client, client.options.sprintKey, window);
    }

    private static void setPhysicalKey(MinecraftClient client, KeyBinding binding, long window) {
        // Yarn 1.21.4 exposes the key through matchesKey rather than getBoundKey().
        // Find the key assigned to this binding, then query its physical state.
        for (int code = GLFW.GLFW_KEY_SPACE; code <= GLFW.GLFW_KEY_LAST; code++) {
            if (binding.matchesKey(code, 0)) {
                binding.setPressed(InputUtil.isKeyPressed(window, code));
                return;
            }
        }
        binding.setPressed(false);
    }

    private static void updateZoom(MinecraftClient client) {
        if (client.options == null) return;
        if (isEnabled("zoom")) {
            if (!zoomOverrideActive) {
                originalFov = client.options.getFov().getValue();
                zoomOverrideActive = true;
            }
            if (client.options.getFov().getValue() != zoomFov) {
                client.options.getFov().setValue(zoomFov);
            }
        } else if (zoomOverrideActive) {
            client.options.getFov().setValue(originalFov);
            zoomOverrideActive = false;
        }
    }

    private static void restoreZoom(MinecraftClient client) {
        if (zoomOverrideActive && client.options != null) {
            client.options.getFov().setValue(originalFov);
        }
        zoomOverrideActive = false;
        saveConfig();
    }

    /**
     * Local client outlines for selected entity groups. Original glow flags are remembered
     * and restored when modules are disabled; no entity state is sent to the server.
     */
    private static void updateEsp(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            ORIGINAL_ENTITY_GLOW.clear();
            lastGlowWorld = null;
            return;
        }
        if (lastGlowWorld != client.world) {
            ORIGINAL_ENTITY_GLOW.clear();
            lastGlowWorld = client.world;
        }

        boolean playerEsp = isEnabled("playeresp");
        boolean mobEsp = isEnabled("mobesp");
        boolean itemEsp = isEnabled("itemesp");
        if (!playerEsp && !mobEsp && !itemEsp && ORIGINAL_ENTITY_GLOW.isEmpty()) return;

        List<Entity> candidates = new ArrayList<>();
        candidates.addAll(client.world.getPlayers());

        Box nearby = client.player.getBoundingBox().expand(espRange);
        // When a visual module is disabled, continue fetching nearby entities briefly so
        // their original glow flags can be restored cleanly.
        boolean restoringTrackedEntities = !ORIGINAL_ENTITY_GLOW.isEmpty();
        if (mobEsp || restoringTrackedEntities) {
            candidates.addAll(client.world.getEntitiesByClass(LivingEntity.class, nearby,
                entity -> entity.isAlive() && !(entity instanceof PlayerEntity)));
        }
        if (itemEsp || restoringTrackedEntities) {
            candidates.addAll(client.world.getEntitiesByClass(ItemEntity.class, nearby,
                entity -> !entity.isRemoved()));
        }

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : candidates) {
            if (entity == client.player || entity.isRemoved()) continue;
            boolean desired = entity instanceof PlayerEntity
                ? playerEsp && client.player.squaredDistanceTo(entity) <= (double) espRange * espRange
                : (entity instanceof LivingEntity && mobEsp)
                    || (entity instanceof ItemEntity && itemEsp);
            UUID uuid = entity.getUuid();
            seen.add(uuid);
            if (desired) {
                ORIGINAL_ENTITY_GLOW.putIfAbsent(uuid, new GlowState(entity, entity.isGlowing()));
                if (!entity.isGlowing()) entity.setGlowing(true);
            } else {
                GlowState original = ORIGINAL_ENTITY_GLOW.remove(uuid);
                if (original != null && entity.isGlowing() != original.wasGlowing()) {
                    entity.setGlowing(original.wasGlowing());
                }
            }
        }

        // Restore entities that walked out of the active radius, and discard stale references.
        var iterator = ORIGINAL_ENTITY_GLOW.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GlowState> entry = iterator.next();
            if (seen.contains(entry.getKey())) continue;
            GlowState original = entry.getValue();
            Entity previousEntity = original.entity();
            if (!previousEntity.isRemoved() && previousEntity.isGlowing() != original.wasGlowing()) {
                previousEntity.setGlowing(original.wasGlowing());
            }
            iterator.remove();
        }
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

        // Minimal custom crosshair overlay; work is done only while the module is enabled.
        if (isEnabled("customcrosshair")) {
            int cx = context.getScaledWindowWidth() / 2;
            int cy = screenHeight / 2;
            int color = accent;
            context.fill(cx - 1, cy - 7, cx + 1, cy - 3, color);
            context.fill(cx - 1, cy + 3, cx + 1, cy + 7, color);
            context.fill(cx - 7, cy - 1, cx - 3, cy + 1, color);
            context.fill(cx + 3, cy - 1, cx + 7, cy + 1, color);
            context.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
        }

        if (isEnabled("healthhud")) {
            float health = Math.max(0.0F, client.player.getHealth());
            float maxHealth = Math.max(1.0F, client.player.getMaxHealth());
            drawHudLine(client, context, "healthhud", 8, screenHeight - 42, "HEALTH",
                String.format(Locale.ROOT, "%.1f / %.1f HP", health, maxHealth), accent);
        }
        if (isEnabled("hungerhud")) {
            int food = client.player.getHungerManager().getFoodLevel();
            float saturation = client.player.getHungerManager().getSaturationLevel();
            drawHudLine(client, context, "hungerhud", 8, screenHeight - 21, "HUNGER",
                food + " / 20 · " + String.format(Locale.ROOT, "%.1f sat", saturation), accent);
        }
        if (isEnabled("blockreachhud")) {
            String reach = client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK
                ? String.format(Locale.ROOT, "%.2f blocks", client.player.getEyePos().distanceTo(client.crosshairTarget.getPos()))
                : "No block targeted";
            drawHudLine(client, context, "blockreachhud", 8, 491, "BLOCK REACH", reach, accent);
        }
        if (isEnabled("frametimehud")) {
            int fps = client.getCurrentFps();
            String frameTime = fps > 0 ? String.format(Locale.ROOT, "%.2f ms", 1000.0 / fps) : "--";
            drawHudLine(client, context, "frametimehud", 8, 512, "FRAME TIME", frameTime, accent);
        }
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
        if (isEnabled("cps")) {
            drawHudLine(client, context, "cps", 8, 386, "CPS", Integer.toString(CLICK_TIMESTAMPS.size()), accent);
        }
        if (isEnabled("worldinfo")) {
            String dimension = client.world.getRegistryKey().getValue().getPath().replace('_', ' ').toUpperCase(Locale.ROOT);
            drawHudLine(client, context, "worldinfo", 8, 407, "WORLD", dimension + " · DAY " + (client.world.getTimeOfDay() / 24000L), accent);
        }
        if (isEnabled("potioncounter")) {
            int potions = 0;
            for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                ItemStack stack = client.player.getInventory().getStack(slot);
                if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION)
                    || stack.isOf(Items.LINGERING_POTION)) potions += stack.getCount();
            }
            drawHudLine(client, context, "potioncounter", 8, 428, "POTIONS", Integer.toString(potions), accent);
        }
        if (isEnabled("biome")) {
            String biomeName = client.world.getBiome(client.player.getBlockPos()).getKey()
                .map(key -> key.getValue().getPath().replace('_', ' ').toUpperCase(Locale.ROOT))
                .orElse("UNKNOWN");
            drawHudLine(client, context, "biome", 8, 365, "BIOME", biomeName, accent);
        }
        if (isEnabled("totemcounter")) drawHudLine(client, context, "totemcounter", 8, 218, "TOTEMS",
            Integer.toString(countItem(client, Items.TOTEM_OF_UNDYING)), accent);
        if (isEnabled("pearlcounter")) drawHudLine(client, context, "pearlcounter", 8, 239, "PEARLS",
            Integer.toString(countItem(client, Items.ENDER_PEARL)), accent);
        if (isEnabled("arrowcounter")) {
            int arrows = countItem(client, Items.ARROW) + countItem(client, Items.SPECTRAL_ARROW)
                + countItem(client, Items.TIPPED_ARROW);
            drawHudLine(client, context, "arrowcounter", 8, 260, "ARROWS", Integer.toString(arrows), accent);
        }
        if (isEnabled("goldencounter")) {
            int apples = countItem(client, Items.GOLDEN_APPLE)
                + countItem(client, Items.ENCHANTED_GOLDEN_APPLE);
            drawHudLine(client, context, "goldencounter", 8, 281, "GOLDEN APPLES", Integer.toString(apples), accent);
        }
        if (isEnabled("armorhud")) {
            drawHudLine(client, context, "armorhud", 8, 302, "ARMOR", armorSummary(client), accent);
        }
        if (isEnabled("durabilityhud")) {
            ItemStack weakest = ItemStack.EMPTY;
            int lowestPercent = 101;
            for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                ItemStack stack = client.player.getInventory().getStack(slot);
                if (stack.isEmpty() || !stack.isDamageable()) continue;
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                int percent = Math.round(100.0f * remaining / Math.max(1, stack.getMaxDamage()));
                if (percent < lowestPercent) { lowestPercent = percent; weakest = stack; }
            }
            String value = weakest.isEmpty() ? "No durable items"
                : weakest.getName().getString() + " · " + lowestPercent + "%";
            drawHudLine(client, context, "durabilityhud", 8, 323, "LOWEST DURABILITY", value, accent);
        }
        if (isEnabled("inventoryhud")) {
            ItemStack selected = client.player.getMainHandStack();
            String value = selected.isEmpty() ? "Empty hand"
                : selected.getName().getString() + " ×" + selected.getCount();
            drawHudLine(client, context, "inventoryhud", 8, 344, "HELD ITEM", value, accent);
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
            String targetName = entity.getName().getString();
            String distance = String.format(Locale.ROOT, "%.1f m", client.player.distanceTo(entity));
            String healthText = entity instanceof LivingEntity living
                ? String.format(Locale.ROOT, "%.1f / %.1f HP", Math.max(0.0F, living.getHealth()), living.getMaxHealth())
                : "ENTITY";
            int x = hudX("targethud", 8);
            int y = hudY("targethud", screenHeight - 43);
            int panelWidth = Math.min(context.getScaledWindowWidth() - x - 8,
                Math.max(142, Math.max(client.textRenderer.getWidth(targetName),
                    client.textRenderer.getWidth(healthText) + client.textRenderer.getWidth(distance)) + 24));
            context.fill(x, y, x + panelWidth, y + 38, 0xD90B101A);
            context.fill(x, y, x + 3, y + 38, accent);
            context.drawText(client.textRenderer, targetName, x + 10, y + 5, 0xFFF3F5FC, true);
            context.drawText(client.textRenderer, distance, x + panelWidth - client.textRenderer.getWidth(distance) - 8,
                y + 5, accent, false);
            context.drawText(client.textRenderer, healthText, x + 10, y + 19, 0xFFD5DCEC, false);
            if (entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F) {
                int barX = x + 10, barY = y + 32, barWidth = panelWidth - 20;
                context.fill(barX, barY, barX + barWidth, barY + 3, 0xFF303747);
                int healthWidth = Math.max(0, Math.min(barWidth,
                    Math.round(barWidth * living.getHealth() / living.getMaxHealth())));
                context.fill(barX, barY, barX + healthWidth, barY + 3, 0xFF69E6B8);
            }
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
            context.fill(nx, 8, nx + width, 30, (Math.round(230.0f * hudOpacity / 100.0f) << 24) | 0x000C111D);
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
        int hudAlpha = Math.round(255.0f * hudOpacity / 100.0f);
        context.fill(x, y, x + width, y + 18, (hudAlpha << 24) | 0x00101420);
        context.fill(x, y, x + 2, y + 18, accent);
        context.drawText(client.textRenderer, text, x + 8, y + 5, 0xFFF3F5FF, false);
    }

    private static void drawKey(
        DrawContext context, MinecraftClient client, KeyBinding binding,
        int x, int y, String label, int accent
    ) {
        int width = label.length() > 1 ? 46 : 20;
        context.fill(x, y, x + width, y + 19,
            binding.isPressed() ? accent : ((Math.round(255.0f * hudOpacity / 100.0f) << 24) | 0x00101420));
        context.fill(x, y, x + width, y + 1,
            binding.isPressed() ? 0xFFFFFFFF : 0xFF353D50);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label,
            x + (width - textWidth) / 2, y + 6,
            binding.isPressed() ? 0xFF10131C : 0xFFE9ECF6, false);
    }
}
