package me.mrnavastar.cookiejar.api;

import java.util.concurrent.CompletableFuture;

public interface ServerCookieJar {

    void setCookie(Object cookie);

    <T> CompletableFuture<T> getCookie(Class<T> cookieType);
}
