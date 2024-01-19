package me.mrnavastar.transferapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.mrnavastar.transferapi.CookieRegistry;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {

    @ModifyReturnValue(method = "acceptsStatusQuery", at = @At("RETURN"))
    private boolean disableIfServerIsTransferOnly(boolean original) {
        return original && !CookieRegistry.isTransferOnly();
    }
}