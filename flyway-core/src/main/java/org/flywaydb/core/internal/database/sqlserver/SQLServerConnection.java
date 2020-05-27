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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * SQL Server connection.
 */
public class SQLServerConnection extends Connection<SQLServerDatabase> {
    private final String originalDatabaseName;
    private final String originalAnsiNulls;
    private final boolean azure;
    private final SQLServerEngineEdition engineEdition;

    SQLServerConnection(SQLServerDatabase database, java.sql.Connection connection) {
        super(database, connection);
        try {
            originalDatabaseName = jdbcTemplate.queryForString("SELECT DB_NAME()");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine current database", e);
        }

        try {
            azure = "SQL Azure".equals(getJdbcTemplate().queryForString(
                    "SELECT CAST(SERVERPROPERTY('edition') AS VARCHAR)"));
        }
        catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine database edition.'", e);
        }

        try {
            engineEdition = SQLServerEngineEdition.fromCode(getJdbcTemplate().queryForInt(
                    "SELECT SERVERPROPERTY('engineedition')"));
        }
        catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine database engine edition.'", e);
        }

        try {
            originalAnsiNulls = azure ? null :
                    jdbcTemplate.queryForString("DECLARE @ANSI_NULLS VARCHAR(3) = 'OFF';\n" +
                            "IF ( (32 & @@OPTIONS) = 32 ) SET @ANSI_NULLS = 'ON';\n" +
                            "SELECT @ANSI_NULLS AS ANSI_NULLS;");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine ANSI NULLS state", e);
        }
    }

    void setCurrentDatabase(String databaseName) throws SQLException {
        if (!azure) {
            jdbcTemplate.execute("USE " + database.quote(databaseName));
        }
    }


    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SCHEMA_NAME()");
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        setCurrentDatabase(originalDatabaseName);
        if (!azure) {
            jdbcTemplate.execute("SET ANSI_NULLS " + originalAnsiNulls);
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new SQLServerSchema(jdbcTemplate, database, originalDatabaseName, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return new SQLServerApplicationLockTemplate(this, jdbcTemplate, originalDatabaseName, table.toString().hashCode()).execute(callable);
    }

    public Boolean isAzureConnection() { return azure; }

    public SQLServerEngineEdition getEngineEdition() { return engineEdition; }
}