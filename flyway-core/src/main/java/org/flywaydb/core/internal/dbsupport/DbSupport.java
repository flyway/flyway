/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Abstraction for database-specific functionality.
 */
public abstract class DbSupport {
    private static final Log LOG = LogFactory.getLog(DbSupport.class);

    /**
     * The JDBC template available for use.
     */
    protected final JdbcTemplate jdbcTemplate;

    /**
     * The original schema of the connection that should be restored later.
     */
    protected final String originalSchema;

    /**
     * The major version of the database.
     */
    protected final int majorVersion;

    /**
     * The minor version of the database.
     */
    protected final int minorVersion;

    /**
     * Creates a new DbSupport instance with this JdbcTemplate.
     *
     * @param jdbcTemplate The JDBC template to use.
     */
    public DbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        originalSchema = jdbcTemplate.getConnection() == null ? null : getCurrentSchemaName();
        Pair<Integer, Integer> majorMinor =
                jdbcTemplate.getConnection() == null ? Pair.of(0, 0) : determineMajorAndMinorVersion();
        majorVersion = majorMinor.getLeft();
        minorVersion = majorMinor.getRight();
        if (jdbcTemplate.getConnection() != null) {
            ensureSupported();
        }
    }

    /**
     * Ensures Flyway supports this version of this database.
     */
    protected abstract void ensureSupported();

    protected void recommendFlywayUpgrade(String database, String version) {
        LOG.warn("Flyway upgrade recommended: " + database + " " + version
                + " is newer than this version of Flyway and support has not been tested.");
    }

    /**
     * @return The DB-specific JdbcTemplate instance.
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Retrieves the schema with this name in the database.
     *
     * @param name The name of the schema.
     * @return The schema.
     */
    public abstract Schema getSchema(String name);

    /**
     * Creates a new SqlStatementBuilder for this specific database.
     *
     * @return The new SqlStatementBuilder.
     */
    public abstract SqlStatementBuilder createSqlStatementBuilder();

    /**
     * @return The name of the db. Used for loading db-specific scripts such as <code>createMetaDataTable.sql</code>.
     */
    public abstract String getDbName();

    /**
     * Retrieves the original schema of the connection.
     *
     * @return The original schema for this connection.
     */
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        return getSchema(originalSchema);
    }

    /**
     * Retrieves the current schema.
     *
     * @return The current schema for this connection.
     */
    public String getCurrentSchemaName() {
        try {
            return doGetCurrentSchemaName();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the current schema for the connection", e);
        }
    }

    /**
     * Retrieves the current schema.
     *
     * @return The current schema for this connection.
     * @throws SQLException when the current schema could not be retrieved.
     */
    protected abstract String doGetCurrentSchemaName() throws SQLException;

    /**
     * Sets the current schema to this schema.
     *
     * @param schema The new current schema for this connection.
     */
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            doChangeCurrentSchemaTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    /**
     * Restores the current schema of the connection to its original setting.
     */
    public void restoreCurrentSchema() {
        try {
            doChangeCurrentSchemaTo(originalSchema);
        } catch (SQLException e) {
            throw new FlywaySqlException("Error restoring current schema to its original setting", e);
        }
    }

    /**
     * Sets the current schema to this schema.
     *
     * @param schema The new current schema for this connection.
     * @throws SQLException when the current schema could not be set.
     */
    protected abstract void doChangeCurrentSchemaTo(String schema) throws SQLException;

    /**
     * @return The database function that returns the current user.
     */
    public abstract String getCurrentUserFunction();

    /**
     * Checks whether ddl transactions are supported by this database.
     *
     * @return {@code true} if ddl transactions are supported, {@code false} if not.
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
    public String quote(String... identifiers) {
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
     * Locks this table and executes this callable.
     *
     * @param table    The table to lock.
     * @param callable The callable to execute.
     * @return The result of the callable.
     */
    public <T> T lock(final Table table, final Callable<T> callable) {
        return new TransactionTemplate(jdbcTemplate.getConnection(), supportsDdlTransactions()).execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                table.lock();
                return callable.call();
            }
        });
    }

    /**
     * @return Whether to only use a single connection for both metadata table management and applying migrations.
     */
    public boolean useSingleConnection() {
        return false;
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
            DatabaseMetaData metaData = jdbcTemplate.getMetaData();
            return Pair.of(metaData.getDatabaseMajorVersion(), metaData.getDatabaseMinorVersion());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the major version of the database", e);
        }
    }

    public String getCreateScript() {
        String resourceName = "org/flywaydb/core/internal/dbsupport/" + getDbName() + "/createMetaDataTable.sql";
        return new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
    }
}
