package com.astraclient.mixin;

import com.astraclient.AstraBackgroundRenderer;
import com.astraclient.AstraClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives the Singleplayer and Multiplayer browser screens the same Astra backdrop. */
@Mixin(Screen.class)
public abstract class AstraWorldMenuBackgroundMixin {
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void astra$renderWorldMenuBackground(DrawContext context, float delta, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (!AstraClient.isCustomMenuBackgroundEnabled()) return;
        if (!(screen instanceof SelectWorldScreen) && !(screen instanceof MultiplayerScreen)) {
            return;
        }
        ci.cancel();
        AstraBackgroundRenderer.render(context);
    }
}
