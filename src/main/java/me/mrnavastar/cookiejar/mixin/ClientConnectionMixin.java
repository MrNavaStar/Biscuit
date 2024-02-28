package me.mrnavastar.cookiejar.mixin;

import me.mrnavastar.cookiejar.api.CookieJar;
import me.mrnavastar.cookiejar.api.ServerCookieJar;
import net.fabricmc.fabric.api.networking.v1.ServerCookieStore;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ServerCookieJar {

    @Override
    public void setCookie(Object cookie) {
       CookieJar.setCookie((ServerCookieStore) this, cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return CookieJar.getCookie((ServerCookieStore) this, cookieType);
    }
}
