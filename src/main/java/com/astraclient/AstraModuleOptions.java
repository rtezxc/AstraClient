package com.astraclient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Typed, per-module option definitions and persistent values.
 * UI code consumes the same definitions that validation and persistence use.
 */
public final class AstraModuleOptions {
    public enum Type { TOGGLE, DROPDOWN, SLIDER, MULTI_SELECT, KEYBIND, TEXT, COLOR, MODE }

    public record Option(
        String id, String label, String description, Type type, String defaultValue,
        double min, double max, double step, List<String> choices
    ) { }

    private static final Map<String, String> VALUES = new LinkedHashMap<>();
    private static final List<String> COLORS = List.of("AA70FF", "54E8FF", "69E6B8", "FF6B9D", "FFC66E", "F3F5FC");
    private static final List<String> ATTACK_MODES = List.of("Normal", "Critical Hits Only", "Mixed");
    private static final List<String> ROTATION_MODES = List.of("None", "Assist", "Visible");
    private static final List<String> TARGET_PRIORITIES = List.of("Closest", "Lowest Health", "Lowest Armor");
    private static final List<String> RENDER_MODES = List.of("Outline", "Bright Outline", "Soft Outline");
    private static final List<String> MOVEMENT_MODES = List.of("Default", "Gentle", "Aggressive");
    private static final List<String> UTILITY_MODES = List.of("Conservative", "Balanced", "Fast");
    private static final List<String> MULTI_TARGETS = List.of("Players", "Hostile Mobs", "Passive Mobs", "Items");

    private AstraModuleOptions() { }

    private static Option toggle(String id, String label, String description, boolean value) {
        return new Option(id, label, description, Type.TOGGLE, Boolean.toString(value), 0, 1, 1, List.of());
    }

    private static Option dropdown(String id, String label, String description, String value, List<String> choices) {
        return new Option(id, label, description, Type.DROPDOWN, value, 0, choices.size() - 1, 1, choices);
    }

    private static Option mode(String id, String label, String description, String value, List<String> choices) {
        return new Option(id, label, description, Type.MODE, value, 0, choices.size() - 1, 1, choices);
    }

    private static Option slider(String id, String label, String description, String value,
                                 double min, double max, double step) {
        return new Option(id, label, description, Type.SLIDER, value, min, max, step, List.of());
    }

    private static Option multi(String id, String label, String description, String value, List<String> choices) {
        return new Option(id, label, description, Type.MULTI_SELECT, value, 0, choices.size() - 1, 1, choices);
    }

    private static Option text(String id, String label, String description, String value) {
        return new Option(id, label, description, Type.TEXT, value, 0, 32, 1, List.of());
    }

    private static Option color(String id, String label, String description, String value) {
        return new Option(id, label, description, Type.COLOR, value, 0, COLORS.size() - 1, 1, COLORS);
    }

