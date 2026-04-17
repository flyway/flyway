/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


public class EncryptionUtils {
    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                                                    .getEncoded(), "AES");
        return secret;
    }

    public static SealedObject encryptObject(String algorithm, Serializable object,
                                             SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IOException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return new SealedObject(object, cipher);
    }

    public static Serializable decryptObject(String algorithm, SealedObject sealedObject,
                                             SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            ClassNotFoundException, BadPaddingException, IllegalBlockSizeException,
            IOException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return (Serializable) sealedObject.getObject(cipher);
    }

    public static byte[] toByteArray(SealedObject sealedObject) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(sealedObject);
            out.flush();
            return bos.toByteArray();
        }
    }

    public static SealedObject fromByteArray(byte[] byteArray) throws IOException, ClassNotFoundException {
        return (SealedObject) new ObjectInputStream(new ByteArrayInputStream(byteArray)).readObject();
    }

    public static String hashString(String strInput, String hashInput) {
        if (strInput == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(strInput.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest(hashInput.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            String result = number.toString(16);
            while (result.length() < 64) {
                result = "0" + result;
            }
            return result;
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }







































}
