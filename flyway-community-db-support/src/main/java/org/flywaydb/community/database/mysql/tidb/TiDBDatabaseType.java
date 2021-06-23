/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.community.database.mysql.tidb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.mysql.MySQLConnection;
import org.flywaydb.core.internal.database.mysql.MySQLDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;

public class TiDBDatabaseType extends MySQLDatabaseType {
    @Override
    public String getName() {
        return "TiDB";
    }

    @Override
    public int getPriority() {
        // TiDB needs to be checked in advance of MySql
        return 1;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return (databaseProductName.contains("MySQL") && databaseProductVersion.contains("TiDB"))
                || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("TiDB"));
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new TiDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }
}