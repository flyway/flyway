/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.database.h2;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@CustomLog
public class H2Schema extends Schema<H2Database, H2Table> {
    private final boolean requiresV2Metadata;

    H2Schema(final JdbcTemplate jdbcTemplate,
        final H2Database database,
        final String name,
        final boolean requiresV2Metadata) {
        super(jdbcTemplate, database, name);
        this.requiresV2Metadata = requiresV2Metadata;
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", name)
            > 0;
    }

    @Override
    protected boolean doEmpty() {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + (database.supportsDropSchemaCascade
            ? " CASCADE"
            : ""));
    }

    @Override
    protected void doClean() throws SQLException {
        for (final Table table : allTables()) {
            table.drop();
        }

        final String sequenceSuffix = requiresV2Metadata ? "" : "IS_GENERATED = false";

        final List<String> sequenceNames = listObjectNames("SEQUENCE", sequenceSuffix);
        for (final String statement : generateDropStatements("SEQUENCE", sequenceNames)) {
            jdbcTemplate.execute(statement);
        }

        final List<String> constantNames = listObjectNames("CONSTANT", "");
        for (final String statement : generateDropStatements("CONSTANT", constantNames)) {
            jdbcTemplate.execute(statement);
        }

        final List<String> aliasNames = jdbcTemplate.queryForStringList(requiresV2Metadata
            ? "SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE = 'FUNCTION' AND ROUTINE_SCHEMA = ?"
            : "SELECT ALIAS_NAME FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE ALIAS_SCHEMA = ?", name);
        for (final String statement : generateDropStatements("ALIAS", aliasNames)) {
            jdbcTemplate.execute(statement);
        }

        final List<String> domainNames = listObjectNames("DOMAIN", "");
        if (!domainNames.isEmpty()) {
            if (name.equals(database.getMainConnection().getCurrentSchema().getName())) {
                for (final String statement : generateDropStatementsForCurrentSchema("DOMAIN", domainNames)) {
                    jdbcTemplate.execute(statement);
                }
            } else {
                LOG.error("Unable to drop DOMAIN objects in schema "
                    + database.quote(name)
                    + " due to H2 bug! (More info: http://code.google.com/p/h2database/issues/detail?id=306)");
            }
        }
    }

    /**
     * Generate the statements for dropping all the objects of this type in this schema.
     *
     * @param objectType  The type of object to drop (Sequence, constant, ...)
     * @param objectNames The names of the objects to drop.
     * @return The list of statements.
     */
    private List<String> generateDropStatements(final String objectType, final List<String> objectNames) {
        final List<String> statements = new ArrayList<>();
        for (final String objectName : objectNames) {
            final String dropStatement = "DROP " + objectType + database.quote(name, objectName);

            statements.add(dropStatement);
        }
        return statements;
    }

    /**
     * Generate the statements for dropping all the objects of this type in the current schema.
     *
     * @param objectType  The type of object to drop (Sequence, constant, ...)
     * @param objectNames The names of the objects to drop.
     * @return The list of statements.
     */
    private List<String> generateDropStatementsForCurrentSchema(final String objectType,
        final List<String> objectNames) {
        final List<String> statements = new ArrayList<>();
        for (final String objectName : objectNames) {
            final String dropStatement = "DROP " + objectType + database.quote(objectName);

            statements.add(dropStatement);
        }
        return statements;
    }

    @Override
    protected H2Table[] doAllTables() throws SQLException {
        final List<String> tableNames = listObjectNames("TABLE",
            "TABLE_TYPE = " + (requiresV2Metadata ? "'BASE TABLE'" : "'TABLE'"));

        final H2Table[] tables = new H2Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new H2Table(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    /**
     * List the names of the objects of this type in this schema.
     *
     * @param objectType  The type of objects to list (Sequence, constant, ...)
     * @param querySuffix Suffix to append to the query to find the objects to list.
     * @return The names of the objects.
     * @throws java.sql.SQLException when the object names could not be listed.
     */
    private List<String> listObjectNames(final String objectType, final String querySuffix) throws SQLException {
        String query = "SELECT "
            + objectType
            + "_NAME FROM INFORMATION_SCHEMA."
            + objectType
            + "S WHERE "
            + objectType
            + "_SCHEMA = ?";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, name);
    }

    @Override
    public Table getTable(final String tableName) {
        return new H2Table(jdbcTemplate, database, this, tableName);
    }
}
