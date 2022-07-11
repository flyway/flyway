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
package org.flywaydb.core.internal.util;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FingerprintUtils {
    public static String getFingerprint(Map<String, String> config) throws Exception {
        Map<String, String> filteredConfig = new HashMap<>();
        if (config.containsKey(ConfigUtils.LICENSE_KEY)) {
            filteredConfig.put(ConfigUtils.LICENSE_KEY, config.get(ConfigUtils.LICENSE_KEY));
        } else {
            filteredConfig.put(ConfigUtils.URL, DatabaseTypeRegister.redactJdbcUrl(config.get(ConfigUtils.URL)));
            filteredConfig.put(ConfigUtils.USER, config.get(ConfigUtils.USER));
        }

        return SHA512String(new Gson().toJson(filteredConfig));
    }

    private static String SHA512String(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] data = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}