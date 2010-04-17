package com.google.code.flyway.core.sql;

import com.google.code.flyway.core.Migration;
import com.google.code.flyway.core.SchemaVersion;
import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

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
     * Creates a new sql file migration.
     *
     * @param resource The resource containing the sql script. In order to correctly guess the target schema version,
     *                 the resource should follow this pattern: sql/Vmajor_minor.sql .
     */
    public SqlMigration(Resource resource) {
        this.resource = resource;
        String versionStr = extractVersionStringFromFileName(resource.getFilename());
        this.schemaVersion = MigrationUtils.extractSchemaVersion(versionStr);
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
        MigrationUtils.executeSqlScript(jdbcTemplate, resource);
    }
}
