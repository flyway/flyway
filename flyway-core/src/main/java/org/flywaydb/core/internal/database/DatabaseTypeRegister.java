/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;

import org.flywaydb.core.internal.database.yugabytedb.YugabyteDBDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseTypeRegister {
    private static final Log LOG = LogFactory.getLog(DatabaseTypeRegister.class);
    public static ClassLoader classLoader = new DatabaseTypeRegister().getClass().getClassLoader();

    private static final List<DatabaseType> registeredDatabaseTypes = new ArrayList<>();
    private static boolean hasRegisteredDatabaseTypes = false;

    private static void registerDatabaseTypes() {
        synchronized (registeredDatabaseTypes) {
            if (hasRegisteredDatabaseTypes) {
                return;
            }

            registeredDatabaseTypes.clear();






            ServiceLoader<DatabaseType> loader = ServiceLoader.load(DatabaseType.class, classLoader);
            for (DatabaseType dt : loader) {
                registeredDatabaseTypes.add(dt);
            }
            registeredDatabaseTypes.add(new YugabyteDBDatabaseType());

            // Sort by preference order
            Collections.sort(registeredDatabaseTypes);

            hasRegisteredDatabaseTypes = true;
        }
    }

    public static DatabaseType getDatabaseTypeForUrl(String url) {
        List<DatabaseType> typesAcceptingUrl = getDatabaseTypesForUrl(url);

        if (typesAcceptingUrl.size() > 0) {
            if (typesAcceptingUrl.size() > 1) {
                StringBuilder builder = new StringBuilder();
                for (DatabaseType type : typesAcceptingUrl) {
                    if (builder.length() > 0) builder.append(", ");
                    builder.append(type.getName());
                }

                LOG.debug("Multiple databases found that handle url '" + redactJdbcUrl(url) + "': " + builder);
            }
            return typesAcceptingUrl.get(0);
        } else {
            throw new FlywayException("No database found to handle " + redactJdbcUrl(url));
        }
    }

    private static List<DatabaseType> getDatabaseTypesForUrl(String url) {
        if (!hasRegisteredDatabaseTypes) {
            registerDatabaseTypes();
        }

        List<DatabaseType> typesAcceptingUrl = new ArrayList<>();

        for (DatabaseType type : registeredDatabaseTypes) {
            if (type.handlesJDBCUrl(url)) {
                typesAcceptingUrl.add(type);
            }
        }

        return typesAcceptingUrl;
    }

    public static String redactJdbcUrl(String url) {
        List<DatabaseType> types = getDatabaseTypesForUrl(url);
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
            return url.replace(password, StringUtils.trimOrPad("", password.length(), '*'));
        }
        return url;
    }

    public static DatabaseType getDatabaseTypeForConnection(Connection connection) {
        if (!hasRegisteredDatabaseTypes) {
            registerDatabaseTypes();
        }

        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        for (DatabaseType type : registeredDatabaseTypes) {
            if (type.handlesDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection)) {
                return type;
            }
        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }
}