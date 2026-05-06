package rip.ysm.security;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class YSMByteBuf implements AutoCloseable {
    private final ByteBuf buf;

    public YSMByteBuf(ByteBuf buf) {
        this.buf = buf.order(ByteOrder.LITTLE_ENDIAN); // YSM的基於C++，使用小端序
    }

    public ByteBuf getRawBuf() { return this.buf; }

    // 消費垃圾數據頭部，防止讀寫出問題
    public int skipGarbageHeader() {
        int garbageLen = buf.readByte() & 0x7F;
        buf.skipBytes(1);
        buf.skipBytes(garbageLen);
        return garbageLen;
    }

    public void writeGarbageHeader(int garbageLen, byte[] garbageData) {
        buf.writeByte(garbageLen | 0x80);
        buf.writeByte(0x00);
        buf.writeBytes(garbageData);
    }

    public int getOffset() {
        return buf.readerIndex();
    }

    public void setOffset(int offset) {
        buf.readerIndex(offset);
    }

    public byte readByte() {
        return buf.readByte();
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public long readDword() {
        return buf.readUnsignedInt();
    }

    public void writeDword(int format) {
        buf.writeInt(format);
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;
        while (true) {
            byte currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) break;
            position += 7;
            if (position >= 64) throw new RuntimeException("VarInt too big");
        }
        return value;
    }

    public void writeVarInt(int value) {
        while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public long readVarLong() {
        long value = 0L;
        int position = 0;
        while (true) {
            byte currentByte = buf.readByte();
            value |= (long) (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) break;
            position += 7;
            if (position >= 64) throw new RuntimeException("VarLong too big");
        }
        return value;
    }

    public void writeVarLong(long value) {
        while ((value & -128L) != 0L) {
            buf.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }
        buf.writeByte((int) value);
    }

    public byte[] readByteArray() {
        int len = readVarInt();
        if (len == 0) return new byte[0];
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    public String readString() {
        int len = readVarInt();
        if (len == 0) return "";
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public byte[] toArray() {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public void skipBytes(int n) {
        buf.skipBytes(n);
    }

    public void writeString(String s) {
        if (s == null || s.isEmpty()) {
            writeVarInt(0);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public void writeByte(byte value) {
        buf.writeByte(value);
    }

    public void writeFloat(float value) {
        buf.writeFloat(value);
    }

    public void writeByteArray(byte[] data) {
        if (data == null || data.length == 0) {
            writeVarInt(0);
            return;
        }
        writeVarInt(data.length);
        buf.writeBytes(data);
    }

    public void writeByteBuf(ByteBuf other) {
        buf.writeBytes(other);
    }

    public void release() {
        if (this.buf != null && this.buf.refCnt() > 0) {
            this.buf.release();
        }
    }

    @Override
    public void close() {
        release();
    }
}