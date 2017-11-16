/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a number of methods to convert between binary and various ASCII-encoded binary representations.
 */
public class BinAscii {
    /**
     * Prevents instantiation.
     */
    private BinAscii() {
        // Do nothing.
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Return the hexadecimal representation of the binary data
     * @throws UnsupportedEncodingException
     * @param str A String
     * @return Return hexadecimal representation into a string
     */
    public static String hexlify(String str) throws UnsupportedEncodingException {
        return hexlify(str.getBytes("UTF-8"));
    }
    /**
     * Return the hexadecimal representation of the binary data
     * @param bytes An array of bytes
     * @return Return hexadecimal representation into a string
     */
    public static String hexlify(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String ret = new String(hexChars);
        return ret;
    }

    /**
     * Return the binary data represented by the hexadecimal string.
     * @param bytes An array of argbuf
     * @return Return the binary data into an array of byte
     */
    public static byte[] unhexlify(byte[] argbuf) {
        return unhexlify(new String(argbuf));
    }

    /**
     * Return the binary data represented by the hexadecimal string.
     * @param str A String
     * @return Return the binary data into an array of byte
     */
    public static byte[] unhexlify(String argbuf) {
        int arglen = argbuf.length();
        if (arglen % 2 != 0)
            throw new RuntimeException("Odd-length string");

        byte[] retbuf = new byte[arglen/2];

        for (int i = 0; i < arglen; i += 2) {
            int top = Character.digit(argbuf.charAt(i), 16);
            int bot = Character.digit(argbuf.charAt(i+1), 16);
            if (top == -1 || bot == -1)
                throw new RuntimeException("Non-hexadecimal digit found");
            retbuf[i / 2] = (byte) ((top << 4) + bot);
        }
        return retbuf;
    }

    /**
     * Splits this array of byte around a given delimiter
     *
     * @param input Array of bytes to split
     * @param delimiter Delimiter to split
     * @return A list of array of bytes
     */
    public static List<byte[]> split(byte[] input, byte delimiter) {
        return split(input, delimiter, -1);
    }

    /**
     * Splits this array of byte around a given delimiter
     *
     * @param input Array of bytes to split
     * @param delimiter Delimiter to split
     * @param limit The result threshold
     * @return A list of array of bytes
     */
    public static List<byte[]> split(byte[] input, byte delimiter, int limit) {
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        limit --;
        for(int i=0; i<input.length; i++) {
            if(delimiter == input[i]) {
                l.add(Arrays.copyOfRange(input, blockStart, i));
                blockStart = i+1;
                i = blockStart;
                if (l.size() == limit) {
                    break;
                }
            }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }
}