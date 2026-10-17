package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Clears the local break cooldown; vanilla block hardness and server rules still apply. */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class AstraFastBreakMixin {
    @Shadow private int blockBreakingCooldown;

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void astra$clearBreakCooldown(BlockPos pos, Direction direction,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (AstraClient.isEnabled("fastbreak")) {
            blockBreakingCooldown = 0;
        }
    }
}
