package me.mrnavastar.transferapi.interfaces;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public interface TransferMeta {

    HashMap<Identifier, byte[]> fabric_getCookieStore();

    HashSet<Identifier> fabric_getRequestedCookies();

    void fabric_setTransferred();
}
