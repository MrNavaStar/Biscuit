package me.mrnavastar.cookiejar.mixin;

import me.mrnavastar.cookiejar.api.ServerCookieJar;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin implements ServerCookieJar {

    @Shadow @Final protected ClientConnection connection;

    @Override
    public void setCookie(Object cookie) {
        ((ServerCookieJar) connection).setCookie(cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return ((ServerCookieJar) connection).getCookie(cookieType);
    }
}
