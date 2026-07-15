/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
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
package org.flywaydb.database.starrocks;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * StarRocks-specific table.
 * <p>
 * StarRocks OLAP tables do not support SELECT ... FOR UPDATE for pessimistic locking.
 * The doLock() method is intentionally a no-op; concurrent migration safety must be
 * ensured externally.
 */
@CustomLog
public class StarRocksTable extends Table<StarRocksDatabase, StarRocksSchema> {

    StarRocksTable(final JdbcTemplate jdbcTemplate,
        final StarRocksDatabase database,
        final StarRocksSchema schema,
        final String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(schema, null, name);
    }

    @Override
    protected void doLock() throws SQLException {
        LOG.warn("Table locking is not supported by StarRocks OLAP tables. "
            + "Concurrent migrations may interfere with each other.");
    }
}
