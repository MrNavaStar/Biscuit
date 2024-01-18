package me.mrnavastar.transferapi.api;

import net.minecraft.util.Identifier;

import java.net.InetSocketAddress;
import java.net.URL;

public interface ServerTransferable {

    void transferToServer(String host, int port);
    void transferToServer(URL address);
    void transferToServer(InetSocketAddress address);

    void setCookieData(Identifier identifier, byte[] payload);

    byte[] getCookieData(Identifier identifier);
}