    /** Returns module-specific settings; no single generic menu is reused for every module. */
    public static List<Option> definitions(String moduleId, String category) {
        List<Option> out = new ArrayList<>();
        out.add(new Option("keybind", "Module Keybind", "Press a key to toggle this module.", Type.KEYBIND, "-1", 0, 348, 1, List.of()));
        switch (moduleId) {
            case "killaura" -> {
                out.add(dropdown("attack_mode", "Attack Mode", "Choose how Kill Aura waits to attack.", "Normal", ATTACK_MODES));
                out.add(toggle("critical_hits", "Critical Hits", "Only attack when a vanilla critical hit is available.", false));
                out.add(toggle("target_players", "Target Players", "Allow player entities as targets.", true));
                out.add(toggle("target_hostile", "Target Hostile Mobs", "Allow hostile mobs as targets.", true));
                out.add(toggle("target_passive", "Target Passive Mobs", "Allow passive mobs as targets.", false));
                out.add(slider("attack_range", "Attack Range", "Vanilla melee range only; this never extends reach.", "3.0", 2.0, 3.0, 0.1));
                out.add(slider("fov", "Target FOV", "Angle around the view direction in which targets can be selected.", "180", 60, 180, 5));
                out.add(dropdown("rotation_mode", "Rotation Mode", "Choose whether target rotation is applied.", "Visible", ROTATION_MODES));
                out.add(dropdown("target_priority", "Target Priority", "Choose which valid target is preferred.", "Closest", TARGET_PRIORITIES));
                out.add(toggle("show_target", "Show Target", "Show target feedback while Kill Aura is active.", true));
                out.add(toggle("attack_effects", "Attack Effects", "Show lightweight target particles.", true));
                out.add(multi("target_types", "Target Types", "Select one or more target groups.", "Players,Hostile Mobs", MULTI_TARGETS));
            }
            case "triggerbot" -> {
                out.add(slider("attack_range", "Attack Range", "Stay within vanilla melee reach.", "3.0", 2.0, 3.0, 0.1));
                out.add(toggle("target_players", "Target Players", "Allow player entities as targets.", true));
                out.add(toggle("target_hostile", "Target Hostile Mobs", "Allow hostile mobs as targets.", true));
                out.add(toggle("target_passive", "Target Passive Mobs", "Allow passive mobs as targets.", false));
                out.add(toggle("critical_hits", "Critical Hits Only", "Wait until a vanilla critical is possible.", false));
                out.add(dropdown("attack_mode", "Attack Mode", "Choose normal or critical-only attacks.", "Normal", ATTACK_MODES));
            }
            case "autoclicker" -> {
                out.add(slider("click_interval", "Click Interval", "Delay between repeated attacks in milliseconds.", "125", 100, 500, 5));
                out.add(toggle("only_when_held", "Require Mouse Hold", "Only repeat while the left mouse button is held.", true));
                out.add(toggle("respect_cooldown", "Respect Attack Cooldown", "Keep vanilla attack cooldown checks enabled.", true));
            }
            case "playeresp", "mobesp", "itemesp" -> {
                out.add(slider("render_range", "Render Range", "Maximum distance for this outline module.", "64", 8, 128, 4));
            }
            case "tracers" -> {
                out.add(slider("render_range", "Tracer Range", "Maximum distance for tracer lines.", "64", 8, 128, 4));
                out.add(color("module_color", "Tracer Color", "Color used for tracer lines.", "54E8FF"));
                out.add(toggle("target_players", "Trace Players", "Draw tracer lines to players.", true));
                out.add(toggle("target_mobs", "Trace Mobs", "Draw tracer lines to mobs.", true));
            }
            case "hitboxes", "targetbox", "targethalo" -> {
                double defaultRange = moduleId.equals("hitboxes") ? 64 : 48;
                out.add(slider("render_range", "Render Range", "Maximum distance for this world overlay.", Double.toString(defaultRange), 8, 128, 4));
                out.add(color("module_color", "Overlay Color", "Color used by this world overlay.", moduleId.equals("targetbox") ? "FF6B9D" : "AA70FF"));
                out.add(dropdown("render_mode", "Render Style", "Choose outline brightness.", "Outline", RENDER_MODES));
            }
            case "blockhighlight" -> {
                out.add(color("module_color", "Highlight Color", "Color used for the targeted block outline.", "AA70FF"));
                out.add(dropdown("render_mode", "Highlight Style", "Choose outline brightness.", "Outline", RENDER_MODES));
            }
            case "nametags" -> {
                out.add(slider("render_range", "Nametag Range", "Maximum distance for on-screen name labels.", "48", 8, 128, 4));
                out.add(color("module_color", "Nametag Color", "Color used for the label accent.", "AA70FF"));
                out.add(toggle("show_labels", "Show Labels", "Show or hide name and distance labels.", true));
            }
            case "xray" -> {
                out.add(slider("render_range", "Ore Highlight Range", "Maximum distance for cached ore outlines.", "12", 4, 16, 1));
                out.add(color("module_color", "Ore Outline Color", "Override the ore outline color.", "AA70FF"));
            }
            case "customcrosshair" -> {
                out.add(color("module_color", "Crosshair Color", "Color used by the custom crosshair.", "AA70FF"));
            }
            case "orbitcubes" -> {
                out.add(slider("effect_density", "Cube Count", "Number of animated wire cubes orbiting the player.", "4", 1, 8, 1));
                out.add(color("module_color", "Cube Color", "Tint used by the orbiting wire cubes.", "AA70FF"));
            }
            case "orbitsparks", "playertrail" -> {
                out.add(slider("effect_density", "Effect Density", "Number of client-side particles emitted per update.", "2", 1, 8, 1));
            }
            case "speed" -> {
                out.add(slider("movement_strength", "Speed Strength", "Controls the local horizontal velocity boost.", "1.0", 0.5, 1.5, 0.05));
                out.add(mode("movement_mode", "Movement Mode", "Gentle, default or aggressive speed profile.", "Default", MOVEMENT_MODES));
                out.add(toggle("only_on_ground", "Only On Ground", "Apply the speed boost only while grounded.", false));
                out.add(toggle("pause_in_menus", "Pause In Menus", "Pause speed while a screen is open.", true));
            }
            case "noslow" -> {
                out.add(slider("movement_strength", "No Slow Strength", "Controls the horizontal velocity compensation while using an item.", "1.0", 0.5, 1.5, 0.05));
                out.add(toggle("only_on_ground", "Only On Ground", "Apply compensation only while grounded.", false));
                out.add(toggle("pause_in_menus", "Pause In Menus", "Pause No Slow while a screen is open.", true));
            }
            case "sprint", "autosprint" -> {
                out.add(toggle("only_on_ground", "Only On Ground", "Only start sprinting while grounded.", true));
                out.add(toggle("pause_in_menus", "Pause In Menus", "Pause automatic sprinting while a screen is open.", true));
            }
            case "fly" -> {
                out.add(slider("movement_strength", "Flight Strength", "Vertical flight movement speed.", "1.0", 0.5, 1.5, 0.05));
            }
            case "autowalk" -> {
                out.add(toggle("pause_in_menus", "Pause In Menus", "Release forced forward movement while a screen is open.", true));
            }
            case "safewalk", "eagle", "inventorymove" -> { }
            case "autoeat" -> {
                out.add(slider("threshold", "Food Threshold", "Eat when hunger is at or below this level.", "16", 10, 19, 1));
            }
            case "autototem", "autotool", "autoweapon", "potionhud" -> { }
            case "blockfinder" -> {
                out.add(slider("scan_range", "Scan Radius", "Radius used to search for a configured block.", "24", 8, 48, 4));
                out.add(text("custom_value", "Block Target", "Use a supported ore alias or a registry ID such as minecraft:diamond_ore.", "DIAMOND"));
                out.add(toggle("show_hud", "Show HUD Result", "Show the nearest matching block in the HUD.", true));
            }
            case "storagefinder" -> {
                out.add(slider("scan_range", "Search Radius", "Maximum radius used to find nearby storage.", "16", 8, 32, 4));
                out.add(toggle("show_hud", "Show HUD Result", "Show the nearest storage location in the HUD.", true));
            }
            case "waypoint" -> {
                out.add(toggle("show_hud", "Show Waypoint HUD", "Show waypoint distance and direction.", true));
                out.add(color("module_color", "Waypoint Color", "Color used by waypoint markers.", "54E8FF"));
            }
            case "breadcrumbs" -> {
                out.add(slider("trail_length", "Trail Length", "Number of recent positions kept in the breadcrumb trail.", "48", 12, 120, 12));
                out.add(toggle("show_hud", "Show Breadcrumb HUD", "Show the breadcrumb minimap.", true));
            }
            case "timechanger" -> {
                out.add(slider("time_of_day", "Time Of Day", "Client-side visual time, from dawn through the full day cycle.", "6000", 0, 24000, 1000));
            }
            case "noweather", "nopumpkinoverlay", "nofireoverlay", "noportaloverlay", "nospyglassoverlay", "nopowdersnowoverlay", "nonauseaoverlay", "novignette", "noscoreboard" -> {
                // These modules remove one visual effect; the module toggle is their meaningful control.
            }
            case "healthhud", "hungerhud", "potioncounter", "durabilityhud", "inventoryhud", "armorhud", "blockreachhud", "frametimehud", "fps", "coordinates", "direction", "ping", "memory", "speedometer", "clock", "cps", "worldinfo", "biome", "totemcounter", "pearlcounter", "arrowcounter", "goldencounter", "sessionstats", "serverinfo" -> {
                out.add(toggle("background", "HUD Background", "Show or hide this widget's panel background.", true));
                out.add(color("module_color", "HUD Accent Color", "Color used for this widget's accent stripe.", "AA70FF"));
            }
            default -> { }
        }
        return List.copyOf(out);
    }

