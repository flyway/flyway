/*-
 * ========================LICENSE_START=================================
 * flyway-gcp-spanner
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
package org.flywaydb.database.spanner;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SpannerTable extends Table<SpannerDatabase, SpannerSchema> {

    private final InsertRowLock insertRowLock;

    public SpannerTable(JdbcTemplate jdbcTemplate, SpannerDatabase database, SpannerSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.insertRowLock = new InsertRowLock(jdbcTemplate);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try (Connection c = database.getNewRawConnection()) {
            Statement s = c.createStatement();
            s.close();
            try (ResultSet tables = c.getMetaData().getTables("", "", this.name, null)) {
                return tables.next();
            }
        }
    }

    @Override
    protected void doLock() throws SQLException {
        String updateLockStatement = "UPDATE " + name + " SET installed_on = CURRENT_TIMESTAMP() WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
        String deleteExpiredLockStatement =
                " DELETE FROM " + name +
                        " WHERE DESCRIPTION = 'flyway-lock'" +
                        " AND installed_on < TIMESTAMP_ADD(CURRENT_TIMESTAMP(), INTERVAL -" + InsertRowLock.LOCK_TIMEOUT_MINS + " MINUTE)";

        if (lockDepth == 0) {
            insertRowLock.doLock(database.getInsertStatement(this), updateLockStatement, deleteExpiredLockStatement, database.getBooleanTrue());
        }
    }

    @Override
    protected void doUnlock() throws SQLException {
        if (lockDepth == 1) {
            insertRowLock.doUnlock(getDeleteLockTemplate());
        }
    }

    private String getDeleteLockTemplate() {
        return "DELETE FROM " + name + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
    }

    @Override
    protected void doDrop() throws SQLException {
        try (Statement statement = jdbcTemplate.getConnection().createStatement()) {
            statement.execute("DROP TABLE " + database.quote(name));
        }
    }

    @Override
    public String toString() {
        return database.quote(name);
    }
}
