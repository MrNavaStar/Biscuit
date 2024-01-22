package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.CookieUtils;
import me.mrnavastar.transferapi.api.ServerCookieStore;
import me.mrnavastar.transferapi.api.ServerTransferable;
import me.mrnavastar.transferapi.interfaces.ClientConnectionMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.HashSet;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements ClientConnectionMeta, ServerTransferable, ServerCookieStore {

    @Shadow public abstract void send(Packet<?> packet);

    @Unique private HashMap<Identifier, Pair<byte[], Mac>> registeredCookies;
    @Unique private final HashMap<Identifier, byte[]> cookieStore = new HashMap<>();
    @Unique private final HashSet<Identifier> requestedCookies = new HashSet<>();
    @Unique private boolean wasTransferred = false;

    @Override
    public void fabric_setCookieRegistry(HashMap<Identifier, Pair<byte[], Mac>> registeredCookies) {
        this.registeredCookies = registeredCookies;
    }

    @Override
    public HashMap<Identifier, Pair<byte[], Mac>> fabric_getCookieRegistry() {
        return registeredCookies;
    }

    @Override
    public HashMap<Identifier, byte[]> fabric_getCookieStore() {
        return cookieStore;
    }

    @Override
    public HashSet<Identifier> fabric_getRequestedCookies() {
        return requestedCookies;
    }

    @Override
    public void fabric_setTransferred() {
        this.wasTransferred = true;
    }

    @Override
    public void transferToServer(String host, int port) {
        //if (!ServerTransferEvents.BEFORE_TRANSFER.invoker().beforeServerTransfer((ClientConnection) (Object) this)) return;
        send(new ServerTransferS2CPacket(host, port));
        //ServerTransferEvents.AFTER_TRANSFER.invoker().afterServerTransfer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public boolean wasTransferred() {
        return wasTransferred;
    }

    @Override
    public void setCookie(Identifier cookieId, byte[] cookie) {
        try {
            Pair<byte[], Mac> signingData = registeredCookies.get(cookieId);
            if (signingData == null) return;

            byte[] signedCookie = CookieUtils.signCookie(cookie, signingData.getLeft(), signingData.getRight());
            if (signedCookie == null) return;

            send(new StoreCookieS2CPacket(cookieId, signedCookie));
            cookieStore.put(cookieId, cookie);
        } catch (InvalidKeyException | CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getCookie(Identifier cookieId) {
        requestedCookies.add(cookieId);
        return cookieStore.getOrDefault(cookieId, new byte[]{});
    }
}