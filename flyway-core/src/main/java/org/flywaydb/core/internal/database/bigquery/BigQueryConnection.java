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
package org.flywaydb.core.internal.database.bigquery;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * BigQuery connection.
 */
public class BigQueryConnection extends Connection<BigQueryDatabase> {
    BigQueryConnection(BigQueryDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        // BigQuery has no concept of current schema, return DefaultDataset if it is set in JDBC, otherwise null.
        String defaultDataset = getJdbcClientOption("DefaultDataset");
        return StringUtils.hasText(defaultDataset) ? defaultDataset.trim() : null;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        // BigQuery has no concept of current schema, do nothing.
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        // BigQuery has no concept of current schema, return DefaultDataset if it is set in JDBC, otherwise null.
        String defaultDataset = getJdbcClientOption("DefaultDataset");
        return StringUtils.hasText(defaultDataset) ? getSchema(defaultDataset.trim()) : null;
    }

    public String getJdbcClientOption(String option) throws SQLException {
        return getJdbcConnection().getClientInfo(option);
    }

    public String getProjectIdRegionQualifier() throws SQLException {
        String projectId = getJdbcClientOption("ProjectId");
        String projectQualifier = StringUtils.hasText(projectId) ? database.quote(projectId.trim()) + "." : "";

        String location = getJdbcClientOption("Location");
        String regionQualifier = StringUtils.hasText(location) ? database.quote("region-" + location.trim()) + "." : "";

        return projectQualifier + regionQualifier;
    }

    @Override
    public Schema getSchema(String name) {
        return new BigQuerySchema(jdbcTemplate, database, name);
    }
}