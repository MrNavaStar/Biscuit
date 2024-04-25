package me.mrnavastar.biscuit.api;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.util.CookieSigner;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.util.Identifier;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Biscuit {

    // Map of cooke ids, signing secrets, and hashing macs
    private static final Map<Class<?>, RegisteredCookie> registeredCookies = new ConcurrentHashMap<>();
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    public record RegisteredCookie(Identifier identifier, byte[] secret, Mac mac) {}

    public static class CookieRegistrar {
        private final Identifier id;
        private final Class<?> cookieType;
        private byte[] secret = new byte[]{};
        private Mac mac = CookieSigner.DEFAULT_MAC;

        public CookieRegistrar(Identifier identifier, Class<?> cookieType) {
            this.id = identifier;
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
            registeredCookies.put(cookieType, new RegisteredCookie(id, secret, mac));
        }
    }

    public static CookieRegistrar register(Identifier identifier, Class<?> cookieType) {
        return new CookieRegistrar(identifier, cookieType);
    }

    public static void unregister(Class<?> cookieType) {
        registeredCookies.remove(cookieType);
    }

    public static void setCookie(CookieJar cookieJar, Object cookie) {
        RegisteredCookie registeredCookie = registeredCookies.get(cookie.getClass());
        if (registeredCookie == null) return;

        try (Output out = new Output(5120)) {
            kryo.writeObject(out, cookie);
            ((InternalStuff) cookieJar).biscuit$send(new StoreCookieS2CPacket(registeredCookie.identifier, CookieSigner.signCookie(out.toBytes(), registeredCookie.secret, registeredCookie.mac)));
        } catch (InvalidKeyException | CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> CompletableFuture<T> getCookie(CookieJar cookieJar, Class<T> cookieType) {
        RegisteredCookie registeredCookie = registeredCookies.get(cookieType);
        if (registeredCookie == null) return null;

        CompletableFuture<T> future = new CompletableFuture<>();
        ((InternalStuff) cookieJar).biscuit$getRawCookie(registeredCookie.identifier).whenComplete((cookieData, t) -> {
            try {
                byte[] validData = CookieSigner.verifyCookie(cookieData, registeredCookie.secret, registeredCookie.mac);
                if (validData == null) {
                    future.cancel(true);
                    return;
                }

                try (Input in = new Input(validData)) {
                    future.complete(kryo.readObject(in, cookieType));
                }

            } catch (Exception e) {
                future.cancel(true);
                throw new RuntimeException(e);
            }
        });
        return future;
    }
}