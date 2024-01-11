package org.flywaydb.community.database.postgresql.yugabytedb;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;

import java.sql.Connection;
import java.util.regex.Pattern;

public class YugabyteDBDatabaseType extends PostgreSQLDatabaseType {
    @Override
    public String getName() {
        return "YugabyteDB";
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:p6spy:postgresql:");
    }

    @Override
    public int getPriority() {
        // Should be checked before plain PostgreSQL
        return 1;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        // The YB is what distinguishes Yugabyte
        return databaseProductName.startsWith("PostgreSQL")
                && Pattern.matches("PostgreSQL\\s\\d{1,2}(\\.\\d{1,2})?-YB-\\d{1,2}(\\.\\d{1,2})?.*", getSelectVersionOutput(connection));
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new YugabyteDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new YugabyteDBParser(configuration, parsingContext);
    }
}