/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
import lombok.*;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.extensibility.RgDomainChecker;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FingerprintUtils;
import org.flywaydb.core.internal.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod(StringUtils.class)
public class RedgateUpdateChecker {
    @RequiredArgsConstructor
    public static class Context {
        public final Map<String, String> config;
        public final List<String> verbs;
        public final String dbEngine;
        public final String dbVersion;
        public final PluginRegister pluginRegister;
    }

    private static final String PLATFORM_URL_ROOT = getRoot();
    private static final String USAGE_CHECKER_ENDPOINT = "/usage-checker";
    private static final String CFU_ENDPOINT = "/flyway/cfu/api/v0/cfu";

    public static boolean isEnabled() {
        return usageChecker("flyway-cfu", VersionPrinter.getVersion());
    }

    public static void checkForVersionUpdates(Context context) {
        String message = cfu(context);
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

    private static String cfu(Context context) {
        try {
            @Cleanup(value = "disconnect") HttpsURLConnection connection = (HttpsURLConnection) new URL(PLATFORM_URL_ROOT + CFU_ENDPOINT).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setConnectTimeout(1000);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = getJsonPayload(context).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line).append('\n');
                }
            }
            response.deleteCharAt(response.length() - 1);

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

    private static String getJsonPayload(Context context) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("currentVersion", VersionPrinter.getVersion());
        json.addProperty("operatingSystem", System.getProperty("os.name"));
        json.addProperty("fingerprint", FingerprintUtils.getFingerprint(context.config));
        json.addProperty("timeStamp", Instant.now().toString());
        json.addProperty("edition", VersionPrinter.EDITION.name());
        json.addProperty("verbs", Arrays.toString(context.verbs.toArray()));
        json.addProperty("engine", context.dbEngine);
        json.addProperty("version", context.dbVersion);
        json.addProperty("fromRedgate", Boolean.toString(isInRedgate(context)));
        return new Gson().toJson(json);
    }

    private static boolean isInRedgate(Context context) {
        if (System.getenv("RGDOMAIN") != null) {
            return true;
        }
        return context.pluginRegister
                .getPlugins(RgDomainChecker.class)
                .stream()
                .anyMatch(c -> c.isInDomain(context.config));
    }
}