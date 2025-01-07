/*-
 * ========================LICENSE_START=================================
 * flyway-database-cassandra
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
package org.flywaydb.database.cassandra;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;

public class CassandraConnection extends Connection<CassandraDatabase> {

    private static final String DEFAULT_KEYSPACE = "system";

    protected CassandraConnection(CassandraDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return getJdbcConnection().getSchema() == null ? DEFAULT_KEYSPACE : getJdbcConnection().getSchema();
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("USE " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new CassandraSchema(jdbcTemplate, database, name);
    }
}
