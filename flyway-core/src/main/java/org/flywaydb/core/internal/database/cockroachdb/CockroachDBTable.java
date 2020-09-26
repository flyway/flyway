/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

/**
 * CockroachDB-specific table.
 *
 * Note that CockroachDB doesn't support table locks. We therefore use a row in the schema history as a lock indicator;
 * if another process ahs inserted such a row we wait (potentially indefinitely) for it to be removed before
 * carrying out a migration.
 */
public class CockroachDBTable extends Table<CockroachDBDatabase, CockroachDBSchema> {
    private final InsertRowLock insertRowLock = new InsertRowLock();

    /**
     * Creates a new CockroachDB table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    CockroachDBTable(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, CockroachDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doDropOnce();
                return null;
            }
        });
    }

    protected void doDropOnce() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return new CockroachDBRetryingStrategy().execute(new SqlCallable<Boolean>() {
            @Override
            public Boolean call() throws SQLException {
                return doExistsOnce();
            }
        });
    }

    protected boolean doExistsOnce() throws SQLException {
        if (schema.cockroachDB1) {
            return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = ?\n" +
                    "   AND    table_name = ?\n" +
                    ")", schema.getName(), name);
        }

        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "   SELECT 1\n" +
                "   FROM   information_schema.tables \n" +
                "   WHERE  table_catalog = ?\n" +
                "   AND    table_schema = 'public'\n" +
                "   AND    table_name = ?\n" +
                ")", schema.getName(), name);
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