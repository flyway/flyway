/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.database.mongodb;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

public class MongoDBSchema extends Schema<MongoDBDatabase, MongoDBTable> {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public MongoDBSchema(JdbcTemplate jdbcTemplate, MongoDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("db.getMongo().getDBNames().indexOf('"+name+"')") >= 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("db.getSiblingDB('" + name + "').dropDatabase()");
    }

    @Override
    protected void doClean() throws SQLException {
        for (MongoDBTable table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected MongoDBTable[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("db.getSiblingDB('" + name + "').getCollectionNames().filter(function (c) { return !c.startsWith(\"system\"); })");
        MongoDBTable[] tables = new MongoDBTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new MongoDBTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new MongoDBTable(jdbcTemplate, database, this, tableName);
    }
}