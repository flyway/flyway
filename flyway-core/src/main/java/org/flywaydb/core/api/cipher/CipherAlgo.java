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
package org.flywaydb.core.api.cipher;

import org.flywaydb.core.api.FlywayException;

/**
 * Cipher.
 */
public interface CipherAlgo {
    /**
     * Encrypt a plain text.
     *
     * @param plaintext Plaintext to encrypt.
     * @param password Password.
     */
    public String encrypt(String plaintext, String password);

    /**
     * Decrypt an encrypted text.
     *
     * @param encryptedText Encrypted text to decrypt.
     * @param password Password.
     */
    public String decrypt(String encryptedText, String password);
}