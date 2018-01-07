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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;

/**
 * Firebird-specific table.
 */
public class FirebirdTable extends Table {
    /**
     * Creates a new Firebird table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public FirebirdTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
            jdbcTemplate.execute("DROP TABLE " + database.quote(name));
    }

    @Override
    protected boolean doExists() throws SQLException {

        return jdbcTemplate.queryForInt("select count(rdb$relation_name) as countTableName\n" +
                "from rdb$relations\n" +
                "where rdb$view_blr is null\n" +
                "and rdb$relation_name = '"+name+"'\n"+
                "and (rdb$system_flag is null or rdb$system_flag = 0)") > 0;

    }

    @Override
    protected void doLock() throws SQLException {
        //TODO: Implement Firebird Locking ???
    }

    @Override
    public String toString() {
        return database.quote(name);
    }
}