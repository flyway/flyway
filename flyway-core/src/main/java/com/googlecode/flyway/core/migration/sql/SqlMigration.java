/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.migration.sql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.BaseMigration;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends BaseMigration {
    /**
     * The resource containing the sql script.
     */
    private final Resource sqlScriptResource;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The encoding of this Sql migration.
     */
    private final String encoding;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of this Sql migration.
     */
    public SqlMigration(Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding, String versionString) {
        initVersion(versionString);
        scriptName = "Sql File: " + sqlScriptResource.getFilename();

        this.sqlScriptResource = sqlScriptResource;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = encoding;
    }

    @Override
    public void doMigrate(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        String sqlScriptSource = ResourceUtils.loadResourceAsString(sqlScriptResource, encoding);
        SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }
}
