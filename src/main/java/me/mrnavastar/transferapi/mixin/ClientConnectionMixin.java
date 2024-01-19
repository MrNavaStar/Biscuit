package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.CookieRegistry;
import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.api.ServerTransferable;
import me.mrnavastar.transferapi.interfaces.TransferMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements TransferMeta, ServerTransferable {

    @Shadow public abstract void send(Packet<?> packet);
    @Unique private final HashMap<Identifier, byte[]> cookies = new HashMap<>();
    @Unique private boolean wasTransferred = false;

    @Override
    public HashMap<Identifier, byte[]> fabric_getCookieStore() {
        return cookies;
    }

    @Override
    public void fabric_setTransferred() {
        this.wasTransferred = true;
    }

    @Override
    public void transferToServer(String host, int port) {
        if (!ServerTransferEvents.BEFORE_TRANSFER.invoker().beforeServerTransfer((ServerPlayerEntity) (Object) this)) return;
        send(new ServerTransferS2CPacket(host, port));
        ServerTransferEvents.AFTER_TRANSFER.invoker().afterServerTransfer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public boolean wasTransferred() {
        return wasTransferred;
    }

    @Override
    public void setCookie(Identifier cookieId, byte[] cookie) {
        cookie = CookieRegistry.signCookie(cookieId, cookie);
        if (cookie == null) return;

        send(new StoreCookieS2CPacket(cookieId, cookie));
        cookies.put(cookieId, cookie);
    }

    @Override
    public byte[] getCookie(Identifier cookieId) {
        return cookies.getOrDefault(cookieId, new byte[]{});
    }
}