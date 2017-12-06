/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.nuodb;

import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;
import java.util.List;

/**
 * NuoDB implementation of Schema.
 */
public class NuoDBSchema extends Schema<NuoDBDatabase> {
    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    NuoDBSchema(JdbcTemplate jdbcTemplate, NuoDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        boolean exists = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM system.schemas WHERE schema=?", name) > 0;
        return exists;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return false;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        doDrop();
        doCreate();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT tablename FROM system.tables WHERE type = 'TABLE' AND schema = ?",
                database.quote(name)
        );

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new NuoDBTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new NuoDBTable(jdbcTemplate, database, this, tableName);
    }
}
