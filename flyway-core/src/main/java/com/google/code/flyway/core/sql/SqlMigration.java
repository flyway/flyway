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

package com.google.code.flyway.core.sql;

import com.google.code.flyway.core.Migration;
import com.google.code.flyway.core.SchemaVersion;
import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.util.Map;

/**
 * Database migration based on a sql file.
 */
public class SqlMigration implements Migration {
    /**
     * The resource containing the sql script.
     */
    private Resource resource;

    /**
     * The target schema version of this migration.
     */
    private SchemaVersion schemaVersion;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    private Map<String, String> placeholders;

    /**
     * Creates a new sql file migration.
     *
     * @param resource The resource containing the sql script. In order to correctly guess the target schema version,
     *                 the resource should follow this pattern: sql/Vmajor_minor.sql .
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    public SqlMigration(Resource resource, Map<String, String> placeholders) {
        this.resource = resource;
        String versionStr = extractVersionStringFromFileName(resource.getFilename());
        this.schemaVersion = MigrationUtils.extractSchemaVersion(versionStr);
        this.placeholders = placeholders;
    }

    /**
     * Extracts the sql file version string from this file name.
     *
     * @param fileName The file name to parse.
     * @return The version string.
     */
    /*private -> for testing*/
    static String extractVersionStringFromFileName(String fileName) {
        int lastDirSeparator = fileName.lastIndexOf("/");
        int extension = fileName.lastIndexOf(".sql");

        return fileName.substring(lastDirSeparator + 1, extension);
    }

    @Override
    public SchemaVersion getVersion() {
        return schemaVersion;
    }

    @Override
    public String getScriptName() {
        return "Sql File: " + resource.getFilename();
    }

    @Override
    public void migrate(SimpleJdbcTemplate jdbcTemplate) {
        MigrationUtils.executeSqlScript(jdbcTemplate, resource, placeholders);
    }
}
