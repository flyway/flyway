/*
 * Copyright 2010-2020 Redgate Software Ltd
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
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBDatabaseType;
import org.flywaydb.core.internal.database.db2.DB2DatabaseType;
import org.flywaydb.core.internal.database.derby.DerbyDatabaseType;

import org.flywaydb.core.internal.database.firebird.FirebirdDatabaseType;
import org.flywaydb.core.internal.database.h2.H2DatabaseType;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBDatabaseType;
import org.flywaydb.core.internal.database.informix.InformixDatabaseType;
import org.flywaydb.core.internal.database.mysql.MariaDBDatabaseType;
import org.flywaydb.core.internal.database.mysql.MySQLDatabaseType;
import org.flywaydb.core.internal.database.oracle.OracleDatabaseType;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType;
import org.flywaydb.core.internal.database.redshift.RedshiftDatabaseType;
import org.flywaydb.core.internal.database.saphana.SAPHANADatabaseType;
import org.flywaydb.core.internal.database.snowflake.SnowflakeDatabaseType;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType;
import org.flywaydb.core.internal.database.sqlserver.SQLServerDatabaseType;
import org.flywaydb.core.internal.database.sybasease.SybaseASEJConnectDatabaseType;
import org.flywaydb.core.internal.database.sybasease.SybaseASEJTDSDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTypeRegister {

    private static final Log LOG = LogFactory.getLog(DatabaseTypeRegister.class);

    private static final List<DatabaseType> registeredDatabaseTypes = new ArrayList<>();
    private static boolean hasRegisteredDatabaseTypes = false;

    public static void registerForDatabaseTypes() {
        synchronized (registeredDatabaseTypes) {
            if (hasRegisteredDatabaseTypes) {
                return;
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            registeredDatabaseTypes.clear();





            registeredDatabaseTypes.add(new CockroachDBDatabaseType(classLoader));
            registeredDatabaseTypes.add(new DB2DatabaseType(classLoader));
            registeredDatabaseTypes.add(new DerbyDatabaseType(classLoader));
            registeredDatabaseTypes.add(new FirebirdDatabaseType(classLoader));
            registeredDatabaseTypes.add(new H2DatabaseType(classLoader));
            registeredDatabaseTypes.add(new HSQLDBDatabaseType(classLoader));
            registeredDatabaseTypes.add(new InformixDatabaseType(classLoader));
            registeredDatabaseTypes.add(new MariaDBDatabaseType(classLoader));
            registeredDatabaseTypes.add(new MySQLDatabaseType(classLoader));
            registeredDatabaseTypes.add(new OracleDatabaseType(classLoader));
            registeredDatabaseTypes.add(new PostgreSQLDatabaseType(classLoader));
            registeredDatabaseTypes.add(new RedshiftDatabaseType(classLoader));
            registeredDatabaseTypes.add(new SAPHANADatabaseType(classLoader));
            registeredDatabaseTypes.add(new SnowflakeDatabaseType(classLoader));
            registeredDatabaseTypes.add(new SQLiteDatabaseType(classLoader));
            registeredDatabaseTypes.add(new SQLServerDatabaseType(classLoader));
            registeredDatabaseTypes.add(new SybaseASEJTDSDatabaseType(classLoader));
            registeredDatabaseTypes.add(new SybaseASEJConnectDatabaseType(classLoader));

            hasRegisteredDatabaseTypes = true;
        }
    }

    public static DatabaseType getDatabaseTypeForUrl(String url) {
        if (!hasRegisteredDatabaseTypes) {
            registerForDatabaseTypes();
        }

        List<DatabaseType> typesAcceptingUrl = new ArrayList<>();

        for (int i = 0; i < registeredDatabaseTypes.size(); i++) {
            DatabaseType type = registeredDatabaseTypes.get(i);
            if (type.handlesJDBCUrl(url)) {
                typesAcceptingUrl.add(type);
            }
        }

        if (typesAcceptingUrl.size() > 0) {
            if (typesAcceptingUrl.size() > 1) {
                StringBuilder builder = new StringBuilder();
                for (DatabaseType type : typesAcceptingUrl) {
                    if (builder.length() > 0) builder.append(", ");
                    builder.append(type.getName());
                }

                LOG.info("Multiple databases found that handle url '" + url + "'. " + builder);
            }

            return typesAcceptingUrl.get(0);
        } else {
            throw new FlywayException("No database found to handle " + url);
        }
    }

    public static DatabaseType getDatabaseTypeForConnection(Connection connection) {
        if (!hasRegisteredDatabaseTypes) {
            registerForDatabaseTypes();
        }

        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        for (int i = 0; i < registeredDatabaseTypes.size(); i++) {
            DatabaseType type = registeredDatabaseTypes.get(i);
            if (type.handlesDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection)) {
                return type;
            }
        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }
}