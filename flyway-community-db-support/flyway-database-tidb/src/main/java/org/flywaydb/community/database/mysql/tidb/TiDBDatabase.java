package org.flywaydb.community.database.mysql.tidb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.database.mysql.MySQLConnection;
import org.flywaydb.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;

public class TiDBDatabase extends MySQLDatabase {

    public TiDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected MySQLConnection doGetConnection(Connection connection) {
        return new TiDBConnection(this, connection);
    }

    @Override
    protected boolean isCreateTableAsSelectAllowed() {return false;}
}