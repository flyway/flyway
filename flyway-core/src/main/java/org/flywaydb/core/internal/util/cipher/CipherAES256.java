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
package org.flywaydb.core.internal.util.cipher;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.cipher.CipherAlgo;
import org.flywaydb.core.internal.util.BinAscii;
import org.flywaydb.core.internal.util.CipherUtils;
import org.flywaydb.core.internal.util.StringUtils;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.List;

/**
 * Cipher for AES256.
 *
 * @see http://mail.openjdk.java.net/pipermail/security-dev/2016-October/014943.html
 */
public class CipherAES256 implements CipherAlgo {

    private static final int SALT_SIZE = 32;

    private static final int KEY_SIZE = 256;

    private static final int KEY_INTERATION_COUNT = 65536;

    /**
     * Encrypt a plain text.
     *
     * @param plaintext Plaintext to encrypt.
     * @param secret Secret.
     */
    @Override
    public String encrypt(String plaintext, String password) {
        try {
            byte[] plaintextBytes = plaintext.getBytes("UTF-8");
            byte[] salt = CipherUtils.getSalt(SALT_SIZE);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, KEY_INTERATION_COUNT, KEY_SIZE);

            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            byte[] encryptedTextBytes = cipher.doFinal(plaintextBytes);


            String strPart = BinAscii.hexlify(salt) + "\n" + BinAscii.hexlify(ivBytes) + "\n" + BinAscii.hexlify(encryptedTextBytes);

            String finalEncryptedStr = BinAscii.hexlify(strPart);

            return StringUtils.formartOnMultipleLines(finalEncryptedStr);
        }
        catch (InvalidKeyException e) {
            throw new FlywayException("Unable to encrypt data with AES256: You should check in $JAVA_HOME/jre/lib/security/java.secruity if crypto.policy is equals to unlimited !", e);
        }
        catch(Exception e) {
            throw new FlywayException("Unable to encrypt data with AES256", e);
        }
    }

    /**
     * Decrypt an encrypted text.
     *
     * @param encryptedText Encrypted text to decrypt.
     * @param secret Secret.
     */
    @Override
    public String decrypt(String encryptedText, String password) {
        try {
            byte LF = (byte) 0x0A;

            encryptedText = StringUtils.removeNewLines(encryptedText);

            byte[] encryptedTextBytes = BinAscii.unhexlify(encryptedText);

            List<byte[]> encTextBytesPart = BinAscii.split(encryptedTextBytes, LF);
            byte[] salt = BinAscii.unhexlify(encTextBytesPart.get(0));
            byte[] ivBytes = BinAscii.unhexlify(encTextBytesPart.get(1));
            byte[] cipherText = BinAscii.unhexlify(encTextBytesPart.get(2));

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, KEY_INTERATION_COUNT, KEY_SIZE);

            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

            byte[] decryptedTextBytes = cipher.doFinal(cipherText);

            return new String(decryptedTextBytes);
        }
        catch(Exception e) {
            throw new FlywayException("Vault password is incorrect or unable to decrypt data with AES256", e);
        }
    }
}