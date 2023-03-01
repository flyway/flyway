package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;

public class ClickHouseDatabase extends Database<ClickHouseConnection>
{
    @Override
    public boolean useSingleConnection() {
        return true;
    }

    public ClickHouseDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    public String getClusterName() {
        return configuration.getPluginRegister().getPlugin(ClickHouseConfigurationExtension.class).getClusterName();
    }

    @Override
    protected ClickHouseConnection doGetConnection(Connection connection) {
        return new ClickHouseConnection(this, connection);
    }

    @Override
    public void ensureSupported() {
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsMultiStatementTransactions() {
        return false;
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
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String clusterName = getClusterName();
        boolean isClustered = StringUtils.hasText(clusterName);

        String script = "CREATE TABLE IF NOT EXISTS " + table + (isClustered ? (" ON CLUSTER " + clusterName) : "") + "(" +
                "    installed_rank Int32," +
                "    version String," +
                "    description String," +
                "    type String," +
                "    script String," +
                "    checksum Nullable(Int32)," +
                "    installed_by String," +
                "    installed_on DateTime DEFAULT now()," +
                "    execution_time Int32," +
                "    success Bool" +
                ")";

        if (isClustered) {
            script += " ENGINE = ReplicatedMergeTree('/clickhouse/tables/{shard}/{database}/{table}', '{replica}')" +
                    " PARTITION BY tuple()" +
                    " ORDER BY (installed_rank);";
        } else {
            script += " ENGINE = MergeTree PRIMARY KEY (version);";
        }

        return script + (baseline ? getBaselineStatement(table) + ";" : "");
    }
}
