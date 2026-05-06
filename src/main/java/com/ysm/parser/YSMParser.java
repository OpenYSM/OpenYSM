package com.ysm.parser;

/**
 * JNI wrapper for YSMParser native library.
 *
 * <p>Loads {@code YSMParserJNI} (built with {@code -DYSM_TARGET_JNI=ON})
 * and exposes the C++ YSM parser to Java.
 */
public class YSMParser {

    static {
        System.loadLibrary("YSMParserJNI");
    }

    /**
     * Parse a .ysm file and extract all resources to the output directory.
     *
     * @param ysmFilePath absolute or relative path to the .ysm file
     * @param outputDir   directory where the parsed project will be written
     * @return true on success
     * @throws IllegalArgumentException if either argument is null
     * @throws RuntimeException         if native parsing fails
     */
    public static native boolean parse(String ysmFilePath, String outputDir);

    /**
     * Parse .ysm data from a byte array.
     *
     * @param ysmData  raw bytes of the .ysm file
     * @param outputDir directory where the parsed project will be written
     * @return true on success
     * @throws IllegalArgumentException if data is null/empty or outputDir is null
     * @throws RuntimeException         if native parsing fails
     */
    public static native boolean parseBytes(byte[] ysmData, String outputDir);

    /**
     * Read the YSGP version of a .ysm file without full parsing.
     *
     * @param ysmFilePath path to the .ysm file
     * @return the YSGP version number, or -1 on error
     * @throws IllegalArgumentException if the path is null
     * @throws RuntimeException         if reading fails
     */
    public static native int getVersion(String ysmFilePath);
}
