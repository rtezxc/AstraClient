package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes the first-person fire overlay without changing fire damage or game state. */
@Mixin(InGameOverlayRenderer.class)
public abstract class AstraFireOverlayMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private static void astra$hideFireOverlay(MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers,
                                              CallbackInfo ci) {
        if (AstraClient.isEnabled("nofireoverlay")) {
            ci.cancel();
        }
    }
}
