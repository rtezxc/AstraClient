package com.astraclient.mixin;

import com.astraclient.AstraClient;
import com.astraclient.AstraModuleOptions;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optional local projection-ratio adjustment. Clones the vanilla projection matrix and changes
 * only the horizontal scale, preserving the game's active FOV and clipping planes.
 */
@Mixin(GameRenderer.class)
public abstract class AstraAspectRatioMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void astra$applyAspectRatio(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!AstraClient.isEnabled("aspectratio")) return;
        Matrix4f original = cir.getReturnValue();
        if (original == null) return;

        double ratio = AstraModuleOptions.numberValue("aspectratio", "ratio", 1.777D);
        ratio = Math.max(1.0D, Math.min(2.5D, ratio));
        float verticalScale = original.m11();
        if (!Float.isFinite(verticalScale) || verticalScale <= 0.0F) return;

        Matrix4f adjusted = new Matrix4f(original);
        adjusted.m00(verticalScale / (float)ratio);
        cir.setReturnValue(adjusted);
    }
}
