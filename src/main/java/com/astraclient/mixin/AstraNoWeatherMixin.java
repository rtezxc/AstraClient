package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Suppresses local weather particles and streaks without changing world weather state. */
@Mixin(WorldRenderer.class)
public abstract class AstraNoWeatherMixin {
    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void astra$hideWeather(CallbackInfo ci) {
        if (AstraClient.isEnabled("noweather")) {
            ci.cancel();
        }
    }
}
