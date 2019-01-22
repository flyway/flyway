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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Snowflake implementation of Flyway Table
 */
public class SnowflakeTable extends Table {

    private static final Log LOG = LogFactory.getLog(SnowflakeTable.class);

    public SnowflakeTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        LOG.debug("Creating new SnowflakeTable");
    }

    //
    // NOTE: the following are overridden to implement
    //

    @Override
    protected boolean doExists() throws SQLException {
        SnowflakeSchema snowflakeSchema = (SnowflakeSchema)schema;
        List<Map<String, String>> objects = snowflakeSchema.getObjects(SnowflakeObjectType.TABLES, name, "name");
        return objects.size() > 0;
    }

    @Override
    protected void doLock() throws SQLException {
        // nop
        return;
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

}
