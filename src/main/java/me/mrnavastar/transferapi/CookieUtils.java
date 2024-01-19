package me.mrnavastar.transferapi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CookieUtils {

    private static byte[] computeSignature(byte[] cookie, byte[] secret, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret, algorithm));
        return mac.doFinal(cookie);
    }

    // Returns null if cookie too big fit.
    // Returns original cookie if secret has a length of zero
    // Returns the signed cookie if all is well
    public static byte[] signCookie(byte[] cookie, byte[] secret, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        int len = cookie.length + 64;
        if (len > 5120) return null; // Cookie is longer than the max size the client accepts
        if (secret.length == 0) return cookie;

        ByteBuffer buffer = ByteBuffer.wrap(new byte[len]);
        buffer.put(computeSignature(cookie, secret, algorithm));
        buffer.put(cookie);
        return buffer.array();
    }

    // Returns null if cookie is invalid, otherwise returns cookie data (without the secret attached).
    public static byte[] verifyCookie(byte[] cookie , byte[] secret, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        ByteBuffer buffer = ByteBuffer.wrap(cookie);
        byte[] signature = new byte[64];
        byte[] data = new byte[cookie.length - 64];
        buffer.get(0, signature, 0, 64);
        buffer.get(63, data, 0, data.length);

        if (signature != computeSignature(data, secret, algorithm)) return null; // Replace this with real error stuff
        return data;
    }
}