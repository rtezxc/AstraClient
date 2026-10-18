package com.astraclient.mixin;

import com.astraclient.AstraClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes cobweb collision only for the local player while No Web is enabled. */
@Mixin(CobwebBlock.class)
public abstract class AstraNoWebMixin {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void astra$ignoreLocalCobweb(BlockState state, World world, BlockPos pos,
                                         Entity entity, CallbackInfo ci) {
        if (AstraClient.isEnabled("noweb") && entity == MinecraftClient.getInstance().player) {
            ci.cancel();
        }
    }
}
