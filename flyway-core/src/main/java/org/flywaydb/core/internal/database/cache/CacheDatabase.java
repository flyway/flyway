package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Cache database.
 */
public class CacheDatabase extends Database<CacheConnection> {

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public CacheDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit) {
        super(configuration, connection, originalAutoCommit);
    }

    @Override
    protected CacheConnection getConnection(Connection connection) {
        return new CacheConnection(configuration, this, connection, originalAutoCommit);
    }

    @Override
    protected void ensureSupported() {
        String version = majorVersion + "." + minorVersion;
        if (majorVersion < 2015) {
            throw new FlywayDbUpgradeRequiredException("Cache", version, "2015.2");
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource sqlScriptResource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed) {
        return new CacheSqlScript(configuration, sqlScriptResource, mixed, placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "Cache";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return cacheQuote(identifier);
    }

    @Override
    public boolean catalogIsSchema() { return false; }

    @Override
    public boolean useSingleConnection() { return false; }

    static String cacheQuote(String identifier) {
        return "\"" + identifier + "\"";
    }
}