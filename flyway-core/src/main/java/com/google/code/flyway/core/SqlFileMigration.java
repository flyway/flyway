package com.google.code.flyway.core;

import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Database migration based on a sql file.
 */
public class SqlFileMigration implements Migration {
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
    public SqlFileMigration(Resource resource) {
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

        String fileNameNoDirNoExtension = fileName.substring(lastDirSeparator + 1, extension);

        int comment = fileNameNoDirNoExtension.indexOf("-");
        if (comment == -1) {
            return fileNameNoDirNoExtension;
        }

        return fileNameNoDirNoExtension.substring(0, comment);
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
