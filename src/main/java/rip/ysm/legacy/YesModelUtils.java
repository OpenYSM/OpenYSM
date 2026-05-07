package rip.ysm.legacy;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.zip.DataFormatException;

import static rip.ysm.util.HashUtils.md5;

public final class YesModelUtils {
    public static final int HEAD = 0x59_53_47_50;
    public static final int VERSION = 0x00_00_00_01;
    public static final int VERSION_II = 0x00_00_00_02;
    private static final String ENCRYPTION_METHOD = "AES";

    public static Map<String, byte[]> input(byte[] data) throws IOException {
        if (data.length < 24) return Collections.emptyMap();
        int head = ByteInteger.bytes2Int(data, 0);
        int version = ByteInteger.bytes2Int(data, 4);
        return inputInternal(data, head, version);
    }

    public static Map<String, byte[]> input(File ysmFile) throws IOException {
        String fileName = removeExtension(ysmFile.getName());
        if (!ResourceLocation.isValidResourceLocation(fileName)) {
            return Collections.emptyMap();
        }
        byte[] data = FileUtils.readFileToByteArray(ysmFile);
        int head = ByteInteger.bytes2Int(data, 0);
        int version = ByteInteger.bytes2Int(data, 4);
        return inputInternal(data, head, version);
    }

    private static Map<String, byte[]> inputInternal(byte[] data, int head, int version) throws IOException {
        if (head != HEAD) {
            return Collections.emptyMap();
        }
        if (version != VERSION && version != VERSION_II) {
            return Collections.emptyMap();
        }

        byte[] md5 = ByteArrays.copy(data, 8, 16);
        byte[] modelFilesData = ByteArrays.copy(data, 24, data.length - 24);
        if (!Arrays.equals(md5, md5(modelFilesData))) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> outputs = Maps.newHashMap();
        ByteArrayInputStream tmp = new ByteArrayInputStream(modelFilesData);
        while (tmp.available() > 0) {
            try {
                Pair<String, byte[]> ysmFileData;
                if (version == VERSION) {
                    ysmFileData = ysmToFile(tmp);
                } else {
                    ysmFileData = ysmToFileNew(tmp);
                }
                outputs.put(ysmFileData.left(), ysmFileData.right());
            } catch (GeneralSecurityException | DataFormatException | IOException e) {
                e.printStackTrace();
            }
        }
        return outputs;
    }

    @NotNull
    private static Pair<String, byte[]> ysmToFile(ByteArrayInputStream tmp) throws IOException, GeneralSecurityException, DataFormatException {
        String name = readString(tmp);
        int size = readInt(tmp);

        byte[] passwordBytes = new byte[16];
        byte[] ivBytes = new byte[16];
        tmp.read(passwordBytes);
        tmp.read(ivBytes);
        SecretKeySpec key = new SecretKeySpec(passwordBytes, ENCRYPTION_METHOD);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        byte[] fileData = new byte[size];
        tmp.read(fileData);

        ByteArrayOutputStream decryptData = AESUtil.decrypt(key, iv, fileData);
        byte[] rawData = DeflateUtil.decompressBytes(decryptData.toByteArray());

        return Pair.of(name, rawData);
    }

    @NotNull
    private static Pair<String, byte[]> ysmToFileNew(ByteArrayInputStream tmp) throws IOException, GeneralSecurityException, DataFormatException {
        String fileName = readBase64String(tmp);
        int fileSize = readInt(tmp);
        int cipherSecretKeySize = readInt(tmp);

        byte[] cipherSecretKey = new byte[cipherSecretKeySize];
        byte[] ivBytes = new byte[16];
        byte[] fileData = new byte[fileSize];
        tmp.read(cipherSecretKey);
        tmp.read(ivBytes);
        tmp.read(fileData);

        byte[] keyFromMd5 = getKeyFromMd5(fileData);
        SecretKey secretSecretKey = AESUtil.getKey(keyFromMd5);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        byte[] decryptSecretKey = AESUtil.decrypt(secretSecretKey, iv, cipherSecretKey).toByteArray();
        SecretKey key = AESUtil.getKey(decryptSecretKey);
        ByteArrayOutputStream decryptData = AESUtil.decrypt(key, iv, fileData);
        byte[] rawData = DeflateUtil.decompressBytes(decryptData.toByteArray());

        return Pair.of(fileName, rawData);
    }

    private static byte[] getKeyFromMd5(byte[] fileData) {
        byte[] md5 = md5(fileData);
        Random random = new Random(toLong(md5));
        byte[] keys = new byte[16];
        random.nextBytes(keys);
        return keys;
    }

    private static long toLong(byte[] bytes) {
        long value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 255);
        }
        return value;
    }

    private static String readString(ByteArrayInputStream stream) throws IOException {
        int size = readInt(stream);
        byte[] stringBytes = new byte[size];
        stream.read(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    private static String readBase64String(ByteArrayInputStream stream) throws IOException {
        int size = readInt(stream);
        byte[] stringBytes = new byte[size];
        stream.read(stringBytes);
        return new String(Base64.getDecoder().decode(stringBytes), StandardCharsets.UTF_8);
    }

    private static boolean readBoolean(ByteArrayInputStream stream) throws IOException {
        return readInt(stream) != 0;
    }

    @SuppressWarnings("all")
    private static int readInt(ByteArrayInputStream stream) throws IOException {
        byte[] sizeBytes = new byte[4];
        stream.read(sizeBytes);
        return ByteInteger.bytes2Int(sizeBytes, 0);
    }

    private static String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex != -1) {
            fileName = fileName.substring(0, lastIndex);
        }
        return fileName;
    }
}
