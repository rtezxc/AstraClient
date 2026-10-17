package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes only the local item-use delay while Fast Place is enabled. */
@Mixin(MinecraftClient.class)
public abstract class AstraFastPlaceMixin {
    @Shadow private int itemUseCooldown;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void astra$clearUseCooldown(CallbackInfo ci) {
        if (AstraClient.isEnabled("fastplace")) {
            itemUseCooldown = 0;
        }
    }
}
