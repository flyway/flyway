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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.util.List;

/**
 * Database migration based on a sql file.
 */
public class SqlMigrationExecutor implements MigrationExecutor {
    /**
     * Database-specific support.
     */
    private final Database database;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The Resource pointing to the sql script.
     * The complete sql script is not held as a member field here because this would use the total size of all
     * sql migrations files in heap space during db migration, see issue 184.
     */
    private final LoadableResource resource;








    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    /**
     * The SQL script that will be executed.
     */
    private SqlScript sqlScript;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param database            The database-specific support.
     * @param resource            The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    SqlMigrationExecutor(Database database, LoadableResource resource, PlaceholderReplacer placeholderReplacer



            , Configuration configuration) {
        this.database = database;
        this.resource = resource;
        this.placeholderReplacer = placeholderReplacer;



        this.configuration = configuration;
    }

    @Override
    public void execute(Connection connection) {
        getSqlScript().execute(database.getMigrationConnection().getJdbcTemplate());
    }

    private synchronized SqlScript getSqlScript() {
        if (sqlScript == null) {
            sqlScript = database.createSqlScript(resource,
                    placeholderReplacer,
                    configuration.isMixed()



            );
        }
        return sqlScript;
    }

    @Override
    public boolean executeInTransaction() {
        return getSqlScript().executeInTransaction();
    }
}