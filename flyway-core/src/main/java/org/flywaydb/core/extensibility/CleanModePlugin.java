package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public interface CleanModePlugin<T extends Database> extends Plugin {
    boolean handlesMode(Mode cleanMode);

    boolean handlesDatabase(Database database);

    void cleanDatabasePostSchema(T database, JdbcTemplate jdbcTemplate) throws SQLException;
}