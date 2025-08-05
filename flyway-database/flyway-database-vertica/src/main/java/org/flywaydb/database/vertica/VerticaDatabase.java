/*-
 * ========================LICENSE_START=================================
 * flyway-database-vertica
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
package org.flywaydb.database.vertica;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class VerticaDatabase extends Database<VerticaConnection> {

    public VerticaDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected VerticaConnection doGetConnection(Connection connection) {
        return new VerticaConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        // We ensure support of our own Vertica DBs ourselves
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        /*
         * Note: 
         * 
         * UNSEGMENTED ALL NODES is crucial in this case as it serves as a trigger for an IMMEDIATE automatic superprojection creation:
         * https://docs.vertica.com/24.1.x/en/admin/projections/auto-projections/#auto-projection-triggers
         * 
         * Otherwise, the superprojection is created only prior to the initial INSERT and at that stage 
         * it results in a deadlock as the CREATE PROJECTION happens to be executed in a separate transaction (by Vertica) from Flyway`s INSERT statement
         * and Flyway calls `doLock` to aquire exclusive lock on the table prior to the INSERT.
         * 
         */
        return "CREATE TABLE " + table + " (\n" +
                quote("installed_rank") + " INT PRIMARY KEY NOT NULL,\n" +
                quote("version") + " VARCHAR(50),\n" +
                quote("description") + " VARCHAR(200) NOT NULL,\n" +
                quote("type") + " VARCHAR(20) NOT NULL,\n" +
                quote("script") + " VARCHAR(1000) NOT NULL,\n" +
                quote("checksum") + " INT,\n" +
                quote("installed_by") + " VARCHAR(100) NOT NULL,\n" +
                quote("installed_on") + " TIMESTAMP NOT NULL DEFAULT NOW(),\n" +
                quote("execution_time") + " INT NOT NULL,\n" +
                quote("success") + " BOOLEAN NOT NULL) UNSEGMENTED ALL NODES;\n" +

                (baseline ? getBaselineStatement(table) + ";\n" : "");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return getOpenQuote() + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote()) + getCloseQuote();
    }

    @Override
    public String getEscapedQuote() {
        return "\"\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}
