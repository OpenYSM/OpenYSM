package rip.ysm.legacy;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class DeflateUtil {
    public static byte[] compressBytes(final byte[] input) {
        if (input.length == 0) {
            return new byte[]{};
        }
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();
        while (!deflater.finished()) {
            final int compressedDataLength = deflater.deflate(buf);
            stream.write(buf, 0, compressedDataLength);
        }
        deflater.end();
        return stream.toByteArray();
    }

    public static byte[] decompressBytes(final byte[] input) throws DataFormatException {
        if (input.length == 0) {
            return new byte[]{};
        }
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        final Inflater inflater = new Inflater();
        inflater.setInput(input, 0, input.length);
        while (!inflater.finished()) {
            final int resultLength = inflater.inflate(buf);
            stream.write(buf, 0, resultLength);
        }
        inflater.end();
        return stream.toByteArray();
    }
}