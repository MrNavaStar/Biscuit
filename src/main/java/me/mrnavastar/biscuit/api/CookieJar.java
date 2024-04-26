package me.mrnavastar.biscuit.api;

import lombok.SneakyThrows;
import me.mrnavastar.biscuit.InternalStuff;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;

import java.util.concurrent.CompletableFuture;

public interface CookieJar {

    /**
     * Transfer the client to another server.
     * @param address The address of the server to transfer to.
     * @param port The port of the server to transfer to.
     */
    default void transfer(String address, int port) {
        ((InternalStuff) this).biscuit$send(new ServerTransferS2CPacket(address, port));
    }

    /**
     * Checks if this client was transferred from another server.
     * <p></p><b>NOTE: The client is able to lie about being transferred, so don't rely on it for mission critical
     * things. Also see {@link me.mrnavastar.biscuit.api.Biscuit.RegisteredCookie#kickIfMissing(boolean)}</b>
     * @return If the client was transferred.
     */
    boolean wasTransferred();

    /**
     * Store a cookie on the client.
     * @param cookie the object to store on the client.
     */
    @SneakyThrows
    default void setCookie(Object cookie) {
        if (this instanceof ServerLoginNetworkHandler) throw new Exception("Cant set cookie during login network phase. This is a client side limitation.");
        Biscuit.setCookie(this, cookie);
    };

    /**
     * Retrieves the cookie stored on the client.
     * @param cookieType The class type of the object to be retrieved.
     * @return The object that was stored on the client.
     */
    default <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return Biscuit.getCookie(this, cookieType);
    }
}