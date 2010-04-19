package com.google.code.flyway.core;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.SQLException;

/**
 * A migration of a single version of the schema.
 *
 * @author Axel Fontaine
 */
public interface Migration {
    /**
     * @return The schema version after the migration is complete.
     */
    SchemaVersion getVersion();

    /**
     * @return The script name for the migration history.
     */
    String getScriptName();

    /**
     * Performs the migration.
     *
     * @param jdbcTemplate To execute the migration statements.
     */
    void migrate(SimpleJdbcTemplate jdbcTemplate);
}
