package me.mrnavastar.transferapi;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerTransferEvents {

    /*public static final Event<BeforeServerTransfer> BEFORE_TRANSFER = EventFactory.createArrayBacked(BeforeServerTransfer.class, callbacks -> player -> {
        for (BeforeServerTransfer callback : callbacks) {
            if (!callback.beforeServerTransfer(player)) return false;
        }
        return true;
    });

    public static final Event<AfterServerTransfer> AFTER_TRANSFER = EventFactory.createArrayBacked(AfterServerTransfer.class, callbacks -> player -> {
        for (AfterServerTransfer callback : callbacks) {
            callback.afterServerTransfer(player);
        }
    });*/

    public static final Event<CookieResponse> COOKIE_RESPONSE = EventFactory.createArrayBacked(CookieResponse.class, callbacks -> (profile, cookieId, cookie) -> {
        for (CookieResponse callback : callbacks) {
            if (callback.onCookieResponse(profile, cookieId, cookie)) return false;
        }
        return true;
    });

    /*@FunctionalInterface
    public interface BeforeServerTransfer {
        boolean beforeServerTransfer(ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface AfterServerTransfer {
        void afterServerTransfer(ServerPlayerEntity player);
    }*/

    @FunctionalInterface
    public interface CookieResponse {
        boolean onCookieResponse(GameProfile profile, Identifier cookieId, byte[] cookie);
    }
}
