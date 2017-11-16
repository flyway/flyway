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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.cipher.CipherAlgo;
import org.flywaydb.core.api.cipher.CipherAlgoFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;

/**
 * Sql script containing an encryption header or Sql script to encrypt
 */
public class SqlScriptVault {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    private static final String HEADER_SQL_ENCRYPTED = "$SQL_VAULT";

    private static final Pattern HEADER_SQL_ENCRYPTED_REGEX = Pattern.compile("^" + Pattern.quote(HEADER_SQL_ENCRYPTED) + ";(?<version>[0-9.]+);(?<cipher>\\w+)$");

    public static final String[] CIPHER_WHITELIST = {"None", "AES128", "AES256"};

    public static final String DEFAULT_VERSION = "1.0";

    public static final String DEFAULT_CIPHER = "AES128";


    /**
     * Check if the Sql source file is encrypted or not
     *
     * @return {@code true} if Sql file is encrypted or {@code false} if not.
     */
    public static boolean isEncrypted(String sqlScriptSource) {
        return sqlScriptSource.trim().startsWith(HEADER_SQL_ENCRYPTED);
    }

    /**
     * Parse header and return an array containing Version, Cipher name and Encrypted data
     *
     * @return Array containing Version, Cipher name and Encrypted data.
     */
    public static String[] parseData(String sqlScriptSource) {
        String[] result = sqlScriptSource.trim().split("\\r?\\n", 2);
        Matcher match = HEADER_SQL_ENCRYPTED_REGEX.matcher(result[0]);

        if (match.find()) {
            return new String[]{match.group("version"), match.group("cipher"), result[1]};
        }

        return null;
    }

    /**
     * Return the decrypted sql source
     *
     * @param sqlScriptSource Sql source to decrypt
     * @param vaultPassword Secret
     * @return The Sql source decrypted
     */
    public static String decrypt(String sqlScriptSource, String vaultPassword) {
        if (!isEncrypted(sqlScriptSource)) {
            throw new FlywayException("Resource is not encrypted");
        }

        if (vaultPassword.isEmpty()) {
            throw new FlywayException("Vault Password is needed to decrypt Sql source file");
        }

        String[] headers = parseData(sqlScriptSource);
        String cipherName = headers[1];
        String encryptedData = headers[2];

        if (!Arrays.asList(CIPHER_WHITELIST).contains(cipherName)) {
            throw new FlywayException(cipherName + " is not available");
        }

        CipherAlgo cipher = CipherAlgoFactory.getCipher(cipherName);

        return cipher.decrypt(encryptedData, vaultPassword);
    }

    /**
     * Encrypt sql source and add header using the default cipher
     *
     * @param sqlScriptSource Sql source to encrypt
     * @param vaultPassword Secret
     * @return The Sql source encrypted with header
     */
    public static String encrypt(String sqlScriptSource, String vaultPassword) {
        return encrypt(sqlScriptSource, vaultPassword, DEFAULT_CIPHER);
    }

    /**
     * Encrypt sql source and add header
     *
     * @param sqlScriptSource Sql source to encrypt
     * @param vaultPassword Secret
     * @param cipherName CIpher to use for encryption
     * @return The Sql source encrypted with header
     */
    public static String encrypt(String sqlScriptSource, String vaultPassword, String cipherName) {
        if (vaultPassword.isEmpty()) {
            throw new FlywayException("Vault Password is needed to encrypt Sql source file");
        }

        if (!Arrays.asList(CIPHER_WHITELIST).contains(cipherName)) {
            throw new FlywayException(cipherName + " is not available");
        }

        CipherAlgo cipher = CipherAlgoFactory.getCipher(cipherName);

        String sqlSourceEncrypted = HEADER_SQL_ENCRYPTED + ";" + DEFAULT_VERSION + ";" + cipherName + "\n";

        sqlSourceEncrypted += cipher.encrypt(sqlScriptSource, vaultPassword);

        return sqlSourceEncrypted;
    }
}