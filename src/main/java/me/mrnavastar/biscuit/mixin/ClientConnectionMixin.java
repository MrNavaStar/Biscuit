package me.mrnavastar.biscuit.mixin;

import me.mrnavastar.biscuit.api.Biscuit;
import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.api.CookieJar;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements CookieJar, InternalStuff {

    @Shadow public abstract void send(Packet<?> packet);

    @Unique
    protected final Map<Identifier, CompletableFuture<byte[]>> pendingCookieRequests = new ConcurrentHashMap<>();

    @Override
    public void biscuit$send(Packet<?> packet) {
        send(packet);
    }

    public boolean biscuit$onCookie(Identifier cookieId, byte[] cookie) {
        CompletableFuture<byte[]> future = pendingCookieRequests.remove(cookieId);
        if (future == null) return true;
        future.complete(cookie);
        return false;
    }

    public CompletableFuture<byte[]> biscuit$getRawCookie(Identifier cookieId) {
        CompletableFuture<byte[]> future = pendingCookieRequests.get(cookieId);
        if (future != null) return future;

        future = new CompletableFuture<>();
        pendingCookieRequests.put(cookieId, future);
        send(new CookieRequestS2CPacket(cookieId));
        return future;
    }

    @Override
    public void setCookie(Object cookie) {
       Biscuit.setCookie((ClientConnection) (Object) this, cookie);
    }

    @Override
    public <T> CompletableFuture<T> getCookie(Class<T> cookieType) {
        return Biscuit.getCookie((ClientConnection) (Object) this, cookieType);
    }
}
