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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.io.Closeable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction for database-specific functionality.
 */
public abstract class Database<C extends Connection> implements Closeable {
    private static final Log LOG = LogFactory.getLog(Database.class);

    /**
     * The Flyway configuration.
     */
    protected final FlywayConfiguration configuration;

    /**
     * The JDBC metadata to use.
     */
    protected  final DatabaseMetaData jdbcMetaData;

    /**
     * The main connection to use.
     */
    protected final C mainConnection;

    /**
     * The connection to use for migrations.
     */
    private C migrationConnection;





    /**
     * The type to assign to a null value.
     */
    private final int nullType;

    /**
     * The major version of the database.
     */
    protected final int majorVersion;

    /**
     * The minor version of the database.
     */
    protected final int minorVersion;

    /**
     * Creates a new Database instance with this JdbcTemplate.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The main connection to use.
     * @param nullType      The type to assign to a null value.
     */
    public Database(FlywayConfiguration configuration, java.sql.Connection connection, int nullType



    ) {
        this.configuration = configuration;
        try {
            this.jdbcMetaData = connection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to get metadata for connection", e);
        }
        this.mainConnection = getConnection(connection, nullType



        );
        this.nullType = nullType;




        Pair<Integer, Integer> majorMinor = determineMajorAndMinorVersion();
        majorVersion = majorMinor.getLeft();
        minorVersion = majorMinor.getRight();
        ensureSupported();
    }

    /**
     * Retrieves a Flyway Connection for this JDBC connection.
     *
     * @param connection The JDBC connection to wrap.
     * @param nullType   The JDBC type to assign to a null value.
     * @return The Flyway Connection.
     */
    protected abstract C getConnection(java.sql.Connection connection, int nullType



    );

    /**
     * Ensures Flyway supports this version of this database.
     */
    protected abstract void ensureSupported();

    protected void recommendFlywayUpgrade(String database, String version) {
        LOG.warn("Flyway upgrade recommended: " + database + " " + version
                + " is newer than this version of Flyway and support has not been tested.");
    }

    /**
     * Creates a new SqlStatementBuilder for this specific database.
     *
     * @return The new SqlStatementBuilder.
     */
    public abstract SqlStatementBuilder createSqlStatementBuilder();

    /**
     * @return The default delimiter for this database.
     */
    public Delimiter getDefaultDelimiter() {
        return Delimiter.SEMICOLON;
    }

    /**
     * @return The name of the db. Used for loading db-specific scripts such as <code>createMetaDataTable.sql</code>.
     */
    public abstract String getDbName();

    /**
     * @return The current database user.
     */
    public final String getCurrentUser() {
        try {
            return doGetCurrentUser();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error retrieving the database user", e);
        }
    }

    protected String doGetCurrentUser() throws SQLException {
        return jdbcMetaData.getUserName();
    }

    /**
     * Checks whether DDL transactions are supported by this database.
     *
     * @return {@code true} if DDL transactions are supported, {@code false} if not.
     */
    public abstract boolean supportsDdlTransactions();













    /**
     * @return The representation of the value {@code true} in a boolean column.
     */
    public abstract String getBooleanTrue();

    /**
     * @return The representation of the value {@code false} in a boolean column.
     */
    public abstract String getBooleanFalse();

    /**
     * Quote these identifiers for use in sql queries. Multiple identifiers will be quoted and separated by a dot.
     *
     * @param identifiers The identifiers to quote.
     * @return The fully qualified quoted identifiers.
     */
    public final String quote(String... identifiers) {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (String identifier : identifiers) {
            if (!first) {
                result.append(".");
            }
            first = false;
            result.append(doQuote(identifier));
        }

        return result.toString();
    }

    /**
     * Quote this identifier for use in sql queries.
     *
     * @param identifier The identifier to quote.
     * @return The fully qualified quoted identifier.
     */
    protected abstract String doQuote(String identifier);

    /**
     * @return {@code true} if this database use a catalog to represent a schema. {@code false} if a schema is simply a schema.
     */
    public abstract boolean catalogIsSchema();

    /**
     * @return Whether to only use a single connection for both schema history table management and applying migrations.
     */
    public boolean useSingleConnection() {
        return false;
    }

    public DatabaseMetaData getJdbcMetaData() {
        return jdbcMetaData;
    }

    /**
     * @return The main connection, used to manipulate the schema history.
     */
    public final C getMainConnection() {
        return mainConnection;
    }

    /**
     * @return The migration connection, used to apply migrations.
     */
    public final C getMigrationConnection() {
        if (migrationConnection == null) {
            this.migrationConnection = useSingleConnection()
                    ? mainConnection
                    : getConnection(JdbcUtils.openConnection(configuration.getDataSource()), nullType



            );
        }
        return migrationConnection;
    }

    /**
     * Returns the major version number of the database.
     *
     * @return the major version number as int.
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Returns the minor version number of the database.
     *
     * @return the minor version number as int.
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * @return The major and minor version of the database.
     */
    protected Pair<Integer, Integer> determineMajorAndMinorVersion() {
        try {
            return Pair.of(jdbcMetaData.getDatabaseMajorVersion(), jdbcMetaData.getDatabaseMinorVersion());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the major version of the database", e);
        }
    }

    public final String getCreateScript(Table table) {
        String source = getRawCreateScript();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("schema", table.getSchema().getName());
        placeholders.put("table", table.getName());
        return new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);
    }

    protected String getRawCreateScript() {
        String resourceName = "org/flywaydb/core/internal/database/" + getDbName() + "/createMetaDataTable.sql";
        return new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
    }

    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public void close() {
        if (!useSingleConnection() && migrationConnection != null) {
            migrationConnection.close();
        }
        mainConnection.close();
    }









}