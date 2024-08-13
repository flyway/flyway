/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.internal.configuration.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.configuration.resolvers.ProvisionerMode;

@Getter
@Setter
public class ResolvedEnvironment {
    private String url;
    private String user;
    private String password;
    private String driver;
    private List<String> schemas;
    private String token;
    private Integer connectRetries;
    private Integer connectRetriesInterval;
    private String initSql;
    private Map<String, String> jdbcProperties;
    private ProvisionerMode provisionerMode;

    public EnvironmentModel toEnvironmentModel() {
        EnvironmentModel result = new EnvironmentModel();
        result.setUrl(url);
        result.setPassword(password);
        result.setUser(user);
        result.setDriver(driver);
        result.setSchemas(schemas);
        result.setConnectRetries(connectRetries);
        result.setConnectRetriesInterval(connectRetriesInterval);
        result.setInitSql(initSql);
        result.setJdbcProperties(jdbcProperties);
        result.setResolvers(Map.of());
        result.setProvisioner("none");
        return result;
    }
}
