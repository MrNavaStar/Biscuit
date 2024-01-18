package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.interfaces.ConnectionGrabber;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import me.mrnavastar.transferapi.api.ServerTransferable;
import net.minecraft.class_9088;
import net.minecraft.class_9150;
import net.minecraft.class_9151;
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
    public void transferToServer(URL address) {
        networkHandler.sendPacket(new class_9151(address.getHost(), address.getPort()));
    }

    @Override
    public void transferToServer(String host, int port) {
        networkHandler.sendPacket(new class_9151(host, port));
    }

    @Override
    public void transferToServer(InetSocketAddress address) {
        networkHandler.sendPacket(new class_9151(address.getHostString(), address.getPort()));
    }

    @Override
    public void setCookieData(Identifier identifier, byte[] payload) {
        networkHandler.sendPacket(new class_9150(identifier, payload)); // Set data
        networkHandler.sendPacket(new class_9088(identifier)); // Request data
    }

    @Override
    public byte[] getCookieData(Identifier identifier) {
        return ((CookieStore)((ConnectionGrabber) networkHandler).getConnection()).getStore().getOrDefault(identifier, new byte[]{});
    }
}