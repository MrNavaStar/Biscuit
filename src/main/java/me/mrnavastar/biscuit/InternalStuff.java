package me.mrnavastar.biscuit;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public interface InternalStuff {

    void biscuit$send(Packet<?> packet);

    default boolean biscuit$onCookie(Identifier cookieId, byte[] cookie) {
        return true;
    };

     CompletableFuture<byte[]> biscuit$getRawCookie(Identifier cookieId);

     default GameProfile biscuit$getUser() {
         return null;
     }
}