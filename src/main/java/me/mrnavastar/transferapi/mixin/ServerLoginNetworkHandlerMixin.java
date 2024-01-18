package me.mrnavastar.transferapi.mixin;

import com.mojang.authlib.GameProfile;
import me.mrnavastar.transferapi.TransferAPI;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import net.minecraft.class_9088;
import net.minecraft.class_9091;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    private void requestCookies(GameProfile profile, CallbackInfo ci) {
        TransferAPI.getRegisteredCookies().forEach(id -> connection.send(new class_9088(id)));
    }

    /*@Inject(method = "onEnterConfiguration", at = @At("TAIL"))
    private void copyCookies(EnterConfigurationC2SPacket packet, CallbackInfo ci, @Local ServerConfigurationNetworkHandler networkHandler) {
        ((CookieStore) networkHandler).copyCookies(cookies);
    }*/

    @Inject(method = "method_55851", at = @At("HEAD"), cancellable = true)
    private void onCookieResponse(class_9091 arg, CallbackInfo ci) {
        System.out.println("COOKIE RESPONSE: " + Arrays.toString(arg.payload()));
        ((CookieStore) connection).getStore().put(arg.key(), arg.payload());
        ci.cancel();
    }

}