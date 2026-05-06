package rip.ysm.zstd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class YsmZstd {
    private static final int STD_BT_RAW = 0;
    private static final int STD_BT_RLE = 1;
    private static final int STD_BT_COMPRESSED = 2;
    private static final int STD_BT_RESERVED = 3;


    public static byte[] wash(byte[] data) {
        if (data == null || data.length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // 1. 验证 ZSTD Magic Number (0xFD2FB528)
        int magic = buffer.getInt(0);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD Magic Number. May be skippable frame or unknown.");
        }

        // 2. 擦除 Frame Header 中的 Checksum 标志位 (绕过 XXHash 魔改)
        // FHD (Frame Header Descriptor) 是紧接在 Magic 之后的第 5 个字节 (index 4)
        byte fhd = data[4];
        // Bit 2 代表 Content Checksum flag。我们用 0xFB (11111011) 清除这一位
        data[4] = (byte) (fhd & 0xFB);

        // 3. 计算 Frame Header 的长度，从而找到第一个 Block 的起点
        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int offset = 4 + frameHeaderSize;

        // 4. 遍历并洗白所有 Block Header
        while (offset + 3 <= data.length) {
            // 读取 3 bytes 的 CBlockHeader (Little Endian)
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset + 1] & 0xFF;
            int b2 = data[offset + 2] & 0xFF;
            int cBlockHeader = b0 | (b1 << 8) | (b2 << 16);

            // --- 提取 YSM 魔改字段 ---
            // bpPtr->lastBlock = (cBlockHeader >> 7) & 1;
            int lastBlock = (b0 >> 7) & 1;

            // bpPtr->blockType = (blockType_e)((cBlockHeader >> 5) & 3);
            int blockTypeYSM = (b0 >> 5) & 3;

            // U32 const rawSize = ((cBlockHeader & 0x1F) << 16) | (cBlockHeader >> 8);
            // U32 const cSize = rawSize ^ 0xD4E9;
            int rawSize = ((b0 & 0x1F) << 16) | b1 | (b2 << 8);
            int cSize = rawSize ^ 0xD4E9;

            // --- 映射回官方 Standard Block Type ---
            // YSM: 0=compressed, 1=rle, 2=reserved, 3=raw
            int blockTypeStd;
            switch (blockTypeYSM) {
                case 0: blockTypeStd = STD_BT_COMPRESSED; break;
                case 1: blockTypeStd = STD_BT_RLE; break;
                case 2: blockTypeStd = STD_BT_RESERVED; break;
                case 3: blockTypeStd = STD_BT_RAW; break;
                default: throw new IllegalStateException("Unknown block type");
            }

            // --- 重新组装为官方 Standard Block Header ---
            // 官方布局: bit 0 = lastBlock, bits 1-2 = blockType, bits 3-23 = size
            int stdHeader = lastBlock | (blockTypeStd << 1) | (cSize << 3);

            data[offset] = (byte) (stdHeader & 0xFF);
            data[offset + 1] = (byte) ((stdHeader >> 8) & 0xFF);
            data[offset + 2] = (byte) ((stdHeader >> 16) & 0xFF);

            // 移动到下一个 block
            // 根据 C 源码: if (bpPtr->blockType == bt_rle) return 1;
            int blockDataSize = (blockTypeStd == STD_BT_RLE) ? 1 : cSize;
            offset += 3 + blockDataSize;

            if (lastBlock == 1) {
                break; // 这是最后一个块，跳出
            }
        }

        return data; // 数据已在原数组中洗白
    }
    /**
     * 将标准 ZSTD 数据“弄脏”为 YSM 魔改格式
     * @param data 标准 ZSTD 压缩数据
     * @return 混淆后的数据（原地修改）
     */
    public static byte[] obfuscate(byte[] data) {
        if (data == null || data.length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // 1. 验证是否为标准 ZSTD Magic Number (0xFD2FB528)
        int magic = buffer.getInt(0);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD frame.");
        }

        // 2. 读取 FHD，确认 Frame Header 大小以找到第一个 Block
        byte fhd = data[4];
        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int offset = 4 + frameHeaderSize;

        // 3. 遍历并混淆所有 Block Header
        while (offset + 3 <= data.length) {
            // 读取标准 3 bytes 的 CBlockHeader (Little Endian)
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset + 1] & 0xFF;
            int b2 = data[offset + 2] & 0xFF;
            int cBlockHeader = b0 | (b1 << 8) | (b2 << 16);

            // --- 提取标准字段 ---
            int lastBlock = cBlockHeader & 1;
            int blockTypeStd = (cBlockHeader >> 1) & 3;
            int cSize = cBlockHeader >> 3;

            // 移动到下一个 block 的偏移量准备（标准和YSM的载荷长度是一样的）
            int blockDataSize = (blockTypeStd == STD_BT_RLE) ? 1 : cSize;

            // --- 映射为 YSM Block Type ---
            // 官方: 0=raw, 1=rle, 2=compressed, 3=reserved
            // YSM:  0=compressed, 1=rle, 2=reserved, 3=raw
            int blockTypeYSM;
            switch (blockTypeStd) {
                case STD_BT_RAW: blockTypeYSM = 3; break;
                case STD_BT_RLE: blockTypeYSM = 1; break;
                case STD_BT_COMPRESSED: blockTypeYSM = 0; break;
                case STD_BT_RESERVED: blockTypeYSM = 2; break;
                default: throw new IllegalStateException("Unknown block type");
            }

            // --- YSM Size 异或加密 ---
            int rawSize = cSize ^ 0xD4E9;

            // --- 重新组装为 YSM 魔改 Block Header ---
            // b0: bit 7 = lastBlock, bits 5-6 = blockTypeYSM, bits 0-4 = rawSize 高 5 位
            // b1: rawSize 低 8 位
            // b2: rawSize 中 8 位

            int ysmB0 = (lastBlock << 7) | (blockTypeYSM << 5) | ((rawSize >> 16) & 0x1F);
            int ysmB1 = rawSize & 0xFF;
            int ysmB2 = (rawSize >> 8) & 0xFF;

            // 写回原地
            data[offset] = (byte) ysmB0;
            data[offset + 1] = (byte) ysmB1;
            data[offset + 2] = (byte) ysmB2;

            offset += 3 + blockDataSize;

            if (lastBlock == 1) {
                break; // 最后一个块，跳出
            }
        }

        return data;
    }

    /**
     * 复用之前的解析逻辑，计算 Frame Header 长度
     */
    private static int calculateFrameHeaderSize(byte fhd) {
        int size = 1; // FHD 本身占 1 字节

        int fcsFieldSize = fhd & 3; // bits 0-1
        boolean singleSegment = ((fhd >> 5) & 1) == 1; // bit 5
        int dictIdFlag = (fhd >> 0) & 3; // wait, dictionary is bits 0-1? No, bits 6-7 in byte.

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
