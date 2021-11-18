/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MachineFingerprintUtils {
    public static String getFingerprint(String... salts) throws Exception {
        if (salts == null || salts.length == 0 || Arrays.stream(salts).noneMatch(StringUtils::hasText)) {
            throw new Exception("All parameters required for getFingerprint");
        }

        byte[] hashedId = salts[0].getBytes(StandardCharsets.UTF_8);
        for (String salt : salts) {
            hashedId = getHashed(salt.getBytes(StandardCharsets.UTF_8), hashedId);
        }

        List<byte[]> hardwareAddresses = getHardwareAddresses();

        if (hardwareAddresses.size() == 0) {
            throw new Exception("No hardware addresses found when creating fingerprint");
        }

        for (byte[] hardwareAddress : hardwareAddresses) {
            hashedId = getHashed(hardwareAddress, hashedId);
        }

        return hashToString(hashedId);
    }

    private static List<byte[]> getHardwareAddresses() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        // This can be null, ignore IntelliJ's suggestion
        //noinspection ConstantConditions
        if (networkInterfaces == null) {
            return new ArrayList<>();
        }

        return Collections.list(networkInterfaces)
                .stream()
                .map(MachineFingerprintUtils::extractHardwareAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static byte[] extractHardwareAddress(NetworkInterface networkInterface) {
        try {
            return networkInterface.getHardwareAddress();
        } catch (SocketException e) {
            return null;
        }
    }

    private static byte[] getHashed(byte[] salt, byte[] digest) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        return md.digest(digest);
    }

    private static String hashToString(byte[] hashedId) {
        String[] hexadecimal = new String[hashedId.length];
        for (int i = 0; i < hexadecimal.length; i++) {
            hexadecimal[i] = String.format("%02X", hashedId[i]);
        }
        return String.join("", hexadecimal);
    }
}