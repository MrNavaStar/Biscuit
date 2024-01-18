package me.mrnavastar.transferapi.interfaces;

import net.minecraft.util.Identifier;
import java.util.HashMap;

public interface CookieStore {
    HashMap<Identifier, byte[]> getStore();
    //void copyCookies(HashMap<Identifier, byte[]> cookies);
}
