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
package org.flywaydb.beta.gcp.bigquery;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class BigQueryTable extends Table<BigQueryDatabase, BigQuerySchema> {
    private final InsertRowLock insertRowLock;

    BigQueryTable(JdbcTemplate jdbcTemplate, BigQueryDatabase database, BigQuerySchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.insertRowLock = new InsertRowLock(jdbcTemplate, 10);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schema.exists()) {
            return false;
        }
        return jdbcTemplate.queryForInt(
                "SELECT COUNT(table_name) FROM " + database.quote(schema.getName()) + ".INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' AND table_name=?", name) > 0;
    }

    @Override
    protected void doLock() throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.MINUTE, -insertRowLock.lockTimeoutMins);
        DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        String updateLockStatement = "UPDATE " + this + " SET installed_on = now() WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
        String deleteExpiredLockStatement =
                " DELETE FROM " + this +
                        " WHERE DESCRIPTION = 'flyway-lock'" +
                        " AND installed_on < TIMESTAMP '" + timestampFormat.format(cal.getTime()) + "'";

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
        return "DELETE FROM " + this + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
    }
}