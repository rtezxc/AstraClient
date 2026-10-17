package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes selected first-person overlays locally. Every injection is optional so
 * minor mapping/signature differences cannot prevent the rest of Astra from loading.
 */
@Mixin(InGameHud.class)
public abstract class AstraOverlayRemovalMixin {
    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hidePumpkinOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (texture == null) return;
        String path = texture.getPath().toLowerCase(java.util.Locale.ROOT);
        if ((AstraClient.isEnabled("nopumpkinoverlay") && path.contains("pumpkinblur"))
                || (AstraClient.isEnabled("nopowdersnowoverlay") && path.contains("powder_snow"))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hidePortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (AstraClient.isEnabled("noportaloverlay")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hideSpyglassOverlay(DrawContext context, float scale, CallbackInfo ci) {
        if (AstraClient.isEnabled("nospyglassoverlay")) {
            ci.cancel();
        }
    }


    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hideNauseaOverlay(DrawContext context, float strength, CallbackInfo ci) {
        if (AstraClient.isEnabled("nonauseaoverlay")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hideVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
        if (AstraClient.isEnabled("novignette")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hideScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (AstraClient.isEnabled("noscoreboard")) {
            ci.cancel();
        }
    }
}
