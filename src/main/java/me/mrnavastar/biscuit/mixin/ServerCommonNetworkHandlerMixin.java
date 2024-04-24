package me.mrnavastar.biscuit.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.api.CookieJar;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin implements CookieJar, InternalStuff {

    @Shadow @Final protected ClientConnection connection;

    @Override
    public void setCookie(Object cookie) {
        ((CookieJar) connection).setCookie(cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return ((CookieJar) connection).getCookie(cookieType);
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerCommonNetworkHandler instance, Text reason, @Local(argsOnly = true) CookieResponseC2SPacket packet) {
        return ((InternalStuff) connection).biscuit$onCookie(packet.key(), packet.payload());
    }

    @Override
    public void biscuit$send(Packet<?> packet) {
        connection.send(packet);
    }

    @Override
    public CompletableFuture<byte[]> biscuit$getRawCookie(Identifier cookieId) {
        return ((InternalStuff) connection).biscuit$getRawCookie(cookieId);
    }
}
