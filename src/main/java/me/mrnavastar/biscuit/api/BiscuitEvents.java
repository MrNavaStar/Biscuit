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

    public static final Event<ValidationFail> MISSING_COOKIE = EventFactory.createArrayBacked(ValidationFail.class, callbacks -> (registeredCookie, handler, server, ci) -> {
        for (ValidationFail callback : callbacks) {
            if (ci.isCanceled()) break;
            callback.onMissing(registeredCookie, handler, server, ci);
        }
    });

    public static final Event<BadTransfer> BAD_TRANSFER = EventFactory.createArrayBacked(BadTransfer.class, callbacks -> (handler, server, ci) -> {
        for (BadTransfer callback : callbacks) {
            callback.onBad(handler, server, ci);
        }
    });

    public static final Event<InvalidCookie> INVALID_COOKIE = EventFactory.createArrayBacked(InvalidCookie.class, callbacks -> (registeredCookie, profile) -> {
        for (InvalidCookie callback : callbacks) {
            callback.onInvalid(registeredCookie, profile);
        }
    });

    public static final Event<PreTransfer> PRE_TRANSFER = EventFactory.createArrayBacked(PreTransfer.class, callbacks -> (packet, profile, ci) -> {
        for (PreTransfer callback : callbacks) {
            callback.onTransfer(packet, profile, ci);
        }
    });

    @FunctionalInterface
    public interface ValidationFail {
        void onMissing(Biscuit.RegisteredCookie registeredCookie, ServerLoginNetworkHandler handler, MinecraftServer server, CI ci);
    }

    @FunctionalInterface
    public interface BadTransfer {
        void onBad(ServerLoginNetworkHandler handler, MinecraftServer server, CI ci);
    }

    @FunctionalInterface
    public interface InvalidCookie {
        void onInvalid(Biscuit.RegisteredCookie registeredCookie, GameProfile profile);
    }

    @FunctionalInterface
    public interface PreTransfer {
        void onTransfer(ServerTransferS2CPacket packet, GameProfile profile, CI ci);
    }
}