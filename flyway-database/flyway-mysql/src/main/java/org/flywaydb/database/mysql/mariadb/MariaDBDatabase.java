package org.flywaydb.database.mysql.mariadb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

public class MariaDBDatabase extends MySQLDatabase {
    public MariaDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected String getConstraintName(String tableName) {
        return "";
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("5.1");
        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.3", Tier.PREMIUM, configuration);
        recommendFlywayUpgradeIfNecessary("11.2");
    }
}