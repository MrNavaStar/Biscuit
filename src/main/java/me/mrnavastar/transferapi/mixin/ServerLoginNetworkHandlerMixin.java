package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import me.mrnavastar.transferapi.CookieRegistry;
import me.mrnavastar.transferapi.CookieUtils;
import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.api.ServerTransferable;
import me.mrnavastar.transferapi.interfaces.ClientConnectionMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.Mac;
import java.security.InvalidKeyException;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin implements ServerTransferable {

    @Shadow @Final private ClientConnection connection;
    @Shadow private GameProfile profile;

    @Override
    public void transferToServer(String host, int port) {
        ((ServerTransferable) connection).transferToServer(host, port);
    }

    @Override
    public boolean wasTransferred() {
        return ((ServerTransferable) connection).wasTransferred();
    }

    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    private void requestCookies(GameProfile profile, CallbackInfo ci) {
        new CookieRegistry(connection);
        CookieRegistry.getRegisteredCookies().forEach(id -> connection.send(new CookieRequestS2CPacket(id)));
    }

    @Inject(method = "onCookieResponse", at = @At("HEAD"))
    private void storeCookie(CookieResponseC2SPacket packet, CallbackInfo ci) throws InvalidKeyException, CloneNotSupportedException {
        if (((ClientConnectionMeta) connection).fabric_getRequestedCookies().remove(packet.key())) return;

        Pair<byte[], Mac> signingData = ((ClientConnectionMeta) connection).fabric_getCookieRegistry().get(packet.key());
        if (signingData == null) return;

        byte[] cookie = CookieUtils.verifyCookie(packet.payload(), signingData.getLeft(), signingData.getRight());
        if (cookie == null) return; // TODO: Fire an event maybe???

        if (ServerTransferEvents.COOKIE_RESPONSE.invoker().onCookieResponse(profile, packet.key(), cookie))
            ((ClientConnectionMeta) connection).fabric_getCookieStore().put(packet.key(), cookie);
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerLoginNetworkHandler instance, Text reason) {
        return false;
    }
}