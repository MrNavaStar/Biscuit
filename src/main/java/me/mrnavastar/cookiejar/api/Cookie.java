package me.mrnavastar.cookiejar.api;

import io.netty.buffer.ByteBuf;

public interface Cookie {

    /**
     * This function is called to encode the cookie into a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to write cookie data to.
     */
    void encode(ByteBuf buf) throws Exception;

    /**
     * This function is called to decode the cookie from a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read cookie data from.
     */
    void decode(ByteBuf buf) throws Exception;
}