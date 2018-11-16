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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * SQLServer-specific table.
 */
public class SQLServerTable extends Table {
    private final String databaseName;

    /**
     * Creates a new SQLServer table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param databaseName The database this table lives in.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    SQLServerTable(JdbcTemplate jdbcTemplate, Database database, String databaseName, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.databaseName = databaseName;
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + this);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean(
                "SELECT CAST(" +
                        "CASE WHEN EXISTS(" +
                        "  SELECT 1 FROM [" + databaseName + "].INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?" +
                        ") " +
                        "THEN 1 ELSE 0 " +
                        "END " +
                        "AS BIT)",
                schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("select * from " + this + " WITH (TABLOCKX)");
    }

    @Override
    public String toString() {
        return database.quote(databaseName, schema.getName(), name);
    }
}