package org.flywaydb.database.informix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Informix database.
 */
public class InformixDatabase extends Database<InformixConnection> {

    public InformixDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected InformixConnection doGetConnection(Connection connection) {
        return new InformixConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("12.10");
        recommendFlywayUpgradeIfNecessary("12.10");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " IN \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table + " (\n" +
                "    installed_rank INT NOT NULL,\n" +
                "    version VARCHAR(50),\n" +
                "    description VARCHAR(200) NOT NULL,\n" +
                "    type VARCHAR(20) NOT NULL,\n" +
                "    script LVARCHAR(1000) NOT NULL,\n" +
                "    checksum INT,\n" +
                "    installed_by VARCHAR(100) NOT NULL,\n" +
                "    installed_on DATETIME YEAR TO FRACTION(3) DEFAULT CURRENT YEAR TO FRACTION(3) NOT NULL,\n" +
                "    execution_time INT NOT NULL,\n" +
                "    success SMALLINT NOT NULL\n" +
                ")" + tablespace + ";\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT CHECK (success in (0,1)) CONSTRAINT " + table.getName() + "_s;\n" +
                "ALTER TABLE " + table + " ADD CONSTRAINT PRIMARY KEY (installed_rank) CONSTRAINT " + table.getName() + "_pk;\n" +
                "CREATE INDEX " + table.getName() + "_s_idx ON " + table + " (success);";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getJdbcMetaData().getUserName();
    }

    @Override
    public boolean supportsDdlTransactions() {
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
    public String getOpenQuote() {
        return "";
    }

    @Override
    public String getCloseQuote() {
        return "";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}