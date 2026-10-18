package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Local notification when another player consumes a totem; no packet behavior is changed. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class AstraTotemPopAlertMixin {
    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void astra$notifyTotemPop(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (!AstraClient.isEnabled("totempopalert") || packet.getStatus() != 35) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        Entity entity = packet.getEntity(client.world);
        if (entity instanceof PlayerEntity player && player != client.player) {
            AstraClient.notifyUser(player.getName().getString() + " popped a totem");
        }
    }
}
