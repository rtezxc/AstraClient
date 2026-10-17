package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.VertexRendering;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.RaycastContext;
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
import net.minecraft.component.DataComponentTypes;
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
import java.nio.file.StandardCopyOption;
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
    private static final int CONFIG_SCHEMA_VERSION = 2;
    private static boolean configNeedsMigration;

    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static final Map<String, Integer> MODULE_KEYBINDS = new LinkedHashMap<>();
    private static final Map<String, Boolean> KEY_WAS_DOWN = new LinkedHashMap<>();
    private static final Map<UUID, GlowState> ORIGINAL_ENTITY_GLOW = new HashMap<>();
    private static final Set<BlockPos> XRAY_ORE_POSITIONS = new HashSet<>();
    private static long lastXrayScanAt;
    private static Object xrayWorld;
    private static Object lastGlowWorld;
    private static final Map<String, Integer> HUD_X = new LinkedHashMap<>();
    private static final Map<String, Integer> HUD_Y = new LinkedHashMap<>();
    private static final String[] HUD_WIDGET_IDS = {
        "fps", "coordinates", "direction", "ping", "memory", "speedometer", "clock",
        "totemcounter", "pearlcounter", "arrowcounter", "goldencounter", "armorhud",
        "potionhud", "targethud", "damageindicators", "blockfinder", "sessionstats", "keystrokes", "biome", "cps", "worldinfo", "serverinfo", "potioncounter", "durabilityhud", "inventoryhud", "healthhud", "hungerhud", "blockreachhud", "frametimehud", "arraylist", "watermark", "fpsgraph", "breadcrumbs", "compasshud", "waypoint", "storagefinder"
    };
    private static final Set<String> HUD_WIDGET_ID_SET = Set.of(HUD_WIDGET_IDS);
    private static final Map<String, HudBounds> HUD_RENDERED_BOUNDS = new LinkedHashMap<>();
    private record GlowState(Entity entity, boolean wasGlowing) { }
    private record HudBounds(int x, int y, int width, int height) { }

    private static int guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static int hudEditKeybindCode = GLFW.GLFW_KEY_F6;
    private static boolean hudEditKeyWasDown;
    private static boolean hudChatEditMode;
    private static boolean hudChatWasOpen;
    private static boolean hudMouseWasDown;
    private static boolean hudRightMouseWasDown;
    private static String draggedHudId;
    private static int hudDragOffsetX, hudDragOffsetY;
    private static boolean guiKeyWasDown;
    private static long guiKeybindIgnoreUntil;
    private static int accentTheme;
    private static long timeChangerPreset = 6000L;
    private static boolean customAccentEnabled;
    private static int customAccentColor = 0xFFAA70FF;
    private static boolean clickSounds = true;
    private static boolean autoSprintGroundOnly = true;
    private static boolean autoSprintDisableInMenus = true;
    private static int guiScale = 100;
    private static int hudOpacity = 84;
    private static boolean hudBackgrounds = true;
    private static boolean customMenuBackground = true;
    private static long sessionStartedAt = System.currentTimeMillis();
    private static String notification = "Astra Client loaded";
    private static long notificationUntil = System.currentTimeMillis() + 2400L;
    private static boolean fullBrightWasEnabled;
    private static boolean appliedNightVision;
    private static boolean hadNightVisionBeforeEnable;
    private static int zoomFov = 30;
    private static int crosshairSize = 7;
    private static int crosshairGap = 3;
    private static int crosshairThickness = 2;
    private static int breadcrumbTrailLength = 48;
    private static boolean waypointSet;
    private static int waypointX, waypointY, waypointZ;
    private static final List<TrailPoint> BREADCRUMB_TRAIL = new ArrayList<>();
    private static Object breadcrumbWorld;
    private static long lastBreadcrumbSampleAt;
    private record TrailPoint(double x, double z) { }
    private static int espRange = 64;
    private static int combatRange = 4;
    private static int killAuraFov = 180;
    // Off by default: requiring a jump/fall made Kill Aura look completely inactive.
    private static boolean killAuraCriticalOnly = false;
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
    private static final int[] FPS_HISTORY = new int[40];
    private static int fpsHistoryIndex;
    private static long lastFpsSampleAt;
    private static LivingEntity lastCombatTarget;
    private static PlayerEntity damageTrackedPlayer;
    private static float lastObservedHealth = -1.0F;
    private static float recentDamageAmount;
    private static long damageAlertUntil;
    private static long lastAutoClickAt;

    static {
        MODULES.put("fullbright", false);
        MODULES.put("customcrosshair", false);
        MODULES.put("playeresp", false);
        MODULES.put("mobesp", false);
        MODULES.put("itemesp", false);
        MODULES.put("tracers", false);
        MODULES.put("zoom", false);
        MODULES.put("autosprint", false);
        MODULES.put("sprint", false);
        MODULES.put("fly", false);
        MODULES.put("autowalk", false);
        MODULES.put("safewalk", false);
        MODULES.put("eagle", false);
        MODULES.put("inventorymove", false);
        MODULES.put("autoeat", false);
        MODULES.put("killaura", false);
        MODULES.put("triggerbot", false);
        MODULES.put("autotool", false);
        MODULES.put("autoweapon", false);
        MODULES.put("biome", false);
        MODULES.put("cps", false);
        MODULES.put("worldinfo", false);
        MODULES.put("serverinfo", false);
        MODULES.put("blockfinder", false);
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
        MODULES.put("damageindicators", false);
        MODULES.put("armorhud", false);
        MODULES.put("potionhud", false);
        MODULES.put("clock", false);
        MODULES.put("totemcounter", false);
        MODULES.put("pearlcounter", false);
        MODULES.put("arrowcounter", false);
        MODULES.put("goldencounter", false);
        MODULES.put("sessionstats", false);
        MODULES.put("arraylist", false);
        MODULES.put("watermark", true);
        MODULES.put("fpsgraph", false);
        MODULES.put("lowhealthalert", false);
        MODULES.put("compasshud", false);
        MODULES.put("breadcrumbs", false);
        MODULES.put("waypoint", false);
        MODULES.put("storagefinder", false);
        MODULES.put("effectalert", false);
        MODULES.put("orbitcubes", false);
        MODULES.put("hitboxes", false);
        MODULES.put("targetbox", false);
        MODULES.put("targethalo", false);
        MODULES.put("playertrail", false);
        MODULES.put("blockhighlight", false);
        MODULES.put("orbitsparks", false);
        MODULES.put("timechanger", false);
        MODULES.put("xray", false);
        MODULES.put("nametags", false);
        MODULES.put("noslow", false);
        MODULES.put("speed", false);
        MODULES.put("noweather", false);
        MODULES.put("fastplace", false);
        MODULES.put("fastbreak", false);
        MODULES.put("scaffold", false);
        MODULES.put("aimassist", false);
        MODULES.put("autototem", false);
        MODULES.put("antiknockback", false);
        MODULES.put("autoclicker", false);
        MODULES.put("criticals", false);
        MODULES.put("reach", false);
        MODULES.put("nopumpkinoverlay", false);
        MODULES.put("nofireoverlay", false);
        MODULES.put("noportaloverlay", false);
        MODULES.put("nospyglassoverlay", false);
        MODULES.put("nopowdersnowoverlay", false);
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

    /** Rejects malformed config or UI key codes before they reach GLFW input polling. */
    private static int normalizeKeyCode(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return GLFW.GLFW_KEY_UNKNOWN;
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_LAST
            ? keyCode : GLFW.GLFW_KEY_UNKNOWN;
    }

    public static void setModuleKeybind(String id, int keyCode) {
        if (!MODULES.containsKey(id)) return;
        keyCode = normalizeKeyCode(keyCode);
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
            case "orbitcubes" -> "Orbit Cubes";
            case "hitboxes" -> "Hitbox ESP";
            case "targetbox" -> "Target Box";
            case "targethalo" -> "Target Halo";
            case "playertrail" -> "Player Trail";
            case "blockhighlight" -> "Block Highlight";
            case "orbitsparks" -> "Orbit Sparks";
            case "timechanger" -> "Time Changer";
            case "customcrosshair" -> "Custom Crosshair";
            case "tracers" -> "Tracers";
            case "autosprint" -> "Auto Sprint";
            case "sprint" -> "Sprint";
            case "fly" -> "Fly";
            case "autowalk" -> "Auto Walk";
            case "safewalk" -> "Safe Walk";
            case "eagle" -> "Eagle";
            case "inventorymove" -> "Inventory Move";
            case "autoeat" -> "Auto Eat";
            case "killaura" -> "Kill Aura";
            case "triggerbot" -> "TriggerBot";
            case "autotool" -> "Auto Tool";
            case "autoweapon" -> "Auto Weapon";
            case "biome" -> "Biome Display";
            case "cps" -> "CPS Counter";
            case "worldinfo" -> "World Information";
            case "serverinfo" -> "Server Information";
            case "blockfinder" -> "Block Finder";
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
            case "damageindicators" -> "Damage Indicators";
            case "armorhud" -> "Armor HUD";
            case "potionhud" -> "Potion HUD";
            case "totemcounter" -> "Totem Counter";
            case "pearlcounter" -> "Pearl Counter";
            case "arrowcounter" -> "Arrow Counter";
            case "goldencounter" -> "Golden Apple Counter";
            case "sessionstats" -> "Session Stats";
            case "arraylist" -> "Active Modules";
            case "watermark" -> "Watermark";
            case "fpsgraph" -> "FPS Graph";
            case "lowhealthalert" -> "Low Health Alert";
            case "compasshud" -> "Compass HUD";
            case "breadcrumbs" -> "Breadcrumbs";
            case "waypoint" -> "Waypoint";
            case "storagefinder" -> "Storage Finder";
            case "effectalert" -> "Effect Expiry Alert";
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
        if (customAccentEnabled) return "CUSTOM";
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
        customAccentEnabled = false;
        accentTheme = (accentTheme + 1) % 4;
        notifyUser("Theme: " + accentName());
        saveConfig();
    }

    public static boolean isCustomAccentEnabled() { return customAccentEnabled; }
    public static int customAccentColor() { return customAccentColor; }
    public static int customAccentRed() { return (customAccentColor >> 16) & 0xFF; }
    public static int customAccentGreen() { return (customAccentColor >> 8) & 0xFF; }
    public static int customAccentBlue() { return customAccentColor & 0xFF; }

    public static void previewCustomAccentColor(int color) {
        customAccentColor = 0xFF000000 | (color & 0x00FFFFFF);
        customAccentEnabled = true;
    }

    public static void setCustomAccentColor(int color) {
        previewCustomAccentColor(color);
        notifyUser("Custom accent: #" + String.format(Locale.ROOT, "%06X", customAccentColor & 0x00FFFFFF));
        saveConfig();
    }

    public static void usePresetAccent() {
        customAccentEnabled = false;
        notifyUser("Theme: " + accentName());
        saveConfig();
    }

    public static int accentColor() {
        if (customAccentEnabled) return customAccentColor;
        return switch (accentTheme) {
            case 1 -> 0xFF54E8FF;
            case 2 -> 0xFFFFC66E;
            case 3 -> 0xFFFF6FAE;
            default -> 0xFFAA70FF;
        };
    }

    public static int guiKeybindCode() { return guiKeybindCode; }
    public static void setGuiKeybindCode(int keyCode) {
        guiKeybindCode = keyCode == GLFW.GLFW_KEY_UNKNOWN ? GLFW.GLFW_KEY_RIGHT_SHIFT : keyCode;
        guiKeyWasDown = false;
        guiKeybindIgnoreUntil = System.currentTimeMillis() + 400L;
        notifyUser("GUI keybind: " + keyName(guiKeybindCode));
        saveConfig();
    }

    public static int hudEditKeybindCode() { return hudEditKeybindCode; }

    public static void setHudEditKeybindCode(int keyCode) {
        hudEditKeybindCode = keyCode == GLFW.GLFW_KEY_UNKNOWN ? GLFW.GLFW_KEY_F6 : keyCode;
        hudEditKeyWasDown = false;
        notifyUser("Chat HUD drag key: " + keyName(hudEditKeybindCode));
        saveConfig();
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

    public static int autoEatHungerThreshold() { return autoEatHungerThreshold; }
    public static void previewAutoEatHungerThreshold(int value) { autoEatHungerThreshold = Math.max(10, Math.min(19, value)); }

    public static int lowHealthThreshold() { return lowHealthThreshold; }
    public static void previewLowHealthThreshold(int value) { lowHealthThreshold = Math.max(2, Math.min(20, value)); }
    public static void setLowHealthThreshold(int value) {
        previewLowHealthThreshold(value);
        notifyUser("Low health alert threshold: " + lowHealthThreshold + " HP");
        saveConfig();
    }

    public static int effectAlertThresholdSeconds() { return effectAlertThresholdSeconds; }
    public static void previewEffectAlertThresholdSeconds(int value) {
        effectAlertThresholdSeconds = Math.max(3, Math.min(30, value));
    }
    public static void setEffectAlertThresholdSeconds(int value) {
        previewEffectAlertThresholdSeconds(value);
        notifyUser("Effect expiry warning: " + effectAlertThresholdSeconds + " seconds");
        saveConfig();
    }

    public static String blockFinderTarget() { return blockFinderTarget; }
    public static void cycleBlockFinderTarget() {
        int current = 0;
        for (int i = 0; i < BLOCK_FINDER_TARGETS.length; i++) {
            if (BLOCK_FINDER_TARGETS[i].equals(blockFinderTarget)) { current = i; break; }
        }
        blockFinderTarget = BLOCK_FINDER_TARGETS[(current + 1) % BLOCK_FINDER_TARGETS.length];
        nearestFoundBlock = null;
        lastBlockFinderScanAt = 0L;
        notifyUser("Block Finder target: " + blockFinderTarget);
        saveConfig();
    }
    public static int blockFinderRange() { return blockFinderRange; }
    public static void previewBlockFinderRange(int value) {
        blockFinderRange = Math.max(8, Math.min(48, value));
    }
    public static void setBlockFinderRange(int value) {
        previewBlockFinderRange(value);
        nearestFoundBlock = null;
        lastBlockFinderScanAt = 0L;
        notifyUser("Block Finder range: " + blockFinderRange + " blocks");
        saveConfig();
    }

    public static boolean tracerPlayers() { return tracerPlayers; }
    public static boolean tracerMobs() { return tracerMobs; }
    public static void toggleTracerPlayers() {
        tracerPlayers = !tracerPlayers;
        cachedTracerTargets.clear();
        notifyUser("Player tracers " + (tracerPlayers ? "enabled" : "disabled"));
        saveConfig();
    }
    public static void toggleTracerMobs() {
        tracerMobs = !tracerMobs;
        cachedTracerTargets.clear();
        notifyUser("Mob tracers " + (tracerMobs ? "enabled" : "disabled"));
        saveConfig();
    }

    public static int storageFinderRange() { return storageFinderRange; }
    public static void previewStorageFinderRange(int value) {
        storageFinderRange = Math.max(8, Math.min(32, value));
        nearestStorageBlock = null;
        lastStorageScanAt = 0L;
    }
    public static void setStorageFinderRange(int value) {
        previewStorageFinderRange(value);
        notifyUser("Storage Finder range: " + storageFinderRange + " blocks");
        saveConfig();
    }

    public static boolean isWaypointSet() { return waypointSet; }
    public static int waypointX() { return waypointX; }
    public static int waypointY() { return waypointY; }
    public static int waypointZ() { return waypointZ; }
    public static void setWaypointHere() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            notifyUser("Waypoint unavailable: join a world first");
            return;
        }
        BlockPos pos = client.player.getBlockPos();
        waypointX = pos.getX();
        waypointY = pos.getY();
        waypointZ = pos.getZ();
        waypointSet = true;
        notifyUser("Waypoint saved at " + waypointX + " " + waypointY + " " + waypointZ);
        saveConfig();
    }
    public static void clearWaypoint() {
        waypointSet = false;
        notifyUser("Waypoint cleared");
        saveConfig();
    }

    public static int breadcrumbTrailLength() { return breadcrumbTrailLength; }
    public static void previewBreadcrumbTrailLength(int value) {
        breadcrumbTrailLength = Math.max(12, Math.min(120, value));
        while (BREADCRUMB_TRAIL.size() > breadcrumbTrailLength) BREADCRUMB_TRAIL.remove(0);
    }
    public static void setBreadcrumbTrailLength(int value) {
        previewBreadcrumbTrailLength(value);
        notifyUser("Breadcrumb trail: " + breadcrumbTrailLength + " points");
        saveConfig();
    }

    public static int crosshairSize() { return crosshairSize; }
    public static int crosshairGap() { return crosshairGap; }
    public static int crosshairThickness() { return crosshairThickness; }

    public static void previewCrosshairSize(int value) { crosshairSize = Math.max(3, Math.min(20, value)); }
    public static void setCrosshairSize(int value) {
        previewCrosshairSize(value);
        notifyUser("Crosshair size: " + crosshairSize);
        saveConfig();
    }
    public static void previewCrosshairGap(int value) { crosshairGap = Math.max(1, Math.min(12, value)); }
    public static void setCrosshairGap(int value) {
        previewCrosshairGap(value);
        notifyUser("Crosshair gap: " + crosshairGap);
        saveConfig();
    }
    public static void previewCrosshairThickness(int value) { crosshairThickness = Math.max(1, Math.min(5, value)); }
    public static void setCrosshairThickness(int value) {
        previewCrosshairThickness(value);
        notifyUser("Crosshair thickness: " + crosshairThickness);
        saveConfig();
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
    public static int killAuraFov() { return killAuraFov; }
    public static boolean killAuraCriticalOnly() { return killAuraCriticalOnly; }
    public static void toggleKillAuraCriticalOnly() {
        killAuraCriticalOnly = !killAuraCriticalOnly;
        notifyUser("Kill Aura critical-only: " + (killAuraCriticalOnly ? "ON" : "OFF"));
        saveConfig();
    }
    public static void setKillAuraFov(int value) {
        killAuraFov = Math.max(60, Math.min(180, value));
        notifyUser("Kill Aura FOV: " + killAuraFov + " degrees");
        saveConfig();
    }
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

    public static boolean areHudBackgroundsEnabled() {
        return hudBackgrounds;
    }

    public static void toggleHudBackgrounds() {
        hudBackgrounds = !hudBackgrounds;
        notifyUser("HUD backgrounds " + (hudBackgrounds ? "enabled" : "hidden"));
        saveConfig();
    }

    /** Whether Astra should replace the vanilla title and world-browser backgrounds. */
    public static boolean isCustomMenuBackgroundEnabled() {
        return customMenuBackground;
    }

    public static void toggleCustomMenuBackground() {
        customMenuBackground = !customMenuBackground;
        notifyUser("Animated menu background " + (customMenuBackground ? "enabled" : "disabled"));
        saveConfig();
    }

    public static int previewHudOpacity(int value) {
        hudOpacity = Math.max(35, Math.min(100, value));
        return hudOpacity;
    }

    public static void resetModuleDefaults() {
        // Release temporary player/input changes before clearing the state needed to undo them.
        // Otherwise resetting while Fly or Auto Tool is active could leave the player altered.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            restoreAutoToolSlot(client);
            if (flyPlayer != null && flyWasActive) flyPlayer.setNoGravity(false);
            if (forcedSafeWalkSneak && client.player != null && client.options != null
                && !client.options.sneakKey.isPressed()) {
                client.player.setSneaking(false);
            }
            // Undo transient visual/input overrides immediately rather than waiting for another tick.
            if (zoomOverrideActive && client.options != null) {
                client.options.getFov().setValue(originalFov);
            }
            if (client.player != null && appliedNightVision && !hadNightVisionBeforeEnable) {
                client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
            for (GlowState glow : ORIGINAL_ENTITY_GLOW.values()) {
                if (glow.entity() != null) glow.entity().setGlowing(glow.wasGlowing());
            }
            lastGlowWorld = client.world;
        }
        ORIGINAL_ENTITY_GLOW.clear();
        XRAY_ORE_POSITIONS.clear();
        lastXrayScanAt = 0L;
        xrayWorld = null;
        zoomOverrideActive = false;
        fullBrightWasEnabled = false;
        appliedNightVision = false;
        hadNightVisionBeforeEnable = false;

        // Keep this list aligned with the startup defaults and loadConfigFile fallbacks.
        // Reset must restore the complete configuration, not just the visible GUI controls.
        MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("watermark"));
        MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
        KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
        HUD_X.clear();
        HUD_Y.clear();

        accentTheme = 0;
        customAccentEnabled = false;
        customAccentColor = 0xFFAA70FF;
        timeChangerPreset = 6000L;
        clickSounds = true;
        guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
        hudEditKeybindCode = GLFW.GLFW_KEY_F6;
        guiKeyWasDown = false;
        hudEditKeyWasDown = false;
        hudChatEditMode = false;
        hudChatWasOpen = false;
        hudMouseWasDown = false;
        hudRightMouseWasDown = false;
        draggedHudId = null;
        HUD_RENDERED_BOUNDS.clear();
        guiKeybindIgnoreUntil = System.currentTimeMillis() + 400L;

        autoSprintGroundOnly = true;
        autoSprintDisableInMenus = true;
        guiScale = 100;
        hudOpacity = 84;
        hudBackgrounds = true;

        zoomFov = 30;
        crosshairSize = 7;
        crosshairGap = 3;
        crosshairThickness = 2;
        breadcrumbTrailLength = 48;
        waypointSet = false;
        waypointX = waypointY = waypointZ = 0;
        BREADCRUMB_TRAIL.clear();
        breadcrumbWorld = null;
        lastBreadcrumbSampleAt = 0L;

        autoEatHungerThreshold = 16;
        lowHealthThreshold = 6;
        effectAlertThresholdSeconds = 10;
        blockFinderTarget = "DIAMOND";
        blockFinderRange = 24;
        storageFinderRange = 16;
        tracerPlayers = true;
        tracerMobs = true;
        espRange = 64;
        combatRange = 4;
        killAuraFov = 180;
        killAuraCriticalOnly = false;
        targetPlayers = true;
        targetHostile = true;
        targetPassive = false;

        // Clear transient trackers so the next tick starts from a clean state.
        CLICK_TIMESTAMPS.clear();
        lastAutoClickAt = 0L;
        attackMouseWasDown = false;
        useMouseWasDown = false;
        fpsHistoryIndex = 0;
        for (int i = 0; i < FPS_HISTORY.length; i++) FPS_HISTORY[i] = 0;
        lastFpsSampleAt = 0L;
        targetEspParticleTick = 0;
        lastCombatTarget = null;
        damageTrackedPlayer = null;
        lastObservedHealth = -1.0F;
        recentDamageAmount = 0.0F;
        damageAlertUntil = 0L;
        autoToolOriginalSlot = -1;
        autoToolSelectedSlot = -1;
        forcedSafeWalkSneak = false;
        flyPlayer = null;
        flyWasActive = false;

        notifyUser("All Astra settings reset to defaults");
        saveConfig();
    }


    /** Applies a named, opt-in module preset. Combat automation is deliberately never enabled by presets. */
    public static void applyQuickProfile(String profile) {
        Set<String> enabled = switch (profile.toLowerCase(Locale.ROOT)) {
            case "performance" -> Set.of("fps", "watermark");
            case "pvp" -> Set.of("fps", "watermark", "keystrokes", "cps", "armorhud",
                "potionhud", "targethud", "tracers", "playeresp", "lowhealthalert", "compasshud");
            case "exploration" -> Set.of("fps", "watermark", "coordinates", "direction", "biome",
                "waypoint", "breadcrumbs", "compasshud", "storagefinder", "blockfinder",
                "worldinfo", "serverinfo", "healthhud", "hungerhud", "armorhud", "sessionstats");
            default -> {
                notifyUser("Unknown profile");
                yield null;
            }
        };
        if (enabled == null) return;
        MODULES.replaceAll((id, oldValue) -> enabled.contains(id));
        notifyUser("Applied " + profile + " profile");
        saveConfig();
    }

    private static Path namedConfigPath(int slot) {
        if (slot < 1 || slot > 5) throw new IllegalArgumentException("Config slot must be 1-5");
        return CONFIG_PATH.resolveSibling("astraclient-config-" + slot + ".properties");
    }

    public static boolean hasNamedConfig(int slot) {
        try { return Files.isRegularFile(namedConfigPath(slot)); }
        catch (RuntimeException exception) { return false; }
    }

    /** Saves a full snapshot of the current settings into one of five independent slots. */
    public static void saveNamedConfig(int slot) {
        try {
            saveConfig();
            Files.copy(CONFIG_PATH, namedConfigPath(slot), StandardCopyOption.REPLACE_EXISTING);
            notifyUser("Saved config slot " + slot);
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not save config slot " + slot + ": " + exception.getMessage());
            notifyUser("Could not save config slot " + slot);
        }
    }

    /** Loads a named snapshot and reinitializes in-memory defaults before applying its properties. */
    public static void loadNamedConfig(int slot) {
        Path source;
        try { source = namedConfigPath(slot); }
        catch (RuntimeException exception) { notifyUser("Invalid config slot"); return; }
        if (!Files.isRegularFile(source)) {
            notifyUser("Slot " + slot + " is empty — save a config first");
            return;
        }
        try {
            // Validate the snapshot before replacing the active file.
            Properties validation = new Properties();
            try (InputStream input = Files.newInputStream(source)) { validation.load(input); }
            validateConfigProperties(validation);
            MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("coordinates"));
            MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
            KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
            HUD_X.clear();
            HUD_Y.clear();
            accentTheme = 0;
            guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
            guiKeyWasDown = false;
            guiKeybindIgnoreUntil = System.currentTimeMillis() + 400L;
            clickSounds = true;
            autoSprintGroundOnly = true;
            autoSprintDisableInMenus = true;
            guiScale = 100;
            hudOpacity = 84;
            hudBackgrounds = true;
            customMenuBackground = true;
            zoomFov = 30;
            autoEatHungerThreshold = 16;
            espRange = 64;
            combatRange = 4;
            targetPlayers = true;
            targetHostile = true;
            targetPassive = false;
            Files.copy(source, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            loadConfig();
            saveConfig();
            notifyUser("Loaded config slot " + slot);
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not load config slot " + slot + ": " + exception.getMessage());
            notifyUser("Could not load config slot " + slot);
        }
    }

    private static final Path NAMED_CONFIGS_DIR =
        FabricLoader.getInstance().getConfigDir().resolve("astraclient-configs");

    private static boolean isValidConfigName(String name) {
        return name != null && name.matches("[A-Za-z0-9_-]{1,24}");
    }

    public static List<String> namedConfigNames() {
        List<String> names = new ArrayList<>();
        try {
            if (!Files.isDirectory(NAMED_CONFIGS_DIR)) return names;
            try (var files = Files.list(NAMED_CONFIGS_DIR)) {
                files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.properties$", ""))
                    .filter(AstraClient::isValidConfigName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(names::add);
            }
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not list named configs: " + exception.getMessage());
        }
        return names;
    }

    private static Path namedConfigPath(String name) {
        if (!isValidConfigName(name)) throw new IllegalArgumentException("Invalid config name");
        return NAMED_CONFIGS_DIR.resolve(name + ".properties");
    }

    public static void handleConfigsChatCommand(String rawMessage) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = rawMessage.trim().split("\\s+");
        if (args.length == 1) {
            client.player.sendMessage(Text.literal("§b[Astra] §fConfig commands: §e.configs add/remove/use/copy/rename/info <name> §7| §e.configs list §7| §e.configs export/import"), false);
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            List<String> names = namedConfigNames();
            client.player.sendMessage(Text.literal(names.isEmpty()
                ? "§b[Astra] §7No saved configs yet. Use §e.configs add <name>§7."
                : "§b[Astra] §fSaved configs (" + names.size() + "): §a" + String.join("§7, §a", names)), false);
            return;
        }
        if (action.equals("export")) { exportConfigBackup(); client.player.sendMessage(Text.literal("§b[Astra] §aCurrent config exported to astraclient-backup.properties"), false); return; }
        if (action.equals("import")) { importConfigBackup(); return; }
        if (args.length < 3) {
            client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .configs add/remove/use/copy/rename/info <name>"), false);
            return;
        }
        String name = args[2];
        if (!isValidConfigName(name)) {
            client.player.sendMessage(Text.literal("§b[Astra] §cName must be 1-24 characters: letters, numbers, _ or -."), false);
            return;
        }
        Path path = namedConfigPath(name);
        if (action.equals("info")) {
            if (!Files.isRegularFile(path)) {
                client.player.sendMessage(Text.literal("§b[Astra] §cNo config named " + name), false);
                return;
            }
            try {
                Properties info = new Properties();
                try (InputStream input = Files.newInputStream(path)) { info.load(input); }
                long size = Files.size(path);
                client.player.sendMessage(Text.literal("§b[Astra] §fConfig §e" + name + " §7| " + info.size()
                    + " settings/properties | " + size + " bytes"), false);
            } catch (Exception exception) {
                client.player.sendMessage(Text.literal("§b[Astra] §cCould not read config info."), false);
            }
            return;
        }
        if (action.equals("copy") || action.equals("rename")) {
            if (args.length < 4 || !isValidConfigName(args[3])) {
                client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .configs " + action + " <old> <new>"), false);
                return;
            }
            String destinationName = args[3];
            Path destination = namedConfigPath(destinationName);
            if (!Files.isRegularFile(path)) {
                client.player.sendMessage(Text.literal("§b[Astra] §cNo config named " + name), false);
                return;
            }
            if (Files.exists(destination)) {
                client.player.sendMessage(Text.literal("§b[Astra] §cConfig " + destinationName + " already exists. Remove it first."), false);
                return;
            }
            try {
                Files.createDirectories(NAMED_CONFIGS_DIR);
                Files.copy(path, destination);
                if (action.equals("rename")) Files.delete(path);
                client.player.sendMessage(Text.literal("§b[Astra] §a" + (action.equals("copy") ? "Copied" : "Renamed")
                    + " §f" + name + " §ato §f" + destinationName), false);
            } catch (Exception exception) {
                client.player.sendMessage(Text.literal("§b[Astra] §cConfig " + action + " failed. Check the game log."), false);
            }
            return;
        }
        switch (action) {
            case "add" -> {
                try {
                    saveConfig();
                    Files.createDirectories(NAMED_CONFIGS_DIR);
                    Files.copy(CONFIG_PATH, path, StandardCopyOption.REPLACE_EXISTING);
                    client.player.sendMessage(Text.literal("§b[Astra] §aSaved config §f" + name), false);
                } catch (Exception exception) {
                    System.err.println("[Astra Client] Could not save config " + name + ": " + exception.getMessage());
                    client.player.sendMessage(Text.literal("§b[Astra] §cCould not save config. Check the game log."), false);
                }
            }
            case "remove" -> {
                try {
                    if (!Files.isRegularFile(path)) {
                        client.player.sendMessage(Text.literal("§b[Astra] §cNo config named " + name), false);
                        return;
                    }
                    Files.delete(path);
                    client.player.sendMessage(Text.literal("§b[Astra] §aRemoved config §f" + name), false);
                } catch (Exception exception) {
                    System.err.println("[Astra Client] Could not remove config " + name + ": " + exception.getMessage());
                    client.player.sendMessage(Text.literal("§b[Astra] §cCould not remove config."), false);
                }
            }
            case "use" -> {
                if (!Files.isRegularFile(path)) {
                    client.player.sendMessage(Text.literal("§b[Astra] §cNo config named " + name + ". Use §e.configs list"), false);
                    return;
                }
                try {
                    Properties validation = new Properties();
                    try (InputStream input = Files.newInputStream(path)) { validation.load(input); }
                    validateConfigProperties(validation);
                    MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("coordinates"));
                    MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
                    KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
                    HUD_X.clear();
                    HUD_Y.clear();
                    accentTheme = 0;
                    guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
                    guiKeyWasDown = false;
                    guiKeybindIgnoreUntil = System.currentTimeMillis() + 400L;
                    clickSounds = true;
                    autoSprintGroundOnly = true;
                    autoSprintDisableInMenus = true;
                    guiScale = 100;
                    hudOpacity = 84;
                    hudBackgrounds = true;
                    zoomFov = 30;
                    autoEatHungerThreshold = 16;
                    espRange = 64;
                    combatRange = 4;
                    targetPlayers = true;
                    targetHostile = true;
                    targetPassive = false;
                    Files.copy(path, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
                    loadConfig();
                    saveConfig();
                    client.player.sendMessage(Text.literal("§b[Astra] §aLoaded config §f" + name), false);
                } catch (Exception exception) {
                    System.err.println("[Astra Client] Could not load config " + name + ": " + exception.getMessage());
                    client.player.sendMessage(Text.literal("§b[Astra] §cCould not load config. Check the game log."), false);
                }
            }
            default -> client.player.sendMessage(Text.literal("§b[Astra] §cUnknown action. Use .configs for help."), false);
        }
    }

    public static void handleAstraChatCommand(String rawMessage) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = rawMessage.trim().split("\\s+");
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            client.player.sendMessage(Text.literal("§b[Astra] §fCommands: §e.astra status/coords/fps§7, §e.astra time <day|noon|sunset|night|normal>§7, §e.astra inventory/armor§7, §e.astra waypoint [set|show|clear]§7, §e.astra modules [all]§7, §e.astra toggle <id>§7, §e.astra disableall§7, §e.astra profile <name>§7, §e.astra hud§7, §e.configs"), false);
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "time" -> {
                if (args.length < 3) {
                    client.player.sendMessage(Text.literal("§b[Astra] §fTime: §eday§7, §enoон§7, §esunset§7, §enight§7, §enormal"), false);
                    return;
                }
                String preset = args[2].toLowerCase(Locale.ROOT);
                switch (preset) {
                    case "day" -> timeChangerPreset = 1000L;
                    case "noon" -> timeChangerPreset = 6000L;
                    case "sunset" -> timeChangerPreset = 12000L;
                    case "night" -> timeChangerPreset = 18000L;
                    case "normal" -> {
                        MODULES.put("timechanger", false);
                        saveConfig();
                        client.player.sendMessage(Text.literal("§b[Astra] §fWorld time override disabled"), false);
                        return;
                    }
                    default -> {
                        client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .astra time <day|noon|sunset|night|normal>"), false);
                        return;
                    }
                }
                MODULES.put("timechanger", true);
                saveConfig();
                client.player.sendMessage(Text.literal("§b[Astra] §fClient time set to §e" + preset), false);
            }
            case "waypoint" -> {
                String action = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : "show";
                switch (action) {
                    case "set" -> setWaypointHere();
                    case "clear" -> clearWaypoint();
                    case "show" -> {
                        if (!isWaypointSet()) {
                            client.player.sendMessage(Text.literal("§b[Astra] §7No waypoint set. Use §e.astra waypoint set"), false);
                        } else {
                            double dx = waypointX() + 0.5 - client.player.getX();
                            double dy = waypointY() + 0.5 - client.player.getY();
                            double dz = waypointZ() + 0.5 - client.player.getZ();
                            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                            client.player.sendMessage(Text.literal("§b[Astra] §fWaypoint: §e" + waypointX() + " "
                                + waypointY() + " " + waypointZ() + " §7| Distance: §a"
                                + String.format(Locale.ROOT, "%.1f", distance) + " blocks"), false);
                        }
                    }
                    default -> client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .astra waypoint <set|show|clear>"), false);
                }
            }
            case "inventory" -> {
                int occupied = 0;
                int stacks = 0;
                for (ItemStack stack : client.player.getInventory().main) {
                    if (!stack.isEmpty()) {
                        occupied++;
                        stacks += stack.getCount();
                    }
                }
                ItemStack held = client.player.getMainHandStack();
                String heldName = held.isEmpty() ? "Empty hand" : held.getName().getString() + " x" + held.getCount();
                client.player.sendMessage(Text.literal("§b[Astra] §fInventory: §a" + occupied
                    + "§7/36 slots occupied, §a" + stacks + "§7 total items"), false);
                client.player.sendMessage(Text.literal("§b[Astra] §fMain hand: §e" + heldName), false);
            }
            case "armor" -> {
                String[] labels = {"Feet", "Legs", "Chest", "Head"};
                List<String> pieces = new ArrayList<>();
                for (int i = 0; i < client.player.getInventory().armor.size(); i++) {
                    ItemStack stack = client.player.getInventory().armor.get(i);
                    pieces.add(labels[i] + ": " + (stack.isEmpty() ? "empty" : stack.getName().getString()));
                }
                client.player.sendMessage(Text.literal("§b[Astra] §fArmor: §e" + String.join(" §7| §e", pieces)), false);
            }
            case "status" -> {
                var player = client.player;
                String dimension = client.world == null ? "unknown" : client.world.getRegistryKey().getValue().toString();
                client.player.sendMessage(Text.literal("§b[Astra] §fStatus §7| FPS: §a" + client.getCurrentFps()
                    + " §7| Health: §c" + String.format(Locale.ROOT, "%.1f", player.getHealth())
                    + "§7/§c" + String.format(Locale.ROOT, "%.1f", player.getMaxHealth())
                    + " §7| Hunger: §e" + player.getHungerManager().getFoodLevel()), false);
                client.player.sendMessage(Text.literal("§b[Astra] §7Dimension: §f" + dimension
                    + " §7| Position: §f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()), false);
            }
            case "coords" -> {
                var player = client.player;
                String direction = player.getHorizontalFacing().getName().toUpperCase(Locale.ROOT);
                client.player.sendMessage(Text.literal("§b[Astra] §fXYZ: §e" + player.getBlockX() + " "
                    + player.getBlockY() + " " + player.getBlockZ() + " §7| Facing: §b" + direction), false);
            }
            case "fps" -> client.player.sendMessage(Text.literal("§b[Astra] §fCurrent FPS: §a" + client.getCurrentFps()), false);
            case "disableall" -> {
                MODULES.replaceAll((id, ignored) -> false);
                KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
                saveConfig();
                client.player.sendMessage(Text.literal("§b[Astra] §aDisabled all modules and saved settings."), false);
            }
            case "modules" -> {
                boolean showAll = args.length > 2 && args[2].equalsIgnoreCase("all");
                List<String> shown = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
                    if (showAll || Boolean.TRUE.equals(entry.getValue())) {
                        shown.add((Boolean.TRUE.equals(entry.getValue()) ? "§a" : "§7") + entry.getKey());
                    }
                }
                client.player.sendMessage(Text.literal("§b[Astra] §f" + (showAll ? "All modules" : "Enabled modules")
                    + " (" + shown.size() + "): " + (shown.isEmpty() ? "none" : String.join("§7, ", shown))), false);
            }
            case "toggle" -> {
                if (args.length < 3 || !MODULES.containsKey(args[2].toLowerCase(Locale.ROOT))) {
                    client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .astra toggle <module-id>. Use .astra modules to see enabled IDs."), false);
                    return;
                }
                String id = args[2].toLowerCase(Locale.ROOT);
                toggle(id);
                client.player.sendMessage(Text.literal("§b[Astra] §f" + id + " is now " + (isEnabled(id) ? "§aON" : "§cOFF")), false);
            }
            case "profile" -> {
                if (args.length < 3) {
                    client.player.sendMessage(Text.literal("§b[Astra] §cUsage: .astra profile <performance|pvp|exploration>"), false);
                    return;
                }
                String profile = args[2].toLowerCase(Locale.ROOT);
                if (!Set.of("performance", "pvp", "exploration").contains(profile)) {
                    client.player.sendMessage(Text.literal("§b[Astra] §cUnknown profile. Choose performance, pvp, or exploration."), false);
                    return;
                }
                applyQuickProfile(profile);
                client.player.sendMessage(Text.literal("§b[Astra] §aApplied profile §f" + profile), false);
            }
            case "hud" -> {
                client.setScreen(new AstraHudEditorScreen());
                client.player.sendMessage(Text.literal("§b[Astra] §aOpened HUD editor"), false);
            }
            default -> client.player.sendMessage(Text.literal("§b[Astra] §cUnknown command. Use .astra help"), false);
        }
    }

    /** Writes a portable copy of the current settings next to the live config file. */
    public static void exportConfigBackup() {
        saveConfig();
        Path backup = CONFIG_PATH.resolveSibling("astraclient-backup.properties");
        try {
            Files.copy(CONFIG_PATH, backup, StandardCopyOption.REPLACE_EXISTING);
            notifyUser("Config exported: astraclient-backup.properties");
        } catch (Exception exception) {
            System.err.println("[Astra Client] Config export failed: " + exception.getMessage());
            notifyUser("Config export failed");
        }
    }

    /** Imports the last exported backup, resetting first so absent old fields use defaults. */
    public static void importConfigBackup() {
        Path backup = CONFIG_PATH.resolveSibling("astraclient-backup.properties");
        if (!Files.isRegularFile(backup)) {
            notifyUser("No config backup found");
            return;
        }
        try {
            Files.copy(backup, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            MODULES.replaceAll((id, ignored) -> id.equals("fps") || id.equals("coordinates"));
            MODULE_KEYBINDS.replaceAll((id, ignored) -> GLFW.GLFW_KEY_UNKNOWN);
            KEY_WAS_DOWN.replaceAll((id, ignored) -> false);
            HUD_X.clear();
            HUD_Y.clear();
            accentTheme = 0;
            guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
            guiKeyWasDown = false;
            guiKeybindIgnoreUntil = System.currentTimeMillis() + 400L;
            clickSounds = true;
            autoSprintGroundOnly = true;
            autoSprintDisableInMenus = true;
            guiScale = 100;
            hudOpacity = 84;
            zoomFov = 30;
            autoEatHungerThreshold = 16;
            espRange = 64;
            combatRange = 4;
            targetPlayers = true;
            targetHostile = true;
            targetPassive = false;
            loadConfig();
            saveConfig();
            notifyUser("Config backup imported");
        } catch (Exception exception) {
            System.err.println("[Astra Client] Config import failed: " + exception.getMessage());
            notifyUser("Config import failed");
        }
    }

    /**
     * Loads settings from the primary file and falls back to the last known-good
     * snapshot if the primary file was truncated or contains invalid values.
     */
    private static void loadConfig() {
        configNeedsMigration = false;
        if (!Files.isRegularFile(CONFIG_PATH)) {
            // First launch should leave a ready-to-edit config instead of waiting for the first toggle.
            saveConfig();
            if (Files.isRegularFile(CONFIG_PATH)) {
                System.out.println("[Astra Client] Created default configuration at " + CONFIG_PATH + ".");
            }
            return;
        }
        try {
            loadConfigFile(CONFIG_PATH);
        } catch (Exception primaryFailure) {
            System.err.println("[Astra Client] Could not load config: " + primaryFailure.getMessage());
            Path backupPath = CONFIG_PATH.resolveSibling("astraclient.properties.bak");
            if (!Files.isRegularFile(backupPath)) {
                System.err.println("[Astra Client] No valid config backup found; keeping defaults for unread settings.");
                return;
            }
            try {
                loadConfigFile(backupPath);
                Files.copy(backupPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
                System.err.println("[Astra Client] Recovered settings from the last known-good config backup.");
            } catch (Exception backupFailure) {
                System.err.println("[Astra Client] Config backup recovery failed: " + backupFailure.getMessage());
            }
        }
        if (configNeedsMigration) {
            saveConfig();
            configNeedsMigration = false;
            System.out.println("[Astra Client] Migrated legacy settings to config schema " + CONFIG_SCHEMA_VERSION + ".");
        }
    }

    /**
     * Validate numeric values before applying any setting. This keeps a malformed file
     * from partially changing the live in-memory configuration before recovery begins.
     */
    private static void validateConfigProperties(Properties properties) {
        for (String key : List.of(
            "config.version", "hud.opacity", "theme.accentTheme", "gui.keybind", "hud.editorKey", "gui.scale",
            "zoom.fov", "crosshair.size", "crosshair.gap", "crosshair.thickness",
            "breadcrumbs.length", "waypoint.x", "waypoint.y", "waypoint.z",
            "autoeat.hungerThreshold", "lowhealth.threshold", "effectalert.thresholdSeconds",
            "blockfinder.range", "storagefinder.range", "esp.range", "combat.range",
            "combat.killauraFov"
        )) {
            String value = properties.getProperty(key);
            if (value != null) Integer.parseInt(value);
        }
        String timePreset = properties.getProperty("timechanger.preset");
        if (timePreset != null) Long.parseLong(timePreset);
        for (String id : HUD_WIDGET_IDS) {
            String x = properties.getProperty("hud." + id + ".x");
            String y = properties.getProperty("hud." + id + ".y");
            if (x != null) Integer.parseInt(x);
            if (y != null) Integer.parseInt(y);
        }
        int version = Integer.parseInt(properties.getProperty("config.version", "0"));
        if (version < 0) throw new IllegalArgumentException("Negative Astra config version");
    }

    private static void loadConfigFile(Path sourcePath) throws Exception {
        Properties properties = new Properties();
        int loadedVersion;
        try (InputStream input = Files.newInputStream(sourcePath)) {
            properties.load(input);
            validateConfigProperties(properties);
            loadedVersion = Integer.parseInt(properties.getProperty("config.version", "0"));
            if (loadedVersion > CONFIG_SCHEMA_VERSION) {
                System.err.println("[Astra Client] Config schema " + loadedVersion
                    + " is newer than supported schema " + CONFIG_SCHEMA_VERSION
                    + "; known settings will be loaded where possible.");
            }
            // Version 0 predates the explicit theme key; retain support for its legacy alias.
            if (loadedVersion == 0 && !properties.containsKey("theme.accentTheme")
                && properties.containsKey("theme.alternateAccent")) {
                properties.setProperty("theme.accentTheme",
                    Boolean.parseBoolean(properties.getProperty("theme.alternateAccent")) ? "1" : "0");
            }
            clickSounds = Boolean.parseBoolean(properties.getProperty("ui.clickSounds", "true"));
            hudOpacity = Math.max(35, Math.min(100, Integer.parseInt(properties.getProperty("hud.opacity", "84"))));
            hudBackgrounds = Boolean.parseBoolean(properties.getProperty("hud.backgrounds", "true"));
            customMenuBackground = Boolean.parseBoolean(properties.getProperty("graphics.menuBackground", "true"));
            autoSprintGroundOnly = Boolean.parseBoolean(properties.getProperty("autosprint.groundOnly", "true"));
            autoSprintDisableInMenus = Boolean.parseBoolean(properties.getProperty("autosprint.disableInMenus", "true"));
            for (String id : MODULES.keySet()) {
                MODULES.put(id, Boolean.parseBoolean(
                    properties.getProperty("module." + id, Boolean.toString(MODULES.get(id)))
                ));
            }
            timeChangerPreset = Math.max(0L, Math.min(24000L, Long.parseLong(properties.getProperty("timechanger.preset", "6000"))));
            accentTheme = Math.max(0, Math.min(3, Integer.parseInt(
                properties.getProperty("theme.accentTheme",
                    Boolean.parseBoolean(properties.getProperty("theme.alternateAccent", "false")) ? "1" : "0")
            )));
            customAccentEnabled = Boolean.parseBoolean(properties.getProperty("theme.customEnabled", "false"));
            String savedCustomAccent = properties.getProperty("theme.customColor", "AA70FF").trim().replace("#", "");
            try {
                customAccentColor = 0xFF000000 | (Integer.parseInt(savedCustomAccent, 16) & 0x00FFFFFF);
            } catch (NumberFormatException ignored) {
                customAccentColor = 0xFFAA70FF;
                customAccentEnabled = false;
            }
            guiKeybindCode = Integer.parseInt(properties.getProperty("gui.keybind", Integer.toString(GLFW.GLFW_KEY_RIGHT_SHIFT)));
            if (guiKeybindCode < GLFW.GLFW_KEY_SPACE || guiKeybindCode > GLFW.GLFW_KEY_LAST) guiKeybindCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
            hudEditKeybindCode = Integer.parseInt(properties.getProperty("hud.editorKey", Integer.toString(GLFW.GLFW_KEY_F6)));
            if (hudEditKeybindCode < GLFW.GLFW_KEY_SPACE || hudEditKeybindCode > GLFW.GLFW_KEY_LAST) hudEditKeybindCode = GLFW.GLFW_KEY_F6;
            guiScale = Math.max(75, Math.min(125,
                Integer.parseInt(properties.getProperty("gui.scale", "100"))));
            zoomFov = Math.max(10, Math.min(60,
                Integer.parseInt(properties.getProperty("zoom.fov", "30"))));
            crosshairSize = Math.max(3, Math.min(20, Integer.parseInt(properties.getProperty("crosshair.size", "7"))));
            crosshairGap = Math.max(1, Math.min(12, Integer.parseInt(properties.getProperty("crosshair.gap", "3"))));
            crosshairThickness = Math.max(1, Math.min(5, Integer.parseInt(properties.getProperty("crosshair.thickness", "2"))));
            breadcrumbTrailLength = Math.max(12, Math.min(120, Integer.parseInt(properties.getProperty("breadcrumbs.length", "48"))));
            waypointSet = Boolean.parseBoolean(properties.getProperty("waypoint.set", "false"));
            waypointX = Integer.parseInt(properties.getProperty("waypoint.x", "0"));
            waypointY = Integer.parseInt(properties.getProperty("waypoint.y", "0"));
            waypointZ = Integer.parseInt(properties.getProperty("waypoint.z", "0"));
            tracerPlayers = Boolean.parseBoolean(properties.getProperty("tracers.players", "true"));
            tracerMobs = Boolean.parseBoolean(properties.getProperty("tracers.mobs", "true"));
            autoEatHungerThreshold = Math.max(10, Math.min(19,
                Integer.parseInt(properties.getProperty("autoeat.hungerThreshold", "16"))));
            lowHealthThreshold = Math.max(2, Math.min(20,
                Integer.parseInt(properties.getProperty("lowhealth.threshold", "6"))));
            effectAlertThresholdSeconds = Math.max(3, Math.min(30,
                Integer.parseInt(properties.getProperty("effectalert.thresholdSeconds", "10"))));
            String savedFinderTarget = properties.getProperty("blockfinder.target", "DIAMOND").trim().toUpperCase(Locale.ROOT);
            blockFinderTarget = savedFinderTarget;
            boolean knownFinderTarget = false;
            for (String candidate : BLOCK_FINDER_TARGETS) if (candidate.equals(blockFinderTarget)) knownFinderTarget = true;
            if (!knownFinderTarget) blockFinderTarget = "DIAMOND";
            blockFinderRange = Math.max(8, Math.min(48,
                Integer.parseInt(properties.getProperty("blockfinder.range", "24"))));
            storageFinderRange = Math.max(8, Math.min(32,
                Integer.parseInt(properties.getProperty("storagefinder.range", "16"))));
            espRange = Math.max(16, Math.min(128,
                Integer.parseInt(properties.getProperty("esp.range", "64"))));
            combatRange = Math.max(3, Math.min(6,
                Integer.parseInt(properties.getProperty("combat.range", "4"))));
            killAuraFov = Math.max(60, Math.min(180, Integer.parseInt(properties.getProperty("combat.killauraFov", "180"))));
            killAuraCriticalOnly = Boolean.parseBoolean(properties.getProperty("combat.killauraCriticalOnly", "false"));
            targetPlayers = Boolean.parseBoolean(properties.getProperty("combat.targetPlayers", "true"));
            targetHostile = Boolean.parseBoolean(properties.getProperty("combat.targetHostile", "true"));
            targetPassive = Boolean.parseBoolean(properties.getProperty("combat.targetPassive", "false"));
            for (String id : MODULE_KEYBINDS.keySet()) {
                String savedKey = properties.getProperty("keybind." + id,
                    Integer.toString(GLFW.GLFW_KEY_UNKNOWN));
                int parsedKey;
                try {
                    parsedKey = Integer.parseInt(savedKey);
                } catch (NumberFormatException malformedKeybind) {
                    // One bad keybind should not discard otherwise valid saved settings.
                    parsedKey = GLFW.GLFW_KEY_UNKNOWN;
                }
                MODULE_KEYBINDS.put(id, normalizeKeyCode(parsedKey));
            }
            for (String id : HUD_WIDGET_IDS) {
                String x = properties.getProperty("hud." + id + ".x");
                String y = properties.getProperty("hud." + id + ".y");
                if (x != null) HUD_X.put(id, Integer.parseInt(x));
                if (y != null) HUD_Y.put(id, Integer.parseInt(y));
            }
        }
        configNeedsMigration = loadedVersion < CONFIG_SCHEMA_VERSION;
    }

    private static void backupExistingConfig(Path configDirectory) {
        if (!Files.isRegularFile(CONFIG_PATH)) return;
        Path backupTemp = null;
        try {
            backupTemp = Files.createTempFile(configDirectory, "astraclient-backup-", ".tmp");
            Files.copy(CONFIG_PATH, backupTemp, StandardCopyOption.REPLACE_EXISTING);
            Path backupPath = CONFIG_PATH.resolveSibling("astraclient.properties.bak");
            try {
                Files.move(backupTemp, backupPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(backupTemp, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            // A backup failure must never prevent the primary settings from being saved.
            System.err.println("[Astra Client] Could not refresh config backup: " + exception.getMessage());
        } finally {
            if (backupTemp != null) {
                try { Files.deleteIfExists(backupTemp); }
                catch (Exception ignored) { }
            }
        }
    }

    public static void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty("config.version", Integer.toString(CONFIG_SCHEMA_VERSION));
        for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
            properties.setProperty("module." + entry.getKey(), Boolean.toString(entry.getValue()));
        }
        properties.setProperty("theme.accentTheme", Integer.toString(accentTheme));
        properties.setProperty("timechanger.preset", Long.toString(timeChangerPreset));
        properties.setProperty("theme.customEnabled", Boolean.toString(customAccentEnabled));
        properties.setProperty("theme.customColor", String.format(Locale.ROOT, "%06X", customAccentColor & 0x00FFFFFF));
        properties.setProperty("ui.clickSounds", Boolean.toString(clickSounds));
        properties.setProperty("autosprint.groundOnly", Boolean.toString(autoSprintGroundOnly));
        properties.setProperty("autosprint.disableInMenus", Boolean.toString(autoSprintDisableInMenus));
        properties.setProperty("gui.keybind", Integer.toString(guiKeybindCode));
        properties.setProperty("hud.editorKey", Integer.toString(hudEditKeybindCode));
        properties.setProperty("gui.scale", Integer.toString(guiScale));
        properties.setProperty("hud.opacity", Integer.toString(hudOpacity));
        properties.setProperty("hud.backgrounds", Boolean.toString(hudBackgrounds));
        properties.setProperty("graphics.menuBackground", Boolean.toString(customMenuBackground));
        properties.setProperty("zoom.fov", Integer.toString(zoomFov));
        properties.setProperty("crosshair.size", Integer.toString(crosshairSize));
        properties.setProperty("crosshair.gap", Integer.toString(crosshairGap));
        properties.setProperty("crosshair.thickness", Integer.toString(crosshairThickness));
        properties.setProperty("breadcrumbs.length", Integer.toString(breadcrumbTrailLength));
        properties.setProperty("waypoint.set", Boolean.toString(waypointSet));
        properties.setProperty("waypoint.x", Integer.toString(waypointX));
        properties.setProperty("waypoint.y", Integer.toString(waypointY));
        properties.setProperty("waypoint.z", Integer.toString(waypointZ));
        properties.setProperty("tracers.players", Boolean.toString(tracerPlayers));
        properties.setProperty("tracers.mobs", Boolean.toString(tracerMobs));
        properties.setProperty("autoeat.hungerThreshold", Integer.toString(autoEatHungerThreshold));
        properties.setProperty("lowhealth.threshold", Integer.toString(lowHealthThreshold));
        properties.setProperty("effectalert.thresholdSeconds", Integer.toString(effectAlertThresholdSeconds));
        properties.setProperty("blockfinder.target", blockFinderTarget);
        properties.setProperty("blockfinder.range", Integer.toString(blockFinderRange));
        properties.setProperty("storagefinder.range", Integer.toString(storageFinderRange));
        properties.setProperty("esp.range", Integer.toString(espRange));
        properties.setProperty("combat.range", Integer.toString(combatRange));
        properties.setProperty("combat.killauraFov", Integer.toString(killAuraFov));
        properties.setProperty("combat.killauraCriticalOnly", Boolean.toString(killAuraCriticalOnly));
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
        Path temporaryFile = null;
        try {
            Path configDirectory = CONFIG_PATH.getParent();
            Files.createDirectories(configDirectory);
            // Write a complete snapshot beside the destination, then swap it into place.
            // A crash or storage interruption during serialization will not truncate the last good config.
            temporaryFile = Files.createTempFile(configDirectory, "astraclient-", ".tmp");
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                properties.store(output, "Astra Client settings");
                output.flush();
            }
            // Preserve the previous complete snapshot before replacing it.
            // Backup errors are logged but never block a normal save.
            backupExistingConfig(configDirectory);
            try {
                Files.move(temporaryFile, CONFIG_PATH,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile, CONFIG_PATH,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            System.err.println("[Astra Client] Could not save config: " + exception.getMessage());
        } finally {
            if (temporaryFile != null) {
                try { Files.deleteIfExists(temporaryFile); }
                catch (Exception cleanupException) {
                    System.err.println("[Astra Client] Could not clean temporary config: " + cleanupException.getMessage());
                }
            }
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

    /** Logs lightweight runtime information without making assumptions about optional renderer compatibility. */
    private static void runStartupDiagnostics() {
        int javaFeature = Runtime.version().feature();
        boolean sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        boolean irisLoaded = FabricLoader.getInstance().isModLoaded("iris");

        System.out.println("[Astra Client] Runtime check: Java " + javaFeature
            + " | target Minecraft 1.21.4 | registered modules " + MODULES.size());
        System.out.println("[Astra Client] HUD widgets " + HUD_WIDGET_IDS.length
            + " | saved config present " + Files.isRegularFile(CONFIG_PATH));
        System.out.println("[Astra Client] Optional renderer detection: Sodium=" + sodiumLoaded
            + ", Iris=" + irisLoaded + ".");
        if (javaFeature < 21) {
            System.err.println("[Astra Client] WARNING: Minecraft 1.21.4 targets Java 21 or newer.");
        }
        if (sodiumLoaded || irisLoaded) {
            System.out.println("[Astra Client] Renderer note: custom overlays use Fabric render callbacks; "
                + "compatibility can vary by renderer and shader versions.");
        }
    }

    @Override
    public void onInitializeClient() {
        loadConfig();
        runStartupDiagnostics();

        // Intercept only Astra's local dot-command; never send it to a multiplayer server.
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message == null) return true;
            String trimmed = message.trim();
            if (trimmed.equalsIgnoreCase(".configs") || trimmed.toLowerCase(Locale.ROOT).startsWith(".configs ")) {
                handleConfigsChatCommand(trimmed);
                return false;
            }
            if (trimmed.equalsIgnoreCase(".astra") || trimmed.toLowerCase(Locale.ROOT).startsWith(".astra ")) {
                handleAstraChatCommand(trimmed);
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long guiWindow = client.getWindow() == null ? 0L : client.getWindow().getHandle();
            updateChatHudEditor(client);
            updateDamageTracker(client);
            updateBlockFinder(client);
            updateStorageFinder(client);
            updateXrayOres(client);
            updateTracers(client);
            updateBreadcrumbs(client);
            updateTimeChanger(client);
            boolean guiDown = guiWindow != 0L && guiKeybindCode != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(guiWindow, guiKeybindCode) == GLFW.GLFW_PRESS;
            if (guiDown && !guiKeyWasDown) {
                if (client.currentScreen == null) {
                    client.setScreen(new AstraScreen());
                } else if (client.currentScreen instanceof AstraScreen) {
                    ((AstraScreen) client.currentScreen).requestAnimatedClose();
                }
            }
            guiKeyWasDown = guiDown;
            processModuleKeybinds(client);
            updateInventoryMove(client);
            updateAutoEat(client);
            updateAutoWalk(client);
            updateCombatModules(client);
            updateAimAssist(client);
            updateAutoTotem(client);
            updateNoSlow(client);
            updateSpeed(client);
            updateScaffold(client);
            updateAntiKnockback(client);
            updateTargetEspParticles(client);
            updatePlayerTrail(client);
            updateOrbitSparks(client);
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
        WorldRenderEvents.AFTER_ENTITIES.register(AstraClient::renderOrbitCubes);
        WorldRenderEvents.AFTER_ENTITIES.register(AstraClient::renderVisualOverlays);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> restoreZoom(client));

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }

    /** Gentle aim assistance only while the user is actively holding attack. */
    private static void updateAimAssist(MinecraftClient client) {
        if (!isEnabled("aimassist") || client.player == null || client.world == null
            || client.currentScreen != null || client.player.isSpectator()
            || client.getWindow() == null
            || org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) != org.lwjgl.glfw.GLFW.GLFW_PRESS) return;

        PlayerEntity player = client.player;
        LivingEntity best = null;
        double bestDistance = 4.0D;
        for (LivingEntity candidate : client.world.getEntitiesByClass(
            LivingEntity.class, player.getBoundingBox().expand(4.0D),
            entity -> isValidCombatTarget(entity, player) && player.canSee(entity))) {
            double distance = player.distanceTo(candidate);
            if (distance > bestDistance || !hasClearLineOfSight(client, candidate)) continue;
            var delta = candidate.getEyePos().subtract(player.getEyePos());
            double desiredYaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
            double yawDifference = wrapDegrees(desiredYaw - player.getYaw());
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            double desiredPitch = -Math.toDegrees(Math.atan2(delta.y, horizontal));
            double pitchDifference = desiredPitch - player.getPitch();
            if (Math.abs(yawDifference) > 24.0D || Math.abs(pitchDifference) > 20.0D) continue;
            best = candidate;
            bestDistance = distance;
        }
        if (best == null) return;

        var delta = best.getEyePos().subtract(player.getEyePos());
        double desiredYaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double desiredPitch = -Math.toDegrees(Math.atan2(delta.y, horizontal));
        float yawStep = (float) Math.max(-3.0D, Math.min(3.0D, wrapDegrees(desiredYaw - player.getYaw()) * 0.16D));
        float pitchStep = (float) Math.max(-2.0D, Math.min(2.0D, (desiredPitch - player.getPitch()) * 0.16D));
        player.setYaw(player.getYaw() + yawStep);
        player.setPitch(Math.max(-90.0F, Math.min(90.0F, player.getPitch() + pitchStep)));
    }

    /** Keeps a totem in the offhand when one is available in the player's inventory. */
    private static void updateAutoTotem(MinecraftClient client) {
        if (!isEnabled("autototem") || client.player == null || client.world == null
            || client.interactionManager == null || client.currentScreen != null
            || client.player.isSpectator() || client.player.age % 10 != 0
            || client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            ItemStack stack = client.player.getInventory().getStack(inventorySlot);
            if (!stack.isOf(Items.TOTEM_OF_UNDYING)) continue;
            int handlerSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
            client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,
                handlerSlot, 40, SlotActionType.SWAP, client.player);
            return;
        }
    }

    /** Client-side movement helpers; they never change packet handling or anti-cheat behavior. */
    private static void updateNoSlow(MinecraftClient client) {
        if (!isEnabled("noslow") || client.player == null || client.world == null
            || client.currentScreen != null || !client.player.isUsingItem()) return;
        var player = client.player;
        var velocity = player.getVelocity();
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizontal < 0.01D || horizontal >= 0.30D) return;
        double factor = Math.min(1.12D, 0.30D / horizontal);
        player.setVelocity(velocity.x * factor, velocity.y, velocity.z * factor);
    }

    private static void updateSpeed(MinecraftClient client) {
        if (!isEnabled("speed") || client.player == null || client.world == null
            || client.currentScreen != null || client.player.isSneaking()) return;
        var player = client.player;
        if (Math.abs(player.forwardSpeed) < 0.01F && Math.abs(player.sidewaysSpeed) < 0.01F) return;
        var velocity = player.getVelocity();
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizontal < 0.01D || horizontal >= 0.36D) return;
        double factor = Math.min(1.13D, 0.36D / horizontal);
        player.setVelocity(velocity.x * factor, velocity.y, velocity.z * factor);
    }

    /** Places a held block beside existing support when the player is bridging over an edge. */
    private static void updateScaffold(MinecraftClient client) {
        if (!isEnabled("scaffold") || client.player == null || client.world == null
            || client.interactionManager == null || client.currentScreen != null
            || client.player.isSpectator() || client.player.age % 4 != 0) return;
        PlayerEntity player = client.player;
        if (Math.abs(player.forwardSpeed) < 0.01F && Math.abs(player.sidewaysSpeed) < 0.01F) return;
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) return;

        BlockPos target = BlockPos.ofFloored(player.getX(), player.getY() - 1.0D, player.getZ());
        if (!client.world.getBlockState(target).isReplaceable()) return;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || direction == Direction.UP) continue;
            BlockPos support = target.offset(direction);
            if (!client.world.getBlockState(support).isSolidBlock(client.world, support)) continue;
            Direction face = direction.getOpposite();
            Vec3d hitPos = Vec3d.ofCenter(support).add(
                face.getOffsetX() * 0.5D, face.getOffsetY() * 0.5D, face.getOffsetZ() * 0.5D);
            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
                new BlockHitResult(hitPos, face, support, false));
            return;
        }
    }

    /** A modest local reduction during the hurt animation; server corrections remain authoritative. */
    private static void updateAntiKnockback(MinecraftClient client) {
        if (!isEnabled("antiknockback") || client.player == null || client.world == null
            || client.currentScreen != null || client.player.hurtTime <= 0
            || client.player.age % 2 != 0) return;
        var velocity = client.player.getVelocity();
        client.player.setVelocity(velocity.x * 0.65D, velocity.y, velocity.z * 0.65D);
    }

    /**
     * Client-side combat helpers. TriggerBot attacks only a valid entity under
     * the crosshair; KillAura searches a short forward cone. Both respect vanilla
     * melee distance, the normal attack cooldown, visibility, and target filters.
     */
    private static void updateCombatModules(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null
            || client.currentScreen != null || client.player.isSpectator()) return;

        // Stay inside ordinary vanilla melee distance. A larger configured search
        // range must not make the client send impossible long-range attacks that
        // multiplayer servers will reject.
        final double attackRange = 3.0D;

        // Auto Clicker repeats ordinary crosshair attacks at a conservative interval;
        // the vanilla attack cooldown and normal melee reach are still enforced.
        if (isEnabled("autoclicker") && client.getWindow() != null
            && org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
            && client.crosshairTarget instanceof EntityHitResult autoHit
            && autoHit.getEntity() instanceof LivingEntity autoTarget
            && isValidCombatTarget(autoTarget, client.player)
            && client.player.distanceTo(autoTarget) <= 3.0D
            && client.player.canSee(autoTarget) && hasClearLineOfSight(client, autoTarget)) {
            long now = System.currentTimeMillis();
            if (now - lastAutoClickAt >= 125L && attackTarget(client, autoTarget)) {
                lastAutoClickAt = now;
                return;
            }
        }

        // TriggerBot is deliberately crosshair-only, but it must not require a
        // critical hit unless the explicit Criticals module is enabled.
        if (isEnabled("triggerbot")
            && client.crosshairTarget instanceof EntityHitResult hit
            && hit.getEntity() instanceof LivingEntity target
            && isValidCombatTarget(target, client.player)
            && client.player.distanceTo(target) <= attackRange
            && client.player.canSee(target) && hasClearLineOfSight(client, target)) {
            if (attackTarget(client, target)) return;
        }

        // If both modules are enabled and TriggerBot has no valid crosshair
        // target (or the attack cooldown is not ready), KillAura can still run.
        if (!isEnabled("killaura")) {
            lastCombatTarget = null;
            return;
        }

        LivingEntity bestTarget = null;
        double bestDistance = attackRange;
        var look = client.player.getRotationVec(1.0F);
        var eye = client.player.getEyePos();

        for (LivingEntity candidate : client.world.getEntitiesByClass(
            LivingEntity.class, client.player.getBoundingBox().expand(attackRange),
            entity -> isValidCombatTarget(entity, client.player))) {
            double distance = client.player.distanceTo(candidate);
            if (distance > bestDistance || !client.player.canSee(candidate)
                || !hasClearLineOfSight(client, candidate)) continue;

            // Prefer targets actually in front of the player, while allowing a
            // generous cone so ordinary mouse movement doesn't make the aura miss.
            var direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            double fovDotThreshold = Math.cos(Math.toRadians(killAuraFov / 2.0D));
            if (look.dotProduct(direction) < fovDotThreshold) continue;

            bestTarget = candidate;
            bestDistance = distance;
        }

        lastCombatTarget = bestTarget;
        // Critical-only is an optional setting, never a hidden requirement for normal attacks.
        if (bestTarget != null && (!killAuraCriticalOnly || isCriticalHitReady(client.player))) {
            attackTarget(client, bestTarget);
        }
    }

    /**
     * Target ESP particles: while Kill Aura is enabled, a small stream of
     * theme-colored-independent spark particles marks the entity currently selected
     * by the aura. This is client-side visual feedback only.
     */
    private static int targetEspParticleTick;

    private static void updateTargetEspParticles(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null
            || !isEnabled("killaura") || lastCombatTarget == null || !lastCombatTarget.isAlive()
            || client.player.distanceTo(lastCombatTarget) > 3.0D) {
            targetEspParticleTick = 0;
            return;
        }

        // Throttle particle emission to keep the effect lightweight on lower-end devices.
        if (++targetEspParticleTick % 3 != 0) return;
        var box = lastCombatTarget.getBoundingBox();
        double x = (box.minX + box.maxX) * 0.5D;
        double z = (box.minZ + box.maxZ) * 0.5D;
        double y = box.minY + 0.15D + Math.random() * Math.max(0.2D, box.getLengthY());
        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2.0D;
            double radius = 0.25D + Math.random() * 0.25D;
            client.world.addParticle(ParticleTypes.END_ROD,
                x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius,
                0.0D, 0.025D, 0.0D);
        }
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

    /**
     * Entity.canSee checks entity visibility, not whether a solid block is between
     * the player and target. Raycast the block world as well so combat modules
     * never select targets through walls.
     */
    private static boolean hasClearLineOfSight(MinecraftClient client, Entity target) {
        if (client.player == null || client.world == null || target == null) return false;
        var start = client.player.getEyePos();
        var end = target.getEyePos();
        var hit = client.world.raycast(new RaycastContext(
            start, end, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, client.player
        ));
        return hit.getType() == HitResult.Type.MISS;
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

    private static boolean attackTarget(MinecraftClient client, LivingEntity target) {
        if (client.player == null || client.interactionManager == null
            || client.currentScreen != null || target == null || !target.isAlive()
            || client.player.isSpectator()
            || (isEnabled("criticals") && !isCriticalHitReady(client.player))
            || client.player.distanceTo(target) > 3.0D
            || client.player.getAttackCooldownProgress(0.0F) < 0.9F) return false;
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    /** Lets keyboard movement bindings remain active while the vanilla inventory is open. */
    /** Simple client-side flight: jump rises, sneak descends, neutral input holds altitude. */
    private static boolean autoEatWasForcing;
    private static int autoEatOriginalSlot = -1;
    private static int autoEatSelectedSlot = -1;
    private static boolean autoEatNoFoodNotified;
    private static int autoEatHungerThreshold = 16;
    private static int lowHealthThreshold = 6;
    private static int effectAlertThresholdSeconds = 10;
    private static final String[] BLOCK_FINDER_TARGETS = {
        "DIAMOND", "ANCIENT DEBRIS", "EMERALD", "GOLD", "IRON", "COAL",
        "COPPER", "LAPIS", "REDSTONE", "QUARTZ"
    };
    private static String blockFinderTarget = "DIAMOND";
    private static int blockFinderRange = 24;
    private static int storageFinderRange = 16;
    private static BlockPos nearestStorageBlock;
    private static double nearestStorageDistance = Double.POSITIVE_INFINITY;
    private static String nearestStorageType = "Storage";
    private static long lastStorageScanAt;
    private static Object storageFinderWorld;
    private static long lastBlockFinderScanAt;
    private static BlockPos nearestFoundBlock;
    private static double nearestFoundDistance = Double.POSITIVE_INFINITY;
    private static Object blockFinderWorld;
    private static final List<LivingEntity> cachedTracerTargets = new ArrayList<>();
    private static long lastTracerScanAt;
    private static Object tracerWorld;
    private static boolean tracerPlayers = true;
    private static boolean tracerMobs = true;
    private static boolean autoWalkWasForcing;
    /**
     * Uses food from the hotbar when hunger is low. It stops when the player
     * changes slots, opens a screen, or has enough food, and restores the old slot.
     */
    private static void updateAutoEat(MinecraftClient client) {
        if (client.options == null) return;
        PlayerEntity player = client.player;
        boolean wantsFood = isEnabled("autoeat") && player != null && client.world != null
            && client.currentScreen == null && player.getHungerManager().getFoodLevel() <= autoEatHungerThreshold;

        if (autoEatWasForcing && (!wantsFood || player == null
                || player.getInventory().selectedSlot != autoEatSelectedSlot)) {
            long window = client.getWindow() == null ? 0L : client.getWindow().getHandle();
            if (window != 0L) setPhysicalKey(client, client.options.useKey, window);
            else client.options.useKey.setPressed(false);
            if (player != null && autoEatOriginalSlot >= 0
                    && player.getInventory().selectedSlot == autoEatSelectedSlot) {
                player.getInventory().selectedSlot = autoEatOriginalSlot;
            }
            autoEatWasForcing = false;
            autoEatOriginalSlot = -1;
            autoEatSelectedSlot = -1;
            autoEatNoFoodNotified = false;
        }

        if (!wantsFood || autoEatWasForcing || player == null) return;
        int selected = player.getInventory().selectedSlot;
        int foodSlot = -1;
        for (int offset = 0; offset < 9; offset++) {
            int slot = (selected + offset) % 9;
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.contains(DataComponentTypes.FOOD)) {
                foodSlot = slot;
                break;
            }
        }
        if (foodSlot < 0) {
            if (!autoEatNoFoodNotified) notifyUser("Auto Eat: no food in hotbar");
            autoEatNoFoodNotified = true;
            return;
        }
        autoEatNoFoodNotified = false;
        autoEatOriginalSlot = selected;
        autoEatSelectedSlot = foodSlot;
        player.getInventory().selectedSlot = foodSlot;
        client.options.useKey.setPressed(true);
        autoEatWasForcing = true;
    }

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
        HUD_RENDERED_BOUNDS.clear();
        if (client.player == null || client.world == null) return;

        int accent = accentColor();
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        if (isEnabled("reach")) renderReachInfo(client, context, screenWidth, accent);
        if (isEnabled("tracers")) renderTracers(client, context, screenWidth, screenHeight, accent);
        if (isEnabled("nametags")) renderNametags(client, context, screenWidth, screenHeight, accent);
        if (isEnabled("breadcrumbs")) renderBreadcrumbs(client, context, screenWidth, screenHeight, accent);

        // A subtle pulsing warning stays out of the way until health is critical.
        if (isEnabled("lowhealthalert") && client.player.getHealth() > 0.0F
                && client.player.getHealth() <= lowHealthThreshold) {
            int alpha = (int) (32 + (Math.sin(System.currentTimeMillis() / 180.0) + 1.0) * 30);
            int warningTint = (alpha << 24) | 0x00FF405D;
            context.fill(0, 0, screenWidth, 3, warningTint);
            context.fill(0, screenHeight - 3, screenWidth, screenHeight, warningTint);
            context.fill(0, 0, 3, screenHeight, warningTint);
            context.fill(screenWidth - 3, 0, screenWidth, screenHeight, warningTint);
            String warning = "LOW HEALTH";
            context.drawTextWithShadow(client.textRenderer, warning,
                    screenWidth / 2 - client.textRenderer.getWidth(warning) / 2,
                    screenHeight / 2 + 18, 0xFFFF657A);
        }

        // Minimal custom crosshair overlay; work is done only while the module is enabled.
        if (isEnabled("customcrosshair")) {
            int cx = context.getScaledWindowWidth() / 2;
            int cy = screenHeight / 2;
            int color = client.crosshairTarget instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity ? 0xFFFF657A
                : client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK
                    ? 0xFF69E6B8 : accent;
            int halfThickness = crosshairThickness / 2;
            int left = cx - halfThickness;
            int right = left + crosshairThickness;
            context.fill(left, cy - crosshairGap - crosshairSize, right, cy - crosshairGap, color);
            context.fill(left, cy + crosshairGap, right, cy + crosshairGap + crosshairSize, color);
            context.fill(cx - crosshairGap - crosshairSize, cy - halfThickness,
                cx - crosshairGap, cy - halfThickness + crosshairThickness, color);
            context.fill(cx + crosshairGap, cy - halfThickness,
                cx + crosshairGap + crosshairSize, cy - halfThickness + crosshairThickness, color);
            context.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
        }

        if (isEnabled("compasshud")) {
            int compassX = Math.max(4, Math.min(screenWidth - 176, hudX("compasshud", screenWidth / 2 - 84)));
            int compassY = Math.max(4, Math.min(screenHeight - 28, hudY("compasshud", 178)));
            int yaw = Math.floorMod((int) Math.round(client.player.getYaw()), 360);
            String heading = client.player.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);
            if (hudBackgrounds) context.fill(compassX, compassY, compassX + 172, compassY + 25, 0xD90A0D16);
            context.fill(compassX, compassY, compassX + 172, compassY + 2, accent);
            context.drawTextWithShadow(client.textRenderer, "COMPASS", compassX + 6, compassY + 6, 0xFF8996AD);
            context.drawTextWithShadow(client.textRenderer, heading + "  " + yaw + "°", compassX + 78, compassY + 6, 0xFFFFFFFF);
            // A yaw-relative compass strip: the center marker is the direction
            // the player is facing, and labels slide as the camera turns.
            String[] compassPoints = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
            int bearing = Math.floorMod(180 + yaw, 360);
            int centerX = compassX + 86;
            for (int slot = -3; slot <= 3; slot++) {
                int slotBearing = Math.floorMod(bearing + slot * 45, 360);
                String point = compassPoints[Math.floorMod(Math.round(slotBearing / 45.0f), 8)];
                int labelX = centerX + slot * 22 - client.textRenderer.getWidth(point) / 2;
                if (labelX >= compassX + 4 && labelX + client.textRenderer.getWidth(point) <= compassX + 168) {
                    int pointColor = slot == 0 ? 0xFFFFFFFF : 0xFF69E6B8;
                    context.drawTextWithShadow(client.textRenderer, point, labelX, compassY + 15, pointColor);
                }
            }
            context.fill(centerX - 1, compassY + 14, centerX + 1, compassY + 24, accent);
            HUD_RENDERED_BOUNDS.put("compasshud", new HudBounds(compassX, compassY, 172, 25));
        }

        if (isEnabled("effectalert")) {
            int alertY = 38;
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                int ticks = effect.getDuration();
                if (ticks < 0 || ticks > effectAlertThresholdSeconds * 20) continue;
                String effectName = effect.getEffectType().value().getName().getString();
                String alert = effectName + " expires in " + formatDuration(ticks);
                int alertX = (screenWidth - client.textRenderer.getWidth(alert)) / 2;
                int alertColor = ticks <= 60 ? 0xFFFF657A : 0xFFFFC266;
                context.fill(alertX - 5, alertY - 2, alertX + client.textRenderer.getWidth(alert) + 5, alertY + 12, 0xB80A0D16);
                context.drawTextWithShadow(client.textRenderer, alert, alertX, alertY, alertColor);
                alertY += 16;
                if (alertY > 110) break;
            }
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
            drawHudLine(client, context, "blockreachhud", hudX("blockreachhud", 150), screenHeight - 84, "BLOCK REACH", reach, accent);
        }
        if (isEnabled("frametimehud")) {
            int fps = client.getCurrentFps();
            String frameTime = fps > 0 ? String.format(Locale.ROOT, "%.2f ms", 1000.0 / fps) : "--";
            drawHudLine(client, context, "frametimehud", hudX("frametimehud", 150), screenHeight - 63, "FRAME TIME", frameTime, accent);
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
        if (isEnabled("serverinfo")) {
            var server = client.getCurrentServerEntry();
            String serverName = server == null ? "SINGLEPLAYER" : server.name;
            if (serverName == null || serverName.isBlank()) serverName = server == null ? "LOCAL WORLD" : server.address;
            if (serverName.length() > 30) serverName = serverName.substring(0, 27) + "...";
            String serverPing = "LOCAL";
            if (client.getNetworkHandler() != null) {
                var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (entry != null) serverPing = entry.getLatency() + " ms";
            }
            int serverX = hudX("serverinfo", 8);
            int serverY = hudY("serverinfo", screenHeight - 122);
            drawHudLineAt(client, context, serverX, serverY, "SERVER", serverName + " · " + serverPing, accent);
        }
                if (isEnabled("waypoint")) {
            String value;
            if (!waypointSet) {
                value = "Not set • open module settings";
            } else {
                double dx = waypointX + 0.5 - client.player.getX();
                double dz = waypointZ + 0.5 - client.player.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz
                    + Math.pow(waypointY + 0.5 - client.player.getY(), 2));
                double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
                double relativeYaw = wrapDegrees(targetYaw - client.player.getYaw());
                String bearing = Math.abs(relativeYaw) < 22.5 ? "AHEAD"
                    : Math.abs(relativeYaw) > 157.5 ? "BEHIND"
                    : relativeYaw < 0 ? "RIGHT" : "LEFT";
                value = waypointX + " " + waypointY + " " + waypointZ + " • "
                    + String.format(Locale.ROOT, "%.1f m", distance) + " • " + bearing;
            }
            drawHudLine(client, context, "waypoint", 8, screenHeight - 80, "WAYPOINT", value, accent);
        }

        if (isEnabled("storagefinder")) {
            String value = nearestStorageBlock == null
                ? "No storage within " + storageFinderRange + " blocks"
                : nearestStorageType + " " + nearestStorageBlock.getX() + " "
                    + nearestStorageBlock.getY() + " " + nearestStorageBlock.getZ()
                    + " • " + String.format(Locale.ROOT, "%.1f m", nearestStorageDistance);
            drawHudLine(client, context, "storagefinder", 8, screenHeight - 100, "STORAGE", value, accent);
        }

