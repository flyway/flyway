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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.sqlscript.SqlScript;

/**
 * Database migration based on a sql file.
 */
public class SqlMigrationExecutor implements MigrationExecutor {
    private final Database database;
    /**
     * The SQL script that will be executed.
     */
    private final SqlScript sqlScript;


















    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScript The SQL script that will be executed.
     */
    SqlMigrationExecutor(Database database, SqlScript sqlScript



    ) {
        this.database = database;
        this.sqlScript = sqlScript;





    }

    @Override
    public void execute(Context context) {
        database.createSqlScriptExecutor(new JdbcTemplate(context.getConnection())



        ).execute(sqlScript);
    }

    @Override
    public boolean canExecuteInTransaction() {
        return sqlScript.executeInTransaction();
    }
}