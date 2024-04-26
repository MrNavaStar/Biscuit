package me.mrnavastar.biscuit.api;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import lombok.Getter;
import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.util.CookieSigner;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Biscuit implements DedicatedServerModInitializer {

    private static final Map<Class<?>, RegisteredCookie> registeredCookies = new ConcurrentHashMap<>();
    private static final Kryo kryo = new Kryo();
    private static boolean blockNonTransferredConnections = false;

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    @Getter
    public static class RegisteredCookie {
        private final Identifier id;
        private byte[] secret = new byte[]{};
        private Mac mac = CookieSigner.DEFAULT_MAC;
        private boolean kickIfMissing = false;

        public RegisteredCookie(Identifier identifier, Class<?> cookieType) {
            this.id = identifier;
            registeredCookies.put(cookieType, this);
        }

        /**
         * Set the secret that should be used to sign this cookie.
         * <p></p><b>NOTE: Do note hard code this value! It should be something that can be changed in a config in case
         * it is ever compromised.</b>
         * @param secret The secret to be set for this cookie.
         */
        public RegisteredCookie setSecret(byte[] secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Set the secret that should be used to sign this cookie.
         * <p></p><b>NOTE: Do note hard code this value! It should be something that can be changed in a config in case
         * it is ever compromised.</b>
         * @param secret The secret to be set for this cookie.
         */
        public RegisteredCookie setSecret(String secret) {
            return setSecret(secret.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Set a custom Mac for cookie singing. By default, biscuit uses a generic "HmacSHA256" algorithm.
         * @param mac The mac to be used for cookie signing.
         */
        public RegisteredCookie setCustomMac(Mac mac) {
            this.mac = mac;
            return this;
        }

        /**
         * Kick the client if they are missing this cookie or if the cookie is invalid (Bad signature).
         * @param kick Whether the client should be kicked. Defaults to false.
         */
        public void kickIfMissing(boolean kick) {
            this.kickIfMissing = kick;
        }
    }

    @Override
    public void onInitializeServer() {
        ServerLoginConnectionEvents.INIT.register((handler, server) -> {
            if (handler.wasTransferred() && blockNonTransferredConnections) {
                BiscuitEvents.CI ci = new BiscuitEvents.CI();
                BiscuitEvents.BAD_TRANSFER.invoker().onBad(handler, server, ci);

                if (!ci.isCanceled()) handler.disconnect(Text.of("This server only accepts clients that have been transferred from another server!"));
            }

            registeredCookies.forEach((type, registeredCookie) -> {
                if (!registeredCookie.kickIfMissing) return;

                ((InternalStuff) handler).biscuit$getRawCookie(registeredCookie.id).whenComplete((raw, t) -> {
                    try {
                        if (raw.length <= registeredCookie.mac.getMacLength() || CookieSigner.verifyCookie(raw, registeredCookie.secret, registeredCookie.mac) == null) {
                            BiscuitEvents.CI ci = new BiscuitEvents.CI();
                            BiscuitEvents.MISSING_COOKIE.invoker().onMissing(registeredCookie, handler, server, ci);
                            if (ci.isCanceled()) return;

                            handler.disconnect(Text.of("Server flagged a cookie sent from your client as invalid!"));
                        }
                    } catch (InvalidKeyException | CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        });
    }

    public static RegisteredCookie register(Identifier identifier, Class<?> cookieType) {
        return new RegisteredCookie(identifier, cookieType);
    }

    public static void unregister(Class<?> cookieType) {
        registeredCookies.remove(cookieType);
    }

    public static void shouldKickNonTransferredConnections(boolean shouldBlock) {
        blockNonTransferredConnections = shouldBlock;
    }

    @ApiStatus.Internal
    public static void setCookie(CookieJar cookieJar, Object cookie) {
        RegisteredCookie registeredCookie = registeredCookies.get(cookie.getClass());
        if (registeredCookie == null) return;

        try (Output out = new Output(5120)) {
            kryo.writeObject(out, cookie);
            ((InternalStuff) cookieJar).biscuit$send(new StoreCookieS2CPacket(registeredCookie.id, CookieSigner.signCookie(out.toBytes(), registeredCookie.secret, registeredCookie.mac)));
        } catch (InvalidKeyException | CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiStatus.Internal
    public static <T> CompletableFuture<T> getCookie(CookieJar cookieJar, Class<T> cookieType) {
        RegisteredCookie registeredCookie = registeredCookies.get(cookieType);
        if (registeredCookie == null) return null;

        CompletableFuture<T> future = new CompletableFuture<>();
        ((InternalStuff) cookieJar).biscuit$getRawCookie(registeredCookie.id).whenComplete((raw, t) -> {
            try {
                byte[] validData = CookieSigner.verifyCookie(raw, registeredCookie.secret, registeredCookie.mac);
                if (validData == null) {
                    future.cancel(true);
                    BiscuitEvents.INVALID_COOKIE.invoker().onInvalid(registeredCookie, ((InternalStuff) cookieJar).biscuit$getUser());
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