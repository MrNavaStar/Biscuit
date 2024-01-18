package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.interfaces.TransferMeta;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements TransferMeta {

    @Unique private final HashMap<Identifier, byte[]> cookies = new HashMap<>();
    @Unique private final AtomicBoolean wasTransferred = new AtomicBoolean();

    @Override
    public HashMap<Identifier, byte[]> fabric_getCookieStore() {
        return cookies;
    }

    @Override
    public AtomicBoolean fabric_wasTransferred() {
        return wasTransferred;
    }
}