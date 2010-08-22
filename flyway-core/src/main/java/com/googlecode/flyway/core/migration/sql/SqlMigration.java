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
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.zip.CRC32;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends BaseMigration {
    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The source of the Sql script, loaded on demand.
     */
    private String sqlScriptSource;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param versionString The migration name in standard Flyway format '<VERSION>__<DESCRIPTION>, e.g. 1_2__Description
     * @param encoding            The encoding of this Sql migration.
     */
    public SqlMigration(Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding, String versionString) {
        initVersion(versionString);
        sqlScriptSource = ResourceUtils.loadResourceAsString(sqlScriptResource, encoding);
        checksum = calculateChecksum(sqlScriptSource);

        // old scriptName = "Sql File: " + sqlScriptResource.getFilename();
        this.scriptName = sqlScriptResource.getFilename();
        this.placeholderReplacer = placeholderReplacer;
        this.migrationType = MigrationType.SQL;
    }

    @Override
    public void doMigrate(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }

    /**
     * Calculates the checksum of this sql script.
     *
     * @return The crc-32 checksum of the script.
     */
    private int calculateChecksum(String sql) {
        final CRC32 crc32 = new CRC32();
        crc32.update(sql.getBytes());
        return (int) crc32.getValue();
    }
}
