/*-
 * ========================LICENSE_START=================================
 * flyway-database-oracle
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.oracle;

import static org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import lombok.CustomLog;
import oracle.jdbc.OracleConnection;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.StringUtils;

import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.util.ClassUtils;
import java.util.logging.LogManager;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

@CustomLog
public class OracleDatabaseType extends BaseDatabaseType {
    // Oracle usernames/passwords can be 1-30 chars, can only contain alphanumerics and # _ $
    // The first (and only) capture group represents the password
    private static final Pattern usernamePasswordPattern = Pattern.compile("^jdbc:oracle:thin:[a-zA-Z0-9#_$]+/([a-zA-Z0-9#_$]+)@.*");
    private static final String TNS_ADMIN = "TNS_ADMIN";
    private static final String ORACLE_HOME = "ORACLE_HOME";

    @Override
    public String getName() {
        return "Oracle";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return isSecretManagerUrl(url, "oracle") || url.startsWith("jdbc:oracle") || url.startsWith("jdbc:p6spy:oracle");
    }

    @Override
    public Pattern getJDBCCredentialsPattern() {
        return usernamePasswordPattern;
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {






        if (url.startsWith("jdbc:p6spy:oracle:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Oracle");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new OracleDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {





        return new OracleParser(configuration










                , parsingContext
        );
    }

    @Override
    public SqlScriptExecutorFactory createSqlScriptExecutorFactory(JdbcConnectionFactory jdbcConnectionFactory,
                                                                   final CallbackExecutor<Event> callbackExecutor,
                                                                   final StatementInterceptor statementInterceptor) {
        final boolean supportsBatch = jdbcConnectionFactory.isSupportsBatch();

        final DatabaseType thisRef = this;

        return new SqlScriptExecutorFactory() {
            @Override
            public SqlScriptExecutor createSqlScriptExecutor(Connection connection, boolean undo, boolean batch, boolean outputQueryResults) {
                if (!supportsBatch) {
                    batch = false;
                }

                return new OracleSqlScriptExecutor(new JdbcTemplate(connection, thisRef), callbackExecutor, undo, batch, outputQueryResults, statementInterceptor);
            }
        };
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        String osUser = System.getProperty("user.name");
        props.put("v$session.osuser", osUser.substring(0, Math.min(osUser.length(), 30)));
        props.put("v$session.program", APPLICATION_NAME);
        props.put("oracle.net.keepAlive", "true");

        String oobb = ClassUtils.getStaticFieldValue("oracle.jdbc.OracleConnection", "CONNECTION_PROPERTY_THIN_NET_DISABLE_OUT_OF_BAND_BREAK", classLoader);
        props.put(oobb, "true");
    }

    @Override
    public void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader) {
        if (config != null) {
            OracleConfigurationExtension configurationExtension = config.getPluginRegister().getPlugin(OracleConfigurationExtension.class);












            if (configurationExtension.getWalletLocation() != null) {
                throw new FlywayEditionUpgradeRequiredException(LicenseGuard.getTier(config), "oracle.net.wallet_location");
            }
            if (!config.getKerberosConfigFile().isEmpty()) {
                throw new FlywayEditionUpgradeRequiredException(LicenseGuard.getTier(config), "oracle.kerberos.config.file");
            }

        }
    }
























    @Override
    public Connection alterConnectionAsNeeded(Connection connection, Configuration configuration) {
        Map<String, String> jdbcProperties = configuration.getJdbcProperties();

        if (jdbcProperties != null && jdbcProperties.containsKey(OracleConnection.PROXY_USER_NAME)) {
            try {
                OracleConnection oracleConnection;

                try {
                    if (connection instanceof OracleConnection) {
                        oracleConnection = (OracleConnection) connection;
                    } else if (connection.isWrapperFor(OracleConnection.class)) {
                        // This includes com.zaxxer.HikariCP.HikariProxyConnection, potentially other unknown wrapper types
                        oracleConnection = connection.unwrap(OracleConnection.class);
                    } else {
                        throw new FlywayException("Unable to extract Oracle connection type from '" + connection.getClass().getName() + "'");
                    }
                } catch (SQLException e) {
                    throw new FlywayException("Unable to unwrap connection type '" + connection.getClass().getName() + "'", e);
                }

                if (!oracleConnection.isProxySession()) {
                    Properties props = new Properties();
                    props.putAll(configuration.getJdbcProperties());
                    oracleConnection.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, props);
                }
            } catch (FlywayException e) {
                LOG.warn(e.getMessage());
            } catch (SQLException e) {
                throw new FlywayException("Unable to open proxy session: " + e.getMessage(), e);
            }
        }

        return super.alterConnectionAsNeeded(connection, configuration);
    }

    /**
     * Workaround until this issue gets fixed: https://github.com/aws/aws-secretsmanager-jdbc/issues/44
     */
    private void registerOracleDriver() {
        try {
            Class<Driver> driver = (Class<Driver>) getClass().getClassLoader().loadClass("oracle.jdbc.OracleDriver");
            DriverManager.registerDriver(driver.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new FlywayException("Unable to register Oracle driver. AWS Secrets Manager may not work", e);
        }
    }

    @Override
    public void setEarlyConnectionProps() {
        // Ideally, checking LOG.isDebugEnabled() would be preferred, but here it has no effect as it always returns true
        // The underlying reason involves Flyway’s complicated LOG initialization process.
        System.setProperty("oracle.jdbc.Trace", "true");

        
        // Using System.setProperty("java.util.logging.config.file", {filePath}) here has no effect.
        // Because the JVM initializes the logging configuration early during startup.
        String loggingPropertiesFile = Paths.get(ClassUtils.getInstallDir(this.getClass()), "assets/logging.properties").toString();
        if (new File(loggingPropertiesFile).exists()) {
            try (FileInputStream fis = new FileInputStream(loggingPropertiesFile)) {
                LOG.debug("Initializing Java logging with custom properties file");
                LogManager.getLogManager().readConfiguration(fis);
            } catch (Exception ignored) {
            }
        }

        String oracleHome = System.getenv(ORACLE_HOME);

        if (StringUtils.hasLength(oracleHome) && System.getenv(TNS_ADMIN) == null) {
            System.setProperty(TNS_ADMIN, oracleHome + "/network/admin");
        }
    }











}