if (isEnabled("blockfinder")) {
            String value = nearestFoundBlock == null
                ? "No " + blockFinderTarget.toLowerCase(Locale.ROOT) + " within " + blockFinderRange + " blocks"
                : nearestFoundBlock.getX() + " " + nearestFoundBlock.getY() + " " + nearestFoundBlock.getZ()
                    + " · " + String.format(Locale.ROOT, "%.1f m", nearestFoundDistance);
            drawHudLine(client, context, "blockfinder", 150, screenHeight - 60, "BLOCK FINDER", value, accent);
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
            int durabilityColor = lowestPercent < 15 ? 0xFFFF657A
                : lowestPercent < 40 ? 0xFFFFC266 : accent;
            drawHudLine(client, context, "durabilityhud", 8, 323, "LOWEST DURABILITY", value, durabilityColor);
        }
        if (isEnabled("inventoryhud")) {
            ItemStack selected = client.player.getMainHandStack();
            String value = selected.isEmpty() ? "Empty hand"
                : selected.getName().getString() + " ×" + selected.getCount();
            drawHudLine(client, context, "inventoryhud", 8, 344, "HELD ITEM", value, accent);
        }
        if (isEnabled("potionhud")) {
            int effectY = hudY("potionhud", 260);
            int potionX = hudX("potionhud", 150);
            int potionStartY = effectY;
            int potionMaxWidth = 0;
            int potionRows = 0;
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                if (effectY + 18 > screenHeight - 20) break;
                String name = effect.getEffectType().value().getName().getString();
                String duration = effect.getDuration() < 0 ? "∞" : formatDuration(effect.getDuration());
                potionMaxWidth = Math.max(potionMaxWidth, drawHudLineAt(client, context, potionX, effectY, "EFFECT",
                    name + " " + duration, accent));
                effectY += 21;
                potionRows++;
            }
            if (potionRows > 0) HUD_RENDERED_BOUNDS.put("potionhud",
                new HudBounds(potionX, potionStartY, potionMaxWidth, potionRows * 21 - 3));
        }
        Entity hudTarget = client.crosshairTarget instanceof EntityHitResult hit
            ? hit.getEntity()
            : isEnabled("killaura") && lastCombatTarget != null && lastCombatTarget.isAlive()
                && client.player.distanceTo(lastCombatTarget) <= 3.0D ? lastCombatTarget : null;
        if (isEnabled("targethud") && hudTarget != null) {
            Entity entity = hudTarget;
            String targetName = entity.getName().getString();
            String distance = String.format(Locale.ROOT, "%.1f m", client.player.distanceTo(entity));
            String healthText = entity instanceof LivingEntity living
                ? String.format(Locale.ROOT, "%.1f / %.1f HP", Math.max(0.0F, living.getHealth()), living.getMaxHealth())
                : "NON-LIVING ENTITY";
            String heldOrType = entity instanceof PlayerEntity targetPlayer
                ? (targetPlayer.getMainHandStack().isEmpty() ? "Empty hand" : targetPlayer.getMainHandStack().getName().getString())
                : entity.getType().getName().getString();
            if (heldOrType.length() > 28) heldOrType = heldOrType.substring(0, 25) + "...";
            int x = hudX("targethud", 150);
            int y = hudY("targethud", screenHeight - 52);
            int panelWidth = Math.min(context.getScaledWindowWidth() - x - 8,
                Math.max(164, Math.max(client.textRenderer.getWidth(targetName),
                    Math.max(client.textRenderer.getWidth(healthText) + client.textRenderer.getWidth(distance),
                        client.textRenderer.getWidth(heldOrType))) + 24));
            if (hudBackgrounds) context.fill(x, y, x + panelWidth, y + 49, 0xD90B101A);
            HUD_RENDERED_BOUNDS.put("targethud", new HudBounds(x, y, panelWidth, 49));
            context.fill(x, y, x + 3, y + 49, accent);
            context.drawText(client.textRenderer, targetName, x + 10, y + 5, 0xFFF3F5FC, true);
            context.drawText(client.textRenderer, distance, x + panelWidth - client.textRenderer.getWidth(distance) - 8,
                y + 5, accent, false);
            context.drawText(client.textRenderer, heldOrType, x + 10, y + 19, 0xFF9FE7F4, false);
            context.drawText(client.textRenderer, healthText, x + 10, y + 31, 0xFFD5DCEC, false);
            if (entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F) {
                int barX = x + 10, barY = y + 43, barWidth = panelWidth - 20;
                context.fill(barX, barY, barX + barWidth, barY + 3, 0xFF303747);
                int healthWidth = Math.max(0, Math.min(barWidth,
                    Math.round(barWidth * living.getHealth() / living.getMaxHealth())));
                int healthColor = living.getHealth() / living.getMaxHealth() > 0.5F ? 0xFF69E6B8
                    : living.getHealth() / living.getMaxHealth() > 0.25F ? 0xFFFFC266 : 0xFFFF657A;
                context.fill(barX, barY, barX + healthWidth, barY + 3, healthColor);
            }
        }
        if (isEnabled("sessionstats")) {
            long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
            String session = String.format(Locale.ROOT, "%02d:%02d:%02d",
                elapsed / 3600, (elapsed / 60) % 60, elapsed % 60);
            drawHudLine(client, context, "sessionstats", 8, 449, "SESSION", session, accent);
        }

        if (isEnabled("fpsgraph")) {
            long now = System.currentTimeMillis();
            if (now - lastFpsSampleAt >= 150L) {
                FPS_HISTORY[fpsHistoryIndex] = Math.max(0, client.getCurrentFps());
                fpsHistoryIndex = (fpsHistoryIndex + 1) % FPS_HISTORY.length;
                lastFpsSampleAt = now;
            }
            int gx = Math.max(2, Math.min(context.getScaledWindowWidth() - 124, hudX("fpsgraph", 150)));
            int gy = Math.max(2, Math.min(screenHeight - 54, hudY("fpsgraph", 70)));
            int graphWidth = 154;
            int graphHeight = 62;
            HUD_RENDERED_BOUNDS.put("fpsgraph", new HudBounds(gx, gy, graphWidth, graphHeight));
            if (hudBackgrounds) context.fill(gx, gy, gx + graphWidth, gy + graphHeight, 0xD90B101A);
            context.fill(gx, gy, gx + graphWidth, gy + 2, accent);
            int currentFps = Math.max(0, client.getCurrentFps());
            int validSamples = 0, minFps = Integer.MAX_VALUE, maxFps = 0, sumFps = 0;
            for (int sample : FPS_HISTORY) {
                if (sample <= 0) continue;
                validSamples++;
                minFps = Math.min(minFps, sample);
                maxFps = Math.max(maxFps, sample);
                sumFps += sample;
            }
            int averageFps = validSamples == 0 ? currentFps : sumFps / validSamples;
            String graphTitle = "FPS  " + currentFps + "  AVG " + averageFps;
            context.drawText(client.textRenderer, graphTitle, gx + 5, gy + 5, 0xFFF2F5FC, false);
            int baseY = gy + graphHeight - 5;
            int graphTop = gy + 24;
            context.fill(gx + 5, baseY, gx + graphWidth - 5, baseY + 1, 0xFF465064);
            context.fill(gx + 5, graphTop, gx + graphWidth - 5, graphTop + 1, 0x553F4B61);
            int plotWidth = graphWidth - 10;
            for (int i = 0; i < FPS_HISTORY.length; i++) {
                int sample = FPS_HISTORY[(fpsHistoryIndex + i) % FPS_HISTORY.length];
                if (sample <= 0) continue;
                int barHeight = Math.max(1, Math.min(baseY - graphTop - 2, Math.round(sample / 240.0f * (baseY - graphTop - 2))));
                int bx = gx + 5 + i * plotWidth / FPS_HISTORY.length;
                int by = baseY - barHeight;
                int barColor = sample >= 60 ? 0xFF69E6B8 : sample >= 30 ? accent : 0xFFFF829A;
                context.fill(bx, by, Math.min(gx + graphWidth - 5, bx + Math.max(1, plotWidth / FPS_HISTORY.length - 1)), baseY, barColor);
            }
            String range = validSamples == 0 ? "MIN --  MAX --" : "MIN " + minFps + "   MAX " + maxFps;
            context.drawText(client.textRenderer, range, gx + 5, gy + graphHeight - 15, 0xFFB5BED0, false);
        }

        if (isEnabled("watermark")) {
            String brand = "ASTRA";
            String subtitle = "CLIENT";
            String fpsText = Math.max(0, client.getCurrentFps()) + " FPS";
            String pingText = "LOCAL";
            if (client.getNetworkHandler() != null && client.player != null) {
                var pingEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (pingEntry != null) pingText = Math.max(0, pingEntry.getLatency()) + " MS";
            }
            String stats = fpsText + "  ·  " + pingText;
            int brandWidth = client.textRenderer.getWidth(brand);
            int subtitleWidth = client.textRenderer.getWidth(subtitle);
            int statsWidth = client.textRenderer.getWidth(stats);
            int panelWidth = brandWidth + subtitleWidth + statsWidth + 44;
            int wx = clampInt(hudX("watermark", 8), 0, Math.max(0, screenWidth - panelWidth));
            int wy = clampInt(hudY("watermark", 8), 0, Math.max(0, screenHeight - 24));
            HUD_RENDERED_BOUNDS.put("watermark", new HudBounds(wx, wy, panelWidth, 24));
            if (hudBackgrounds) context.fill(wx, wy, wx + panelWidth, wy + 24, 0xE90B101A);
            context.fill(wx, wy, wx + 3, wy + 24, accent);
            context.fill(wx + panelWidth - 2, wy, wx + panelWidth, wy + 24, 0xFF4AD9F5);
            int shineWidth = Math.max(10, panelWidth / 5);
            int shineX = wx + (int) ((System.currentTimeMillis() / 7L) % (panelWidth + shineWidth)) - shineWidth;
            int shineLeft = Math.max(wx + 3, shineX);
            int shineRight = Math.min(wx + panelWidth - 2, shineX + shineWidth);
            if (shineRight > shineLeft) context.fill(shineLeft, wy + 1, shineRight, wy + 3, 0x80FFFFFF);
            context.drawText(client.textRenderer, brand, wx + 9, wy + 4, 0xFFF2F5FC, true);
            context.drawText(client.textRenderer, subtitle, wx + 12 + brandWidth, wy + 7, 0xFF4AD9F5, false);
            int statsX = wx + 18 + brandWidth + subtitleWidth;
            context.fill(statsX - 5, wy + 5, statsX - 4, wy + 19, 0xFF394155);
            context.drawText(client.textRenderer, stats, statsX, wy + 7, 0xFFD8E1F1, false);
        }

        if (isEnabled("arraylist")) {
            List<String> activeNames = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
                String id = entry.getKey();
                if (!entry.getValue() || id.equals("arraylist") || HUD_WIDGET_ID_SET.contains(id)) continue;
                activeNames.add(readableName(id));
            }
            activeNames.sort((left, right) -> {
                int widthOrder = Integer.compare(client.textRenderer.getWidth(right), client.textRenderer.getWidth(left));
                return widthOrder != 0 ? widthOrder : left.compareToIgnoreCase(right);
            });
            int listX = hudX("arraylist", 150);
            int listY = hudY("arraylist", 8);
            int arrayWidth = activeNames.isEmpty() ? 126
                : activeNames.stream().mapToInt(name -> client.textRenderer.getWidth(name) + 14).max().orElse(126);
            HUD_RENDERED_BOUNDS.put("arraylist", new HudBounds(listX, listY, arrayWidth,
                Math.max(14, activeNames.size() * 14)));
            for (int i = 0; i < activeNames.size(); i++) {
                String name = activeNames.get(i);
                int rowWidth = client.textRenderer.getWidth(name) + 14;
                int rowY = listY + i * 14;
                int rowRight = listX + rowWidth;
                int rowColor = (i % 2 == 0) ? 0xD9121825 : 0xD91C2030;
                if (hudBackgrounds) context.fill(listX, rowY, rowRight, rowY + 13, rowColor);
                context.fill(listX, rowY, listX + 2, rowY + 13, accent);
                context.fill(rowRight - 2, rowY, rowRight, rowY + 13, 0xFF4AD9F5);
                context.drawText(client.textRenderer, name, listX + 7, rowY + 3, 0xFFF2F5FC, false);
            }
        }

        if (System.currentTimeMillis() < notificationUntil && !notification.isEmpty()) {
            String label = "ASTRA  •  " + notification;
            int width = Math.min(context.getScaledWindowWidth() - 16, client.textRenderer.getWidth(label) + 28);
            int nx = context.getScaledWindowWidth() - width - 8;
            if (hudBackgrounds) context.fill(nx, 8, nx + width, 30, (Math.round(230.0f * hudOpacity / 100.0f) << 24) | 0x000C111D);
            context.fill(nx, 8, nx + 3, 30, accent);
            context.drawText(client.textRenderer, label, nx + 10, 15, 0xFFF4F5FC, false);
        }

        if (isEnabled("keystrokes")) {
            int keyX = clampInt(hudX("keystrokes", 8), 0, Math.max(0, screenWidth - 96));
            int savedY = hudY("keystrokes", -110);
            int keyY = clampInt(savedY < 0 ? screenHeight + savedY : savedY, 0, Math.max(0, screenHeight - 85));
            drawKey(context, client, client.options.forwardKey, keyX + 22, keyY, "W", accent);
            drawKey(context, client, client.options.leftKey, keyX, keyY + 22, "A", accent);
            drawKey(context, client, client.options.backKey, keyX + 22, keyY + 22, "S", accent);
            drawKey(context, client, client.options.rightKey, keyX + 44, keyY + 22, "D", accent);
            drawKey(context, client, client.options.jumpKey, keyX + 11, keyY + 44, "JUMP", accent);
            long windowHandle = client.getWindow() == null ? 0L : client.getWindow().getHandle();
            boolean leftMouseDown = windowHandle != 0L
                && org.lwjgl.glfw.GLFW.glfwGetMouseButton(windowHandle, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            boolean rightMouseDown = windowHandle != 0L
                && org.lwjgl.glfw.GLFW.glfwGetMouseButton(windowHandle, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            drawMouseKey(context, client, keyX, keyY + 66, "LMB", leftMouseDown, accent);
            drawMouseKey(context, client, keyX + 48, keyY + 66, "RMB", rightMouseDown, accent);
            HUD_RENDERED_BOUNDS.put("keystrokes", new HudBounds(keyX, keyY, 96, 85));
        }

        if (isEnabled("damageindicators") && System.currentTimeMillis() < damageAlertUntil) {
            String damageText = "-" + String.format(Locale.ROOT, "%.1f", recentDamageAmount) + " HP";
            int panelWidth = client.textRenderer.getWidth(damageText) + 20;
            int x = hudX("damageindicators", screenWidth / 2 - panelWidth / 2);
            int y = hudY("damageindicators", screenHeight / 2 + 26);
            x = clampInt(x, 0, Math.max(0, screenWidth - panelWidth));
            y = clampInt(y, 0, Math.max(0, screenHeight - 18));
            if (hudBackgrounds) context.fill(x, y, x + panelWidth, y + 18, 0xD90B101A);
            context.fill(x, y, x + 3, y + 18, 0xFFFF657A);
            context.drawTextWithShadow(client.textRenderer, damageText, x + 10, y + 5, 0xFFFF8291);
            HUD_RENDERED_BOUNDS.put("damageindicators", new HudBounds(x, y, panelWidth, 18));
        }

        renderChatHudEditingOverlay(client, context);
    }

    private static void updateTimeChanger(MinecraftClient client) {
        if (client.world == null || !isEnabled("timechanger")) return;
        // ClientWorld is local to this client; this does not issue a server command.
        client.world.setTime(client.world.getTime(), timeChangerPreset, false);
    }

    private static void renderOrbitCubes(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!isEnabled("orbitcubes") || client.player == null || client.world == null
                || context.matrixStack() == null || context.consumers() == null) return;

        var playerPos = client.player.getLerpedPos(context.tickCounter().getTickDelta(true));
        var cameraPos = context.camera().getPos();
        double time = (System.currentTimeMillis() % 120000L) / 120000.0D * Math.PI * 2.0D;
        int accent = accentColor();
        float red = ((accent >> 16) & 255) / 255.0F;
        float green = ((accent >> 8) & 255) / 255.0F;
        float blue = (accent & 255) / 255.0F;
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        var matrices = context.matrixStack();

        // Four lightweight glass-like wire cubes orbit the player. Their tint reads
        // the live theme color, so preset and custom accent changes apply immediately.
        for (int i = 0; i < 4; i++) {
            double angle = time * 1.7D + i * (Math.PI / 2.0D);
            double radius = 1.15D + (i % 2) * 0.45D;
            double x = playerPos.x + Math.cos(angle) * radius;
            double z = playerPos.z + Math.sin(angle) * radius;
            double y = playerPos.y + 0.95D + Math.sin(angle * 1.35D + i) * 0.42D;
            float size = 0.22F + (i % 2) * 0.07F;

            matrices.push();
            matrices.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (time * 100.0D + i * 31.0D)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (time * 73.0D + i * 19.0D)));
            VertexRendering.drawBox(matrices, lines,
                new Box(-size, -size, -size, size, size, size),
                red, green, blue, 0.72F);
            matrices.pop();
        }
    }

    private static void updatePlayerTrail(MinecraftClient client) {
        if (!isEnabled("playertrail") || client.player == null || client.world == null
                || client.player.age % 3 != 0) return;
        var player = client.player;
        double backX = -Math.sin(Math.toRadians(player.getYaw())) * 0.28D;
        double backZ = Math.cos(Math.toRadians(player.getYaw())) * 0.28D;
        // Client-side particles only; this visual sends no gameplay packets.
        client.world.addParticle(ParticleTypes.END_ROD,
            player.getX() + backX + (Math.random() - 0.5D) * 0.16D,
            player.getY() + 0.12D + Math.random() * 0.22D,
            player.getZ() + backZ + (Math.random() - 0.5D) * 0.16D,
            0.0D, 0.015D, 0.0D);
    }

    private static void updateOrbitSparks(MinecraftClient client) {
        if (!isEnabled("orbitsparks") || client.player == null || client.world == null
                || client.player.age % 2 != 0) return;
        var player = client.player;
        double time = player.age * 0.18D;
        for (int i = 0; i < 2; i++) {
            double angle = time + i * Math.PI;
            double radius = 0.82D + i * 0.12D;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 0.65D + Math.sin(time * 1.4D + i) * 0.35D;
            client.world.addParticle(ParticleTypes.END_ROD, x, y, z,
                -Math.sin(angle) * 0.015D, 0.025D, Math.cos(angle) * 0.015D);
        }
    }

    private static void renderVisualOverlays(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || context.matrixStack() == null
                || context.consumers() == null) return;

        boolean showXray = isEnabled("xray") && !XRAY_ORE_POSITIONS.isEmpty();
        boolean showHitboxes = isEnabled("hitboxes");
        boolean showTargetBox = isEnabled("targetbox") && lastCombatTarget != null
            && lastCombatTarget.isAlive() && !lastCombatTarget.isRemoved()
            && lastCombatTarget.getWorld() == client.world;
        boolean showTargetHalo = isEnabled("targethalo") && lastCombatTarget != null
            && lastCombatTarget.isAlive() && !lastCombatTarget.isRemoved()
            && lastCombatTarget.getWorld() == client.world;
        boolean showBlockHighlight = isEnabled("blockhighlight")
            && client.crosshairTarget instanceof BlockHitResult blockHit
            && client.crosshairTarget.getType() == HitResult.Type.BLOCK;
        if (!showXray && !showHitboxes && !showTargetBox && !showTargetHalo && !showBlockHighlight) return;

        int accent = accentColor();
        float red = ((accent >> 16) & 255) / 255.0F;
        float green = ((accent >> 8) & 255) / 255.0F;
        float blue = (accent & 255) / 255.0F;
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        var matrices = context.matrixStack();
        var cameraPos = context.camera().getPos();
        double rangeSq = (double) espRange * espRange;

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        if (showBlockHighlight && client.crosshairTarget instanceof BlockHitResult blockHit) {
            VertexRendering.drawBox(matrices, lines,
                new Box(blockHit.getBlockPos()).expand(0.002D), red, green, blue, 1.0F);
        }

        // Cached ore outlines use the normal world depth buffer: they highlight loaded ore blocks
        // without changing block visibility, world state, or chunk rendering.
        if (showXray) {
            double xrayRangeSq = 12.0D * 12.0D;
            for (BlockPos orePos : XRAY_ORE_POSITIONS) {
                if (client.player.squaredDistanceTo(orePos.toCenterPos()) > xrayRangeSq) continue;
                int ore = oreColor(orePos, client);
                float oreRed = ((ore >> 16) & 255) / 255.0F;
                float oreGreen = ((ore >> 8) & 255) / 255.0F;
                float oreBlue = (ore & 255) / 255.0F;
                VertexRendering.drawBox(matrices, lines, new Box(orePos).expand(0.003D),
                    oreRed, oreGreen, oreBlue, 0.95F);
            }
        }

        if (showHitboxes) {
            Box nearby = client.player.getBoundingBox().expand(espRange);
            for (Entity entity : client.world.getEntitiesByClass(Entity.class, nearby,
                    candidate -> candidate != client.player && !candidate.isRemoved()
                        && (candidate instanceof LivingEntity || candidate instanceof ItemEntity))) {
                if (client.player.squaredDistanceTo(entity) > rangeSq) continue;
                float entityRed = red, entityGreen = green, entityBlue = blue;
                if (entity instanceof PlayerEntity) {
                    entityRed = 0.28F; entityGreen = 0.92F; entityBlue = 0.78F;
                } else if (entity instanceof ItemEntity) {
                    entityRed = 1.0F; entityGreen = 0.76F; entityBlue = 0.25F;
                }
                VertexRendering.drawBox(matrices, lines, entity.getBoundingBox(),
                    entityRed, entityGreen, entityBlue, 0.9F);
            }
        }

        if ((showTargetBox || showTargetHalo) && client.player.squaredDistanceTo(lastCombatTarget) <= rangeSq) {
            Box targetBounds = lastCombatTarget.getBoundingBox();
            if (showTargetBox) {
                VertexRendering.drawBox(matrices, lines, targetBounds.expand(0.055D),
                    1.0F, 0.18F, 0.52F, 1.0F);
            }
            if (showTargetHalo) {
                double time = System.currentTimeMillis() / 500.0D;
                double centerX = (targetBounds.minX + targetBounds.maxX) * 0.5D;
                double centerZ = (targetBounds.minZ + targetBounds.maxZ) * 0.5D;
                double radius = Math.max(0.62D, Math.max(targetBounds.maxX - targetBounds.minX, targetBounds.maxZ - targetBounds.minZ) * 0.72D);
                double baseY = targetBounds.minY + 0.08D;
                // Two orbiting rings made of tiny wire cubes create a lightweight animated halo.
                for (int ring = 0; ring < 2; ring++) {
                    for (int i = 0; i < 18; i++) {
                        double angle = time + i * (Math.PI * 2.0D / 18.0D) + ring * Math.PI / 18.0D;
                        double x = centerX + Math.cos(angle) * radius;
                        double z = centerZ + Math.sin(angle) * radius;
                        double y = baseY + ring * Math.max(0.45D, (targetBounds.maxY - targetBounds.minY) * 0.42D)
                            + Math.sin(angle * 2.0D) * 0.08D;
                        float size = 0.045F;
                        matrices.push();
                        matrices.translate(x, y, z);
                        VertexRendering.drawBox(matrices, lines,
                            new Box(-size, -size, -size, size, size, size),
                            ring == 0 ? red : 1.0F, ring == 0 ? green : 0.35F,
                            ring == 0 ? blue : 0.85F, 0.95F);
                        matrices.pop();
                    }
                }
            }
        }
        matrices.pop();
    }

    private static void updateBreadcrumbs(MinecraftClient client) {
        if (!isEnabled("breadcrumbs") || client.player == null || client.world == null) {
            BREADCRUMB_TRAIL.clear();
            breadcrumbWorld = null;
            lastBreadcrumbSampleAt = 0L;
            return;
        }
        if (breadcrumbWorld != client.world) {
            breadcrumbWorld = client.world;
            BREADCRUMB_TRAIL.clear();
            lastBreadcrumbSampleAt = 0L;
        }
        long now = System.currentTimeMillis();
        if (now - lastBreadcrumbSampleAt < 250L) return;
        lastBreadcrumbSampleAt = now;
        var pos = client.player.getPos();
        BREADCRUMB_TRAIL.add(new TrailPoint(pos.x, pos.z));
        while (BREADCRUMB_TRAIL.size() > breadcrumbTrailLength) BREADCRUMB_TRAIL.remove(0);
    }

    private static void renderBreadcrumbs(MinecraftClient client, DrawContext context,
                                          int screenWidth, int screenHeight, int accent) {
        if (client.player == null || BREADCRUMB_TRAIL.isEmpty()) return;
        int boxW = 112, boxH = 112;
        int x = clampInt(hudX("breadcrumbs", screenWidth - boxW - 12), 2, Math.max(2, screenWidth - boxW - 2));
        int y = clampInt(hudY("breadcrumbs", screenHeight / 2 - boxH / 2), 2, Math.max(2, screenHeight - boxH - 2));
        if (hudBackgrounds) context.fill(x, y, x + boxW, y + boxH, 0xD90B101A);
        context.fill(x, y, x + boxW, y + 2, accent);
        context.drawText(client.textRenderer, "BREADCRUMBS", x + 7, y + 6, 0xFFEFF2FA, true);
        int centerX = x + boxW / 2;
        int centerY = y + boxH / 2 + 6;
        context.fill(centerX, y + 23, centerX + 1, y + boxH - 6, 0x403C465C);
        context.fill(x + 6, centerY, x + boxW - 6, centerY + 1, 0x403C465C);
        double yaw = Math.toRadians(client.player.getYaw());
        double scale = 1.7D;
        int previousX = Integer.MIN_VALUE, previousY = Integer.MIN_VALUE;
        int start = Math.max(0, BREADCRUMB_TRAIL.size() - breadcrumbTrailLength);
        for (int i = start; i < BREADCRUMB_TRAIL.size(); i++) {
            TrailPoint point = BREADCRUMB_TRAIL.get(i);
            double dx = point.x() - client.player.getX();
            double dz = point.z() - client.player.getZ();
            double localRight = dx * Math.cos(yaw) - dz * Math.sin(yaw);
            double localForward = dx * Math.sin(yaw) + dz * Math.cos(yaw);
            int px = centerX + (int) Math.round(localRight * scale);
            int py = centerY - (int) Math.round(localForward * scale);
            if (px < x + 4 || px > x + boxW - 4 || py < y + 23 || py > y + boxH - 4) {
                previousX = Integer.MIN_VALUE;
                previousY = Integer.MIN_VALUE;
                continue;
            }
            float progress = (i + 1.0F) / Math.max(1, BREADCRUMB_TRAIL.size());
            int alpha = Math.max(45, Math.min(220, Math.round(220.0F * progress)));
            int trailColor = (alpha << 24) | (accent & 0x00FFFFFF);
            if (previousX != Integer.MIN_VALUE) drawHudTracerLine(context, previousX, previousY, px, py, trailColor);
            int dot = i == BREADCRUMB_TRAIL.size() - 1 ? 3 : 2;
            context.fill(px - dot / 2, py - dot / 2, px + dot / 2 + 1, py + dot / 2 + 1, trailColor);
            previousX = px;
            previousY = py;
        }
        // Player heading marker.
        context.fill(centerX - 1, centerY - 6, centerX + 2, centerY + 2, 0xFFFFFFFF);
        context.fill(centerX - 4, centerY, centerX + 5, centerY + 2, 0xFFFFFFFF);
        HUD_RENDERED_BOUNDS.put("breadcrumbs", new HudBounds(x, y, boxW, boxH));
    }

    /** Periodically caches nearby ore blocks so the X-Ray overlay has bounded per-frame work. */
    private static void updateXrayOres(MinecraftClient client) {
        if (!isEnabled("xray") || client.player == null || client.world == null) {
            XRAY_ORE_POSITIONS.clear();
            xrayWorld = null;
            lastXrayScanAt = 0L;
            return;
        }
        if (xrayWorld != client.world) {
            xrayWorld = client.world;
            XRAY_ORE_POSITIONS.clear();
            lastXrayScanAt = 0L;
        }
        long now = System.currentTimeMillis();
        if (now - lastXrayScanAt < 1800L) return;
        lastXrayScanAt = now;
        XRAY_ORE_POSITIONS.clear();

        BlockPos center = client.player.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = center.getX() - 10; x <= center.getX() + 10; x++) {
            for (int y = Math.max(client.world.getBottomY(), center.getY() - 8);
                 y <= Math.min(client.world.getTopYInclusive(), center.getY() + 8); y++) {
                for (int z = center.getZ() - 10; z <= center.getZ() + 10; z++) {
                    mutable.set(x, y, z);
                    var state = client.world.getBlockState(mutable);
                    if (state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
                        || state.isOf(Blocks.EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)
                        || state.isOf(Blocks.GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
                        || state.isOf(Blocks.IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
                        || state.isOf(Blocks.COPPER_ORE) || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)
                        || state.isOf(Blocks.COAL_ORE) || state.isOf(Blocks.DEEPSLATE_COAL_ORE)
                        || state.isOf(Blocks.LAPIS_ORE) || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)
                        || state.isOf(Blocks.REDSTONE_ORE) || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)
                        || state.isOf(Blocks.NETHER_GOLD_ORE) || state.isOf(Blocks.NETHER_QUARTZ_ORE)
                        || state.isOf(Blocks.ANCIENT_DEBRIS)) {
                        XRAY_ORE_POSITIONS.add(mutable.toImmutable());
                        if (XRAY_ORE_POSITIONS.size() >= 350) return;
                    }
                }
            }
        }
    }

    private static int oreColor(BlockPos pos, MinecraftClient client) {
        var state = client.world.getBlockState(pos);
        if (state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) return 0xFF42E9F5;
        if (state.isOf(Blocks.EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) return 0xFF55F29A;
        if (state.isOf(Blocks.ANCIENT_DEBRIS)) return 0xFFFF8A55;
        if (state.isOf(Blocks.GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
            || state.isOf(Blocks.NETHER_GOLD_ORE)) return 0xFFFFD45E;
        if (state.isOf(Blocks.IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) return 0xFFD3D9E6;
        if (state.isOf(Blocks.LAPIS_ORE) || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)) return 0xFF579CFF;
        if (state.isOf(Blocks.REDSTONE_ORE) || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)) return 0xFFFF5B65;
        if (state.isOf(Blocks.COPPER_ORE) || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)) return 0xFFE99A67;
        return 0xFFB3A0FF;
    }

    private static void updateTracers(MinecraftClient client) {
        if (!isEnabled("tracers") || client.player == null || client.world == null) {
            cachedTracerTargets.clear();
            tracerWorld = null;
            lastTracerScanAt = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (tracerWorld != client.world) {
            tracerWorld = client.world;
            lastTracerScanAt = 0L;
            cachedTracerTargets.clear();
        }
        if (now - lastTracerScanAt < 250L) return;
        lastTracerScanAt = now;
        List<LivingEntity> found = client.world.getEntitiesByClass(
            LivingEntity.class, client.player.getBoundingBox().expand(espRange),
            entity -> entity != client.player && entity.isAlive()
                && client.player.distanceTo(entity) <= espRange
                && (entity instanceof PlayerEntity ? tracerPlayers : tracerMobs)
        );
        found.sort((left, right) ->
            Double.compare(client.player.distanceTo(left), client.player.distanceTo(right)));
        cachedTracerTargets.clear();
        cachedTracerTargets.addAll(found.subList(0, Math.min(8, found.size())));
    }

    /** On-screen distance readout; this module never increases the vanilla attack reach. */
    private static void renderReachInfo(MinecraftClient client, DrawContext context, int screenWidth, int accent) {
        if (client.player == null || client.crosshairTarget == null) return;
        double distance;
        double vanillaReach;
        if (client.crosshairTarget instanceof EntityHitResult entityHit) {
            // Measure to the actual crosshair hit point, not the entity's eye position.
            distance = client.player.getEyePos().distanceTo(entityHit.getPos());
            vanillaReach = 3.0D;
        } else if (client.crosshairTarget instanceof BlockHitResult blockHit) {
            distance = client.player.getEyePos().distanceTo(blockHit.getPos());
            vanillaReach = 4.5D;
        } else {
            return;
        }
        String label = String.format(Locale.ROOT, "Reach: %.2f / %.2f blocks", distance, vanillaReach);
        context.fill(8, 8, 12 + client.textRenderer.getWidth(label), 22, 0xB80A0D16);
        context.fill(8, 8, 10, 22, accent);
        context.drawTextWithShadow(client.textRenderer, label, 14, 11, 0xFFF4F6FF);
    }

    private static void renderNametags(MinecraftClient client, DrawContext context,
                                        int screenWidth, int screenHeight, int accent) {
        if (!isEnabled("nametags") || client.player == null || client.world == null) return;
        PlayerEntity player = client.player;
        double fov = client.options.getFov().getValue();
        if (fov < 1.0D) fov = 70.0D;
        for (Entity entity : client.world.getEntitiesByClass(Entity.class,
            player.getBoundingBox().expand(48.0D),
            candidate -> candidate != player && !candidate.isRemoved()
                && (candidate instanceof PlayerEntity || candidate instanceof LivingEntity)
                && player.distanceTo(candidate) <= 48.0D)) {
            var delta = entity.getBoundingBox().getCenter().subtract(player.getEyePos());
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            if (horizontal < 0.1D) continue;
            double targetYaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
            double yawDifference = wrapDegrees(targetYaw - player.getYaw());
            double targetPitch = -Math.toDegrees(Math.atan2(delta.y, horizontal));
            double pitchDifference = targetPitch - player.getPitch();
            if (Math.abs(yawDifference) > fov * 0.58D || Math.abs(pitchDifference) > 65.0D) continue;

            int sx = screenWidth / 2 + (int) Math.round(yawDifference * screenWidth / fov);
            int sy = screenHeight / 2 + (int) Math.round(pitchDifference * screenHeight / fov)
                - Math.max(8, (int) (entity.getHeight() * 9.0D));
            String label = entity.getDisplayName().getString();
            String distance = String.format(Locale.ROOT, "%.1fm", player.distanceTo(entity));
            String text = label + "  " + distance;
            int tw = client.textRenderer.getWidth(text);
            if (sx < 2 || sx + tw > screenWidth - 2 || sy < 2 || sy > screenHeight - 12) continue;
            context.fill(sx - tw / 2 - 4, sy - 3, sx + tw / 2 + 4, sy + 11, 0xB80A0D16);
            context.fill(sx - tw / 2 - 4, sy - 3, sx - tw / 2 - 2, sy + 11, accent);
            context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(text), sx, sy, 0xFFF4F6FF);
        }
    }

    private static void renderTracers(MinecraftClient client, DrawContext context,
                                      int screenWidth, int screenHeight, int accent) {
        if (client.player == null || cachedTracerTargets.isEmpty()) return;
        int originX = screenWidth / 2;
        int originY = screenHeight - 3;
        for (LivingEntity target : cachedTracerTargets) {
            if (target == null || !target.isAlive() || target.getWorld() != client.world) continue;
            double dx = target.getX() - client.player.getX();
            double dz = target.getZ() - client.player.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance < 0.01) continue;
            double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
            double yawDifference = wrapDegrees(targetYaw - client.player.getYaw());
            if (Math.abs(yawDifference) > 88.0) continue;
            double targetPitch = -Math.toDegrees(Math.atan2(
                target.getEyeY() - client.player.getEyeY(), horizontalDistance));
            double pitchDifference = targetPitch - client.player.getPitch();
            int tx = originX + (int) Math.round(yawDifference / 88.0 * (screenWidth / 2.0 - 10));
            int ty = screenHeight / 2 + (int) Math.round(pitchDifference / 60.0 * (screenHeight / 2.0 - 10));
            tx = clampInt(tx, 3, Math.max(3, screenWidth - 3));
            ty = clampInt(ty, 3, Math.max(3, screenHeight - 3));
            int color = target instanceof PlayerEntity ? 0xFF54E8FF : accent;
            drawHudTracerLine(context, originX, originY, tx, ty, color);
            context.fill(tx - 2, ty - 2, tx + 3, ty + 3, color);
        }
    }

    private static double wrapDegrees(double degrees) {
        while (degrees <= -180.0) degrees += 360.0;
        while (degrees > 180.0) degrees -= 360.0;
        return degrees;
    }

    private static void drawHudTracerLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0) return;
        context.getMatrices().push();
        context.getMatrices().translate(x1, y1, 0.0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
            (float) Math.toDegrees(Math.atan2(dy, dx))));
        context.fill(0, -1, (int) Math.ceil(length), 1, color);
        context.getMatrices().pop();
    }

    private static void updateStorageFinder(MinecraftClient client) {
        if (!isEnabled("storagefinder") || client.player == null || client.world == null) {
            nearestStorageBlock = null;
            nearestStorageDistance = Double.POSITIVE_INFINITY;
            storageFinderWorld = null;
            lastStorageScanAt = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (storageFinderWorld != client.world) {
            storageFinderWorld = client.world;
            lastStorageScanAt = 0L;
            nearestStorageBlock = null;
        }
        if (now - lastStorageScanAt < 2500L) return;
        lastStorageScanAt = now;
        BlockPos center = client.player.getBlockPos();
        int radius = storageFinderRange;
        int bottom = Math.max(client.world.getBottomY(), center.getY() - 6);
        int top = Math.min(client.world.getTopYInclusive(), center.getY() + 6);
        BlockPos start = new BlockPos(center.getX() - radius, bottom, center.getZ() - radius);
        BlockPos end = new BlockPos(center.getX() + radius, top, center.getZ() + radius);
        BlockPos closest = null;
        String closestType = "Storage";
        double closestSquared = Double.POSITIVE_INFINITY;
        for (BlockPos pos : BlockPos.iterate(start, end)) {
            double distanceSquared = pos.toCenterPos().squaredDistanceTo(client.player.getPos());
            if (distanceSquared > radius * radius || distanceSquared >= closestSquared) continue;
            Block block = client.world.getBlockState(pos).getBlock();
            if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST && block != Blocks.ENDER_CHEST
                && block != Blocks.BARREL && !(block instanceof ShulkerBoxBlock)) continue;
            closest = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            closestType = block.getName().getString();
            closestSquared = distanceSquared;
        }
        nearestStorageBlock = closest;
        nearestStorageType = closestType;
        nearestStorageDistance = closest == null ? Double.POSITIVE_INFINITY : Math.sqrt(closestSquared);
    }

    private static void updateBlockFinder(MinecraftClient client) {
        if (!isEnabled("blockfinder") || client.player == null || client.world == null) {
            nearestFoundBlock = null;
            nearestFoundDistance = Double.POSITIVE_INFINITY;
            blockFinderWorld = null;
            lastBlockFinderScanAt = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (blockFinderWorld != client.world) {
            blockFinderWorld = client.world;
            lastBlockFinderScanAt = 0L;
            nearestFoundBlock = null;
        }
        // Throttle scanning to about once every 1.8 seconds. The scan is limited to
        // loaded-world lookup calls in a bounded box around the player, never each frame.
        if (now - lastBlockFinderScanAt < 1800L) return;
        lastBlockFinderScanAt = now;
        BlockPos center = client.player.getBlockPos();
        int radius = blockFinderRange;
        int bottom = Math.max(client.world.getBottomY(), center.getY() - 8);
        int top = Math.min(client.world.getTopYInclusive(), center.getY() + 8);
        BlockPos start = new BlockPos(center.getX() - radius, bottom, center.getZ() - radius);
        BlockPos end = new BlockPos(center.getX() + radius, top, center.getZ() + radius);
        BlockPos closest = null;
        double closestSquared = Double.POSITIVE_INFINITY;
        for (BlockPos pos : BlockPos.iterate(start, end)) {
            double distanceSquared = pos.toCenterPos().squaredDistanceTo(client.player.getPos());
            if (distanceSquared > radius * radius || distanceSquared >= closestSquared) continue;
            if (isBlockFinderTarget(client.world.getBlockState(pos).getBlock())) {
                closest = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                closestSquared = distanceSquared;
            }
        }
        nearestFoundBlock = closest;
        nearestFoundDistance = closest == null ? Double.POSITIVE_INFINITY : Math.sqrt(closestSquared);
    }

    private static boolean isBlockFinderTarget(Block block) {
        return switch (blockFinderTarget) {
            case "DIAMOND" -> block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
            case "ANCIENT DEBRIS" -> block == Blocks.ANCIENT_DEBRIS;
            case "EMERALD" -> block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE;
            case "GOLD" -> block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE;
            case "IRON" -> block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE;
            case "COAL" -> block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE;
            case "COPPER" -> block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE;
            case "LAPIS" -> block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE;
            case "REDSTONE" -> block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE;
            case "QUARTZ" -> block == Blocks.NETHER_QUARTZ_ORE;
            default -> false;
        };
    }

    private static void updateDamageTracker(MinecraftClient client) {
        if (client.player == null) {
            damageTrackedPlayer = null;
            lastObservedHealth = -1.0F;
            return;
        }
        float health = client.player.getHealth();
        if (client.player != damageTrackedPlayer || lastObservedHealth < 0.0F) {
            damageTrackedPlayer = client.player;
            lastObservedHealth = health;
            return;
        }
        if (health < lastObservedHealth - 0.01F && isEnabled("damageindicators")) {
            recentDamageAmount = lastObservedHealth - health;
            damageAlertUntil = System.currentTimeMillis() + 1600L;
        }
        lastObservedHealth = health;
    }

    /**
     * Chat HUD editor: opening chat automatically enables drag mode. Drag any
     * visible HUD widget to reposition it; right-click toggles that widget.
     * The configured editor key can still toggle edit mode while chat is open.
     * Positions are saved on release rather than every tick or rendered frame.
     */
    private static void updateChatHudEditor(MinecraftClient client) {
        long handle = client.getWindow() == null ? 0L : client.getWindow().getHandle();
        boolean chatOpen = client.currentScreen instanceof ChatScreen;
        if (chatOpen && !hudChatWasOpen) {
            hudChatEditMode = true;
            draggedHudId = null;
            hudMouseWasDown = false;
            hudRightMouseWasDown = false;
            notifyUser("HUD EDIT MODE: drag widgets to move; right-click to toggle");
        }
        hudChatWasOpen = chatOpen;
        boolean editorKeyDown = handle != 0L && hudEditKeybindCode != GLFW.GLFW_KEY_UNKNOWN
            && GLFW.glfwGetKey(handle, hudEditKeybindCode) == GLFW.GLFW_PRESS;
        if (chatOpen && editorKeyDown && !hudEditKeyWasDown) {
            hudChatEditMode = !hudChatEditMode;
            if (!hudChatEditMode) finishHudDrag();
            notifyUser(hudChatEditMode
                ? "HUD EDIT MODE: drag widgets; right-click toggles them"
                : "HUD edit mode closed");
        }
        hudEditKeyWasDown = editorKeyDown;

        if (!chatOpen) {
            if (hudChatEditMode || draggedHudId != null) {
                hudChatEditMode = false;
                finishHudDrag();
            }
            hudMouseWasDown = false;
            hudRightMouseWasDown = false;
            return;
        }
        if (!hudChatEditMode || handle == 0L) {
            hudMouseWasDown = handle != 0L && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            hudRightMouseWasDown = handle != 0L && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            return;
        }

        boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        var window = client.getWindow();
        double mouseX = client.mouse.getX() * window.getScaledWidth() / Math.max(1.0, window.getWidth());
        double mouseY = client.mouse.getY() * window.getScaledHeight() / Math.max(1.0, window.getHeight());

        if (leftDown && !hudMouseWasDown) {
            String hit = hitTestHud(mouseX, mouseY);
            if (hit != null) {
                HudBounds bounds = HUD_RENDERED_BOUNDS.get(hit);
                if (bounds != null) {
                    draggedHudId = hit;
                    hudDragOffsetX = (int) Math.round(mouseX) - bounds.x();
                    hudDragOffsetY = (int) Math.round(mouseY) - bounds.y();
                }
            }
        }
        if (leftDown && draggedHudId != null) {
            HudBounds current = HUD_RENDERED_BOUNDS.get(draggedHudId);
            if (current != null) {
                int x = clampInt((int) Math.round(mouseX) - hudDragOffsetX, 0,
                    Math.max(0, window.getScaledWidth() - current.width()));
                int y = clampInt((int) Math.round(mouseY) - hudDragOffsetY, 0,
                    Math.max(0, window.getScaledHeight() - current.height()));
                previewHudPosition(draggedHudId, x, y);
            }
        }
        if (!leftDown && hudMouseWasDown && draggedHudId != null) finishHudDrag();

        if (rightDown && !hudRightMouseWasDown) {
            String hit = hitTestHud(mouseX, mouseY);
            if (hit != null) toggle(hit);
        }
        hudMouseWasDown = leftDown;
        hudRightMouseWasDown = rightDown;
    }

    private static void finishHudDrag() {
        if (draggedHudId != null) {
            String name = readableName(draggedHudId);
            saveConfig();
            notifyUser(name + " position saved");
        }
        draggedHudId = null;
    }

    private static String hitTestHud(double mouseX, double mouseY) {
        for (int i = HUD_WIDGET_IDS.length - 1; i >= 0; i--) {
            HudBounds bounds = HUD_RENDERED_BOUNDS.get(HUD_WIDGET_IDS[i]);
            if (bounds != null && mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width()
                && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height()) {
                return HUD_WIDGET_IDS[i];
            }
        }
        return null;
    }

    private static int defaultHudX(String id, int screenWidth) {
        return switch (id) {
            case "arraylist", "potionhud", "targethud", "blockreachhud", "frametimehud", "fpsgraph" -> 150;
            case "serverinfo" -> 8;
            case "compasshud" -> screenWidth / 2 - 84;
            case "damageindicators" -> screenWidth / 2 - 44;
            case "blockfinder" -> 150;
            case "waypoint", "storagefinder" -> 8;
            default -> 8;
        };
    }

    private static int defaultHudY(String id, int screenHeight) {
        return switch (id) {
            case "fps" -> 8;
            case "coordinates" -> 29;
            case "direction" -> 50;
            case "ping" -> 71;
            case "memory" -> 92;
            case "speedometer" -> 113;
            case "clock" -> 134;
            case "watermark" -> 160;
            case "compasshud" -> 178;
            case "arraylist" -> 8;
            case "fpsgraph" -> 70;
            case "totemcounter" -> 218;
            case "pearlcounter" -> 239;
            case "arrowcounter" -> 260;
            case "goldencounter" -> 281;
            case "armorhud" -> 302;
            case "durabilityhud" -> 323;
            case "inventoryhud" -> 344;
            case "biome" -> 365;
            case "cps" -> 386;
            case "worldinfo" -> 407;
            case "serverinfo" -> screenHeight - 122;
            case "potioncounter" -> 428;
            case "sessionstats" -> 449;
            case "potionhud" -> 260;
            case "targethud" -> screenHeight - 43;
            case "damageindicators" -> screenHeight / 2 + 26;
            case "blockfinder" -> screenHeight - 60;
            case "waypoint" -> screenHeight - 80;
            case "storagefinder" -> screenHeight - 100;
            case "healthhud" -> screenHeight - 42;
            case "hungerhud" -> screenHeight - 21;
            case "blockreachhud" -> screenHeight - 84;
            case "frametimehud" -> screenHeight - 63;
            case "keystrokes" -> screenHeight - 110;
            default -> 8;
        };
    }

    private static void renderChatHudEditingOverlay(MinecraftClient client, DrawContext context) {
        if (!hudChatEditMode || !(client.currentScreen instanceof ChatScreen)) return;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int accent = accentColor();
        long now = System.currentTimeMillis();
        String key = keyName(hudEditKeybindCode);
        String banner = "ASTRA HUD EDIT  •  DRAG ANY WIDGET TO MOVE  •  RIGHT-CLICK TO TOGGLE  •  " + key + " TO EXIT";
        int bannerWidth = Math.min(screenWidth - 12, client.textRenderer.getWidth(banner) + 18);
        int bx = Math.max(6, (screenWidth - bannerWidth) / 2);
        int by = 6;
        context.fill(bx, by, bx + bannerWidth, by + 23, 0xE90B101A);
        context.fill(bx, by, bx + bannerWidth, by + 2, accent);
        int shineWidth = Math.max(12, bannerWidth / 7);
        int shineX = bx + (int) ((now / 5L) % (bannerWidth + shineWidth)) - shineWidth;
        int shineLeft = Math.max(bx + 2, shineX);
        int shineRight = Math.min(bx + bannerWidth - 2, shineX + shineWidth);
        if (shineRight > shineLeft) context.fill(shineLeft, by + 1, shineRight, by + 3, 0xBFFFFFFF);
        context.drawText(client.textRenderer, banner, bx + 9, by + 8, 0xFFF3F5FC, true);

        double mouseX = client.mouse.getX() * screenWidth / Math.max(1.0, client.getWindow().getWidth());
        double mouseY = client.mouse.getY() * screenHeight / Math.max(1.0, client.getWindow().getHeight());
        for (String id : HUD_WIDGET_IDS) {
            HudBounds actual = HUD_RENDERED_BOUNDS.get(id);
            int fallbackX = defaultHudX(id, screenWidth);
            int fallbackY = defaultHudY(id, screenHeight);
            int x = hudX(id, fallbackX);
            int y = hudY(id, fallbackY);
            if (y < 0) y = screenHeight + y;
            int boxWidth = actual == null
                ? Math.max(78, Math.min(220, client.textRenderer.getWidth(readableName(id) + "  OFF") + 16))
                : actual.width();
            int boxHeight = actual == null ? 18 : actual.height();
            if (actual == null || !isEnabled(id)) {
                x = clampInt(x, 0, Math.max(0, screenWidth - boxWidth));
                y = clampInt(y, 28, Math.max(28, screenHeight - boxHeight));
            } else {
                x = actual.x();
                y = actual.y();
            }
            boolean enabled = isEnabled(id);
            boolean hover = mouseX >= x && mouseX <= x + boxWidth && mouseY >= y && mouseY <= y + boxHeight;
            boolean dragging = id.equals(draggedHudId);
            int pulse = 55 + (int) (45 * (0.5 + 0.5 * Math.sin(now / (dragging ? 105.0 : 190.0))));
            int glow = (pulse << 24) | (accent & 0x00FFFFFF);
            if (enabled && actual != null) {
                context.fill(x - 1, y - 1, x + boxWidth + 1, y, glow);
                context.fill(x - 1, y + boxHeight, x + boxWidth + 1, y + boxHeight + 1, glow);
                context.fill(x - 1, y, x, y + boxHeight, glow);
                context.fill(x + boxWidth, y, x + boxWidth + 1, y + boxHeight, glow);
                if (hover || dragging) {
                    context.fill(x, y, x + boxWidth, y + 2, accent);
                    context.fill(x, y + boxHeight - 2, x + boxWidth, y + boxHeight, accent);
                }
            } else {
                context.fill(x, y, x + boxWidth, y + boxHeight, enabled ? 0xA91A2231 : 0x99101722);
                context.fill(x, y, x + 3, y + boxHeight, enabled ? accent : 0xFF657084);
                context.drawText(client.textRenderer, readableName(id) + (enabled ? "  ON" : "  OFF"),
                    x + 8, y + Math.max(4, (boxHeight - 9) / 2), enabled ? 0xFFF3F5FC : 0xFF8996AD, false);
            }
            if (hover) {
                String hint = readableName(id) + (enabled ? "  •  DRAG TO MOVE" : "  •  RIGHT-CLICK TO ENABLE");
                int hintW = Math.min(screenWidth - 12, client.textRenderer.getWidth(hint) + 12);
                int hintX = clampInt((int) Math.round(mouseX) + 8, 6, Math.max(6, screenWidth - hintW - 6));
                int hintY = clampInt((int) Math.round(mouseY) - 17, 32, Math.max(32, screenHeight - 14));
                context.fill(hintX, hintY, hintX + hintW, hintY + 13, 0xF20B101A);
                context.drawText(client.textRenderer, hint, hintX + 6, hintY + 3, accent, false);
            }
            HUD_RENDERED_BOUNDS.put(id, new HudBounds(x, y, boxWidth, boxHeight));
        }
    }

    private static int clampInt(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
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
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        x = Math.max(0, Math.min(screenWidth - 50, x));
        y = Math.max(0, Math.min(screenHeight - 20, y));
        int width = drawHudLineAt(client, context, x, y, label, value, accent);
        HUD_RENDERED_BOUNDS.put(id, new HudBounds(x, y, width, 18));
    }

    private static int drawHudLineAt(MinecraftClient client, DrawContext context, int x, int y,
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
        if (hudBackgrounds) context.fill(x, y, x + width, y + 18, (hudAlpha << 24) | 0x00101420);
        context.fill(x, y, x + 2, y + 18, accent);
        context.drawText(client.textRenderer, text, x + 8, y + 5, 0xFFF3F5FF, false);
        return width;
    }

    private static void drawKey(
        DrawContext context, MinecraftClient client, KeyBinding binding,
        int x, int y, String label, int accent
    ) {
        int width = label.length() > 1 ? 46 : 20;
        if (binding.isPressed()) context.fill(x, y, x + width, y + 19, accent);
        else if (hudBackgrounds) context.fill(x, y, x + width, y + 19,
            (Math.round(255.0f * hudOpacity / 100.0f) << 24) | 0x00101420);
        context.fill(x, y, x + width, y + 1,
            binding.isPressed() ? 0xFFFFFFFF : 0xFF353D50);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label,
            x + (width - textWidth) / 2, y + 6,
            binding.isPressed() ? 0xFF10131C : 0xFFE9ECF6, false);
    }

    private static void drawMouseKey(
        DrawContext context, MinecraftClient client, int x, int y,
        String label, boolean pressed, int accent
    ) {
        int width = 46;
        if (pressed) context.fill(x, y, x + width, y + 19, accent);
        else if (hudBackgrounds) context.fill(x, y, x + width, y + 19,
            (Math.round(255.0f * hudOpacity / 100.0f) << 24) | 0x00101420);
        context.fill(x, y, x + width, y + 1, pressed ? 0xFFFFFFFF : 0xFF353D50);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label,
            x + (width - textWidth) / 2, y + 6,
            pressed ? 0xFF10131C : 0xFFE9ECF6, false);
    }
}
