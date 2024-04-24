package me.mrnavastar.biscuit.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CookieSigner {

    public static final Mac DEFAULT_MAC;

    static {
        try {
            DEFAULT_MAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] computeSignature(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException, CloneNotSupportedException {
        Mac localMac = (Mac) mac.clone();
        localMac.init(new SecretKeySpec(secret, localMac.getAlgorithm()));
        return localMac.doFinal(cookie);
    }

    // Returns original cookie if secret has a length of zero
    // Returns empty if cookie too big fit.
    // Returns the signed cookie if all is well
    public static byte[] signCookie(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException, CloneNotSupportedException {
        if (cookie == null || secret == null) return new byte[0];

        int macLen = mac.getMacLength();
        int len = cookie.length + macLen;
        if (len > 5120) return new byte[0]; // Cookie is longer than the max size the client accepts
        if (secret.length == 0) return cookie;

        byte[] data = new byte[len];
        System.arraycopy(computeSignature(cookie, secret, mac), 0, data, 0, macLen);
        System.arraycopy(cookie, 0, data, macLen, cookie.length);
        return data;
    }

    // Returns original cookie if secret has a length of zero
    // Returns empty if cookie is invalid, otherwise returns cookie data (without the secret attached).
    public static byte[] verifyCookie(byte[] cookie, byte[] secret, Mac mac) throws InvalidKeyException, CloneNotSupportedException {
        if (cookie == null || secret == null) return new byte[0];
        if (secret.length == 0) return cookie;

        int macLen = mac.getMacLength();
        byte[] signature = new byte[macLen];
        byte[] data = new byte[cookie.length - macLen];
        System.arraycopy(cookie, 0, signature, 0, macLen);
        System.arraycopy(cookie, macLen, data, 0, data.length);

        if (Arrays.equals(signature, computeSignature(data, secret, mac))) return data;
        return new byte[0];
    }
}