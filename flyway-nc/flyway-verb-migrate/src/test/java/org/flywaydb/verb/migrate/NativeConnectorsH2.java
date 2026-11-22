/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.database.h2.H2Parser;
import org.flywaydb.core.internal.nc.DatabaseSupport;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.NativeConnectorsJdbc;

/**
 * Test-only H2 native connector. The product does not ship one, so the native migrate path cannot be
 * exercised through H2 unless this plugin is on the classpath.
 */
public class NativeConnectorsH2 extends NativeConnectorsJdbc {
    static final AtomicInteger initializations = new AtomicInteger();

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        initializations.incrementAndGet();
        super.initialize(environment, configuration);
    }

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url != null && url.startsWith("jdbc:h2:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public String getDatabaseType() {
        return "H2";
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return H2Parser::new;
    }

    @Override
    public boolean handlesProductName(final Connection connection, final String databaseProductName) {
        return databaseProductName.startsWith("H2");
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        return count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '"
            + getCurrentSchema()
            + "' AND UPPER(TABLE_NAME) = '"
            + tableName.toUpperCase()
            + "'") > 0;
    }

    @Override
    public void createSchemaHistoryTable(final Configuration configuration) {
        doExecute("CREATE TABLE "
            + getTableNameWithSchema(configuration.getTable())
            + " ("
            + doQuote("installed_rank")
            + " INT NOT NULL PRIMARY KEY, "
            + doQuote("version")
            + " VARCHAR(50), "
            + doQuote("description")
            + " VARCHAR(200) NOT NULL, "
            + doQuote("type")
            + " VARCHAR(20) NOT NULL, "
            + doQuote("script")
            + " VARCHAR(1000) NOT NULL, "
            + doQuote("checksum")
            + " INT, "
            + doQuote("installed_by")
            + " VARCHAR(100) NOT NULL, "
            + doQuote("installed_on")
            + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
            + doQuote("execution_time")
            + " INT NOT NULL, "
            + doQuote("success")
            + " BOOLEAN NOT NULL)", false);
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        if (schema == null) {
            return false;
        }
        return count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '"
            + schema
            + "'") > 0;
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        return count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '"
            + schema
            + "' AND TABLE_TYPE IN ('TABLE', 'BASE TABLE')") == 0;
    }

    @Override
    public void createSchemas(final String... schemas) {
        for (final String schema : schemas) {
            doExecute("CREATE SCHEMA " + doQuote(schema), false);
        }
    }

    @Override
    public void startTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException exception) {
            throw new FlywayException(exception);
        }
    }

    @Override
    public void commitTransaction() {
        try {
            connection.commit();
        } catch (final SQLException exception) {
            throw new FlywayException(exception);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            connection.rollback();
        } catch (final SQLException exception) {
            throw new FlywayException(exception);
        }
    }

    @Override
    public void doCleanSchema(final String schema) {
        throw new FlywayException("clean is not used by this test connector");
    }

    @Override
    public void doDropSchema(final String schema) {
        throw new FlywayException("drop schema is not used by this test connector");
    }

    private int count(final String sql) {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            return rows.next() ? rows.getInt(1) : 0;
        } catch (final SQLException exception) {
            throw new FlywayException(exception);
        }
    }
}
