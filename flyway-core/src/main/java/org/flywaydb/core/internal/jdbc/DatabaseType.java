/*
 * Copyright 2010-2018 Boxfuse GmbH
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
public enum DatabaseType {
    /**
     * CockroachDB.
     */
    COCKROACHDB(Types.NULL),

    /**
     * DB2.
     */
    DB2(Types.VARCHAR),

    /**
     * Derby.
     */
    DERBY(Types.VARCHAR),

    /**
     * H2.
     */
    H2(Types.VARCHAR),

    /**
     * HSQLDB.
     */
    HSQLDB(Types.VARCHAR),

    /**
     * Informix.
     */
    INFORMIX(Types.VARCHAR),

    /**
     * For regular MySQL, MariaDB and Google Cloud SQL.
     */
    MYSQL(Types.VARCHAR),

    /**
     * Oracle.
     */
    ORACLE(Types.VARCHAR),

    /**
     * PostgreSQL.
     */
    POSTGRESQL(Types.NULL),

    /**
     * Redshift.
     */
    REDSHIFT(Types.VARCHAR),

    /**
     * SQLite.
     */
    SQLITE(Types.VARCHAR),

    /**
     * SQL Server.
     */
    SQLSERVER(Types.VARCHAR),

    /**
     * Sybase ASE, using the legacy jTDS driver.
     */
    SYBASEASE_JTDS(Types.NULL),

    /**
     * Sybase ASE, using the jConnect driver.
     */
    SYBASEASE_JCONNECT(Types.VARCHAR),

    /**
     * SAP HANA.
     */
    SAPHANA(Types.VARCHAR);

    private final int nullType;

    DatabaseType(int nullType) {
        this.nullType = nullType;
    }

    public static DatabaseType fromJdbcConnection(Connection connection) {
        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String postgreSQLVersion = databaseProductName.startsWith("PostgreSQL") ? getPostgreSQLVersion(connection) : "";

        return fromDatabaseProductNameAndPostgreSQLVersion(databaseProductName, postgreSQLVersion);
    }

    private static DatabaseType fromDatabaseProductNameAndPostgreSQLVersion(String databaseProductName, String postgreSQLVersion) {
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
        if (databaseProductName.contains("MySQL")) {
            // Google Cloud SQL returns different names depending on the environment and the SDK version.
            //   ex.: Google SQL Service/MySQL
            return MYSQL;
        }
        if (databaseProductName.startsWith("Oracle")) {
            return ORACLE;
        }
        if (databaseProductName.startsWith("PostgreSQL 8") && postgreSQLVersion.contains("Redshift")) {
            return REDSHIFT;
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            if (postgreSQLVersion.contains("CockroachDB")) {
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
        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the PostgreSQL version string for this connection.
     *
     * @param connection The connection to use.
     * @return The PostgreSQL version string.
     */
    private static String getPostgreSQLVersion(Connection connection) {
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
     * @return The JDBC type used to represent {@code null} in prepared statements.
     */
    public int getNullType() {
        return nullType;
    }
}