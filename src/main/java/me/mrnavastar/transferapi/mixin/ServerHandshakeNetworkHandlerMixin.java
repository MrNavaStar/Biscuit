package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.interfaces.TransferMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerHandshakeNetworkHandler;method_56048(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;Z)V", ordinal = 1))
    private void setWasTransferred(HandshakeC2SPacket packet, CallbackInfo ci) {
        ((TransferMeta) connection).fabric_setTransferred();
    }
}