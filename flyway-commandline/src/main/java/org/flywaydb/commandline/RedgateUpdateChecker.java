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
package org.flywaydb.commandline;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.license.VersionPrinter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RedgateUpdateChecker {

    private static class UpdateCheckResponse {
        public String message;
    }

    private static final String UPDATE_CHECK_ENDPOINT = "https://repo.flywaydb.org/update-check";
    private static final String REDGATE_SERVER_ADDRESS = "https://repo.flywaydb.org";
    private static final Log LOG = LogFactory.getLog(RedgateUpdateChecker.class);

    public static String getUpdateCheckMessage(String jdbcUrl) {
        String message = "";

        if (!isRedgateServerReachable()) {
            LOG.debug("Could not reach Redgate server for update check.");
            return message;
        }

        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) new URL(UPDATE_CHECK_ENDPOINT).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = getJsonPayload(jdbcUrl);
            putJsonInConnectionRequestBody(connection, jsonPayload);

            StringBuilder response = new StringBuilder();

            try(BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line).append('\r');
                }
            }

            message = new Gson().fromJson(response.toString(), UpdateCheckResponse.class).message;

        } catch (Exception e) {
            LOG.debug("Failed to perform update check: " + e.getMessage());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return message;
    }

    private static boolean isRedgateServerReachable() {
        HttpsURLConnection connection = null;

        try {
            HttpsURLConnection.setFollowRedirects(false);
            connection = (HttpsURLConnection) new URL(REDGATE_SERVER_ADDRESS).openConnection();

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);

            return (connection.getResponseCode() == HttpsURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void putJsonInConnectionRequestBody(HttpsURLConnection connection, String jsonPayload) throws IOException {
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private static String getJsonPayload(String jdbcUrl) throws Exception {
        String version = VersionPrinter.getVersion();
        String operatingSystem = System.getProperty("os.name");
        JsonObject json = new JsonObject();
        json.addProperty("currentVersion", version);
        json.addProperty("operatingSystem", operatingSystem);
        json.addProperty("fingerprint", getFingerprint(operatingSystem, jdbcUrl));
        return new Gson().toJson(json);
    }

    private static String getFingerprint(String operatingSystem, String jdbcUrl) throws Exception {
        if (isEmpty(operatingSystem) || isEmpty(jdbcUrl)) {
            throw new Exception("All parameters required for getFingerprint - operatingSystem: " + isEmpty(operatingSystem) + ", jdbcUrl: " + isEmpty(jdbcUrl));
        }

        byte[] hardwareAddress;
        byte[] hashedId = operatingSystem.getBytes(StandardCharsets.UTF_8);
        hashedId = getHashed(operatingSystem.getBytes(StandardCharsets.UTF_8), hashedId);
        hashedId = getHashed(jdbcUrl.getBytes(StandardCharsets.UTF_8), hashedId);
        hashedId = getHashed(System.getProperty("user.dir").getBytes(StandardCharsets.UTF_8), hashedId);
        hardwareAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();

        if(hardwareAddress == null || hardwareAddress.length == 0) {
            throw new Exception("Hardware address was null or empty when creating fingerprint");
        }

        hashedId = getHashed(hardwareAddress, hashedId);
        return hashToString(hashedId);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
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