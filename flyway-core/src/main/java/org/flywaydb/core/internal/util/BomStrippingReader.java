package org.flywaydb.core.internal.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Reader that strips the BOM at the beginning of a stream.
 */
public class BomStrippingReader extends FilterReader {
    private static final int EMPTY_STREAM = -1;

    /**
     * Creates a new BOM-stripping reader.
     *
     * @param in a Reader object providing the underlying stream.
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     */
    public BomStrippingReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != EMPTY_STREAM && BomFilter.isBom((char) c)) {
            // Skip BOM
            return super.read();
        }
        return c;
    }
}