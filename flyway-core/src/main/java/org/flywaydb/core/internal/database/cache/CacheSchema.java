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
package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.sqlite.SQLiteSchema;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Cache implementation of Schema.
 */
public class CacheSchema extends Schema<CacheDatabase> {

    private static final Log LOG = LogFactory.getLog(SQLiteSchema.class);
    /**
     * Creates a new Cache schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    CacheSchema(JdbcTemplate jdbcTemplate, CacheDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() {
        return true;
    }

    @Override
    protected boolean doEmpty() {
        return false;
    }

    @Override
    protected void doCreate() {
        LOG.info("Cache does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() {
        LOG.info("Cache does not support dropping schemas. Schema not dropped: " + name);
    }

    @Override
    protected void doClean() {
        Stream.of(allTables()).forEach(Table::drop);
    }


    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                        //Search for all the table names in the schema
                        "SELECT SqlTableName from %dictionary.compiledclass where SqlSchemaName = ?", name);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.
        return tableNames.stream().map(tableName -> new CacheTable(jdbcTemplate, database, this, tableName)).toArray(Table[]::new);
    }

    @Override
    public Table getTable(String tableName) {
        return new CacheTable(jdbcTemplate, database, this, tableName);
    }
}