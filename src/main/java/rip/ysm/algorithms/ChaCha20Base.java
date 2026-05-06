package rip.ysm.algorithms;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

public abstract class ChaCha20Base {
    protected static final int[] SIGMA = toIntArray(new byte[]{101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107});
    public final int[] state = new int[16];
    public int rounds;

    protected static int rotateLeft(int i, int i2) {
        return (i >>> (-i2)) | (i << i2);
    }

    public static void quarterRound(int[] x, int a, int b, int c, int d) {
        x[a] += x[b]; x[d] = rotateLeft(x[d] ^ x[a], 16);
        x[c] += x[d]; x[b] = rotateLeft(x[b] ^ x[c], 12);
        x[a] += x[b]; x[d] = rotateLeft(x[d] ^ x[a], 8);
        x[c] += x[d]; x[b] = rotateLeft(x[b] ^ x[c], 7);
    }

    public static void shuffleState(int[] x, int rounds) {
        int halfRounds = rounds / 2;
        for (int i = 0; i < halfRounds; i++) {
            quarterRound(x, 0, 4, 8, 12);
            quarterRound(x, 1, 5, 9, 13);
            quarterRound(x, 2, 6, 10, 14);
            quarterRound(x, 3, 7, 11, 15);
            quarterRound(x, 0, 5, 10, 15);
            quarterRound(x, 1, 6, 11, 12);
            quarterRound(x, 2, 7, 8, 13);
            quarterRound(x, 3, 4, 9, 14);
        }
    }

    protected static int[] toIntArray(byte[] bArr) {
        IntBuffer asIntBuffer = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] iArr = new int[asIntBuffer.remaining()];
        asIntBuffer.get(iArr);
        return iArr;
    }

    protected static byte[] toByteArray(int[] ints) {
        ByteBuffer buf = ByteBuffer.allocate(ints.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        buf.asIntBuffer().put(ints);
        return buf.array();
    }

    public byte[] processBlock() {
        int[] workingState = state.clone();
        shuffleState(workingState, this.rounds);
        for (int i = 0; i < 16; i++) {
            workingState[i] += state[i];
        }
        return toByteArray(workingState);
    }

    public void incrementCounter() {
        state[12]++;
        if (state[12] == 0) {
            state[13]++;
        }
    }
}