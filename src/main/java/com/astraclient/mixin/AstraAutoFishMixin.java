package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Detects the vanilla fishing-bobber splash sound for Astra's optional Auto Fish module. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class AstraAutoFishMixin {
    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void astra$detectFishingSplash(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (AstraClient.isEnabled("autofish")
            && packet.getSound() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
            AstraClient.onAutoFishSplash(packet.getX(), packet.getY(), packet.getZ());
        }
    }
}
