/*-
 * ========================LICENSE_START=================================
 * flyway-firebird
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
package org.flywaydb.database.firebird;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

public class FirebirdDatabase extends Database<FirebirdConnection> {
    /**
     * Creates a new FirebirdDatabase instance with this JdbcTemplate.
     *
     * @param configuration The Flyway configuration.
     */
    public FirebirdDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected FirebirdConnection doGetConnection(Connection connection) {
        return new FirebirdConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("3.0");
    }

    @Override
    public boolean supportsDdlTransactions() {
        // but can't use DDL changes in DML in same transaction
        return true;
    }

    @Override
    public String getBooleanTrue() {
        // boolean datatype introduced in Firebird 3, but this allows broader support
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        // boolean datatype introduced in Firebird 3, but this allows broader support
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return getOpenQuote() + identifier.replace(getCloseQuote(), getEscapedQuote()) + getCloseQuote();
    }

    @Override
    public String getEscapedQuote() {
        return "\"\"";
    }

    @Override
    public boolean catalogIsSchema() {
        // database == schema
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String createScript = "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INTEGER CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INTEGER,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INTEGER NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL\n" +
                ");\n" +
                "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");\n";

        if (baseline) {
            // COMMIT RETAIN is needed to be able to insert into the created table.
            // This will commit the transaction, but reuse the transaction handle so the JDBC driver doesn't break with
            // an "invalid transaction handle" error.
            createScript += "COMMIT RETAIN;\n" +
                    getBaselineStatement(table) + ";\n";

        }

        return createScript;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        // JDBC DatabaseMetaData.getUserName() reports original user used for connecting, but this may be remapped
        return getMainConnection().getJdbcTemplate().queryForString("select CURRENT_USER from RDB$DATABASE");
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}
