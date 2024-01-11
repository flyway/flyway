package org.flywaydb.database.derby;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Derby database.
 */
public class DerbyDatabase extends Database<DerbyConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     */
    public DerbyDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected DerbyConnection doGetConnection(Connection connection) {
        return new DerbyConnection(this, connection);
    }

    @Override
    public final void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("10.11.1.1");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.14", Tier.PREMIUM, configuration);

        recommendFlywayUpgradeIfNecessary("10.15");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "CREATE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT CURRENT_USER FROM SYSIBM.SYSDUMMY1");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "true";
    }

    @Override
    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}