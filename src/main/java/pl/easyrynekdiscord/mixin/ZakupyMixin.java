package pl.easyrynekdiscord.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.easyrynekdiscord.DiscordNotifier;

/** Podpina sie pod Zakupy.dodaj(Zakup) z moda EasyRynek - tam laduje kazdy zakup. */
@Pseudo
@Mixin(targets = "pl.rynekklikacz.Zakupy", remap = false)
public abstract class ZakupyMixin {

    @Inject(method = "dodaj", at = @At("HEAD"), remap = false)
    private static void easyrynekdiscord$onZakup(@Coerce Object zakup, CallbackInfo ci) {
        try {
            DiscordNotifier.zakup(zakup);
        } catch (Throwable t) {
            // nigdy nie wywalamy moda rynku przez blad powiadomienia
            t.printStackTrace();
        }
    }
}
