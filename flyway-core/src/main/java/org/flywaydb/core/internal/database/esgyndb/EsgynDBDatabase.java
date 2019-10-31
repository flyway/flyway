/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.esgyndb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * EsgynDB database.
 */
public class EsgynDBDatabase extends Database<EsgynDBConnection> {
    /**
     * Creates a new Database instance.
     *
     * @param configuration The Flyway configuration.
     */
    public EsgynDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory
    ) {
        super(configuration, jdbcConnectionFactory
        );
    }

    @Override
    protected EsgynDBConnection doGetConnection(Connection connection) {
        return new EsgynDBConnection(this, connection);
    }

    /**
     * Ensures Flyway supports this version of this database.
     */
    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("2.0");
    /**
     * Ensures this database it at least as recent as this version otherwise suggest upgrade to this higher edition of
     * Flyway.
     *
     * @param oldestSupportedVersionInThisEdition The oldest supported version of the database by this edition of Flyway.
     * @param editionWhereStillSupported          The edition of Flyway that still supports this version of the database,
     *                                            in case it's too old.
     */
        recommendFlywayUpgradeIfNecessary("2.7");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL,\n" +
				" PRIMARY KEY (\"installed_rank\")\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
	}

    /**
     * @return The current database user.
     */
    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("VALUES(CURRENT_USER)");
    }

    /**
     * Checks whether DDL transactions are supported by this database.
     *
     * @return {@code true} if DDL transactions are supported, {@code false} if not.
     */
    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    /**
     * @return {@code true} if this database supports changing a connection's current schema. {@code false if not}.
     */
    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    /**
     * @return The representation of the value {@code true} in a boolean column.
     */
    @Override
    public String getBooleanTrue() {
        return "true";
    }

    /**
     * @return The representation of the value {@code false} in a boolean column.
     */
    @Override
    public String getBooleanFalse() {
        return "true";
    }

    /**
     * Quote this identifier for use in sql queries.
     *
     * @param identifier The identifier to quote.
     * @return The fully qualified quoted identifier.
     */
    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    /**
     * @return {@code true} if this database use a catalog to represent a schema. {@code false} if a schema is simply a schema.
     */
    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    /**
     * @return Whether to only use a single connection for both schema history table management and applying migrations.
     */
    @Override
    public boolean useSingleConnection() {
        return false;
    }
}