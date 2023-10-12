/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.questdb;

import java.sql.Connection;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabase;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

public class QuestDBDatabase extends PostgreSQLDatabase {

    public QuestDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected QuestDBConnection doGetConnection(Connection connection) {
        return new QuestDBConnection(this, connection);
    }

    @Override
    public void ensureSupported() {
        // Checks the Postgres version
        ensureDatabaseIsRecentEnough("11.3");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table.getName() + " (\n" +
               "    \"installed_rank\" INT,\n" +
               "    \"version\" STRING,\n" +
               "    \"description\" STRING,\n" +
               "    \"type\" STRING,\n" +
               "    \"script\" STRING,\n" +
               "    \"checksum\" INT,\n" +
               "    \"installed_by\" STRING,\n" +
               "    \"installed_on\" TIMESTAMP,\n" +
               "    \"execution_time\" INT,\n" +
               "    \"success\" BOOLEAN\n" +
               ") timestamp (installed_on);\n";
    }

    @Override
    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table.getName()
               + " (" + quote("installed_rank")
               + ", " + quote("version")
               + ", " + quote("description")
               + ", " + quote("type")
               + ", " + quote("script")
               + ", " + quote("checksum")
               + ", " + quote("installed_by")
               + ", " + quote("installed_on")
               + ", " + quote("execution_time")
               + ", " + quote("success")
               + ")"
               + " VALUES (?, ?, ?, ?, ?, ?, ?, now(), ?, ?)";
    }

    public String getSelectStatement(Table table) {
        return "SELECT " + quote("installed_rank")
               + "," + quote("version")
               + "," + quote("description")
               + "," + quote("type")
               + "," + quote("script")
               + "," + quote("checksum")
               + "," + quote("installed_on")
               + "," + quote("installed_by")
               + "," + quote("execution_time")
               + "," + quote("success")
               + " FROM " + table.getName()
               + " WHERE " + quote("installed_rank") + " > ?"
               + " ORDER BY " + quote("installed_rank");
    }
}