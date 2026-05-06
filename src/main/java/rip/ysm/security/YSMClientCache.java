package rip.ysm.security;

import rip.ysm.algorithms.CityHash;
import rip.ysm.algorithms.MT19937;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YSMClientCache {

    public static String generateCacheFileName(long hash1, long hash2, byte[] rtKey) {
        if (rtKey == null || rtKey.length != 56) return null;
        int seed = 114514; // todo: 换成真随机数

        MT19937 mt = new MT19937(Integer.toUnsignedLong(seed));
        long m1 = hash1 ^ mt.extract_number();
        long m2 = hash2 ^ mt.extract_number();

        ByteBuffer buf = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(seed);
        buf.putLong(m1);
        buf.putLong(m2);
        byte[] bufArray = buf.array();

        for (int i = 0; i < bufArray.length; i++) {
            bufArray[i] ^= rtKey[i % rtKey.length];
        }

        StringBuilder sb = new StringBuilder(40);
        for (byte b : bufArray) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    public static boolean verifyFileContent(File cacheFile, long hash1, long hash2) {
        if (cacheFile == null || !cacheFile.exists() || cacheFile.length() <= 8) {
            return false;
        }

        try {
            byte[] fileData = Files.readAllBytes(cacheFile.toPath());
            int payloadLen = fileData.length - 8;

            long realHash = ByteBuffer.wrap(fileData, payloadLen, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();

            byte[] payload = Arrays.copyOfRange(fileData, 0, payloadLen);
            CityHash ch = new CityHash();
            long calculatedHash = ch.hash64WithSeed(payload, YsmCrypt.SEED_CACHE_VERIFICATION);

            long verif = calculatedHash ^ hash1 ^ hash2;
            return verif == realHash;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UUID getModelUUIDFromFileName(String fileName, byte[] rtKey) {
        if (fileName == null || fileName.length() != 40 || rtKey == null || rtKey.length != 56) {
            return null;
        }

        try {
            byte[] buf = new byte[20];
            for (int i = 0; i < 20; i++) {
                int high = Character.digit(fileName.charAt(i * 2), 16);
                int low = Character.digit(fileName.charAt(i * 2 + 1), 16);
                if (high == -1 || low == -1) return null;
                buf[i] = (byte) ((high << 4) | low);
            }

            for (int i = 0; i < buf.length; i++) {
                buf[i] ^= rtKey[i % rtKey.length];
            }

            ByteBuffer byteBuf = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
            int seed = byteBuf.getInt();
            long m1 = byteBuf.getLong();
            long m2 = byteBuf.getLong();

            MT19937 mt = new MT19937(Integer.toUnsignedLong(seed));
            long hash1 = m1 ^ mt.extract_number();
            long hash2 = m2 ^ mt.extract_number();

            return new UUID(hash1, hash2);
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<UUID, File> buildCacheIndex(File cacheDir, byte[] rtKey) {
        Map<UUID, File> cacheIndex = new HashMap<>();

        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            return cacheIndex;
        }

        File[] files = cacheDir.listFiles();
        if (files == null) return cacheIndex;

        System.out.println("scanning cache directory");
        for (File file : files) {
            if (file.isFile()) {
                UUID realModelUuid = getModelUUIDFromFileName(file.getName(), rtKey);
                if (realModelUuid != null) {
                    cacheIndex.put(realModelUuid, file);
                }
            }
        }
        System.out.println("indexed " + cacheIndex.size() + " cached models.");
        return cacheIndex;
    }
}