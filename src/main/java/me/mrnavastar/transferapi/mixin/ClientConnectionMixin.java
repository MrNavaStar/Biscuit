package me.mrnavastar.transferapi.mixin;

import me.mrnavastar.transferapi.interfaces.CookieStore;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements CookieStore {

    @Unique
    private final HashMap<Identifier, byte[]> cookies = new HashMap<>();

    @Override
    public HashMap<Identifier, byte[]> getStore() {
        return cookies;
    }
}
