/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-oracle
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.experimental.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.BiFunction;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalJdbc;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.database.oracle.OracleParser;

public class ExperimentalOracle extends ExperimentalJdbc {

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("jdbc:oracle")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public List<String> supportedVerbs() {
        return List.of("info", "migrate", "validate");
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        final int connectRetries = environment.getConnectRetries() != null ? environment.getConnectRetries() : 0;
        final int connectRetriesInterval = environment.getConnectRetriesInterval() != null
            ? environment.getConnectRetriesInterval()
            : 0;
        connection = JdbcUtils.openConnection(configuration.getDataSource(), connectRetries, connectRetriesInterval);
        metaData = getDatabaseMetaData();
        currentSchema = getDefaultSchema(configuration);
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM ALL_TABLES where TABLE_NAME='" + tableName + "'"
                    + " AND OWNER = " + "'" + getCurrentSchema() + "'");
            return resultSet.next();
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        try (final Statement statement = connection.createStatement()) {

            String sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OWNER = " + "'" + schema + "'"
                + " AND OBJECT_TYPE IN ('TABLE', 'VIEW', 'INDEX', 'SEQUENCE', 'PROCEDURE', 'FUNCTION', 'TRIGGER')"
                + "AND OBJECT_NAME NOT LIKE 'flyway_schema_history%'";

            ResultSet resultSet = statement.executeQuery(sql);
            return !resultSet.next() || resultSet.getInt(1) == 0;
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM ALL_USERS WHERE USERNAME = " + "'" + schema + "'");
            return resultSet.next();
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void createSchemas(final String... schemas) {
        for (String schema: schemas) {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("CREATE USER " + doQuote(schema) + " IDENTIFIED BY "
                    + doQuote("Flyway"));
                statement.execute("GRANT RESOURCE TO " + doQuote(schema));
                statement.execute("GRANT UNLIMITED TABLESPACE TO " + doQuote(schema));
            } catch (Exception e) {
                throw new FlywayException(e);
            }
        }

        try {
            if (schemas.length != 0) {
                connection.setSchema(doQuote(schemas[0]));
            }
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public String getDatabaseType() {
        return "Oracle";
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return OracleParser::new;
    }

    @Override
    public void startTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public void doCleanSchema(final String schema) {
        throw new FlywayException("doCleanSchema method not supported yet in Oracle Native Connectors Mode");
    }

    @Override
    public void doDropSchema(final String schema) {
        throw new FlywayException("doDropSchema method not supported yet in Oracle Native Connectors Mode");
    }

    @Override
    public void createSchemaHistoryTable(final String tableName) {
        try (final Statement statement = connection.createStatement()) {
            final String createSql = "CREATE TABLE "
                + getTableNameWithSchema(tableName)
                + " (\n"
                + doQuote("installed_rank") + " INT NOT NULL,\n     "
                + doQuote("version") + " VARCHAR2(50),\n     "
                + doQuote("description") + " VARCHAR2(200) NOT NULL,\n     "
                + doQuote("type") + " VARCHAR2(20) NOT NULL,\n     "
                + doQuote("script") + " VARCHAR2(1000) NOT NULL,\n     "
                + doQuote("checksum") + " INT,\n     "
                + doQuote("installed_by") + " VARCHAR2(100) NOT NULL,\n     "
                + doQuote("installed_on") + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n     "
                + doQuote("execution_time") + " INT NOT NULL,\n     "
                + doQuote("success") + " NUMBER(1) NOT NULL,\n     "
                + "CONSTRAINT "
                + doQuote(tableName + "_pk") + " PRIMARY KEY (" + doQuote("installed_rank") + ")\n"
                + " )\n";
            statement.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new FlywayException(e);
        }
    }

    @Override
    protected boolean supportsBoolean() {
        return false;
    }
}
