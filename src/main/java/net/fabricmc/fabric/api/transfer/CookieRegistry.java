package net.fabricmc.fabric.api.transfer;

import net.fabricmc.fabric.impl.transfer.CookieSigner;
import me.mrnavastar.transferapi.commands.DebugCommands;
import net.fabricmc.fabric.impl.transfer.ClientConnectionMeta;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class CookieRegistry implements ModInitializer {

    // Map of cooke ids, signing secrets, and hashing macs
    private static final HashMap<Identifier, Pair<byte[], Mac>> registeredCookies = new HashMap<>();

    public CookieRegistry() {}

    public CookieRegistry(ClientConnection connection) {
        ((ClientConnectionMeta) connection).fabric_setCookieRegistry(registeredCookies);
    }

    public static class CookieRegistrar {
        private final Identifier cookieId;
        private byte[] secret = new byte[]{};
        private Mac mac = CookieSigner.DEFAULT_MAC;

        public CookieRegistrar(Identifier cookieId) {
            this.cookieId = cookieId;
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
}