package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import me.mrnavastar.transferapi.CookieRegistry;
import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.api.ServerTransferable;
import me.mrnavastar.transferapi.interfaces.TransferMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements ServerTransferable {

    @Shadow @Final protected ClientConnection connection;
    @Shadow protected abstract GameProfile getProfile();

    @Override
    public void transferToServer(String host, int port) {
        ((ServerTransferable) connection).transferToServer(host, port);
    }

    @Override
    public boolean wasTransferred() {
        return ((ServerTransferable) connection).wasTransferred();
    }

    @Override
    public void setCookie(Identifier cookieId, byte[] cookie) {
        ((ServerTransferable) connection).setCookie(cookieId, cookie);
    }

    @Override
    public byte[] getCookie(Identifier cookieId) {
        return ((ServerTransferable) connection).getCookie(cookieId);
    }

    @Inject(method = "onCookieResponse", at = @At("HEAD"))
    private void storeCookie(CookieResponseC2SPacket packet, CallbackInfo ci) {
        if (((TransferMeta) connection).fabric_getRequestedCookies().remove(packet.key())) return;

        byte[] cookie = CookieRegistry.verifyCookie(packet.key(), packet.payload());
        if (cookie == null) return; // TODO: Fire an event maybe???

        if (ServerTransferEvents.COOKIE_RESPONSE.invoker().onCookieResponse(getProfile(), packet.key(), cookie))
            ((TransferMeta) connection).fabric_getCookieStore().put(packet.key(), cookie);
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerCommonNetworkHandler instance, Text reason) {
        return false;
    }
}