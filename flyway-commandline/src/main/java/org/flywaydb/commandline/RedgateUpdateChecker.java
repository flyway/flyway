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
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.MachineFingerprintUtils;
import org.flywaydb.core.internal.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod(StringUtils.class)
public class RedgateUpdateChecker {
    private static final String PLATFORM_URL_ROOT = getRoot();
    private static final String USAGE_CHECKER_ENDPOINT = "/usage-checker";
    private static final String CFU_ENDPOINT = "/flyway/cfu/api/v0/cfu";

    public static boolean isEnabled() {
        boolean flag = Boolean.parseBoolean(System.getenv("FLYWAY_REDGATE_UPDATE_CHECK"));
        return flag && usageChecker("flyway-cfu", VersionPrinter.getVersion());
    }

    public static void checkForVersionUpdates(String jdbcUrl) {
        String message = cfu(jdbcUrl);
        if (message.hasText()) {
            LOG.info(message);
        }
    }

    private static boolean usageChecker(String clientName, String clientVersion) {
        try {
            String url = PLATFORM_URL_ROOT + USAGE_CHECKER_ENDPOINT + String.format("?client_name=%s&client_version=%s", clientName, clientVersion);
            @Cleanup(value = "disconnect") HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return Boolean.parseBoolean(rd.readLine());
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static String cfu(String jdbcUrl) {
        try {
            @Cleanup(value = "disconnect") HttpsURLConnection connection = (HttpsURLConnection) new URL(PLATFORM_URL_ROOT + CFU_ENDPOINT).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = getJsonPayload(jdbcUrl).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try(BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line).append('\n');
                }
            }
            response.deleteCharAt(response.length()-1);

            return response.toString();
        } catch (Exception e) {
            LOG.debug("Failed to perform update check: " + e.getMessage());
            return "";
        }
    }

    private static String getRoot() {
        String root = System.getenv("REDGATE_PLATFORM_URL");
        return root.hasText() ? root : "https://www.redgate-platform.com";
    }

    private static String getJsonPayload(String jdbcUrl) throws Exception {
        String operatingSystem = System.getProperty("os.name");
        JsonObject json = new JsonObject();
        json.addProperty("currentVersion", VersionPrinter.getVersion());
        json.addProperty("operatingSystem", operatingSystem);
        json.addProperty("fingerprint", MachineFingerprintUtils.getFingerprint(operatingSystem, jdbcUrl, System.getProperty("user.dir")));
        json.addProperty("timeStamp", Instant.now().toString());
        return new Gson().toJson(json);
    }
}