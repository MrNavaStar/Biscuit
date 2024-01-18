package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.TransferAPI;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {

    @Shadow @Final private ClientConnection connection;
    @Shadow private GameProfile profile;

    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    private void requestCookies(GameProfile profile, CallbackInfo ci) {
        TransferAPI.getRegisteredCookies().forEach(id -> connection.send(new CookieRequestS2CPacket(id)));
    }

    @Inject(method = "onCookieResponse", at = @At("HEAD"))
    private void storeCookie(CookieResponseC2SPacket packet, CallbackInfo ci) {
        ((CookieStore) connection).fabric_getStore().put(packet.key(), packet.payload());
        ServerTransferEvents.COOKIE_RESPONSE.invoker().onCookieResponse(profile, packet);
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerLoginNetworkHandler instance, Text reason) {
        return false;
    }
}