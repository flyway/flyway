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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The various types of databases Flyway supports.
 */
@SuppressWarnings("SqlDialectInspection")
public enum DatabaseType {
    COCKROACHDB("CockroachDB", Types.NULL, false),
    DB2("DB2", Types.VARCHAR, true),



    DERBY("Derby", Types.VARCHAR, true),
    FIREBIRD("Firebird", Types.NULL, true), // TODO does it support read only transactions?
    H2("H2", Types.VARCHAR, true),
    HSQLDB("HSQLDB", Types.VARCHAR, true),
    INFORMIX("Informix", Types.VARCHAR, true),
    MARIADB("MariaDB", Types.VARCHAR, true),
    MYSQL("MySQL", Types.VARCHAR, true),
    ORACLE("Oracle", Types.VARCHAR, true),
    POSTGRESQL("PostgreSQL", Types.NULL, true),
    REDSHIFT("Redshift", Types.VARCHAR, true),
    SQLITE("SQLite", Types.VARCHAR, false),
    SQLSERVER("SQL Server", Types.VARCHAR, true),
    SYBASEASE_JTDS("Sybase ASE", Types.NULL, true),
    SYBASEASE_JCONNECT("Sybase ASE", Types.VARCHAR, true),
    SAPHANA("SAP HANA", Types.VARCHAR, true),
    SNOWFLAKE("Snowflake", Types.VARCHAR, false);

    private final String name;

    private final int nullType;

    private final boolean supportsReadOnlyTransactions;

    DatabaseType(String name, int nullType, boolean supportsReadOnlyTransactions) {
        this.name = name;
        this.nullType = nullType;
        this.supportsReadOnlyTransactions = supportsReadOnlyTransactions;
    }

    public static DatabaseType fromJdbcConnection(Connection connection) {
        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        return fromDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection);
    }

    private static DatabaseType fromDatabaseProductNameAndVersion(String databaseProductName,
                                                                  String databaseProductVersion,
                                                                  Connection connection) {
        if (databaseProductName.startsWith("Apache Derby")) {
            return DERBY;
        }
        if (databaseProductName.startsWith("SQLite")) {
            return SQLITE;
        }
        if (databaseProductName.startsWith("H2")) {
            return H2;
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
            return HSQLDB;
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            return SQLSERVER;
        }

        // #2289: MariaDB JDBC driver 2.4.0 and newer report MariaDB as "MariaDB"
        if (databaseProductName.startsWith("MariaDB")
                // Older versions of the driver report MariaDB as "MySQL"
                || (databaseProductName.contains("MySQL") && databaseProductVersion.contains("MariaDB"))
                // Azure Database For MariaDB reports as "MySQL"
                || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("MariaDB"))) {
            return MARIADB;
        }

        if (databaseProductName.contains("MySQL")) {
            // Google Cloud SQL returns different names depending on the environment and the SDK version.
            //   ex.: Google SQL Service/MySQL
            return MYSQL;
        }
        if (databaseProductName.startsWith("Oracle")) {
            return ORACLE;
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            String selectVersionQueryOutput = getSelectVersionOutput(connection);
            if (databaseProductName.startsWith("PostgreSQL 8") && selectVersionQueryOutput.contains("Redshift")) {
                return REDSHIFT;
            }
            if (selectVersionQueryOutput.contains("CockroachDB")) {
                return COCKROACHDB;
            }
            return POSTGRESQL;
        }
        if (databaseProductName.startsWith("DB2")) {





            return DB2;
        }
        if (databaseProductName.startsWith("ASE")) {
            return SYBASEASE_JTDS;
        }
        if (databaseProductName.startsWith("Adaptive Server Enterprise")) {
            return SYBASEASE_JCONNECT;
        }
        if (databaseProductName.startsWith("HDB")) {
            return SAPHANA;
        }
        if (databaseProductName.startsWith("Informix")) {
            return INFORMIX;
        }
        if (databaseProductName.startsWith("Firebird")) {
            return FIREBIRD;
        }
        if (databaseProductName.startsWith("Snowflake")) {
            return SNOWFLAKE;
        }
        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the version string for this connection as described by SELECT VERSION(), which may differ
     * from the connection metadata.
     *
     * @param connection The connection to use.
     * @return The version string.
     */
    public static String getSelectVersionOutput(Connection connection) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = connection.prepareStatement("SELECT version()");
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            return "";
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * @return The human-readable name for this database.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The JDBC type used to represent {@code null} in prepared statements.
     */
    public int getNullType() {
        return nullType;
    }

    @Override
    public String toString() {
        return name;
    }











}