package me.mrnavastar.biscuit.mixin;

import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.api.BiscuitEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements InternalStuff {

    @Shadow public abstract void send(Packet<?> packet);

    @Shadow @Nullable private volatile PacketListener packetListener;
    @Unique
    protected final Map<Identifier, CompletableFuture<byte[]>> pendingCookieRequests = new ConcurrentHashMap<>();

    @Inject(method = "sendInternal", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (!(packet instanceof ServerTransferS2CPacket p)) return;

        BiscuitEvents.CI ci1 = new BiscuitEvents.CI();
        BiscuitEvents.PRE_TRANSFER.invoker().onTransfer(p, ((InternalStuff) packetListener).biscuit$getUser(), ci1);
        if (ci1.isCanceled()) ci.cancel();
    }

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
}