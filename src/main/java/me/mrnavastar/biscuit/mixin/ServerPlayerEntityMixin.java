package me.mrnavastar.biscuit.mixin;

import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.api.CookieJar;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements CookieJar, InternalStuff {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Override
    public void setCookie(Object cookie) {
        networkHandler.setCookie(cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return networkHandler.getCookie(cookieType);
    }

    @Override
    public void biscuit$send(Packet<?> packet) {
        networkHandler.sendPacket(packet);
    }

    @Override
    public CompletableFuture<byte[]> biscuit$getRawCookie(Identifier cookieId) {
        return ((InternalStuff) networkHandler).biscuit$getRawCookie(cookieId);
    }
}
