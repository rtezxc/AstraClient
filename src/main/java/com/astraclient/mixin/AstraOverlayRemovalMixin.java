package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
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
        if (AstraClient.isEnabled("nopumpkinoverlay")
                && texture != null
                && texture.getPath().toLowerCase(java.util.Locale.ROOT).contains("pumpkinblur")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hideFireOverlay(DrawContext context, CallbackInfo ci) {
        if (AstraClient.isEnabled("nofireoverlay")) {
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

    @Inject(method = "renderPowderSnowOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void astra$hidePowderSnowOverlay(DrawContext context, PlayerEntity player, CallbackInfo ci) {
        if (AstraClient.isEnabled("nopowdersnowoverlay")) {
            ci.cancel();
        }
    }
}
