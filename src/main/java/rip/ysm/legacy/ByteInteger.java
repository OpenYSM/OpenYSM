package rip.ysm.legacy;

public final class ByteInteger {
    public static byte[] int2Bytes(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[3 - i] = (byte) (value >> 8 * i);
        }
        return b;
    }

    public static int bytes2Int(byte[] b, int start) {
        int len = 4;
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = b[i] & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }
}
