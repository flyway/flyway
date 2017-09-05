/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;

/**
 * Database migration based on a sql file.
 */
public class SqlMigrationExecutor implements MigrationExecutor {

    /**
     * The Resource pointing to the sql script.
     * The complete sql script is not held as a member field here because this would use the total size of all
     * sql migrations files in heap space during db migration, see issue 184.
     */
    private final LoadableResource sqlScriptResource;

    /**
     * The Flyway configuration.
     */
    private final FlywayConfiguration configuration;

    /**
     * The SQL script that will be executed.
     */
    private SqlScript sqlScript;

    /**
     * Creates a new sql script migration based on this sql script.
     *  @param sqlScriptResource   The resource containing the sql script.
     * @param configuration       The Flyway configuration.
     */
    public SqlMigrationExecutor(LoadableResource sqlScriptResource, FlywayConfiguration configuration) {
        this.sqlScriptResource = sqlScriptResource;
        this.configuration = configuration;
    }

    @Override
    public void execute(Connection connection) {
        // TODO: This reverts parts of commit ca87e59aed534e4ad9bef610517fc595cf2c39c8
//        JdbcTemplate jdbcTemplate = connection == dbSupport.getJdbcTemplate().getConnection()
//                ? dbSupport.getJdbcTemplate()
//                : new JdbcTemplate(connection, 0);

        getSqlScript().execute(new JdbcTemplate(connection, 0));
    }

    private synchronized SqlScript getSqlScript() {
        if (sqlScript == null) {
            sqlScript = new SqlScript(sqlScriptResource, configuration);
        }
        return sqlScript;
    }

    @Override
    public boolean executeInTransaction() {
        return getSqlScript().executeInTransaction();
    }
}