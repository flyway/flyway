/*
 * Copyright 2010-2020 Redgate Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBDatabase;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBParser;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBRetryingStrategy;
import org.flywaydb.core.internal.database.db2.DB2Database;
import org.flywaydb.core.internal.database.db2.DB2Parser;
import org.flywaydb.core.internal.database.derby.DerbyDatabase;
import org.flywaydb.core.internal.database.derby.DerbyParser;

import org.flywaydb.core.internal.database.firebird.FirebirdDatabase;
import org.flywaydb.core.internal.database.firebird.FirebirdParser;
import org.flywaydb.core.internal.database.h2.H2Database;
import org.flywaydb.core.internal.database.h2.H2Parser;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBDatabase;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBParser;
import org.flywaydb.core.internal.database.informix.InformixDatabase;
import org.flywaydb.core.internal.database.informix.InformixParser;
import org.flywaydb.core.internal.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.database.mysql.MySQLParser;
import org.flywaydb.core.internal.database.oracle.OracleDatabase;
import org.flywaydb.core.internal.database.oracle.OracleParser;
import org.flywaydb.core.internal.database.oracle.OracleSqlScriptExecutor;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabase;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLParser;
import org.flywaydb.core.internal.database.redshift.RedshiftDatabase;
import org.flywaydb.core.internal.database.redshift.RedshiftParser;
import org.flywaydb.core.internal.database.saphana.SAPHANADatabase;
import org.flywaydb.core.internal.database.saphana.SAPHANAParser;
import org.flywaydb.core.internal.database.snowflake.SnowflakeDatabase;
import org.flywaydb.core.internal.database.snowflake.SnowflakeParser;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabase;
import org.flywaydb.core.internal.database.sqlite.SQLiteParser;
import org.flywaydb.core.internal.database.sqlserver.SQLServerDatabase;
import org.flywaydb.core.internal.database.sqlserver.SQLServerParser;
import org.flywaydb.core.internal.database.sybasease.SybaseASEDatabase;
import org.flywaydb.core.internal.database.sybasease.SybaseASEParser;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.*;

import java.sql.Connection;

import static org.flywaydb.core.internal.sqlscript.SqlScriptMetadata.getMetadataResource;

/**
 * Factory for obtaining the correct Database instance for the current connection.
 */
public class DatabaseFactory {
    private static final Log LOG = LogFactory.getLog(DatabaseFactory.class);

    /**
     * Prevent instantiation.
     */
    private DatabaseFactory() {
        //Do nothing
    }

    /**
     * Initializes the appropriate Database class for the database product used by the data source.
     *
     * @param configuration The Flyway configuration.
     * @param printInfo     Where the DB info should be printed in the logs.
     * @return The appropriate Database class.
     */
    public static Database createDatabase(Configuration configuration, boolean printInfo,
                                          JdbcConnectionFactory jdbcConnectionFactory



    ) {
        OracleDatabase.enableTnsnamesOraSupport();

        String databaseProductName = jdbcConnectionFactory.getProductName();
        if (printInfo) {
            LOG.info("Database: " + jdbcConnectionFactory.getJdbcUrl() + " (" + databaseProductName + ")");
            LOG.debug("Driver  : " + jdbcConnectionFactory.getDriverInfo());
        }

        DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();

        Database database = createDatabase(databaseType, configuration, jdbcConnectionFactory



        );

        String intendedCurrentSchema = configuration.getDefaultSchema();
        if (!database.supportsChangingCurrentSchema() && intendedCurrentSchema != null) {
            LOG.warn(databaseProductName + " does not support setting the schema for the current session. " +
                    "Default schema will NOT be changed to " + intendedCurrentSchema + " !");
        }

        return database;
    }

