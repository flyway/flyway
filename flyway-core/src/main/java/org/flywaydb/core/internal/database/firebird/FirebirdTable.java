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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class FirebirdTable extends Table<FirebirdDatabase, FirebirdSchema> {

    /**
     * Creates a new Firebird table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public FirebirdTable(JdbcTemplate jdbcTemplate, FirebirdDatabase database, FirebirdSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + this);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from RDB$RELATIONS\n" +
                "where RDB$RELATION_NAME = ?\n" +
                "and RDB$VIEW_BLR is null", name) > 0;
    }

    @Override
    protected void doLock() throws SQLException {
        /*
         Firebird has row-level locking on all transaction isolation levels (this requires fetching the row to lock).
         Table-level locks can only be reserved in SERIALIZABLE (isc_tpb_consistency) with caveats.
         This approach will read all records from table (without roundtrips to the server) to locking all records; it
         will not claim a table-level lock unless the isolation level is SERIALIZABLE. This means that inserts are
         still possible as are selects that don't use 'with lock'.
        */
        jdbcTemplate.execute("execute block as\n"
                + "declare tempvar integer;\n"
                + "begin\n"
                + "  for select 1 from " + this + " with lock into :tempvar do\n"
                + "  begin\n"
                + "  end\n"
                + "end");
    }

    @Override
    public String toString() {
        // No schema, only plain table name
        return database.doQuote(name);
    }
}