package org.flywaydb.database.mongodb;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;

public class MongoDBDatabaseType extends BaseDatabaseType {

    @Override
    public String getName() {
        return "MongoDB";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:mongodb:") || url.startsWith("jdbc:mongodb+srv:");
    }


    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        return "com.dbschema.MongoJdbcDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Mongo DB");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new MongoDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new MongoDBParser(configuration, parsingContext);
    }

}