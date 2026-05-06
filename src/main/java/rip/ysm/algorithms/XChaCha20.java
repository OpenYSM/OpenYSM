package rip.ysm.algorithms;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

public class XChaCha20 extends ChaCha20Base {

    public XChaCha20(byte[] key, byte[] nonce, int rounds) throws InvalidKeyException {
        if (key.length != 32) throw new InvalidKeyException("Key must be 32 bytes");
        if (nonce.length != 24) throw new IllegalArgumentException("Nonce must be 24 bytes");

        this.rounds = rounds;
        keySetup(key, nonce);
    }

    private void keySetup(byte[] keyBytes, byte[] nonceBytes) {
        int[] key = toIntArray(keyBytes);
        int[] nonce = toIntArray(nonceBytes);
        int[] subkey = hChaCha20(key, nonce, this.rounds);
        System.arraycopy(SIGMA, 0, this.state, 0, 4);
        System.arraycopy(subkey, 0, this.state, 4, 8);
        this.state[12] = 0;
        this.state[13] = 0;
        this.state[14] = nonce[4];
        this.state[15] = nonce[5];
    }

    private static int[] hChaCha20(int[] key, int[] nonce, int rounds) {
        int[] x = new int[16];
        System.arraycopy(SIGMA, 0, x, 0, 4);
        System.arraycopy(key, 0, x, 4, 8);
        x[12] = nonce[0];
        x[13] = nonce[1];
        x[14] = nonce[2];
        x[15] = nonce[3];

        shuffleState(x, rounds);

        return new int[]{x[0], x[1], x[2], x[3], x[12], x[13], x[14], x[15]};
    }

    public byte[] processBytes(byte[] in, int offset, int length) {
        byte[] out = new byte[length];
        int inIdx = offset;
        int outIdx = 0;
        int len = length;

        while (len > 0) {
            byte[] keystream = processBlock();
            int blockLen = Math.min(64, len);

            for (int i = 0; i < blockLen; i++) {
                out[outIdx + i] = (byte) (in[inIdx + i] ^ keystream[i]);
            }
            incrementCounter();
            inIdx += blockLen;
            outIdx += blockLen;
            len -= blockLen;
        }
        return out;
    }

    public int updateStateYSM(long hash) {
        int hashMod = (int) Long.remainderUnsigned(hash, 3);
        this.rounds = 10 * hashMod + 10;
        int lo = (int) (hash & 0xFFFFFFFFL);
        int hi = (int) ((hash >>> 32) & 0xFFFFFFFFL);
        for (int i = 4; i < 16; ++i) {
            if (i % 2 == 0) {
                this.state[i] ^= lo;
            } else {
                this.state[i] ^= hi;
            }
        }
        return (int) (((hash & 0x3FL) | 0x40L) << 6);
    }

    public static byte[] decryptYSM(byte[] data, byte[] key, byte[] iv, long seed) throws InvalidKeyException {
        byte[] keyIv = new byte[56];
        System.arraycopy(key, 0, keyIv, 0, 32);
        System.arraycopy(iv, 0, keyIv, 32, 24);

        CityHash cityHash = new CityHash();
        long hash2 = cityHash.hash64WithSeed(keyIv, seed);

        int nextRoundSize = (int) (((hash2 & 0x3FL) | 0x40L) << 6);
        int blockPointer = 0;

        int initialRounds = 10 * ((int) Long.remainderUnsigned(hash2, 3)) + 10;
        XChaCha20 ctx = new XChaCha20(key, iv, initialRounds);

        ByteBuffer result = ByteBuffer.allocate(data.length);

        while (blockPointer < data.length) {
            if (blockPointer + nextRoundSize > data.length) {
                nextRoundSize = data.length - blockPointer;
            }

            byte[] decChunk = ctx.processBytes(data, blockPointer, nextRoundSize);
            blockPointer += nextRoundSize;
            result.put(decChunk);
            if (blockPointer < data.length) {
                long resHash = cityHash.hash64WithSeed(decChunk, seed);
                nextRoundSize = ctx.updateStateYSM(resHash);
            }
        }

        return result.array();
    }
}