    private static Database createDatabase(DatabaseType databaseType, Configuration configuration,
                                           JdbcConnectionFactory jdbcConnectionFactory



    ) {
        switch (databaseType) {
            case COCKROACHDB:
                return new CockroachDBDatabase(configuration, jdbcConnectionFactory



                );
            case DB2:
                return new DB2Database(configuration, jdbcConnectionFactory



                );





            case DERBY:
                return new DerbyDatabase(configuration, jdbcConnectionFactory



                );
            case FIREBIRD:
                return new FirebirdDatabase(configuration, jdbcConnectionFactory



                );
            case H2:
                return new H2Database(configuration, jdbcConnectionFactory



                );
            case HSQLDB:
                return new HSQLDBDatabase(configuration, jdbcConnectionFactory



                );
            case INFORMIX:
                return new InformixDatabase(configuration, jdbcConnectionFactory



                );
            case MARIADB:
            case MYSQL:
                return new MySQLDatabase(configuration, jdbcConnectionFactory



                );
            case ORACLE:
                return new OracleDatabase(configuration, jdbcConnectionFactory



                );
            case POSTGRESQL:
                return new PostgreSQLDatabase(configuration, jdbcConnectionFactory



                );
            case REDSHIFT:
                return new RedshiftDatabase(configuration, jdbcConnectionFactory



                );
            case SNOWFLAKE:
                return new SnowflakeDatabase(configuration, jdbcConnectionFactory



                );
            case SQLITE:
                return new SQLiteDatabase(configuration, jdbcConnectionFactory



                );
            case SAPHANA:
                return new SAPHANADatabase(configuration, jdbcConnectionFactory



                );
            case SQLSERVER:
                return new SQLServerDatabase(configuration, jdbcConnectionFactory



                );
            case SYBASEASE_JCONNECT:
            case SYBASEASE_JTDS:
                return new SybaseASEDatabase(configuration, jdbcConnectionFactory



                );
            default:
                throw new FlywayException("Unsupported Database: " + databaseType.name());
        }
    }

    public static SqlScriptFactory createSqlScriptFactory(final JdbcConnectionFactory jdbcConnectionFactory,
                                                          final Configuration configuration,
                                                          final ParsingContext parsingContext) {
        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();















            return new SqlScriptFactory() {
                @Override
                public SqlScript createSqlScript(LoadableResource resource, boolean mixed, ResourceProvider resourceProvider) {
                    return new ParserSqlScript(createParser(jdbcConnectionFactory, configuration



                            , parsingContext
                    ), resource, getMetadataResource(resourceProvider, resource), mixed);
                }
            };



    }

    private static Parser createParser(JdbcConnectionFactory jdbcConnectionFactory, Configuration configuration



            , ParsingContext parsingContext
    ) {
        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();

        switch (databaseType) {
            case COCKROACHDB:
                return new CockroachDBParser(configuration, parsingContext);
            case DB2:
                return new DB2Parser(configuration, parsingContext);




            case DERBY:
                return new DerbyParser(configuration, parsingContext);
            case FIREBIRD:
                return new FirebirdParser(configuration, parsingContext);
            case H2:
                return new H2Parser(configuration, parsingContext);
            case HSQLDB:
                return new HSQLDBParser(configuration, parsingContext);
            case INFORMIX:
                return new InformixParser(configuration, parsingContext);
            case MARIADB:
            case MYSQL:
                return new MySQLParser(configuration, parsingContext);
            case ORACLE:
                return new OracleParser(configuration










                        , parsingContext
                );
            case POSTGRESQL:
                return new PostgreSQLParser(configuration, parsingContext);
            case REDSHIFT:
                return new RedshiftParser(configuration, parsingContext);
            case SQLITE:
                return new SQLiteParser(configuration, parsingContext);
            case SAPHANA:
                return new SAPHANAParser(configuration, parsingContext);
            case SNOWFLAKE:
                return new SnowflakeParser(configuration, parsingContext);
            case SQLSERVER:
                return new SQLServerParser(configuration, parsingContext);
            case SYBASEASE_JCONNECT:
            case SYBASEASE_JTDS:
                return new SybaseASEParser(configuration, parsingContext);
            default:
                throw new FlywayException("Unsupported Database: " + databaseType.name());
        }
    }

    public static SqlScriptExecutorFactory createSqlScriptExecutorFactory(
            final JdbcConnectionFactory jdbcConnectionFactory




    ) {
        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();




        if (DatabaseType.ORACLE == databaseType) {
            return new SqlScriptExecutorFactory() {
                @Override
                public SqlScriptExecutor createSqlScriptExecutor(Connection connection



                ) {
                    return new OracleSqlScriptExecutor(new JdbcTemplate(connection, databaseType)



                    );
                }
            };
        }

        return new SqlScriptExecutorFactory() {
            @Override
            public SqlScriptExecutor createSqlScriptExecutor(Connection connection



            ) {
                return new DefaultSqlScriptExecutor(new JdbcTemplate(connection, databaseType)



                );
            }
        };
    }

    public static DatabaseExecutionStrategy createExecutionStrategy(Connection connection) {
        if (connection == null) {
            return new DefaultExecutionStrategy();
        }

        DatabaseType databaseType = DatabaseType.fromJdbcConnection(connection);
        switch (databaseType) {
            case COCKROACHDB:
                return new CockroachDBRetryingStrategy();
            default:
                return new DefaultExecutionStrategy();
        }
    }
}