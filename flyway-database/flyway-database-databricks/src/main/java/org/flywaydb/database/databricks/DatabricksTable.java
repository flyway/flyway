/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DatabricksTable extends Table<DatabricksDatabase, DatabricksSchema> {
    private final InsertRowLock insertRowLock;

    public DatabricksTable(final JdbcTemplate jdbcTemplate,
        final DatabricksDatabase database,
        final DatabricksSchema schema,
        final String name) {
        super(jdbcTemplate, database, schema, name);
        this.insertRowLock = new InsertRowLock(jdbcTemplate);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + ";");
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schema.exists()) {
            return false;
        }
        final List<Map<String, String>> tables = jdbcTemplate.queryForList("show tables in "
            + database.quote(schema.getName())
            + " like '"
            + name
            + "';");
        return tables.stream().anyMatch(table -> table.get("tableName").equals(name));
    }

    @Override
    protected void doLock() throws SQLException {
        final String updateLockStatement = "UPDATE "
            + this
            + " SET installed_on = CURRENT_TIMESTAMP() WHERE version = '?' AND DESCRIPTION = 'flyway-lock';";
        final String deleteExpiredLockStatement = " DELETE FROM "
            + this
            + " WHERE DESCRIPTION = 'flyway-lock'"
            + " AND installed_on < TIMESTAMP '?';";

        if (lockDepth == 0) {
            insertRowLock.doLock(database.getInsertStatement(this),
                updateLockStatement,
                deleteExpiredLockStatement,
                database.getBooleanTrue());
        }
    }

    @Override
    protected void doUnlock() throws SQLException {
        if (lockDepth == 1) {
            insertRowLock.doUnlock(getDeleteLockTemplate());
        }
    }

    private String getDeleteLockTemplate() {
        return "DELETE FROM " + this + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock';";
    }
}
