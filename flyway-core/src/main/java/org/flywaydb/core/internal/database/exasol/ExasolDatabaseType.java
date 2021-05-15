package org.flywaydb.core.internal.database.exasol;

import java.sql.Connection;
import java.sql.Types;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

/**
 * @author artem
 */
public class ExasolDatabaseType extends BaseDatabaseType {

    @Override
    public String getName() {
        return "Exasol";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(final String url) {
        return url.startsWith("jdbc:exa:");
    }

    @Override
    public String getDriverClass(final String url, final ClassLoader classLoader) {
        return "com.exasol.jdbc.EXADriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(final String databaseProductName,
                                                        final String databaseProductVersion,
                                                        final Connection connection) {
        return databaseProductName.startsWith("EXASolution");
    }

    @Override
    public Database createDatabase(final Configuration configuration, final JdbcConnectionFactory jdbcConnectionFactory,
                                   final StatementInterceptor statementInterceptor) {
        return new ExasolDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(final Configuration configuration, final ResourceProvider resourceProvider,
                               final ParsingContext parsingContext) {
        return new ExasolParser(configuration, parsingContext);
    }
}
