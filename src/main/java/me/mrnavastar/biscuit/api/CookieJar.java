package me.mrnavastar.biscuit.api;

import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

public interface CookieJar {

    @SneakyThrows
    default void setCookie(Object cookie) {
        throw new Exception("Cant set cookie during login network phase");
    };

    <T> CompletableFuture<T> getCookie(Class<T> cookieType);
}
