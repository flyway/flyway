package org.flywaydb.community.database.mysql.tidb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.database.mysql.MySQLConnection;
import org.flywaydb.database.mysql.MySQLDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;

public class TiDBDatabaseType extends MySQLDatabaseType {
    @Override
    public String getName() {
        return "TiDB";
    }

    @Override
    public int getPriority() {
        // TiDB needs to be checked in advance of MySql
        return 1;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return (databaseProductName.contains("MySQL") && databaseProductVersion.contains("TiDB"))
                || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("TiDB"));
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new TiDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }
}