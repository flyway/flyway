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
package org.flywaydb.core.internal.database.informix;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

/**
 * Informix implementation of Schema.
 */
public class InformixSchema extends Schema<InformixDatabase> {
    /**
     * Creates a new Informix schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    InformixSchema(JdbcTemplate jdbcTemplate, InformixDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM systables where owner = ? and tabid > 99", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return doAllTables().length == 0;
    }

    @Override
    protected void doCreate() {
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
    }

    @Override
    protected void doClean() throws SQLException {
        List<String> procedures = jdbcTemplate.queryForStringList("SELECT t.procname FROM \"informix\".sysprocedures AS t" +
                " WHERE t.owner=? AND t.mode='O' AND t.externalname IS NULL" +
                " AND t.procname NOT IN (" +
                // Exclude Informix TimeSeries procs
                " 'tscontainerusage', 'tscontainertotalused', 'tscontainertotalpages'," +
                " 'tscontainernelems', 'tscontainerpctused', 'tsl_flushstatus', 'tsmakenullstamp'" +
                ")", name);
        for (String procedure : procedures) {
            jdbcTemplate.execute("DROP PROCEDURE " + procedure);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        List<String> sequences = jdbcTemplate.queryForStringList("SELECT t.tabname FROM \"informix\".systables AS t" +
                " WHERE owner=? AND t.tabid > 99 AND t.tabtype='Q'" +
                " AND t.tabname NOT IN ('iot_data_seq')", name);
        for (String sequence : sequences) {
            jdbcTemplate.execute("DROP SEQUENCE " + sequence);
        }
    }

    private Table[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new InformixTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        return findTables("SELECT t.tabname FROM \"informix\".systables AS t" +
                " WHERE owner=? AND t.tabid > 99 AND t.tabtype='T'" +
                " AND t.tabname NOT IN (" +
                // Exclude Informix TimeSeries tables
                " 'calendarpatterns', 'calendartable'," +
                " 'tscontainertable', 'tscontainerwindowtable', 'tsinstancetable', " +
                " 'tscontainerusageactivewindowvti', 'tscontainerusagedormantwindowvti'" +
                ")", name);
    }

    @Override
    public Table getTable(String tableName) {
        return new InformixTable(jdbcTemplate, database, this, tableName);
    }
}