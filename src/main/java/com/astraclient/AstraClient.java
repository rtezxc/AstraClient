package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public final class AstraClient implements ClientModInitializer {
    private static KeyBinding openGuiKey;
    private static boolean fullBright;
    private static boolean autoSprint;

    public static boolean isFullBrightEnabled() {
        return fullBright;
    }

    public static boolean isAutoSprintEnabled() {
        return autoSprint;
    }

    public static void setFullBright(boolean enabled) {
        fullBright = enabled;
    }

    public static void setAutoSprint(boolean enabled) {
        autoSprint = enabled;
    }

    @Override
    public void onInitializeClient() {
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

            if (client.player == null || client.world == null) {
                return;
            }

            if (autoSprint && client.player.forwardSpeed > 0.0F && !client.player.isSneaking()) {
                client.player.setSprinting(true);
            }

            if (fullBright && client.player.age % 100 == 0) {
                client.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false)
                );
            }
        });

        System.out.println("[Astra Client] Initialized for Minecraft 1.21.4");
    }
}
