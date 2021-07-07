/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.yugabytedb;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLSchema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class YugabyteDBSchema extends PostgreSQLSchema {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public YugabyteDBSchema(JdbcTemplate jdbcTemplate, YugabyteDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    public Table getTable(String tableName) {
        return new YugabyteDBTable(jdbcTemplate, (YugabyteDBDatabase) database, this, tableName);
    }
}
