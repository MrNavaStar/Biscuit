package me.mrnavastar.transferapi;

import me.mrnavastar.transferapi.commands.DebugCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;

public class CookieRegistry implements ModInitializer {

    // Map of cooke ids, signing secrets, and hashing macs
    private static final HashMap<Identifier, Pair<byte[], Mac>> registeredCookies = new HashMap<>();

    public static class CookieRegistrar {
        private final Identifier cookieId;
        private byte[] secret = new byte[]{};
        private Mac mac = CookieUtils.DEFAULT_MAC;

        public CookieRegistrar(Identifier cookieId) {
            this.cookieId = cookieId;
        }

        public CookieRegistrar setSecret(byte[] secret) {
            this.secret = secret;
            return this;
        }

        public CookieRegistrar setSecret(String secret) {
            this.secret = secret.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public CookieRegistrar setCustomMac(Mac mac) {
            this.mac = mac;
            return this;
        }

        public void finish() {
            registeredCookies.put(cookieId, new Pair<>(secret, mac));
        }
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DebugCommands.init(server.getCommandManager().getDispatcher()));
    }

    public static CookieRegistrar register(Identifier cookieId) {
        return new CookieRegistrar(cookieId);
    }

    public static void unregister(Identifier cookieId) {
        registeredCookies.remove(cookieId);
    }

    public static List<Identifier> getRegisteredCookies() {
        return registeredCookies.keySet().stream().toList();
    }

    public static byte[] signCookie(Identifier cookieId, byte[] cookie) {
        Pair<byte[], Mac> signingData = registeredCookies.get(cookieId);
        if (signingData == null) return null;

        try {
            return CookieUtils.signCookie(cookie, signingData.getLeft(), signingData.getRight());
        } catch (InvalidKeyException ignore) {
            return null;
        }
    }

    public static byte[] verifyCookie(Identifier cookieId, byte[] cookie) {
        Pair<byte[], Mac> signingData = registeredCookies.get(cookieId);
        if (signingData == null) return null;

        try {
            return CookieUtils.verifyCookie(cookie, signingData.getLeft(), signingData.getRight());
        } catch (InvalidKeyException ignore) {
            return null;
        }
    }
}