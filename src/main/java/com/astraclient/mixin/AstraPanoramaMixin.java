package com.astraclient.mixin;

import com.astraclient.AstraBackgroundRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces only the title screen panorama; Minecraft keeps ownership of its widgets. */
@Mixin(TitleScreen.class)
public abstract class AstraPanoramaMixin {
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void astra$renderBackground(DrawContext context, float delta, CallbackInfo ci) {
        if (!AstraClient.isCustomMenuBackgroundEnabled()) return;
        ci.cancel();
        AstraBackgroundRenderer.render(context);
    }
}
