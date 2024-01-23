package me.mrnavastar.cookiejar.mixin;

import me.mrnavastar.cookiejar.api.Cookie;
import me.mrnavastar.cookiejar.api.CookieJar;
import me.mrnavastar.cookiejar.api.ServerCookieJar;
import net.fabricmc.fabric.api.networking.v1.ServerCookieStore;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ServerCookieJar {

    @Override
    public void setCookie(Cookie cookie) {
       CookieJar.setCookie((ServerCookieStore) this, cookie);
    }

    @Override
    public <T extends Cookie> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return CookieJar.getCookie((ServerCookieStore) this, cookieType);
    }
}
