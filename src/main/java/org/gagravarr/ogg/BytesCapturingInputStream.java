/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.ogg;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper around an InputStream, which captures all
 *  bytes as they are read, and makes those available.
 * Used when you want to read from a stream, but also
 *  have a record easily of what it contained
 */
public class BytesCapturingInputStream extends FilterInputStream {
    private ByteArrayOutputStream data;

    public BytesCapturingInputStream(InputStream input) {
        super(input);
        data = new ByteArrayOutputStream();
    }

    @Override
    public int read() throws IOException {
        int value = super.read();
        data.write(value);
        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read > 0)
            data.write(b, off, read);
        return read;
    }

    /**
     * Returns all data captured so far, without resetting.
     * Subsequent calls will return the same data, plus any
     *  new bits.
     */
    public byte[] getData() {
        return data.toByteArray();
    }
}
