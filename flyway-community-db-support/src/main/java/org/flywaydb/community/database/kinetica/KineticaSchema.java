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
package org.flywaydb.community.database.kinetica;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

public class KineticaSchema extends Schema<KineticaDatabase,KineticaTable> {


    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public KineticaSchema(JdbcTemplate jdbcTemplate, KineticaDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA") == 0;

    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + name);

    }

    @Override
    protected void doClean() throws SQLException {
      //
    }

    @Override
    protected KineticaTable[] doAllTables() throws SQLException {
        String query = "select table_name from information_schema.tables where table_schema= '"+name +"'";
        List<String> tables = jdbcTemplate.queryForStringList(query, name);
        KineticaTable[]  kineticaTables = new KineticaTable[tables.size()];
        int index = 0;
        for (String table: tables) {
            kineticaTables[index] = new KineticaTable(jdbcTemplate, database, this, table);
            index++;
        }
        return kineticaTables;
    }


    @Override
    public Table getTable(String tableName) {
        return new KineticaTable(jdbcTemplate, database, this, tableName);
    }
}
