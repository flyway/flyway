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
package org.flywaydb.core.internal.database.exasol;

import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

/**
 * @author artem
 * @date 14.09.2020
 * @time 16:27
 */
public class ExasolSchema extends Schema<ExasolDatabase, ExasolTable> {

    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public ExasolSchema(final JdbcTemplate jdbcTemplate,
                        final ExasolDatabase database, final String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {

        return jdbcTemplate.queryForBoolean(
          "SELECT EXISTS (SELECT 1 FROM EXA_ALL_SCHEMAS WHERE SCHEMA_NAME = ?)", name
        );
    }

    @Override
    protected boolean doEmpty() throws SQLException {

        return !jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
          "    SELECT 1 FROM EXA_ALL_FUNCTIONS WHERE FUNCTION_SCHEMA = ?\n" +
          "  UNION ALL\n" +
          "    SELECT 1 FROM EXA_ALL_TABLES WHERE TABLE_SCHEMA = ?\n" +
          "  UNION ALL\n" +
          "    SELECT 1 FROM EXA_ALL_VIEWS WHERE VIEW_SCHEMA = ?\n" +
          ")", name, name, name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {

        dropObjects("VIEW");
        dropObjects("FUNCTION");
        dropObjects("TABLE");

    }

    private void dropObjects(final String objectType) throws SQLException {

        final List<String> names
          = jdbcTemplate.queryForStringList("SELECT "+ objectType + "_NAME FROM EXA_ALL_" + objectType
          + "S WHERE " + objectType + "_SCHEMA = ?", name);

        for (final String object : names) {
            jdbcTemplate.execute("DROP " + objectType + " " + database.quote(name) + "." + database.quote(object));
        }
    }

    @Override
    protected ExasolTable[] doAllTables() throws SQLException {

        final List<String> tableNames = jdbcTemplate.queryForStringList(
          "SELECT TABLE_NAME FROM EXA_ALL_TABLES WHERE TABLE_SCHEMA = ?", name
        );

        return tableNames
          .stream()
          .map(table -> new ExasolTable(jdbcTemplate, database, this, table))
          .toArray(ExasolTable[]::new);
    }

    @Override
    public ExasolTable getTable(final String tableName) {
        return new ExasolTable(jdbcTemplate, database, this, tableName);
    }
}
