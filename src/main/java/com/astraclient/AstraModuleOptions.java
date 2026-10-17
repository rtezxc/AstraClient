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
                out.add(dropdown("rotation_mode", "Rotation Mode", "Choose whether target rotation is applied.", "None", ROTATION_MODES));
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
            case "playeresp", "mobesp", "itemesp", "tracers", "hitboxes", "targetbox", "targethalo", "nametags", "xray", "blockhighlight", "orbitsparks", "orbitcubes", "playertrail", "customcrosshair" -> {
                out.add(slider("render_range", "Render Range", "Maximum distance for this visual module.", "64", 8, 128, 4));
                out.add(dropdown("render_mode", "Render Mode", "Choose the supported visual style.", "Outline", RENDER_MODES));
                out.add(color("module_color", "Module Color", "Color used by supported Astra visual elements.", "AA70FF"));
                out.add(toggle("show_labels", "Show Labels", "Show text labels when this module supports labels.", true));
                if (moduleId.equals("tracers")) {
                    out.add(toggle("target_players", "Trace Players", "Draw tracer lines to players.", true));
                    out.add(toggle("target_mobs", "Trace Mobs", "Draw tracer lines to mobs.", true));
                }
            }
            case "speed", "noslow", "sprint", "autosprint", "fly", "autowalk", "safewalk", "eagle", "inventorymove" -> {
                out.add(slider("movement_strength", "Movement Strength", "Strength factor used by supported movement logic.", "1.0", 0.5, 1.5, 0.05));
                out.add(mode("movement_mode", "Movement Mode", "Select the movement behavior profile.", "Default", MOVEMENT_MODES));
                out.add(toggle("only_on_ground", "Only On Ground", "Restrict supported movement effects to grounded movement.", false));
                out.add(toggle("pause_in_menus", "Pause In Menus", "Do not apply this module while a screen is open.", true));
            }
            case "autoeat", "autototem", "autotool", "autoweapon", "potioncounter", "durabilityhud", "inventoryhud", "armorhud", "potionhud", "healthhud", "hungerhud" -> {
                out.add(toggle("smart_mode", "Smart Mode", "Use the module's normal safety checks before acting.", true));
                out.add(slider("threshold", "Activation Threshold", "Threshold for modules that use a numeric trigger.", "16", 1, 20, 1));
                out.add(toggle("notify_changes", "Notify Changes", "Show a short notification when the module acts.", true));
                if (moduleId.equals("autototem")) {
                    out.add(toggle("offhand_only", "Offhand Only", "Only move a totem into the offhand slot.", true));
                }
            }
            case "blockfinder", "storagefinder", "waypoint", "breadcrumbs", "timechanger", "noweather", "nopumpkinoverlay", "nofireoverlay", "noportaloverlay", "nospyglassoverlay", "nopowdersnowoverlay", "nonauseaoverlay", "novignette", "noscoreboard" -> {
                out.add(slider("scan_range", "Scan / Display Range", "Distance or interval used by this world/visual utility.", "24", 8, 64, 4));
                out.add(toggle("show_hud", "Show HUD Feedback", "Show the related HUD marker or information where supported.", true));
                out.add(text("custom_value", "Custom Value", "Custom target, block name, or text used by supported utilities.", moduleId.equals("blockfinder") ? "DIAMOND" : ""));
                out.add(color("module_color", "Module Color", "Color used by supported markers and overlays.", "54E8FF"));
            }
            default -> {
                if ("HUD".equals(category)) {
                    out.add(slider("text_scale", "Text Scale", "Scale factor for HUD elements that support individual sizing.", "1.0", 0.5, 2.0, 0.1));
                    out.add(toggle("background", "Background", "Show a background behind this HUD element where supported.", true));
                    out.add(color("module_color", "Module Color", "Color used by supported HUD elements.", "AA70FF"));
                    out.add(toggle("show_in_arraylist", "Show In Array List", "List this module in the enabled-module HUD.", true));
                } else if ("World".equals(category)) {
                    out.add(slider("scan_range", "Scan Range", "Search radius for supported world utilities.", "24", 8, 64, 4));
                    out.add(toggle("show_hud", "Show HUD Feedback", "Show related information where supported.", true));
                    out.add(text("custom_value", "Custom Value", "Target block or text used by supported utilities.", ""));
                } else if ("Movement".equals(category)) {
                    out.add(slider("movement_strength", "Movement Strength", "Strength factor for supported movement modules.", "1.0", 0.5, 1.5, 0.05));
                    out.add(mode("movement_mode", "Movement Mode", "Choose a supported movement profile.", "Default", MOVEMENT_MODES));
                    out.add(toggle("pause_in_menus", "Pause In Menus", "Pause this module while a screen is open.", true));
                } else if ("Render".equals(category)) {
                    out.add(slider("render_range", "Render Range", "Maximum distance for this visual module.", "64", 8, 128, 4));
                    out.add(color("module_color", "Module Color", "Color used by supported visual elements.", "AA70FF"));
                    out.add(toggle("show_labels", "Show Labels", "Show labels where this module supports them.", true));
                } else if ("Combat".equals(category)) {
                    out.add(dropdown("target_priority", "Target Priority", "Choose the preferred valid target.", "Closest", TARGET_PRIORITIES));
                    out.add(slider("attack_range", "Attack Range", "Maximum supported vanilla attack distance.", "3.0", 2.0, 3.0, 0.1));
                    out.add(toggle("critical_hits", "Critical Hits Only", "Wait for a vanilla critical when supported.", false));
                } else if ("Inventory".equals(category) || "Player".equals(category)) {
                    out.add(toggle("smart_mode", "Smart Mode", "Use conservative checks before acting.", true));
                    out.add(slider("threshold", "Activation Threshold", "Threshold for modules with numeric triggers.", "10", 1, 20, 1));
                    out.add(toggle("notify_changes", "Notify Changes", "Show a short notification when the module acts.", true));
                } else {
                    out.add(toggle("notify_changes", "Notify Changes", "Show a short notification when the module acts.", true));
                    out.add(mode("behavior_mode", "Behavior Mode", "Choose the module's supported behavior profile.", "Balanced", UTILITY_MODES));
                    out.add(text("custom_text", "Custom Text", "Custom text used by modules that support it.", ""));
                }
            }
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
