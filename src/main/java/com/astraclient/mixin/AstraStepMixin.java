package com.astraclient.mixin;

import com.astraclient.AstraClient;
import com.astraclient.AstraModuleOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the local player's effective step height while enabled. The vanilla API exposes
 * getStepHeight() in this version but no setter, so this is applied at the point of use.
 */
@Mixin(Entity.class)
public abstract class AstraStepMixin {
    @Inject(method = "getStepHeight", at = @At("RETURN"), cancellable = true)
    private void astra$overrideLocalStepHeight(CallbackInfoReturnable<Float> cir) {
        Entity entity = (Entity)(Object)this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity != client.player || !AstraClient.isEnabled("step")) return;
        double configured = AstraModuleOptions.numberValue("step", "height", 1.0D);
        float requested = (float)Math.max(0.6D, Math.min(1.5D, configured));
        cir.setReturnValue(Math.max(cir.getReturnValueF(), requested));
    }
}
