/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.database.spanner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;

import java.sql.SQLException;
import java.util.List;

public class SpannerSchema extends Schema<SpannerDatabase, SpannerTable> {
    private static final Log LOG = LogFactory.getLog(SpannerSchema.class);

    public SpannerSchema(JdbcTemplate jdbcTemplate, SpannerDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() {
        return name.equals("");
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        try (Connection c = database.getNewRawConnection()){
            Statement s = c.createStatement();
            s.close();
            try(ResultSet tables = c.getMetaData().getTables("", "", null, null)){
                return !tables.next();
            }
        }
    }

    @Override
    protected void doCreate() {
        LOG.info("Spanner does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        doClean();
    }

    @Override
    protected void doClean() throws SQLException {
        List<String> statements = new ArrayList<>();

        for (String[] foreignKeyAndTable: doAllForeignKeys()) {
            String foreignKey = foreignKeyAndTable[0];
            String table = foreignKeyAndTable[1];
            statements.add("ALTER TABLE " + table + " DROP CONSTRAINT " + foreignKey);
        }





         for (String statement : statements) {
             jdbcTemplate.execute(statement);
         }


        statements.clear();

        for (Table table : doAllTables()) {
            for (String index: doAllIndexes(table)) {
                if (!index.equalsIgnoreCase("PRIMARY_KEY")) {
                    jdbcTemplate.execute("DROP INDEX " + index);
                }
            }
            statements.add("DROP TABLE " + table);
        }





         for (String statement : statements) {
            jdbcTemplate.execute(statement);
         }

    }

    @Override
    protected SpannerTable[] doAllTables() throws SQLException {
        List<SpannerTable> tablesList = new ArrayList<>();
        Connection c = jdbcTemplate.getConnection();

        ResultSet tablesRs = c.getMetaData().getTables("", "", null, null);
        while (tablesRs.next()) {
            tablesList.add(new SpannerTable(jdbcTemplate, database, this,
                    tablesRs.getString("TABLE_NAME")));
        }
        tablesRs.close();

        SpannerTable[] tables = new SpannerTable[tablesList.size()];
        return tablesList.toArray(tables);
    }

    private List<String[]> doAllForeignKeys() {
        List<String[]> foreignKeyAndTableList = new ArrayList<>();

        Results foreignKeyRs = jdbcTemplate.executeStatement("SELECT CONSTRAINT_NAME, TABLE_NAME " +
                "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "WHERE CONSTRAINT_TYPE='FOREIGN KEY'");

        for (Result result: foreignKeyRs.getResults()) {
            for (List<String> row: result.getData()) {
                String[] foreignKeyAndTable = {row.get(0), row.get(1)};
                foreignKeyAndTableList.add(foreignKeyAndTable);
            }
        }

        return foreignKeyAndTableList;
    }

    private List<String> doAllIndexes(Table table) throws SQLException {
        List<String> indexList = new ArrayList<>();
        Connection c = jdbcTemplate.getConnection();

        ResultSet indexRs = c.getMetaData().getIndexInfo("", "", table.getName(), false, false);
        while (indexRs.next()) {
            indexList.add(indexRs.getString("INDEX_NAME"));
        }
        indexRs.close();

        return indexList;
    }

    @Override
    public Table getTable(String tableName) {
        return new SpannerTable(jdbcTemplate, database, this, tableName);
    }
}