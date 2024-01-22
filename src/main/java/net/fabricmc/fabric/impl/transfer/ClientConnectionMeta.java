package net.fabricmc.fabric.impl.transfer;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.crypto.Mac;
import java.util.HashMap;
import java.util.HashSet;

public interface ClientConnectionMeta {

    void fabric_setCookieRegistry(HashMap<Identifier, Pair<byte[], Mac>> registeredCookies);

    HashMap<Identifier, Pair<byte[], Mac>> fabric_getCookieRegistry();

    HashMap<Identifier, byte[]> fabric_getCookieStore();

    HashSet<Identifier> fabric_getRequestedCookies();

    void fabric_setTransferred();
}
