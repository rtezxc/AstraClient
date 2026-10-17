package com.astraclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives the two main world-entry buttons a friendly circular Astra style. */
@Mixin(TitleScreen.class)
public abstract class AstraTitleButtonsMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void astra$shapeMainButtons(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int centerX = screen.width / 2;
        // Keep the primary actions compact: the previous 104x104 layout
        // covered the vanilla Realms and Options buttons at common resolutions.
        int mainY = screen.height / 2 - 48;
        for (var element : screen.children()) {
            if (!(element instanceof ButtonWidget button)) continue;
            String label = button.getMessage().getString().toLowerCase(java.util.Locale.ROOT);
            if (label.contains("singleplayer") || label.contains("single player")) {
                button.setDimensions(164, 32);
                button.setPosition(centerX - 170, mainY);
            } else if (label.contains("multiplayer")) {
                button.setDimensions(164, 32);
                button.setPosition(centerX + 6, mainY);
            } else if (label.contains("minecraft realms") || label.equals("realms")) {
                // Realms is a separate vanilla button on 1.21.4; give it its own row.
                button.setDimensions(164, 24);
                button.setPosition(centerX - 82, mainY + 40);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void astra$drawFriendlyMenuHint(DrawContext context, int mouseX, int mouseY,
                                             float delta, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int centerX = screen.width / 2;
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
            Text.literal("Choose how you want to play"), centerX, screen.height / 2 - 68, 0xFFF4F3FF);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
            Text.literal("Solo world  •  Play with others"), centerX, screen.height / 2 + 65, 0xFFB7C3D9);
    }
}
