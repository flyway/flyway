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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Table;

import java.sql.SQLException;
import java.util.List;

/**
 * Sybase ASE schema (database).
 */
public class SybaseASESchema extends Schema<SybaseASEDatabase> {
    SybaseASESchema(JdbcTemplate jdbcTemplate, SybaseASEDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        //There is no schema in SAP ASE. Always return true
        return true;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        //There is no schema in SAP ASE, check whether database is empty
        //Check for tables, views stored procs and triggers
        return jdbcTemplate.queryForInt("select count(*) from sysobjects ob where (ob.type='U' or ob.type = 'V' or ob.type = 'P' or ob.type = 'TR') and ob.name != 'sysquerymetrics'") == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        //There is no schema in SAP ASE. Do nothing for creation.
    }

    @Override
    protected void doDrop() throws SQLException {
        //There is no schema in Sybase, no schema can be dropped. Clean instead.
        doClean();
    }

    /**
     * This clean method is equivalent to cleaning the whole database.
     *
     * @see Schema#doClean()
     */
    @Override
    protected void doClean() throws SQLException {
        //Drop view
        dropObjects("V");
        //Drop tables
        dropObjects("U");
        //Drop stored procs
        dropObjects("P");
        //Drop triggers
        dropObjects("TR");
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        //Retrieving all table names
        List<String> tableNames = retrieveAllTableNames();

        Table[] result = new Table[tableNames.size()];

        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            result[i] = new SybaseASETable(jdbcTemplate, database, this, tableName);
        }

        return result;
    }

    @Override
    public Table getTable(String tableName) {
        return new SybaseASETable(jdbcTemplate, database, this, tableName);
    }

    /**
     * @return all table names in the current database.
     */
    private List<String> retrieveAllTableNames() throws SQLException {
        return jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", "U");
    }

    private void dropObjects(String sybaseObjType) throws SQLException {
        //Getting the table names
        List<String> objNames = jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", sybaseObjType);

        //for each table, drop it
        for (String name : objNames) {
            String sql;

            if ("U".equals(sybaseObjType)) {
                sql = "drop table ";
            } else if ("V".equals(sybaseObjType)) {
                sql = "drop view ";
            } else if ("P".equals(sybaseObjType)) {
                //dropping stored procedure
                sql = "drop procedure ";
            } else if ("TR".equals(sybaseObjType)) {
                sql = "drop trigger ";
            } else {
                throw new IllegalArgumentException("Unknown database object type " + sybaseObjType);
            }

            jdbcTemplate.execute(sql + name);
        }
    }
}