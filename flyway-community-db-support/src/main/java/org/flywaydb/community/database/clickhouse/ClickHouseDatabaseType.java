package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;

public class ClickHouseDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "ClickHouse";
    }

    @Override
    public int getNullType() {
        return 0;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:clickhouse:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        return "com.clickhouse.jdbc.ClickHouseDriver";
    }

    @Override
    public String getBackupDriverClass(String url, ClassLoader classLoader) {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return true;
    }

    @Override
    public ClickHouseDatabase createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new ClickHouseDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new ClickHouseParser(configuration, parsingContext, 3);
    }
}
