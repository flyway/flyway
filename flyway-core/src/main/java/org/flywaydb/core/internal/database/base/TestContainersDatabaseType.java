package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;

public class TestContainersDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "Testcontainers";
    }

    @Override
    public int getNullType() {
        return 0;
    }

    @Override
    public boolean supportsReadOnlyTransactions() {
        return false;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:tc:") || url.startsWith("jdbc:p6spy:tc:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:tc:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "org.testcontainers.jdbc.ContainerDatabaseDriver";
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !url.contains("user=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        return !url.contains("password=");
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return false;
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        throw new IllegalStateException();
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        throw new IllegalStateException();
    }
}