package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.api.ServerCookieStore;
import me.mrnavastar.transferapi.api.ServerTransferable;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerTransferable, ServerCookieStore {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Override
    public void transferToServer(String host, int port) {
        ((ServerTransferable) networkHandler).transferToServer(host, port);
    }

    @Override
    public boolean wasTransferred() {
        return ((ServerTransferable) networkHandler).wasTransferred();
    }

    @Override
    public void setCookie(Identifier cookieId, byte[] cookie) {
        ((ServerCookieStore) networkHandler).setCookie(cookieId, cookie);
    }

    @Override
    public byte[] getCookie(Identifier cookieId) {
        return ((ServerCookieStore) networkHandler).getCookie(cookieId);
    }
}