package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents fall damage only for the local player while the No Fall module is enabled. */
@Mixin(LivingEntity.class)
public abstract class AstraNoFallMixin {
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void astra$cancelLocalFallDamage(float fallDistance, float damageMultiplier,
                                              DamageSource damageSource,
                                              CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof PlayerEntity player
            && player == MinecraftClient.getInstance().player
            && AstraClient.isEnabled("nofall")) {
            cir.setReturnValue(true);
        }
    }
}
