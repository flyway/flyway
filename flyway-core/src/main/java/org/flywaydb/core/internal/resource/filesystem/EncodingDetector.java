/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.resource.filesystem;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncodingDetector {
    private static final int UTF16BigEndianHeaderByte1 = 254;
    private static final int UTF16BigEndianHeaderByte2 = 255;

    private static final int UTF16LittleEndianHeaderByte1 = 255;
    private static final int UTF16LittleEndianHeaderByte2 = 254;

    private static final String[] detectableCharsets = {
            StandardCharsets.UTF_8.toString(),
            StandardCharsets.ISO_8859_1.toString(),
            StandardCharsets.UTF_16.toString()
    };

    public static Charset detectFileEncoding(Path path) throws FlywayEncodingDetectionException {
        try {
            byte[] buffer = readAllBytesFromPath(path);
            Charset charset = detectCharset(buffer);

            if (charset == null) {
                throw new FlywayEncodingDetectionException("Could not detect charset for " + path);
            }

            return charset;
        } catch (Exception e) {
            throw new FlywayEncodingDetectionException("Could not detect encoding for " + path, e);
        }
    }

    private static byte[] readAllBytesFromPath(Path path) {
        try {
            return Files.readAllBytes(path.toAbsolutePath());
        } catch (Exception e) {
            throw new FlywayEncodingDetectionException("Could not detect encoding for " +
                                                               path + " as the file could not be read", e);
        }
    }

    private static Charset detectCharset(byte[] buffer) {
        Charset charset = readCharsetHeader(buffer);

        if (charset != null) {
            return charset;
        }

        for (String charsetName : detectableCharsets) {
            Charset detectingCharset = Charset.forName(charsetName);

            if (canCorrectlyDecodeBufferWithNamedCharset(buffer, detectingCharset)) {
                charset = detectingCharset;
                break;
            }
        }

        if (charset != StandardCharsets.UTF_16 &&
                charset != StandardCharsets.UTF_16BE &&
                charset != StandardCharsets.UTF_16LE &&
                isLikelyToBeUTF16(buffer)) {
            charset = StandardCharsets.UTF_16;
        }

        return charset;
    }

    private static Charset readCharsetHeader(byte[] buffer) {
        if (buffer.length < 2) {
            // We assume two header bytes
            return null;
        }

        if (buffer.length % 2 != 0) {
            // UTF16 files cannot be an odd length
            return null;
        }

        int headerByte1 = Byte.toUnsignedInt(buffer[0]);
        int headerByte2 = Byte.toUnsignedInt(buffer[1]);

        if (UTF16BigEndianHeaderByte1 == headerByte1 && UTF16BigEndianHeaderByte2 == headerByte2) {
            return StandardCharsets.UTF_16BE;
        }

        if (UTF16LittleEndianHeaderByte1 == headerByte1 && UTF16LittleEndianHeaderByte2 == headerByte2) {
            return StandardCharsets.UTF_16LE;
        }

        return null;
    }

    /**
     * UTF16 documents tend to have most odd indexed bytes be a 0 byte. Non UTF16 this is very rare.
     * We use this fact to ascertain if a BOMLess file is infact UTF16.
     */
    private static boolean isLikelyToBeUTF16(byte[] buffer) {
        if (buffer.length % 2 != 0) {
            // UTF16 files cannot be an odd length
            return false;
        }

        int matchCount = 0;

        for (int i = 1; i < buffer.length; i = i + 2) {
            boolean oddIndexedByteIsZero = (Byte.toUnsignedInt(buffer[i]) == 0);

            if (oddIndexedByteIsZero) {
                matchCount++;
            } else {
                matchCount--;
            }
        }

        // if matchCount is greater than 0 (more than half of odd indexed bytes are zero) we can assume this is
        // BOMLess UTF16. A lower threshold may be applicable.
        return matchCount > 0;
    }

    private static boolean canCorrectlyDecodeBufferWithNamedCharset(byte[] buffer, Charset charset) {
        try {
            charset.newDecoder().decode(ByteBuffer.wrap(buffer));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
