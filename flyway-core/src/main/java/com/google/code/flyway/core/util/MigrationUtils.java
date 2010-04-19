package com.google.code.flyway.core.util;

import com.google.code.flyway.core.SchemaVersion;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Collection of utility methods used in migrations.
 *
 * @author Axel Fontaine
 */
public class MigrationUtils {
    /**
     * Prevents instantiation.
     */
    private MigrationUtils() {
        //Nothing
    }

    /**
     * Executes this sql script using this jdbcTemplate.
     *
     * @param jdbcTemplate   To execute the script.
     * @param scriptResource Classpath resource containing the script.
     */
    public static void executeSqlScript(SimpleJdbcTemplate jdbcTemplate, Resource scriptResource) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(scriptResource.getInputStream(), "UTF-8");
            String[] sqlStatements = SqlScriptParser.parse(reader);
            for (String sqlStatement : sqlStatements) {
                jdbcTemplate.update(sqlStatement);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute sql script: " + scriptResource.getFilename(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * Extracts the schema version from a migration name formatted as V1_2__Description.
     *
     * @param migrationName The string to parse.
     * @return The extracted schema version.
     */
    public static SchemaVersion extractSchemaVersion(String migrationName) {
        // Drop the leading V
        String version = migrationName.substring(1);

        // Handle the description
        String description = null;
        int descriptionPos = version.indexOf("__");
        if (descriptionPos != -1) {
            description = version.substring(descriptionPos + 2).replaceAll("_", " ");
            version = version.substring(0, descriptionPos);
        }

        return new SchemaVersion(version.replace("_", "."), description);
    }
}
