package me.mrnavastar.cookiejar.mixin;

import me.mrnavastar.cookiejar.api.ServerCookieJar;
import net.fabricmc.fabric.api.networking.v1.ServerCookieStore;
import net.fabricmc.fabric.api.networking.v1.ServerTransferable;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerTransferable, ServerCookieStore, ServerCookieJar {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Override
    public void transferToServer(String host, int port) {
        //networkHandler.transferToServer(host, port);
    }

    @Override
    public boolean wasTransferred() {
        //return networkHandler.wasTransferred();
        return false;
    }

    @Override
    public void setCookie(Identifier identifier, byte[] bytes) {
        //networkHandler.setCookie(identifier, bytes);
    }

    @Override
    public CompletableFuture<byte[]> getCookie(Identifier identifier) {
        //return networkHandler.getCookie(identifier);
        return null;
    }

    @Override
    public void setCookie(Object cookie) {
        ((ServerCookieJar) networkHandler).setCookie(cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return ((ServerCookieJar) networkHandler).getCookie(cookieType);
    }
}
