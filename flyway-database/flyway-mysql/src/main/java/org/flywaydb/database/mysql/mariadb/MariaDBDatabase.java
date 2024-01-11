package org.flywaydb.database.mysql.mariadb;

import org.flywaydb.core.api.configuration.Configuration;
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
}