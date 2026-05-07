package rip.ysm.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private HashUtils() {
    }

    public static byte[] md5(byte[] data) {
        return digest("MD5", data);
    }

    public static String md5Hex(byte[] data) {
        return hex(md5(data));
    }

    public static String sha256Hex(byte[] data) {
        return hex(digest("SHA-256", data));
    }

    private static byte[] digest(String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing JDK digest algorithm: " + algorithm, e);
        }
    }

    private static String hex(byte[] data) {
        char[] out = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xFF;
            out[i * 2] = HEX[value >>> 4];
            out[i * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(out);
    }
}
