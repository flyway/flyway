/**
 * Copyright (C) 2010-2012 the original author or authors.
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
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;

import java.util.zip.CRC32;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends Migration {
    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The Resource pointing to the sql script.
     * The complete sql script is not held as a member field here because this would use the total size of all
     * sql migrations files in heap space during db migration, see issue 184.
     */
    private final ClassPathResource sqlScriptResource;

    /**
     * The encoding of the sql script.
     */
    private final String encoding;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of this Sql migration.
     * @param versionString       The migration name in standard Flyway format '<VERSION>__<DESCRIPTION>, e.g.
     *                            1_2__Description
     * @param scriptName          The filename of this sql script, including the relative path from the root of
     *                            the classpath location it was found.
     */
    public SqlMigration(ClassPathResource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding,
                        String versionString, String scriptName) {
        this.sqlScriptResource = sqlScriptResource;
        this.encoding = encoding;

        schemaVersion = MigrationInfoHelper.extractSchemaVersion(versionString);
        description = MigrationInfoHelper.extractDescription(versionString);

        String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
        checksum = calculateChecksum(sqlScriptSource);

        this.script = scriptName;
        this.placeholderReplacer = placeholderReplacer;
    }

    @Override
    public String getLocation() {
        return sqlScriptResource.getLocationOnDisk();
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
        SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptSource, placeholderReplacer);
        sqlScript.execute(jdbcTemplate);
    }

    /**
     * Calculates the checksum of this sql script.
     *
     * @param sql The sql to calculate the checksum for.
     * @return The crc-32 checksum of the script.
     */
    private int calculateChecksum(String sql) {
        final CRC32 crc32 = new CRC32();
        crc32.update(sql.getBytes());
        return (int) crc32.getValue();
    }

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.SQL;
    }
}
