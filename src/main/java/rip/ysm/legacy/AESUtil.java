package rip.ysm.legacy;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public final class AESUtil {
    public static ByteArrayOutputStream encrypt(SecretKey key, AlgorithmParameterSpec iv, byte[] input) throws IOException, GeneralSecurityException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] buffer = new byte[64];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) {
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            outputStream.write(outputBytes);
        }

        return outputStream;
    }

    public static ByteArrayOutputStream decrypt(SecretKey key, AlgorithmParameterSpec iv, byte[] input) throws IOException, GeneralSecurityException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] buffer = new byte[64];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) {
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            outputStream.write(outputBytes);
        }

        return outputStream;
    }

    public static SecretKey generateKey() {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        generator.init(128);
        return generator.generateKey();
    }

    public static SecretKey getKey(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
