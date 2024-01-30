package org.flywaydb.database.derby;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

@CustomLog
public class DerbyDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "Derby";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:derby:") || url.startsWith("jdbc:p6spy:derby:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:derby:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        if (url.startsWith("jdbc:derby://")) {
            return "org.apache.derby.jdbc.ClientDriver";
        }
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Apache Derby");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new DerbyDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new DerbyParser(configuration, parsingContext);
    }

    @Override
    public void shutdownDatabase(String url, Driver driver) {

        // only do this on the embedded version
        if (!url.startsWith("jdbc:derby://")) {
            try {
                int i = url.indexOf(";");
                String shutdownUrl = (i < 0 ? url : url.substring(0, i)) + ";shutdown=true";

                driver.connect(shutdownUrl, new Properties());
            } catch (SQLException e) {
                LOG.debug("Unexpected error on Derby Embedded Database shutdown: " + e.getMessage());
            }
        }
    }
}