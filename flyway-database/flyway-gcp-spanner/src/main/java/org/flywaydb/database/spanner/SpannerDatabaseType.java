package org.flywaydb.database.spanner;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;






import java.sql.Connection;
import java.sql.Types;

@CustomLog
public class SpannerDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "Google Cloud Spanner";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public int getPriority() {
        // All regular database types (including non-beta Spanner support) take priority over this beta
        return -1;
    }

    @Override
    public boolean supportsReadOnlyTransactions() {
        return true;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return (url.startsWith("jdbc:cloudspanner:") || url.startsWith("jdbc:p6spy:cloudspanner:"));
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:cloudspanner:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.google.cloud.spanner.jdbc.JdbcDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.contains("Google Cloud Spanner");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SpannerDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new SpannerParser(configuration, parsingContext);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !url.contains("credentials=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        return !url.contains("credentials=");
    }

    @Override
    public void printMessages(Configuration configuration) {



            LOG.info(""); //this can go when the beta message above is retired.
            LOG.info("Experiencing performance issues while using GCP Spanner?");
            LOG.info("Find out how Flyway Teams improves performance with batching at " +
                             FlywayDbWebsiteLinks.TEAMS_FEATURES_FOR_CLOUD_SPANNER);
            LOG.info("");



    }
}