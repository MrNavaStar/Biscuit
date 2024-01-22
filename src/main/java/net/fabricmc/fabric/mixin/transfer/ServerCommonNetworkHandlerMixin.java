package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.api.transfer.ServerCookieStore;
import net.fabricmc.fabric.api.transfer.ServerTransferable;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements ServerTransferable, ServerCookieStore {

    @Shadow @Final protected ClientConnection connection;

    @Override
    public void transferToServer(String host, int port) {
        ((ServerTransferable) connection).transferToServer(host, port);
    }

    @Override
    public boolean wasTransferred() {
        return ((ServerTransferable) connection).wasTransferred();
    }

    @Override
    public void setCookie(Identifier cookieId, byte[] cookie) {
        ((ServerCookieStore) connection).setCookie(cookieId, cookie);
    }

    @Override
    public byte[] getCookie(Identifier cookieId) {
        return ((ServerCookieStore) connection).getCookie(cookieId);
    }
}