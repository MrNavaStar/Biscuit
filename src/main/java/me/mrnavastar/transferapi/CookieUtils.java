package me.mrnavastar.transferapi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CookieUtils {

    public static final Mac DEFAULT_MAC;

    static {
        try {
            DEFAULT_MAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] computeSignature(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException {
        mac.init(new SecretKeySpec(secret, mac.getAlgorithm()));
        return mac.doFinal(cookie);
    }

    // Returns original cookie if secret has a length of zero
    // Returns null if cookie too big fit.
    // Returns the signed cookie if all is well
    public static byte[] signCookie(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException {
        if (cookie == null || secret == null) return null;
        int len = cookie.length + mac.getMacLength();
        if (len > 5120) return null; // Cookie is longer than the max size the client accepts
        if (secret.length == 0) return cookie;

        ByteBuffer buffer = ByteBuffer.wrap(new byte[len]);
        buffer.put(computeSignature(cookie, secret, mac));
        buffer.put(cookie);

        return buffer.array();
    }

    // Returns original cookie if secret has a length of zero
    // Returns null if cookie is invalid, otherwise returns cookie data (without the secret attached).
    public static byte[] verifyCookie(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException {
        if (cookie == null || secret == null) return null;
        if (secret.length == 0) return cookie;

        ByteBuffer buffer = ByteBuffer.wrap(cookie);
        int macLen = mac.getMacLength();

        byte[] signature = new byte[macLen];
        byte[] data = new byte[cookie.length - macLen];
        buffer.get(0, signature, 0, macLen);
        buffer.get(macLen, data, 0, data.length);

        if (Arrays.equals(signature, computeSignature(data, secret, mac))) return data;
        return null;
    }
}