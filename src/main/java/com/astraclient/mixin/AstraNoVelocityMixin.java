package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional local-player velocity suppression. This does not modify other entity velocity
 * updates and is completely inactive unless the No Velocity module is enabled.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class AstraNoVelocityMixin {
    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void astra$cancelLocalVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (AstraClient.isEnabled("novelocity")
            && client.player != null
            && packet.getEntityId() == client.player.getId()) {
            ci.cancel();
        }
    }
}
