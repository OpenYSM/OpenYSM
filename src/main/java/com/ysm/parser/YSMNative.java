package com.ysm.parser;

/**
 * JNI wrapper for low-level native algorithms used by YSM.
 *
 * <p>Exposes CityHash, Zstd, XChaCha20, ModifiedChaCha, and MT19937 primitives
 * directly to Java. All methods are static and thread-safe.
 */
public class YSMNative {
    // ── CityHash ──────────────────────────────────────────────────────────

    public static native long cityHash64(byte[] data);
    public static native long cityHash64WithSeed(byte[] data, long seed);
    public static native long[] cityHash128(byte[] data);
    public static native long[] cityHash128WithSeed(byte[] data, long seedLow, long seedHigh);

    // ── Zstd ──────────────────────────────────────────────────────────────

    public static native byte[] zstdDecompress(byte[] data);
    public static native byte[] zstdCompress(byte[] data, int level);

    // ── XChaCha20 ─────────────────────────────────────────────────────────

    /**
     * @param key   32-byte key
     * @param iv    24-byte nonce
     * @param rounds number of rounds (10, 20, or 30)
     */
    public static native byte[] xchacha20Encrypt(byte[] data, byte[] key, byte[] iv, int rounds);

    /**
     * Decryption is the same operation as encryption for XChaCha20.
     */
    public static native byte[] xchacha20Decrypt(byte[] data, byte[] key, byte[] iv, int rounds);

    /**
     * YSM-specific modified ChaCha decryptor used by V3 resources.
     *
     * @param key   32-byte key
     * @param iv    24-byte nonce
     * @param seed  CityHash seed controlling block updates
     */
    public static native byte[] modifiedChaChaDecrypt(byte[] data, byte[] key, byte[] iv, long seed);

    // ── MT19937 (stateful) ────────────────────────────────────────────────

    /**
     * Create a new MT19937-64 RNG instance.
     * @return opaque handle for subsequent calls
     */
    public static native long mt19937Create(long seed);

    /** Return the next 64-bit random value from the generator. */
    public static native long mt19937Next(long handle);

    /** Fill and return {@code count} random bytes from the generator. */
    public static native byte[] mt19937GenerateBytes(long handle, int count);

    /** Destroy the generator and release native resources. */
    public static native void mt19937Destroy(long handle);

    /**
     * 解压 YSM 魔改的 ZSTD 数据。
     * 底层会自动执行 wash (洗白) 操作，然后进行标准 ZSTD 解压。
     *
     * @param data 压缩且被混淆过的 byte 数组
     * @return 解压后的原始 byte 数组
     * @throws RuntimeException 如果底层解码失败或内存分配失败
     * @throws IllegalArgumentException 如果传入的数据为 null
     */
    public static native byte[] ysmZstdDecompress(byte[] data);

    /**
     * 将数据进行标准 ZSTD 压缩，并混淆为 YSM 魔改格式。
     * 底层会先进行标准 ZSTD 压缩，然后自动执行 obfuscate (弄脏) 操作。
     *
     * @param data 需要压缩的原始 byte 数组
     * @param level ZSTD 压缩等级 (通常推荐 3，最大通常支持到 22)
     * @return 压缩且混淆后的 byte 数组
     * @throws RuntimeException 如果底层压缩失败
     * @throws IllegalArgumentException 如果传入的数据为 null
     */
    public static native byte[] ysmZstdCompress(byte[] data, int level);
}
