package org.flywaydb.community.database.databricks;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabricksDatabase extends Database<DatabricksConnection> {
    public DatabricksDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected DatabricksConnection doGetConnection(Connection connection) {
        return new DatabricksConnection(this, connection);
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user() as user;");
    }

    @Override
    public void ensureSupported() {
        // Always latest Databricks version.
    }

    @Override
    public boolean supportsDdlTransactions() {
        // Databricks i non-transactional
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public boolean supportsMultiStatementTransactions() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    @Override
    public String doQuote(String identifier) {
        return getOpenQuote() + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote()) + getCloseQuote();
    }

    @Override
    protected String getOpenQuote() {
        return "`";
    }

    @Override
    protected String getCloseQuote() {
        return "`";
    }

    @Override
    public String getEscapedQuote() {
        return "\\`";
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String sql = "CREATE TABLE " + table + " (\n" +
                "    `installed_rank` INT NOT NULL,\n" +
                "    `version` STRING,\n" +
                "    `description` STRING NOT NULL,\n" +
                "    `type` STRING NOT NULL,\n" +
                "    `script` STRING NOT NULL,\n" +
                "    `checksum` INT,\n" +
                "    `installed_by` STRING NOT NULL,\n" +
                "    `installed_on` TIMESTAMP NOT NULL,\n" +
                "    `execution_time` INT NOT NULL,\n" +
                "    `success` BOOLEAN NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "");
        return sql;
    }

    @Override
    public String getInsertStatement(Table table) {
        // Explicitly set installed_on to CURRENT_TIMESTAMP().
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("installed_on")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(), ?, ?)";
    }
}
