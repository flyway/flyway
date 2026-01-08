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
package org.flywaydb.core.internal.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.CommunityDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

@CustomLog
public class DatabaseTypeRegister {

    private static final List<DatabaseType> SORTED_DATABASE_TYPES = new PluginRegister().getInstancesOf(DatabaseType.class)
        .stream()
        .sorted()
        .toList();

    public static List<DatabaseType> getDatabaseTypes() {
        return new ArrayList<>(SORTED_DATABASE_TYPES);
    }

    public static DatabaseType getDatabaseTypeForUrl(final String url, final Configuration configuration) {
        final ArrayList<DatabaseType> typesAcceptingUrl = new ArrayList<>(getDatabaseTypesForUrl(url, configuration));

        if (typesAcceptingUrl.isEmpty()) {
            throw new FlywayException("No Flyway database plugin found to handle " + redactJdbcUrl(url, typesAcceptingUrl)
                + ". See " + FlywayDbWebsiteLinks.DATABASE_TROUBLESHOOTING + " for troubleshooting");
        } else {
            typesAcceptingUrl.sort(Comparator.comparing(DatabaseType::getPriority));
            return typesAcceptingUrl.get(0);
        }
    }

    public static List<DatabaseType> getDatabaseTypesForUrl(final String url, final Configuration configuration) {
        final List<DatabaseType> typesAcceptingUrl = SORTED_DATABASE_TYPES.stream().filter(type -> configuration == null
            || configuration.isCommunityDBSupportEnabled()
            || !(type instanceof CommunityDatabaseType)).filter(type -> type.handlesJDBCUrl(url)).toList();

        if (typesAcceptingUrl.size() > 1) {
            final String typeNames = String.join(",", typesAcceptingUrl.stream().map(DatabaseType::getName).toList());

            LOG.debug("Multiple databases found that handle url '"
                + redactJdbcUrl(url, typesAcceptingUrl)
                + "': "
                + typeNames);
        }

        return typesAcceptingUrl;
    }

    public static DatabaseType getDatabaseTypeForEngineName(final String engineName,
        final Configuration configuration) {
        return SORTED_DATABASE_TYPES.stream().filter(type -> configuration == null
            || configuration.isCommunityDBSupportEnabled()
            || !(type instanceof CommunityDatabaseType)).filter(type -> type.getSupportedEngines()
            .stream()
            .anyMatch(engineName::equalsIgnoreCase)).findFirst().orElseThrow(() -> new FlywayException(
            "No Flyway database plugin found to handle " + engineName + " engine"
                + ". See " + FlywayDbWebsiteLinks.DATABASE_TROUBLESHOOTING + " for troubleshooting"));
    }

    public static String redactJdbcUrl(final String url) {
        return redactJdbcUrl(url, (Configuration) null);
    }

    public static String redactJdbcUrl(final String url, final Configuration configuration) {
        final List<DatabaseType> types = getDatabaseTypesForUrl(url, configuration);
        return redactJdbcUrl(url, types);
    }

    public static String redactJdbcUrl(String url, final Collection<? extends DatabaseType> types) {
        if (types.isEmpty()) {
            url = redactJdbcUrl(url, BaseDatabaseType.getDefaultJDBCCredentialsPattern());
        } else {
            for (final DatabaseType type : types) {
                final List<Pattern> dbPatterns = type.getJDBCCredentialsPatterns();
                if (dbPatterns != null && !dbPatterns.isEmpty()) {
                    for (final Pattern dbPattern : dbPatterns) {
                        url = redactJdbcUrl(url, dbPattern);
                    }
                }
            }
        }
        return url;
    }

    private static String redactJdbcUrl(final String url, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(url);
        final String replacement = "********";
        final StringBuilder redactedJdbcUrlBuilder = new StringBuilder();
        int lastEndIndex = 0;

        while (matcher.find()) {
            redactedJdbcUrlBuilder.append(url, lastEndIndex, matcher.start(1));
            redactedJdbcUrlBuilder.append(replacement);
            lastEndIndex = matcher.end(1);
        }
        redactedJdbcUrlBuilder.append(url.substring(lastEndIndex));
        return redactedJdbcUrlBuilder.toString();
    }

    public static DatabaseType getDatabaseTypeForConnection(final Connection connection,
        final Configuration configuration) {
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        final String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        final String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        return SORTED_DATABASE_TYPES.stream().filter(type -> configuration == null
            || configuration.isCommunityDBSupportEnabled()
            || !(type instanceof CommunityDatabaseType)).filter(type -> type.handlesDatabaseProductNameAndVersion(
            databaseProductName,
            databaseProductVersion,
            connection)).findFirst().orElseThrow(() -> new FlywayException("Unsupported Database: "
            + databaseProductName));
    }
}
