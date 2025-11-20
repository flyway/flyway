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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;

@CustomLog
public class SpannerSchema extends Schema<SpannerDatabase, SpannerTable> {

    public SpannerSchema(final JdbcTemplate jdbcTemplate, final SpannerDatabase database, final String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() {
        return "".equals(name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        try (final Connection c = database.getNewRawConnection()) {
            final Statement s = c.createStatement();
            s.close();
            try (final ResultSet tables = c.getMetaData().getTables("", "", null, null)) {
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
        final Collection<String> statements = new ArrayList<>();

        for (final String[] foreignKeyAndTable : doAllForeignKeys()) {
            final String foreignKey = foreignKeyAndTable[0];
            final String table = foreignKeyAndTable[1];
            statements.add("ALTER TABLE " + table + " DROP CONSTRAINT " + foreignKey);
        }
        executeStatements(statements);

        for (final String view : doAllViews()) {
            statements.add("DROP VIEW " + view);
        }
        executeStatements(statements);

        for (final Table<SpannerDatabase, SpannerSchema> table : doAllTables()) {
            for (final String index : doAllIndexes(table)) {
                if (!"PRIMARY_KEY".equalsIgnoreCase(index)) {
                    jdbcTemplate.execute("DROP INDEX " + index);
                }
            }
            statements.add("DROP TABLE " + table);
        }
        executeStatements(statements);
    }

    private void executeStatements(final Collection<String> statements) throws SQLException {
        final Configuration config = database.getConfiguration();

        final Results cleanStatementResults = jdbcTemplate.executeBatch(statements);
        if (cleanStatementResults.getException() != null) {
            throw cleanStatementResults.getException();
        }

        statements.clear();
    }

    private String[] doAllViews() throws SQLException {
        final Collection<String> viewList = new ArrayList<>();
        final Connection connection = jdbcTemplate.getConnection();

        final ResultSet viewResults = connection.getMetaData().getTables("", "", null, new String[] { "VIEW" });
        while (viewResults.next()) {
            viewList.add(viewResults.getString("TABLE_NAME"));
        }
        viewResults.close();

        return viewList.toArray(String[]::new);
    }

    @Override
    protected SpannerTable[] doAllTables() throws SQLException {
        final List<SpannerTable> tablesList = new ArrayList<>();
        final Connection connection = jdbcTemplate.getConnection();

        final ResultSet tablesRs = connection.getMetaData().getTables("", "", null, new String[] { "TABLE" });
        while (tablesRs.next()) {
            tablesList.add(new SpannerTable(jdbcTemplate, database, this, tablesRs.getString("TABLE_NAME")));
        }
        tablesRs.close();

        final SpannerTable[] tables = new SpannerTable[tablesList.size()];
        return tablesList.toArray(tables);
    }

    private List<String[]> doAllForeignKeys() {
        final List<String[]> foreignKeyAndTableList = new ArrayList<>();

        final Results foreignKeyRs = jdbcTemplate.executeStatement("SELECT CONSTRAINT_NAME, TABLE_NAME "
            + "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
            + "WHERE CONSTRAINT_TYPE='FOREIGN KEY' "
            + "AND TABLE_SCHEMA=''");

        for (final Result result : foreignKeyRs.getResults()) {
            for (final List<String> row : result.data()) {
                final String[] foreignKeyAndTable = { row.get(0), row.get(1) };
                foreignKeyAndTableList.add(foreignKeyAndTable);
            }
        }

        return foreignKeyAndTableList;
    }

    private List<String> doAllIndexes(final Table<SpannerDatabase, SpannerSchema> table) throws SQLException {
        final List<String> indexList = new ArrayList<>();
        final Connection c = jdbcTemplate.getConnection();

        final ResultSet indexRs = c.getMetaData().getIndexInfo("", "", table.getName(), false, false);
        while (indexRs.next()) {
            indexList.add(indexRs.getString("INDEX_NAME"));
        }
        indexRs.close();

        return indexList;
    }

    @Override
    public Table<SpannerDatabase, SpannerSchema> getTable(final String tableName) {
        return new SpannerTable(jdbcTemplate, database, this, tableName);
    }
}
