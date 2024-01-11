package org.flywaydb.community.database.postgresql.yugabytedb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.database.postgresql.PostgreSQLDatabase;

import java.sql.Connection;

public class YugabyteDBDatabase extends PostgreSQLDatabase {

    public YugabyteDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected YugabyteDBConnection doGetConnection(Connection connection) {
        return new YugabyteDBConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        // Checks the Postgres version
        ensureDatabaseIsRecentEnough("11.2");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

}