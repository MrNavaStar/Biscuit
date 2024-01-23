package me.mrnavastar.cookiejar.api;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.mrnavastar.cookiejar.CookieSigner;
import me.mrnavastar.cookiejar.DebugCommands;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerCookieStore;
import net.minecraft.util.Identifier;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CookieJar implements DedicatedServerModInitializer {

    // Map of cooke ids, signing secrets, and hashing macs
    private static final ConcurrentHashMap<Class<? extends Cookie>, RegisteredCookie> registeredCookies = new ConcurrentHashMap<>();

    private record RegisteredCookie(Identifier identifier, byte[] secret, Mac mac) {}

    public static class CookieRegistrar {
        private final Identifier cookieId;
        private final Class<? extends Cookie> cookieType;
        private byte[] secret = new byte[]{};
        private Mac mac = CookieSigner.DEFAULT_MAC;

        public CookieRegistrar(Identifier cookieId, Class<? extends Cookie> cookieType) {
            this.cookieId = cookieId;
            this.cookieType = cookieType;
        }

        public CookieRegistrar setSecret(byte[] secret) {
            this.secret = secret;
            return this;
        }

        public CookieRegistrar setSecret(String secret) {
            return setSecret(secret.getBytes(StandardCharsets.UTF_8));
        }

        public CookieRegistrar setCustomMac(Mac mac) {
            this.mac = mac;
            return this;
        }

        public void finish() {
            registeredCookies.put(cookieType, new RegisteredCookie(cookieId, secret, mac));
        }
    }

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> DebugCommands.init(dispatcher));

        /*ServerLoginConnectionEvents.INIT.register((serverLoginNetworkHandler, minecraftServer) -> {
            serverLoginNetworkHandler.wasTransferred();
        });*/
    }

    public static CookieRegistrar register(Identifier cookieId, Class<? extends Cookie> cookieType) {
        return new CookieRegistrar(cookieId, cookieType);
    }

    public static void unregister(Class<? extends Cookie> cookieType) {
        registeredCookies.remove(cookieType);
    }

    public static void setCookie(ServerCookieStore cookieStore, Cookie cookie) {
        try {
            RegisteredCookie registeredCookie = registeredCookies.get(cookie.getClass());
            if (registeredCookie == null) return;

            ByteBuf buf = Unpooled.buffer(0, 5120);
            cookie.encode(buf);
            cookieStore.setCookie(registeredCookie.identifier, CookieSigner.signCookie(buf.array(), registeredCookie.secret, registeredCookie.mac));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static <T extends Cookie> CompletableFuture<T> getCookie(ServerCookieStore cookieStore, Class<T> cookieType) {
        RegisteredCookie registeredCookie = registeredCookies.get(cookieType);
        if (registeredCookie == null) return null;

        CompletableFuture<T> future = new CompletableFuture<>();
        cookieStore.getCookie(registeredCookie.identifier).whenComplete((cookieData, t) -> {
            try {
                byte[] validData = CookieSigner.verifyCookie(cookieData, registeredCookie.secret, registeredCookie.mac);
                if (validData == null) {
                    future.cancel(true);
                    return;
                }

                ByteBuf buf = Unpooled.wrappedBuffer(validData);
                Cookie cookieInstance = cookieType.getDeclaredConstructor().newInstance();
                cookieInstance.decode(buf);
                future.complete(cookieType.cast(cookieInstance));
            } catch (Exception e) {
                future.cancel(true);
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        return future;
    }
}