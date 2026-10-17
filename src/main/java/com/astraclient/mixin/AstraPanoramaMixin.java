package com.astraclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.PanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla title-screen panorama with Astra's lightweight animated
 * geometric background. The normal title-screen buttons and accessibility
 * behaviour remain owned by Minecraft.
 */
@Mixin(PanoramaRenderer.class)
public abstract class AstraPanoramaMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void astra$renderBackground(DrawContext context, int width, int height, float alpha, CallbackInfo ci) {
        ci.cancel();
        long time = System.currentTimeMillis();
        context.fill(0, 0, width, height, 0xFF070A14);

        // Layered horizontal color fields give the scene depth without loading
        // a large texture or adding a shader dependency.
        for (int y = 0; y < height; y += 4) {
            float progress = y / (float) Math.max(1, height);
            int blue = (int) (18 + progress * 18);
            int green = (int) (10 + progress * 5);
            context.fill(0, y, width, Math.min(height, y + 4),
                0xFF000000 | (8 << 16) | (green << 8) | blue);
        }

        int gridStep = Math.max(24, Math.min(42, width / 28));
        int drift = (int) ((time / 45L) % gridStep);
        for (int x = -gridStep + drift; x < width; x += gridStep) {
            context.fill(x, 0, x + 1, height, 0x183C6A9A);
        }
        for (int y = drift; y < height; y += gridStep) {
            context.fill(0, y, width, y + 1, 0x183C6A9A);
        }

        // Large angular Astra emblem, offset from the center so it frames rather
        // than competes with the vanilla title and menu buttons.
        int cx = width * 3 / 4;
        int cy = height / 2;
        int pulse = 90 + (int) (35 * (0.5 + 0.5 * Math.sin(time / 520.0)));
        int glow = (pulse << 24) | 0x003F43C8;
        for (int ring = 0; ring < 4; ring++) {
            int radius = 62 + ring * 17;
            int inset = (int) (Math.sin(time / 1100.0 + ring) * 8.0);
            int color = ring % 2 == 0 ? glow : 0x454AD9F5;
            context.fill(cx - radius, cy - radius + inset, cx + radius, cy - radius + inset + 1, color);
            context.fill(cx - radius, cy + radius + inset, cx + radius, cy + radius + inset + 1, color);
            context.fill(cx - radius, cy - radius + inset, cx - radius + 1, cy + radius + inset, color);
            context.fill(cx + radius - 1, cy - radius + inset, cx + radius, cy + radius + inset, color);
        }

        // Sparse moving stars/data points.
        for (int i = 0; i < 44; i++) {
            int sx = Math.floorMod(i * 97 + (int) (time / (19L + i % 5)), Math.max(1, width));
            int sy = Math.floorMod(i * 53 + (int) (time / (31L + i % 7)), Math.max(1, height));
            int size = i % 7 == 0 ? 3 : 1;
            context.fill(sx, sy, Math.min(width, sx + size), Math.min(height, sy + size),
                i % 3 == 0 ? 0xFF55E7FF : 0xFF7770D8);
        }

        // Corner rails and understated branding complete the custom backdrop.
        context.fill(18, 18, Math.min(width - 18, 170), 20, 0xFF4AD9F5);
        context.fill(18, 18, 20, 64, 0xFF4AD9F5);
        context.fill(Math.max(18, width - 170), Math.max(18, height - 22), width - 18, height - 20, 0xFF6D54D9);
        context.fill(width - 20, Math.max(18, height - 64), width - 18, height - 20, 0xFF6D54D9);
        context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
            "ASTRA  /  FABRIC CLIENT", 28, 31, 0xFFB9C8E8, false);
        context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
            "NEBULA INTERFACE  •  1.21.4", 28, 44, 0xFF687DAB, false);
    }
}
