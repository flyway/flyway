package com.google.code.flyway.core;

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
     * Extracts the schema version from a version string formatted as Vmajor_minor.
     *
     * @param versionStr The string to parse.
     * @return The extracted schema version.
     */
    public static SchemaVersion extractSchemaVersion(String versionStr) {
        int underscorePosition = versionStr.indexOf("_");
        int major = Integer.parseInt(versionStr.substring(1, underscorePosition));

        String remainder = versionStr.substring(underscorePosition + 1);
        int nextUnderscorePosition = remainder.indexOf("_");
        int minor;
        if (nextUnderscorePosition < 0) {
            minor = Integer.parseInt(remainder);
        } else {
            minor = Integer.parseInt(remainder.substring(0, nextUnderscorePosition));
        }

        return new SchemaVersion(major, minor);
    }
}