    public static String value(String moduleId, String optionId, String fallback) {
        return VALUES.getOrDefault(key(moduleId, optionId), fallback);
    }

    public static boolean boolValue(String moduleId, String optionId, boolean fallback) {
        return Boolean.parseBoolean(value(moduleId, optionId, Boolean.toString(fallback)));
    }

    public static double numberValue(String moduleId, String optionId, double fallback) {
        try { return Double.parseDouble(value(moduleId, optionId, Double.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public static int colorValue(String moduleId, String optionId, int fallback) {
        String hex = value(moduleId, optionId, String.format(Locale.ROOT, "%06X", fallback & 0x00FFFFFF)).trim().replace("#", "");
        try { return 0xFF000000 | (Integer.parseInt(hex, 16) & 0x00FFFFFF); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public static void setValue(String moduleId, Option option, String rawValue) {
        if (moduleId == null || option == null || rawValue == null) return;
        String normalized = normalize(option, rawValue);
        VALUES.put(key(moduleId, option.id()), normalized);
        AstraClient.saveConfig();
    }

    /** Update a live preview while dragging or typing; the screen persists on commit. */
    public static void previewValue(String moduleId, Option option, String rawValue) {
        if (moduleId == null || option == null || rawValue == null) return;
        VALUES.put(key(moduleId, option.id()), normalize(option, rawValue));
    }

    /** Keep legacy settings controls and the typed editor in sync for the same option. */
    public static void syncBooleanValue(String moduleId, String optionId, boolean value) {
        for (Option option : definitions(moduleId, "Movement")) {
            if (option.id().equals(optionId) && option.type() == Type.TOGGLE) {
                VALUES.put(key(moduleId, optionId), Boolean.toString(value));
                return;
            }
        }
    }

    /** Mirrors an existing legacy control into the typed settings store without an extra disk write. */
    public static void syncValue(String moduleId, String optionId, String value) {
        if (moduleId == null || optionId == null || value == null) return;
        VALUES.put(key(moduleId, optionId), value.length() > 128 ? value.substring(0, 128) : value);
    }

    public static String normalize(Option option, String raw) {
        try {
            return switch (option.type()) {
                case TOGGLE -> Boolean.toString(Boolean.parseBoolean(raw));
                case SLIDER -> {
                    double value = Double.parseDouble(raw);
                    value = Math.max(option.min(), Math.min(option.max(), value));
                    if (option.step() > 0) value = option.min() + Math.round((value - option.min()) / option.step()) * option.step();
                    yield formatNumber(value);
                }
                case DROPDOWN, MODE -> option.choices().contains(raw) ? raw : option.defaultValue();
                case MULTI_SELECT -> {
                    List<String> selected = new ArrayList<>();
                    for (String choice : option.choices()) {
                        if (java.util.Arrays.asList(raw.split(",")).contains(choice)) selected.add(choice);
                    }
                    yield String.join(",", selected);
                }
                case KEYBIND -> {
                    int key = Integer.parseInt(raw);
                    yield Integer.toString(key >= -1 && key <= 348 ? key : -1);
                }
                case TEXT -> raw.length() > 64 ? raw.substring(0, 64) : raw;
                case COLOR -> {
                    String hex = raw.trim().replace("#", "").toUpperCase(Locale.ROOT);
                    yield COLORS.contains(hex) ? hex : option.defaultValue();
                }
            };
        } catch (RuntimeException invalid) {
            return option.defaultValue();
        }
    }

    public static void load(Properties properties) {
        VALUES.clear();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith("moduleSetting.")) continue;
            String remainder = key.substring("moduleSetting.".length());
            int split = remainder.indexOf('.');
            if (split <= 0 || split == remainder.length() - 1) continue;
            String moduleId = remainder.substring(0, split);
            String optionId = remainder.substring(split + 1);
            String raw = properties.getProperty(key, "");
            // Earlier builds defaulted Kill Aura to no rotation, which can make attacks
            // fail on servers that validate facing. Migrate that old default once; users
            // can still choose None again from the settings screen.
            if (moduleId.equals("killaura") && optionId.equals("rotation_mode") && raw.equals("None")) {
                raw = "Visible";
            }
            // Preserve unknown future options as strings for forward-compatible config migration.
            VALUES.put(key(moduleId, optionId), raw.length() > 128 ? raw.substring(0, 128) : raw);
        }
    }

    public static void save(Properties properties) {
        for (Map.Entry<String, String> entry : VALUES.entrySet()) {
            properties.setProperty("moduleSetting." + entry.getKey(), entry.getValue());
        }
    }

    public static void reset() {
        VALUES.clear();
    }

    private static String key(String moduleId, String optionId) {
        return moduleId + "." + optionId;
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.000001) return Long.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
