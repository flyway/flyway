package org.flywaydb.database.singlestore;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class SingleStoreDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "SingleStoreDB";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:singlestore:") || url.startsWith("jdbc:p6spy:singlestore:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:singlestore:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.singlestore.jdbc.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.contains("SingleStore");
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("connectionAttributes",
            String.format("_connector_name:%s,_connector_version:%s,_product_version:%s,program_name:%s,program_version:%s,program_vendor:%s",
                "SingleStoreDB Flyway connector",
                VersionPrinter.getVersion(),
                VersionPrinter.getVersion(),
                "Redgate_Flyway",
                VersionPrinter.getVersion(),
                "Redgate"
            ));
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SingleStoreDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new SingleStoreParser(configuration, parsingContext);
    }

    @Override
    public String instantiateClassExtendedErrorMessage() {
        return "Failure probably due to inability to load dependencies. Please ensure you have downloaded 'https://mvnrepository.com/artifact/com.singlestore/singlestore-jdbc-client' and extracted to 'flyway/drivers' folder";
    }
}