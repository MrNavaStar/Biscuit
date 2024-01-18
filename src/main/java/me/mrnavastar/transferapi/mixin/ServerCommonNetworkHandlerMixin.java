package me.mrnavastar.transferapi.mixin;

import lombok.Getter;
import me.mrnavastar.transferapi.interfaces.ConnectionGrabber;
import me.mrnavastar.transferapi.interfaces.CookieStore;
import net.minecraft.class_9091;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Getter
@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements CookieStore, ConnectionGrabber {

    @Shadow @Final protected ClientConnection connection;

    @Inject(method = "method_55851", at = @At("HEAD"), cancellable = true)
    private void onCookieResponse(class_9091 arg, CallbackInfo ci) {
        System.out.println("COOKIE RESPONSE: " + Arrays.toString(arg.payload()));
        ((CookieStore) connection).getStore().put(arg.key(), arg.payload());
        ci.cancel();
    }
}