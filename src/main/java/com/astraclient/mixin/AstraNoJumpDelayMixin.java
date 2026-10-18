package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clears the vanilla jump cooldown only for Astra's local player when enabled. */
@Mixin(LivingEntity.class)
public abstract class AstraNoJumpDelayMixin {
    @Shadow private int jumpingCooldown;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void astra$clearLocalJumpCooldown(CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity player
            && player == MinecraftClient.getInstance().player
            && AstraClient.isEnabled("nojumpdelay")) {
            jumpingCooldown = 0;
        }
    }
}
