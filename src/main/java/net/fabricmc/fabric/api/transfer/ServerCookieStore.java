package net.fabricmc.fabric.api.transfer;

import net.minecraft.util.Identifier;

public interface ServerCookieStore {

    void setCookie(Identifier cookieId, byte[] cookie);

    byte[] getCookie(Identifier cookieId);
}
