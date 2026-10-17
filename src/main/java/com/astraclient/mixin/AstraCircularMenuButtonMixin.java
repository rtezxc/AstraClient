package com.astraclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds Astra's lightweight neon capsule style to the two main title-screen
 * navigation buttons. The original button click and narration behavior remain
 * untouched; only their rendering is replaced.
 */
@Mixin(PressableWidget.class)
public abstract class AstraCircularMenuButtonMixin {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void astra$renderPremiumTitleButton(DrawContext context, int mouseX, int mouseY,
                                                float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof TitleScreen)) return;

        ClickableWidget button = (ClickableWidget) (Object) this;
        String label = button.getMessage().getString().toLowerCase(java.util.Locale.ROOT);
        boolean solo = label.contains("singleplayer") || label.contains("single player");
        boolean multi = label.contains("multiplayer");
        if (!solo && !multi) return;

        int x = button.getX();
        int y = button.getY();
        int w = button.getWidth();
        int h = button.getHeight();
        // Do not cancel vanilla rendering until we know the custom renderer can
        // draw this widget. Otherwise unusually small/scaled buttons become invisible.
        if (w < 40 || h < 10) return;
        ci.cancel();

        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int accent = solo ? 0xFFB16CFF : 0xFF39DDF5;
        int fill = hovered ? 0xFF25243A : 0xFF111522;
        int radius = Math.max(4, Math.min(h / 2, 12));

        // Row-by-row rounded capsule: avoids custom textures and heavyweight blur,
        // keeping it friendly to Sodium, mobile renderers, and low-end devices.
        for (int py = 0; py < h; py++) {
            int edge = 0;
            if (py < radius) {
                edge = radius - (int) Math.floor(Math.sqrt(
                    Math.max(0.0, radius * radius - (radius - py) * (radius - py))));
            } else if (py >= h - radius) {
                int fromBottom = h - 1 - py;
                edge = radius - (int) Math.floor(Math.sqrt(
                    Math.max(0.0, radius * radius - (radius - 1 - fromBottom) * (radius - 1 - fromBottom))));
            }
            edge = Math.min(edge, w / 3);
            context.fill(x + edge, y + py, x + w - edge, y + py + 1, accent);
            if (py > 0 && py < h - 1 && w - edge * 2 > 4) {
                context.fill(x + edge + 1, y + py, x + w - edge - 1, y + py + 1, fill);
            }
        }

        // Slim accent marker gives each action a distinct identity.
        int markerX = x + Math.max(radius + 5, 12);
        int markerY = y + h / 2 - 2;
        context.fill(markerX, markerY, markerX + 4, markerY + 4, accent);

        String title = solo ? "SINGLEPLAYER" : "MULTIPLAYER";
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(title),
            x + w / 2 + 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFE8EAF5);
    }
}
