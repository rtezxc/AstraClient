package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Suppresses entity-collision pushing only for the local player while enabled. */
@Mixin(Entity.class)
public abstract class AstraNoPushMixin {
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void astra$cancelLocalPush(Entity other, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity
            && (Object)this == MinecraftClient.getInstance().player
            && AstraClient.isEnabled("nopush")) {
            ci.cancel();
        }
    }
}
