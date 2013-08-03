/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.resolver.sql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.resolver.MigrationExecutor;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.Resource;

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
     * The encoding of the sql script.
     */
    private final String encoding;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param dbSupport           The database-specific support.
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of this Sql migration.
     */
    public SqlMigrationExecutor(DbSupport dbSupport, Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding) {
        this.dbSupport = dbSupport;
        this.sqlScriptResource = sqlScriptResource;
        this.encoding = encoding;
        this.placeholderReplacer = placeholderReplacer;
    }

    public void execute(Connection connection) {
        String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
        String sqlScriptSourceNoPlaceholders = placeholderReplacer.replacePlaceholders(sqlScriptSource);
        SqlScript sqlScript = new SqlScript(sqlScriptSourceNoPlaceholders, dbSupport);
        sqlScript.execute(new JdbcTemplate(connection, 0));
    }
}
