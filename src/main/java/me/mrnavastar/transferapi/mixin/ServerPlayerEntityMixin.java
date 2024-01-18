package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.ServerTransferEvents;
import me.mrnavastar.transferapi.interfaces.ConnectionGrabber;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import me.mrnavastar.transferapi.api.ServerTransferable;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.URL;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerTransferable {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Override
    public void transferToServer(String host, int port) {
        if (!ServerTransferEvents.BEFORE_TRANSFER.invoker().beforeServerTransfer((ServerPlayerEntity) (Object) this)) return;

        networkHandler.sendPacket(new ServerTransferS2CPacket(host, port));
        ServerTransferEvents.AFTER_TRANSFER.invoker().afterServerTransfer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void transferToServer(InetSocketAddress address) {
        transferToServer(address.getHostString(), address.getPort());
    }

    @Override
    public void transferToServer(URL address) {
        transferToServer(address.getHost(), address.getPort());
    }

    @Override
    public void setCookieData(Identifier identifier, byte[] payload) {
        networkHandler.sendPacket(new StoreCookieS2CPacket(identifier, payload));
        networkHandler.sendPacket(new CookieRequestS2CPacket(identifier));
    }

    @Override
    public byte[] getCookieData(Identifier identifier) {
        return ((CookieStore)((ConnectionGrabber) networkHandler).fabric_getConnection()).fabric_getStore().getOrDefault(identifier, new byte[]{});
    }
}