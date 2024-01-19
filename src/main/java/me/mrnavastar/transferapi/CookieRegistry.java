package me.mrnavastar.transferapi;

import lombok.Getter;
import me.mrnavastar.transferapi.commands.DebugCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class CookieRegistry implements ModInitializer {

    @Getter
    private static boolean transferOnly = false;
    // Map of cooke ids and cookie signing secrets
    private static final HashMap<Identifier, byte[]> registeredCookies = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DebugCommands.init(server.getCommandManager().getDispatcher()));

        //
        /*ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            if (transferOnly && handler.wasTransferred()) handler.disconnect(Text.of("This server is transfer only!"));
        });*/
    }

    public static void registerCookie(Identifier cookieId) {
        registeredCookies.put(cookieId, new byte[]{});
    }

    public static void registerCookie(Identifier cookieId, String secret) {
        registeredCookies.put(cookieId, secret.getBytes(StandardCharsets.UTF_8));
    }

    public static void unregisterCookie(Identifier cookieId) {
        registeredCookies.remove(cookieId);
    }

    public static List<Identifier> getRegisteredCookies() {
        return registeredCookies.keySet().stream().toList();
    }

    public static byte[] signCookie(Identifier cookieId, byte[] cookie) {
        byte[] secret = registeredCookies.get(cookieId);
        if (secret == null) return null;

        try {
            return CookieUtils.signCookie(cookie, secret, "HmacSHA256");
        } catch (NoSuchAlgorithmException | InvalidKeyException ignore) {
            return null;
        }
    }

    public static byte[] verifyCookie(Identifier cookieId, byte[] cookie) {
        byte[] secret = registeredCookies.get(cookieId);
        if (secret == null) return null;

        try {
            return CookieUtils.verifyCookie(cookie, secret, "HmacSHA256");
        } catch (NoSuchAlgorithmException | InvalidKeyException ignore) {
            return null;
        }
    }
}