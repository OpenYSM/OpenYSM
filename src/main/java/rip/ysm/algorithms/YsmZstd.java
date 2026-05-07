package rip.ysm.algorithms;

import com.elfmcys.yesstevemodel.NativeLibLoader;
import com.ysm.parser.YSMNative;
import rip.ysm.zstd.ZstdUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class YsmZstd {
    private static volatile boolean nativeYsmZstdAvailable = true;

    public static byte[] decompress(byte[] rawData) throws IOException {
        if (NativeLibLoader.isLoaded() && nativeYsmZstdAvailable) {
            try {
                return YSMNative.ysmZstdDecompress(rawData);
            } catch (UnsatisfiedLinkError e) {
                nativeYsmZstdAvailable = false;
            }
        }

        byte[] data = YsmZstd.wash(rawData);
        //FileUtils.writeByteArrayToFile(new File("test.bin"),data);
        return ZstdUtil.decompress(data);
    }

    public static byte[] compress(byte[] rawData) {
        if (NativeLibLoader.isLoaded() && nativeYsmZstdAvailable) {
            try {
                return YSMNative.ysmZstdCompress(rawData, 3);
            } catch (UnsatisfiedLinkError e) {
                nativeYsmZstdAvailable = false;
            }
        }
        byte[] zstdData = ZstdUtil.compress(rawData,3);
        return YsmZstd.obfuscate(zstdData);
    }

    private static byte[] wash(byte[] data) {
        if (data == null || data.length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int magic = buffer.getInt(0);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD Magic Number. May be skippable frame or unknown.");
        }

        byte fhd = data[4];
        data[4] = (byte) (fhd & 0xFB);

        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int offset = 4 + frameHeaderSize;

        while (offset + 3 <= data.length) {
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset + 1] & 0xFF;
            int b2 = data[offset + 2] & 0xFF;
            int cBlockHeader = b0 | (b1 << 8) | (b2 << 16);
            int lastBlock = (b0 >> 7) & 1;
            int blockTypeYSM = (b0 >> 5) & 3;

            int rawSize = ((b0 & 0x1F) << 16) | b1 | (b2 << 8);
            int cSize = rawSize ^ 0xD4E9;
            int blockTypeStd = switch (blockTypeYSM) {
                case 0 -> 2;
                case 1 -> 1;
                case 2 -> 3;
                case 3 -> 0;
                default -> throw new IllegalStateException("Unknown block type");
            };

            int stdHeader = lastBlock | (blockTypeStd << 1) | (cSize << 3);

            data[offset] = (byte) (stdHeader & 0xFF);
            data[offset + 1] = (byte) ((stdHeader >> 8) & 0xFF);
            data[offset + 2] = (byte) ((stdHeader >> 16) & 0xFF);

            int blockDataSize = (blockTypeStd == 1) ? 1 : cSize;
            offset += 3 + blockDataSize;

            if (lastBlock == 1) {
                break;
            }
        }
        return data;
    }

    private static byte[] obfuscate(byte[] data) {
        if (data == null || data.length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int magic = buffer.getInt(0);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD frame.");
        }

        byte fhd = data[4];
        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int offset = 4 + frameHeaderSize;

        while (offset + 3 <= data.length) {
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset + 1] & 0xFF;
            int b2 = data[offset + 2] & 0xFF;
            int cBlockHeader = b0 | (b1 << 8) | (b2 << 16);

            int lastBlock = cBlockHeader & 1;
            int blockTypeStd = (cBlockHeader >> 1) & 3;
            int cSize = cBlockHeader >> 3;

            int blockDataSize = (blockTypeStd == 1) ? 1 : cSize;

            int blockTypeYSM = switch (blockTypeStd) {
                case 0 -> 3;
                case 1 -> 1;
                case 2 -> 0;
                case 3 -> 2;
                default -> throw new IllegalStateException("Unknown block type");
            };

            int rawSize = cSize ^ 0xD4E9;
            int ysmB0 = (lastBlock << 7) | (blockTypeYSM << 5) | ((rawSize >> 16) & 0x1F);
            int ysmB1 = rawSize & 0xFF;
            int ysmB2 = (rawSize >> 8) & 0xFF;

            data[offset] = (byte) ysmB0;
            data[offset + 1] = (byte) ysmB1;
            data[offset + 2] = (byte) ysmB2;

            offset += 3 + blockDataSize;

            if (lastBlock == 1) {
                break;
            }
        }

        return data;
    }

    private static int calculateFrameHeaderSize(byte fhd) {
        int size = 1;
        int fcsFieldSize = fhd & 3;
        boolean singleSegment = ((fhd >> 5) & 1) == 1;
        int dictIdFlag = (fhd >> 0) & 3;

        int dictIdSize = 0;
        int dictIdBits = fhd & 3;
        if (dictIdBits == 1) dictIdSize = 1;
        else if (dictIdBits == 2) dictIdSize = 2;
        else if (dictIdBits == 3) dictIdSize = 4;

        int fcsSize = 0;
        int fcsBits = (fhd >> 6) & 3;
        if (fcsBits == 0) fcsSize = singleSegment ? 1 : 0;
        else if (fcsBits == 1) fcsSize = 2;
        else if (fcsBits == 2) fcsSize = 4;
        else if (fcsBits == 3) fcsSize = 8;

        int windowDescSize = singleSegment ? 0 : 1;

        return size + windowDescSize + dictIdSize + fcsSize;
    }
}
