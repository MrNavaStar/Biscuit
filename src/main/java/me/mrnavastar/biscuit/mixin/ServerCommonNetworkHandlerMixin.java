package me.mrnavastar.biscuit.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import me.mrnavastar.biscuit.InternalStuff;
import me.mrnavastar.biscuit.api.Biscuit;
import me.mrnavastar.biscuit.api.CookieJar;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin implements CookieJar, InternalStuff {

    @Shadow @Final protected ClientConnection connection;
    @Shadow @Final private boolean transferred;

    @Unique GameProfile profile;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData, CallbackInfo ci) {
        profile = clientData.gameProfile();
    }

    @WrapWithCondition(method = "onCookieResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private boolean cancelDisconnect(ServerCommonNetworkHandler instance, Text reason, @Local(argsOnly = true) CookieResponseC2SPacket packet) {
        return ((InternalStuff) connection).biscuit$onCookie(packet.key(), packet.payload());
    }

    @Override
    public void biscuit$send(Packet<?> packet) {
        connection.send(packet);
    }

    @Override
    public CompletableFuture<byte[]> biscuit$getRawCookie(Identifier cookieId) {
        return ((InternalStuff) connection).biscuit$getRawCookie(cookieId);
    }

    @Override
    public GameProfile biscuit$getUser() {
        return profile;
    }

    @Override
    public boolean wasTransferred() {
        return transferred;
    }
}