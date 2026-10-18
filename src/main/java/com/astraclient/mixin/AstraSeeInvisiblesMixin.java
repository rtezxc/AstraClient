package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows the local viewer to render otherwise invisible living entities. This only affects
 * the local client's visibility query; it does not change server state or outgoing packets.
 */
@Mixin(Entity.class)
public abstract class AstraSeeInvisiblesMixin {
    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    private void astra$showInvisibleLivingEntities(PlayerEntity viewer,
                                                   CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity)(Object)this;
        if (AstraClient.isEnabled("seeinvisibles")
            && viewer == MinecraftClient.getInstance().player
            && entity instanceof LivingEntity
            && !entity.isRemoved()) {
            cir.setReturnValue(false);
        }
    }
}
