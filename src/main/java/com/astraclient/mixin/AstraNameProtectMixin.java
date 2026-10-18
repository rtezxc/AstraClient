package com.astraclient.mixin;

import com.astraclient.AstraClient;
import com.astraclient.AstraModuleOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

/**
 * Local-display-only name masking. Outgoing chat, commands, packets, and server-side identity
 * are untouched. Replacement fragments deliberately drop click/hover metadata so a spoofed
 * alias cannot inherit interactive actions from the original username.
 */
@Mixin(ChatHud.class)
public abstract class AstraNameProtectMixin {
    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/text/Text;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Text astra$protectOwnName(Text message) {
        return astra$rewriteName(message);
    }

    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Text astra$protectSignedOwnName(Text message) {
        return astra$rewriteName(message);
    }

    @Unique
    private static Text astra$rewriteName(Text message) {
        if (message == null || !AstraClient.isEnabled("nameprotect")) return message;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return message;

        String username = client.player.getName().getString();
        String alias = AstraModuleOptions.value("nameprotect", "alias", "Astra").trim();
        if (username.isBlank() || alias.isBlank() || alias.length() > 32 || username.equals(alias)
            || !message.getString().contains(username)) {
            return message;
        }

        MutableText rebuilt = Text.empty();
        message.visit((style, segment) -> {
            astra$astra$appendReplacement(rebuilt, segment, style, username, alias);
            return Optional.empty();
        }, Style.EMPTY);
        return rebuilt;
    }

    @Unique
    private static void astra$appendReplacement(MutableText output, String segment, Style style,
                                         String username, String alias) {
        int from = 0;
        int found;
        while ((found = segment.indexOf(username, from)) >= 0) {
            if (found > from) {
                output.append(Text.literal(segment.substring(from, found)).setStyle(style));
            }
            // The alias is display text only and cannot inherit commands or hover actions.
            output.append(Text.literal(alias).setStyle(Style.EMPTY));
            from = found + username.length();
        }
        if (from < segment.length()) {
            output.append(Text.literal(segment.substring(from)).setStyle(style));
        }
    }
}
