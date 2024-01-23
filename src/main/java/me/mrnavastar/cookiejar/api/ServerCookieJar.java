package me.mrnavastar.cookiejar.api;

import java.util.concurrent.CompletableFuture;

public interface ServerCookieJar {

    void setCookie(Cookie cookie);

    <T extends Cookie> CompletableFuture<T> getCookie(Class<T> cookieType);
}
