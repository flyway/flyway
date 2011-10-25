/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import java.util.zip.CRC32;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration extends Migration {
    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The source of the Sql script, loaded on demand.
     */
    private final String sqlScriptSource;

    /**
     * The location of this sql migration
     */
    private final String location;

    /**
     * Creates a new sql script migration based on this sql script.
     *
     * @param sqlScriptResource   The resource containing the sql script.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of this Sql migration.
     * @param versionString       The migration name in standard Flyway format '<VERSION>__<DESCRIPTION>, e.g.
     *                            1_2__Description
     * @param scriptName          The filename of this sql script, including the relative path from the root of
     *                            baseDir.
     */
    public SqlMigration(Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding,
                        String versionString, String scriptName) {
        schemaVersion = MigrationInfoHelper.extractSchemaVersion(versionString);
        description = MigrationInfoHelper.extractDescription(versionString);

        this.sqlScriptSource = ResourceUtils.loadResourceAsString(sqlScriptResource, encoding);
        checksum = calculateChecksum(sqlScriptSource);

        this.script = scriptName;
        this.placeholderReplacer = placeholderReplacer;
        this.location = ResourceUtils.getResourceLocation(sqlScriptResource);
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        SqlScript sqlScript = dbSupport.createSqlScript(sqlScriptSource, placeholderReplacer);
        sqlScript.execute(jdbcTemplate);
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

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.SQL;
    }
}
