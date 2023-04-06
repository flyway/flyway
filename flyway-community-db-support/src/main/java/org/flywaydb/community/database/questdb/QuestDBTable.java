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

import java.sql.SQLException;

import org.flywaydb.core.internal.database.postgresql.PostgreSQLTable;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class QuestDBTable extends PostgreSQLTable {
    /**
     * @param jdbcTemplate The JDBC template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    public QuestDBTable(JdbcTemplate jdbcTemplate, QuestDBDatabase database, QuestDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE ?", name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForStringList("SHOW TABLES").contains(name);
    }

    @Override
    protected void doLock() {
        // Unable to lock as QuestDB does not support locking. No concurrent migration supported.
    }
}