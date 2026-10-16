package com.astraclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AstraClient implements ClientModInitializer {

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {

        openGuiKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.astraclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal("§b§lASTRA CLIENT §7» §fGUI loading..."),
                        true
                    );
                }
            }
        });

        System.out.println("[Astra Client] Initialized successfully!");
    }
}
