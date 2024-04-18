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
package org.flywaydb.core.internal.database;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.CommunityDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.plugin.PluginRegister;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CustomLog
public class DatabaseTypeRegister {

    private static final List<DatabaseType> SORTED_DATABASE_TYPES = new PluginRegister().getPlugins(DatabaseType.class).stream().sorted().collect(Collectors.toList());

    public static DatabaseType getDatabaseTypeForUrl(String url, Configuration configuration) {
        List<DatabaseType> typesAcceptingUrl =  getDatabaseTypesForUrl(url, configuration);

        if (typesAcceptingUrl.isEmpty()) {
            throw new FlywayException("No database found to handle " + redactJdbcUrl(url, typesAcceptingUrl));
        } else {
            return typesAcceptingUrl.get(0);
        }
    }

    public static List<DatabaseType> getDatabaseTypesForUrl(String url, Configuration configuration) {
        List<DatabaseType> typesAcceptingUrl =  SORTED_DATABASE_TYPES.stream()
            .filter(type -> configuration == null ||
                configuration.isCommunityDBSupportEnabled() ||
                !(type instanceof CommunityDatabaseType))
            .filter(type -> type.handlesJDBCUrl(url))
            .toList();

        if (typesAcceptingUrl.size() > 1) {
            final String typeNames = String.join(",", typesAcceptingUrl.stream().map(DatabaseType::getName).toList());

            LOG.debug("Multiple databases found that handle url '" + redactJdbcUrl(url, typesAcceptingUrl) + "': " + typeNames);
        }

        return typesAcceptingUrl;
    }

    public static DatabaseType getDatabaseTypeForEngineName(String engineName, Configuration configuration) {
        return SORTED_DATABASE_TYPES.stream()
            .filter(type -> configuration == null ||
                configuration.isCommunityDBSupportEnabled() ||
                !(type instanceof CommunityDatabaseType))
            .filter(type -> type.getSupportedEngines().stream().anyMatch(engineName::equalsIgnoreCase))
            .findFirst()
            .orElseThrow(() -> new FlywayException("No database found to handle " + engineName + " engine"));
    }

    public static String redactJdbcUrl(String url) {
        return redactJdbcUrl(url, (Configuration) null);
    }

    public static String redactJdbcUrl(String url, final Configuration configuration) {
        List<DatabaseType> types = getDatabaseTypesForUrl(url, configuration);
        return redactJdbcUrl(url, types);
    }

    public static String redactJdbcUrl(String url, final List<DatabaseType> types) {
        if (types.isEmpty()) {
            url = redactJdbcUrl(url, BaseDatabaseType.getDefaultJDBCCredentialsPattern());
        } else {
            for (DatabaseType type : types) {
                Pattern dbPattern = type.getJDBCCredentialsPattern();
                if (dbPattern != null) {
                    url = redactJdbcUrl(url, dbPattern);
                }
            }
        }
        return url;
    }

    private static String redactJdbcUrl(String url, Pattern pattern) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String password = matcher.group(1);
            return url.replace(password, "********");
        }
        return url;
    }

    public static DatabaseType getDatabaseTypeForConnection(Connection connection, Configuration configuration) {
        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        return SORTED_DATABASE_TYPES.stream()
            .filter(type -> configuration == null ||
                configuration.isCommunityDBSupportEnabled() ||
                !(type instanceof CommunityDatabaseType))
            .filter(type -> type.handlesDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection))
            .findFirst()
            .orElseThrow(() -> new FlywayException("Unsupported Database: " + databaseProductName));
    }
}
