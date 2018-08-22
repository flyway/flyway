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
package org.flywaydb.core.internal.database.phoenix;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PhoenixTable extends Table {
    /**
     * Creates a new table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public PhoenixTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        Connection c = jdbcTemplate.getConnection();
        try (ResultSet rs = c.getMetaData().getTables(c.getCatalog(), schema.getName(), name, null)) {
            return rs.next();
        }
    }

    @Override
    protected void doLock() throws SQLException {
        // Nope, no table locks in Phoenix.
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(name) + " CASCADE");
    }
}