package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.interfaces.ConnectionGrabber;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Getter
@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements CookieStore, ConnectionGrabber {

    @Shadow @Final protected ClientConnection connection;
    @Shadow protected abstract GameProfile getProfile();

    @Inject(method = "onCookieResponse", at = @At("HEAD"))
    private void storeCookie(CookieResponseC2SPacket packet, CallbackInfo ci) {
        if (ServerTransferEvents.COOKIE_RESPONSE.invoker().onCookieResponse(getProfile(), packet))
            ((CookieStore) connection).fabric_getStore().put(packet.key(), packet.payload());
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerCommonNetworkHandler instance, Text reason) {
        return false;
    }
}