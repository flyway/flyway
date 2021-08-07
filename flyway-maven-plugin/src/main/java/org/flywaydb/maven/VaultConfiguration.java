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
package org.flywaydb.maven;

import org.flywaydb.core.internal.configuration.ConfigUtils;

import java.util.Map;

public class VaultConfiguration {
    public static final String VAULT_URL = "flyway.vault.url";
    public static final String VAULT_TOKEN = "flyway.vault.token";
    public static final String VAULT_SECRETS = "flyway.vault.secrets";

    private String vaultUrl;
    private String vaultToken;
    private String[] vaultSecrets;

    public void extract(Map<String, String> conf) {
        ConfigUtils.putIfSet(conf, VAULT_URL, vaultUrl);
        ConfigUtils.putIfSet(conf, VAULT_TOKEN, vaultToken);
        ConfigUtils.putArrayIfSet(conf, VAULT_SECRETS, vaultSecrets);
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public void setVaultUrl(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }

    public String getVaultToken() {
        return vaultToken;
    }

    public void setVaultToken(String vaultToken) {
        this.vaultToken = vaultToken;
    }

    public String[] getVaultSecrets() {
        return vaultSecrets;
    }

    public void setVaultSecrets(String[] vaultSecrets) {
        this.vaultSecrets = vaultSecrets;
    }
}