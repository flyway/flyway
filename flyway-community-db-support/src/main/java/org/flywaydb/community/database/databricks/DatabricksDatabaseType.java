package org.flywaydb.community.database.databricks;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class DatabricksDatabaseType extends BaseDatabaseType {
    private static final String DATABRICKS_JDBC_DRIVER = "com.databricks.client.jdbc.Driver";
    private static final String DATABRICKS_JDBC41_DRIVER = "com.databricks.client.jdbc41.Driver";

    @Override
    public String getName() {
        return "Databricks";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:databricks:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        return "com.databricks.client.jdbc42.Driver";
    }

    @Override
    public String getBackupDriverClass(String url, ClassLoader classLoader) {
        if (ClassUtils.isPresent(DATABRICKS_JDBC41_DRIVER, classLoader)) {
            return DATABRICKS_JDBC41_DRIVER;
        }
        return DATABRICKS_JDBC_DRIVER;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("SparkSQL");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new DatabricksDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new DatabricksParser(configuration, parsingContext);
    }
}
