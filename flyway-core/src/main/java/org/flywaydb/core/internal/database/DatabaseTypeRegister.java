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
import java.util.Arrays;
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
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.plugin.PluginRegister;

@CustomLog
public class DatabaseTypeRegister {
    private static final PluginRegister pluginRegister = new PluginRegister();

    // Order DatabaseType before native connector types, then by plugin priority (highest first)
    private static final List<GeneralDatabaseType> SORTED_DATABASE_TYPES = pluginRegister.getInstancesOf(GeneralDatabaseType.class)
        .stream()
        .sorted(Comparator.comparing((GeneralDatabaseType t) -> !(t instanceof DatabaseType))
            .thenComparing(Comparator.naturalOrder()))
        .toList();

    public static List<GeneralDatabaseType> getDatabaseTypes() {
        return new ArrayList<>(SORTED_DATABASE_TYPES);
    }

    public static List<GeneralDatabaseType> getDatabaseTypesForUrl(final String url, final Configuration configuration) {
        final List<GeneralDatabaseType> typesAcceptingUrl = SORTED_DATABASE_TYPES.stream().filter(type -> configuration == null
            || configuration.isCommunityDBSupportEnabled()
            || !(type instanceof CommunityDatabaseType)).filter(type -> acceptsUrl(type, url)).toList();

        if (typesAcceptingUrl.size() > 1) {
            final String typeNames = String.join(",", typesAcceptingUrl.stream().map(GeneralDatabaseType::getName).toList());

            LOG.debug("Multiple databases found that handle url '"
                + redactJdbcUrlWithKnownTypes(url, typesAcceptingUrl)
                + "': "
                + typeNames);
        }

        return typesAcceptingUrl;
    }

    public static String redactJdbcUrl(final String url) {
        return redactJdbcUrlWithKnownTypes(url, getDatabaseTypesForUrl(url, null));
    }

    public static String redactJdbcUrlWithKnownTypes(String url, final Collection<? extends GeneralDatabaseType> types) {
        if (types.isEmpty()) {
            final List<Pattern> dbPatterns = BaseDatabaseType.getDefaultJDBCCredentialsPatterns();
            url = redactJdbcUrl(url, dbPatterns);
        } else {
            for (final GeneralDatabaseType type : types) {
                final List<Pattern> dbPatterns = getUrlRedactionPatterns(type);
                url = redactJdbcUrl(url, dbPatterns);
            }
        }
        return url;
    }

    private static String redactJdbcUrl(final String url, final List<Pattern> dbPatterns) {
        String redactedUrl = url;
        if (dbPatterns != null && !dbPatterns.isEmpty()) {
            for (final Pattern dbPattern : dbPatterns) {
                redactedUrl = redactJdbcUrl(redactedUrl, dbPattern);
            }
        }
        return redactedUrl;
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
        final List<DatabaseType> sortedDatabaseTypesLegacyOnly = pluginRegister.getInstancesOf(DatabaseType.class)
            .stream()
            .sorted()
            .toList();
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        final String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        final String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        return sortedDatabaseTypesLegacyOnly.stream().filter(type -> configuration == null
            || configuration.isCommunityDBSupportEnabled()
            || !(type instanceof CommunityDatabaseType)).filter(type -> type.handlesDatabaseProductNameAndVersion(
            databaseProductName,
            databaseProductVersion,
            connection)).findFirst().orElseThrow(() -> new FlywayException("Unsupported Database: "
            + databaseProductName));
    }

    private static boolean acceptsUrl(final GeneralDatabaseType type, final String url) {
        if (type instanceof DatabaseType databaseType) {
            return databaseType.handlesJDBCUrl(url);
        }
        if (type instanceof NativeConnectorsDatabase nativeConnectorsDatabase) {
            return nativeConnectorsDatabase.supportsUrl(url).isSupported();
        }
        return false;
    }

    private static List<Pattern> getUrlRedactionPatterns(final GeneralDatabaseType type) {
        if (type instanceof DatabaseType databaseType) {
            return databaseType.getJDBCCredentialsPatterns();
        }
        if (type instanceof NativeConnectorsDatabase nativeDatabase) {
            return Arrays.asList(nativeDatabase.getUrlRedactionPatterns());
        }
        return List.of();
    }
}
