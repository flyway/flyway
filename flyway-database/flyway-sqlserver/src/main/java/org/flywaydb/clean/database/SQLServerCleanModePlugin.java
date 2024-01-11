package org.flywaydb.clean.database;

import org.flywaydb.core.extensibility.CleanModePlugin;
import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.database.sqlserver.SQLServerDatabase;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SQLServerCleanModePlugin implements CleanModePlugin<SQLServerDatabase> {
    @Override
    public boolean handlesMode(Mode cleanMode) {
        return cleanMode == Mode.ALL;
    }

    @Override
    public boolean handlesDatabase(Database database) {
        return database instanceof SQLServerDatabase;
    }

    @Override
    public void cleanDatabasePostSchema(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        if (supportsColumnEncryptionKeys(database)) {
            for (String statement : cleanColumnEncryptionKeys(database, jdbcTemplate)) {
                jdbcTemplate.execute(statement);
            }
        }

        if (supportsColumnMasterKeys(database)) {
            for (String statement : cleanColumnMasterKeys(database, jdbcTemplate)) {
                jdbcTemplate.execute(statement);
            }
        }

        for (String statement : cleanSymmetricKey(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanEventNotifications(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanDatabaseExtendedProperties(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanDatabaseRoles(database, jdbcTemplate)) {
            executeIgnoringErrors(jdbcTemplate, statement);
        }

        boolean oldAutoCommit = jdbcTemplate.getConnection().getAutoCommit();
        jdbcTemplate.getConnection().setAutoCommit(true);
        for (String statement : cleanFulltextStoplist(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : cleanSearchPropertyList(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : cleanFullTextCatalogs(database, jdbcTemplate)) {
            jdbcTemplate.execute(statement);
        }
        jdbcTemplate.getConnection().setAutoCommit(oldAutoCommit);
    }

    private void executeIgnoringErrors(JdbcTemplate jdbcTemplate, String statement) {
        try {
            jdbcTemplate.execute(statement);
        } catch (Exception ignored) {
        }
    }

    private boolean supportsColumnEncryptionKeys(SQLServerDatabase database) {
        return database.getVersion().isAtLeast("13");
    }

    private boolean supportsColumnMasterKeys(SQLServerDatabase database) {
        return database.getVersion().isAtLeast("13");
    }

    private List<String> cleanFullTextCatalogs(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.fulltext_catalogs").stream()
                           .map(name -> "DROP FULLTEXT CATALOG " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanColumnEncryptionKeys(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.column_encryption_keys").stream()
                           .map(name -> "DROP COLUMN ENCRYPTION KEY " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanColumnMasterKeys(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.column_master_keys").stream()
                           .map(name -> "DROP COLUMN MASTER KEY " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanSymmetricKey(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.symmetric_keys").stream()
                           .map(name -> "DROP SYMMETRIC KEY " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanEventNotifications(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.event_notifications").stream()
                           .map(name -> "DROP EVENT NOTIFICATION " + database.quote(name) + " ON DATABASE")
                           .collect(Collectors.toList());
    }

    private List<String> cleanDatabaseExtendedProperties(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.extended_properties WHERE class = 0").stream()
                           .map(name -> "EXEC sp_dropextendedproperty  @name= " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanDatabaseRoles(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("" +
                                                       "SELECT name FROM sys.database_principals " +
                                                       "WHERE (type = 'A' OR type = 'R') AND is_fixed_role = 0").stream()
                           .map(name -> "DROP ROLE " + database.quote(name))
                           .collect(Collectors.toList());
    }

    private List<String> cleanFulltextStoplist(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.fulltext_stoplists").stream()
                           .map(name -> "DROP FULLTEXT STOPLIST " + database.quote(name) + ";")
                           .collect(Collectors.toList());
    }

    private List<String> cleanSearchPropertyList(SQLServerDatabase database, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.queryForStringList("SELECT name FROM sys.registered_search_property_lists").stream()
                           .map(name -> "DROP SEARCH PROPERTY LIST " + database.quote(name) + ";")
                           .collect(Collectors.toList());
    }
}