/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.database.sqlserver.synapse;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.sqlserver.SQLServerDatabase;
import org.flywaydb.core.internal.database.sqlserver.SQLServerSchema;
import org.flywaydb.core.internal.database.sqlserver.SQLServerTable;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * Synapse-specific table.
 */
public class SynapseTable extends SQLServerTable {
    private final InsertRowLock insertRowLock = new InsertRowLock();

    /**
     * Creates a new Synapse table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param databaseName The database this table lives in.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    SynapseTable(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, SQLServerSchema schema, String name) {
        super(jdbcTemplate, database, databaseName, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        if (lockDepth > 0) {
            // Lock has already been taken - so the relevant row in the table already exists
            return;
        }

        insertRowLock.doLock(jdbcTemplate, database.getInsertStatement(this), database.getBooleanTrue());
    }

    @Override
    protected void doUnlock() throws SQLException {
        // Leave the locking row alone until we get to the final level of unlocking
        if (lockDepth > 1) {
            return;
        }

        String selectLockTemplate = getSelectLockTemplate();
        String deleteLockTemplate = getDeleteLockTemplate();

        insertRowLock.doUnlock(jdbcTemplate, selectLockTemplate, deleteLockTemplate);
    }

    private String getSelectLockTemplate() {
        return "SELECT COUNT(*) FROM " + this.toString() + " WHERE version != '?' AND DESCRIPTION = 'flyway-lock'";
    }

    private String getDeleteLockTemplate() {
        return "DELETE FROM " + this.toString() + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
    }
}