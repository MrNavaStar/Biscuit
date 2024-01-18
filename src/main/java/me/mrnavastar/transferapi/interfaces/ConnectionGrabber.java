package me.mrnavastar.transferapi.interfaces;

import net.minecraft.network.ClientConnection;

public interface ConnectionGrabber {
    ClientConnection getConnection();
}
