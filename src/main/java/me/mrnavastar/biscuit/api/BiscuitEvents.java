package me.mrnavastar.biscuit.api;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

public class BiscuitEvents {

    @Getter
    public static class CI {
        private boolean canceled = false;

        public void cancel() {
            canceled = true;
        }
    }

    /**
     * Fired when a client joins the server and is missing a cookie with
     * {@link me.mrnavastar.biscuit.api.Biscuit.RegisteredCookie#kickIfMissing(boolean)} being set.
     */
    public static final Event<ValidationFail> MISSING_COOKIE = EventFactory.createArrayBacked(ValidationFail.class, callbacks -> (registeredCookie, handler, server, ci) -> {
        for (ValidationFail callback : callbacks) {
            if (ci.isCanceled()) break;
            callback.onMissing(registeredCookie, handler, server, ci);
        }
    });

    /**
     * Fired when a client joins the server without a transfer and
     * {@link Biscuit#shouldKickNonTransferredConnections(boolean)} has been set to true.
     */
    public static final Event<BadTransfer> BAD_TRANSFER = EventFactory.createArrayBacked(BadTransfer.class, callbacks -> (handler, server, ci) -> {
        for (BadTransfer callback : callbacks) {
            callback.onBad(handler, server, ci);
        }
    });

    /**
     * Fired when a cookie sent from the client has an invalid signature (The cookie has likely been tampered with).
     */
    public static final Event<InvalidCookie> INVALID_COOKIE = EventFactory.createArrayBacked(InvalidCookie.class, callbacks -> (registeredCookie, profile) -> {
        for (InvalidCookie callback : callbacks) {
            callback.onInvalid(registeredCookie, profile);
        }
    });

    /**
     * Fired when before a client is transferred to another server.
     */
    public static final Event<PreTransfer> PRE_TRANSFER = EventFactory.createArrayBacked(PreTransfer.class, callbacks -> (packet, profile, cookieJar, ci) -> {
        for (PreTransfer callback : callbacks) {
            callback.onTransfer(packet, profile, cookieJar, ci);
        }
    });

    @FunctionalInterface
    public interface ValidationFail {
        /**
         * Fired when a client joins the server and is missing a cookie with
         * {@link me.mrnavastar.biscuit.api.Biscuit.RegisteredCookie#kickIfMissing(boolean)} being set.
         * @param registeredCookie Cookie Metadata.
         */
        void onMissing(Biscuit.RegisteredCookie registeredCookie, ServerLoginNetworkHandler handler, MinecraftServer server, CI ci);
    }

    @FunctionalInterface
    public interface BadTransfer {
        /**
         * Fired when a client joins the server without a transfer and
         * {@link Biscuit#shouldKickNonTransferredConnections(boolean)} has been set to true.
         */
        void onBad(ServerLoginNetworkHandler handler, MinecraftServer server, CI ci);
    }

    @FunctionalInterface
    public interface InvalidCookie {
        /**
         * Fired when a cookie sent from the client has an invalid signature (The cookie has likely been tampered with).
         */
        void onInvalid(Biscuit.RegisteredCookie registeredCookie, GameProfile profile);
    }

    @FunctionalInterface
    public interface PreTransfer {
        /**
         * Fired when before a client is transferred to another server.
         */
        void onTransfer(ServerTransferS2CPacket packet, GameProfile profile, CookieJar cookieJar, CI ci);
    }
}