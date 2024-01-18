package me.mrnavastar.transferapi.interfaces;

import net.minecraft.util.Identifier;
import java.util.HashMap;

public interface CookieStore {
    HashMap<Identifier, byte[]> fabric_getStore();
}
