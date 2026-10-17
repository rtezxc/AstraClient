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

/**
 * Responsive title-screen layout and Astra button skin.
 *
 * Skinning is performed after TitleScreen has rendered its widgets. This keeps
 * Minecraft's real ButtonWidget click, narration, and focus handling intact and
 * avoids relying on mixins into inherited/abstract widget render methods.
 */
@Mixin(TitleScreen.class)
public abstract class AstraTitleButtonsMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void astra$layoutMainButtons(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int centerX = screen.width / 2;
        boolean stacked = screen.width < 380;
        int primaryW = Math.min(164, Math.max(100, screen.width - 24));
        int mainY = screen.height / 2 - (stacked ? 66 : 48);
        int soloY = mainY;
        int multiY = stacked ? mainY + 36 : mainY;
        int realmsY = stacked ? mainY + 72 : mainY + 36;
        int utilityY = realmsY + 28;
        int utilityW = Math.min(98, Math.max(72, (screen.width - 36) / 2));

        for (var element : screen.children()) {
            if (!(element instanceof ButtonWidget button)) continue;
            String label = button.getMessage().getString().toLowerCase(java.util.Locale.ROOT);
            if (label.contains("singleplayer") || label.contains("single player")) {
                button.setDimensions(primaryW, 32);
                button.setPosition(stacked ? centerX - primaryW / 2 : centerX - primaryW - 6, soloY);
            } else if (label.contains("multiplayer")) {
                button.setDimensions(primaryW, 32);
                button.setPosition(stacked ? centerX - primaryW / 2 : centerX + 6, multiY);
            } else if (label.contains("minecraft realms") || label.equals("realms")) {
                button.setDimensions(Math.min(164, Math.max(100, screen.width - 24)), 20);
                button.setPosition(centerX - button.getWidth() / 2, realmsY);
            } else if (label.contains("options")) {
                button.setDimensions(utilityW, 20);
                button.setPosition(centerX - utilityW - 3, utilityY);
            } else if (label.contains("quit")) {
                button.setDimensions(utilityW, 20);
                button.setPosition(centerX + 3, utilityY);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void astra$drawPremiumMenu(DrawContext context, int mouseX, int mouseY,
                                      float delta, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int centerX = screen.width / 2;
        boolean stacked = screen.width < 380;
        int primaryW = Math.min(164, Math.max(100, screen.width - 24));
        int mainY = screen.height / 2 - (stacked ? 66 : 48);
        int soloY = mainY;
        int multiY = stacked ? mainY + 36 : mainY;
        int realmsY = stacked ? mainY + 72 : mainY + 36;
        int utilityY = realmsY + 28;
        int headerY = mainY - 20;
        int footerY = utilityY + 29;

        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
            Text.literal("Choose how you want to play"), centerX, headerY, 0xFFF4F3FF);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
            Text.literal("Solo world  •  Play with others"), centerX, footerY, 0xFFB7C3D9);

        // Re-skin the actual buttons after vanilla draws them. Their hitboxes and
        // action callbacks remain the original Minecraft widgets.
        for (var element : screen.children()) {
            if (!(element instanceof ButtonWidget button)) continue;
            String label = button.getMessage().getString().toLowerCase(java.util.Locale.ROOT);
            boolean solo = label.contains("singleplayer") || label.contains("single player");
            boolean multi = label.contains("multiplayer");
            if (!solo && !multi) continue;
            int x = button.getX();
            int y = button.getY();
            int w = button.getWidth();
            int h = button.getHeight();
            int accent = solo ? 0xFFB16CFF : 0xFF39DDF5;
            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            drawCapsule(context, x, y, w, h, accent, hovered);
            context.fill(x + 12, y + h / 2 - 2, x + 16, y + h / 2 + 2, accent);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(solo ? "SINGLEPLAYER" : "MULTIPLAYER"),
                x + w / 2 + 3, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFE8EAF5);
        }
    }

    private static void drawCapsule(DrawContext context, int x, int y, int w, int h,
                                    int accent, boolean hovered) {
        if (w <= 0 || h <= 0) return;
        // Cover the full vanilla rectangle first; rounded corners reveal only
        // Astra's opaque background, never leftover gray button pixels.
        context.fill(x, y, x + w, y + h, 0xFF080B13);
        int radius = Math.max(4, Math.min(h / 2, 12));
        int fill = hovered ? 0xFF25243A : 0xFF111522;
        for (int py = 0; py < h; py++) {
            int inset = 0;
            if (py < radius) {
                double dy = radius - py;
                inset = radius - (int) Math.floor(Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
            } else if (py >= h - radius) {
                int fromBottom = h - 1 - py;
                double dy = radius - 1 - fromBottom;
                inset = radius - (int) Math.floor(Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
            }
            inset = Math.max(0, Math.min(inset, w / 3));
            context.fill(x + inset, y + py, x + w - inset, y + py + 1, accent);
            if (py > 0 && py < h - 1 && w - inset * 2 > 4) {
                context.fill(x + inset + 1, y + py, x + w - inset - 1, y + py + 1, fill);
            }
        }
    }
}
