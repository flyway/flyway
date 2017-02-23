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
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.sql.Connection;

/**
 * Database migration based on a sql file.
 */
public class SqlMigrationExecutor implements MigrationExecutor {
    /**
     * Database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The Resource pointing to the sql script.
     * The complete sql script is not held as a member field here because this would use the total size of all
     * sql migrations files in heap space during db migration, see issue 184.
     */
    private final Resource sqlScriptResource;

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
     *
     * @param dbSupport           The database-specific support.
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    public SqlMigrationExecutor(DbSupport dbSupport, Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, FlywayConfiguration configuration) {
        this.dbSupport = dbSupport;
        this.sqlScriptResource = sqlScriptResource;
        this.placeholderReplacer = placeholderReplacer;
        this.configuration = configuration;
    }

    @Override
    public void execute(Connection connection) {
        getSqlScript().execute(new JdbcTemplate(connection, 0));
    }

    private synchronized SqlScript getSqlScript() {
        if (sqlScript == null) {
            sqlScript = new SqlScript(dbSupport, sqlScriptResource, placeholderReplacer, configuration.getEncoding(), configuration.isAllowMixedMigrations());
        }
        return sqlScript;
    }

    @Override
    public boolean executeInTransaction() {
        return getSqlScript().executeInTransaction();
    }
}
