package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.mrnavastar.transferapi.TransferAPI;
import me.mrnavastar.transferapi.interfaces.TransferMeta;
import net.minecraft.class_9099;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {

    @Shadow @Final private ClientConnection connection;

    @WrapWithCondition(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerHandshakeNetworkHandler;method_56048(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;Z)V", ordinal = 0))
    private boolean onlyAcceptTransfer(ServerHandshakeNetworkHandler instance, HandshakeC2SPacket handshakeC2SPacket, boolean bl) {
        if (TransferAPI.isTransferOnly()) {
            connection.method_56329(class_9099.field_48248);
            Text text = Text.of("This server only accepts clients that have been transferred from another server!");
            connection.send(new LoginDisconnectS2CPacket(text));
            connection.disconnect(text);
        }
        return !TransferAPI.isTransferOnly();
    }

    @Inject(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerHandshakeNetworkHandler;method_56048(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;Z)V", ordinal = 1))
    private void setWasTransferred(HandshakeC2SPacket packet, CallbackInfo ci) {
        ((TransferMeta) connection).fabric_wasTransferred().set(true);
    }
}