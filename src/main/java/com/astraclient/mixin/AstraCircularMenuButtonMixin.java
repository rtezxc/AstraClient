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

/** Draws smooth pixel-stepped circular buttons for Singleplayer and Multiplayer. */
@Mixin(ButtonWidget.class)
public abstract class AstraCircularMenuButtonMixin {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void astra$renderCircularButton(DrawContext context, int mouseX, int mouseY,
                                            float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof TitleScreen)) return;
        ButtonWidget button = (ButtonWidget) (Object) this;
        String label = button.getMessage().getString().toLowerCase(java.util.Locale.ROOT);
        boolean solo = label.contains("singleplayer") || label.contains("single player");
        boolean multi = label.contains("multiplayer");
        if (!solo && !multi) return;

        ci.cancel();
        int x = button.getX();
        int y = button.getY();
        int w = button.getWidth();
        int h = button.getHeight();
        int cx = x + w / 2;
        int cy = y + h / 2;
        int radius = Math.min(w, h) / 2 - 1;
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int accent = solo ? 0xFF8B5CF6 : 0xFF36D9F5;
        int inner = hovered ? 0xFF20283B : 0xFF111827;

        // Pixel-stepped circles work on Minecraft's DrawContext without custom textures.
        for (int py = -radius; py <= radius; py++) {
            int outerHalf = (int) Math.floor(Math.sqrt(Math.max(0, radius * radius - py * py)));
            int innerRadius = Math.max(1, radius - 4);
            int innerHalf = (int) Math.floor(Math.sqrt(Math.max(0, innerRadius * innerRadius - py * py)));
            context.fill(cx - outerHalf, cy + py, cx + outerHalf + 1, cy + py + 1, accent);
            if (Math.abs(py) <= innerRadius) {
                context.fill(cx - innerHalf, cy + py, cx + innerHalf + 1, cy + py + 1, inner);
            }
        }

        String title = solo ? "SINGLE" : "MULTI";
        String subtitle = solo ? "PLAYER" : "PLAYER";
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(solo ? "SOLO" : "ONLINE"),
            cx, cy - 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(subtitle),
            cx, cy + 2, 0xFFD8E2F5);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(hovered ? "CLICK TO PLAY" : title),
            cx, cy + 18, accent);
    }
}
