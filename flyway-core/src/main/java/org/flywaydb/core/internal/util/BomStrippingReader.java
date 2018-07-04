/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * Reader that strips the BOM at the beginning of a stream.
 */
public class BomStrippingReader extends FilterReader {
    private static final int EMPTY_STREAM = -1;
    private static final char BOM = '\ufeff';

    /**
     * Creates a new BOM-stripping reader.
     *
     * @param in a Reader object providing the underlying stream.
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     */
    public BomStrippingReader(Reader in) throws IOException {
        super(new PushbackReader(in));
        PushbackReader pbr = (PushbackReader) this.in;
        int firstChar = pbr.read();
        if (firstChar != EMPTY_STREAM && firstChar != BOM) {
            pbr.unread(firstChar);
        }
    }
}