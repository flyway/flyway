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

import org.flywaydb.core.api.FlywayException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Contains a number of methods usefull for encryption
 */
public class CipherUtils {
    /**
     * Prevents instantiation.
     */
    private CipherUtils() {
        // Do nothing.
    }

    /**
     * Return a random salt
     *
     * @param size Number of random byte to return
     * @return An array of bytes
     */
    public static byte[] getSalt(int size) {
        SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new FlywayException("Unable to get SHA1PRNG algorithm to generate random salt", e);
		}
        byte[] salt = new byte[size];
        sr.nextBytes(salt);
        return salt;
    }
}