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
package org.flywaydb.core.internal.configuration.resolvers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.flywaydb.core.api.FlywayException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HashicorpVaultResolver implements PropertyResolver {
    @Override
    public String getName() {
        return "vault";
    }

    @Override
    public String resolve(String key, PropertyResolverContext context) {
        String url = context.resolveProperty("vault", "url");
        String token = context.resolveProperty("vault", "token");
        String engineName = context.resolveProperty("vault", "engineName");
        String engineVersion = context.resolveProperty("vault", "engineVersion");

        if (!url.endsWith("/")) {
            url += "/";
        }

        url = url + engineName + "/";

        if (engineVersion.equalsIgnoreCase("V2")) {
            url = url + "data/";
        }

        try {
            return readSecretWithPath(url, token, key);
        } catch (Exception e) {
            throw new FlywayException("Failed to resolve", e);
        }
    }

    private String readSecretWithPath(String url, String token, String secret) throws Exception {
        if (!secret.startsWith("/")) {
            secret = "/" + secret;
        }
        String path = secret.substring(0, secret.lastIndexOf("/"));
        String secretName = secret.substring(secret.lastIndexOf("/") + 1);

        return readSecret(url + path, token, secretName);
    }

    private String readSecret(String url, String token, String secret) throws Exception {
        if (url.startsWith("https")) {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            return readSecret(conn, token, secret);
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return readSecret(conn, token, secret);
    }

    private String readSecret(URLConnection conn, String token, String secret) throws Exception {
        JsonObject secretResponse = new Gson().fromJson(getSecretFromVault(conn, token), JsonObject.class);

        JsonObject secretObj;
        if (isKV1Response(secretResponse)) {
            secretObj = secretResponse.getAsJsonObject("data");
        } else if (isKV2Response(secretResponse)) {
            secretObj = secretResponse.getAsJsonObject("data").getAsJsonObject("data");
        } else {
            throw new FlywayException("Vault response unaccepted. Expected a KV1 or KV2 secret, but was: " + secretResponse);
        }

        if (!secretObj.has(secret)) {
            throw new FlywayException("'" + secret + "' is not a valid Vault secret");
        }

        return secretObj.get(secret).getAsString();
    }

    private String getSecretFromVault(URLConnection conn, String token) throws Exception {
        conn.setRequestProperty("X-Vault-Token", token);

        StringBuilder rawSecret = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                rawSecret.append(line);
            }
        } catch (FileNotFoundException e) {
            throw new FlywayException("Vault secret location '" + conn.getURL() + "' could not be found. Ensure the path to your secret is correct.");
        }
        return rawSecret.toString();
    }

    /**
     * A KV1 response has one child object 'data', which has a child element that is the secret
     */
    private boolean isKV1Response(JsonObject secretResponse) {
        JsonObject secretObj;

        try {
            secretObj = secretResponse.getAsJsonObject("data");
        } catch (ClassCastException e) {
            return false;
        }

        try {
            if (secretObj.has("data")) {
                secretObj.getAsJsonObject("data");
            } else {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return true;
        }
    }

    /**
     * A KV2 response has two child objects called 'data', then a child element that is the secret
     */
    private boolean isKV2Response(JsonObject secretResponse) {
        try {
            secretResponse.getAsJsonObject("data").getAsJsonObject("data");
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}