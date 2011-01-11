/**
 * Copyright (C) 2009-2010 the original author or authors.
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
package com.googlecode.flyway.commandline;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Driver;
import java.util.Properties;

/**
 * Main class and central entry point of the Flyway command-line tool.
 */
public class Main {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Main.class);

    /**
     * Main method.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        printVersion();

        try {
            Flyway flyway = new Flyway();

            flyway.configure(loadConfigurationFile(args));
            overrideConfiguration(flyway, args);

            String operation = determineOperation(args);
            if ("clean".equals(operation)) {
                flyway.clean();
            } else if ("init".equals(operation)) {
                flyway.init(null, null);
            } else if ("migrate".equals(operation)) {
                flyway.migrate();
            } else if ("validate".equals(operation)) {
                flyway.validate();
            } else if ("status".equals(operation)) {
                flyway.status();
            } else if ("history".equals(operation)) {
                flyway.history();
            } else {
                printUsage();
            }
        } catch (Exception e) {
            LOG.error(e.toString());

            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }

            System.exit(1);
        }
    }

    /**
     * Prints the version number on the console.
     */
    private static void printVersion() {
        //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * Prints the usage instructions on the console.
     */
    private static void printUsage() {


    }

    /**
     * Loads the configuration from the configuration file. If a configuration file is specified using the -configfile
     * argument it will be used, otherwise the default config file (conf/flyway.properties) will be loaded.
     *
     * @param args The command-line arguments passed in.
     *
     * @return The loaded configuration.
     *
     * @throws FlywayException when the configuration file could not be loaded.
     */
    private static Properties loadConfigurationFile(String[] args) throws FlywayException {
        String configFile = determineConfigurationFile(args);

        Properties properties = new Properties();
        if (configFile != null) {
            try {
                properties.load(new InputStreamReader(new FileInputStream(configFile), determineConfigurationFileEncoding(args)));
            } catch (IOException e) {
                throw new FlywayException("Unable to load config file: " + configFile, e);
            }
        }
        return properties;
    }

    /**
     * Determines the file to use for loading the configuration.
     *
     * @param args The command-line arguments passed in.
     *
     * @return The configuration file.
     */
    private static String determineConfigurationFile(String[] args) {
        for (String arg : args) {
             if (isArgumentForProperty(arg, "configFile")) {
                 return getArgumentValue(arg);
             }
        }

        return getInstallationDir() + "/conf/flyway.properties";
    }

    /**
     * @return The installation directory of the Flyway Command-line tool.
     */
    private static String getInstallationDir() {
        return Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    /**
     * Determines the encoding to use for loading the configuration.
     *
     * @param args The command-line arguments passed in.
     *
     * @return The encoding. (default: UTF-8)
     */
    private static String determineConfigurationFileEncoding(String[] args) {
        for (String arg : args) {
             if (isArgumentForProperty(arg, "configFileEncoding")) {
                 return getArgumentValue(arg);
             }
        }

        return "UTF-8";
    }

    /**
     * Overrides the configuration from the config file with the properties passed in directly from the command-line.
     *
     * @param flyway The Flyway instance to configure.
     * @param args   The command-line arguments that were passed in.
     */
    private static void overrideConfiguration(Flyway flyway, String[] args) {
        String driver = null;
        String url = null;
        String user = null;
        String password = "";

        for (String arg : args) {
            if (isArgumentForProperty(arg, "baseDir")) {
                flyway.setBaseDir(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "basePackage")) {
                flyway.setBasePackage(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "encoding")) {
                flyway.setEncoding(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "placeholderPrefix")) {
                flyway.setPlaceholderPrefix(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "placeholderSuffix")) {
                flyway.setPlaceholderSuffix(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "sqlMigrationPrefix")) {
                flyway.setSqlMigrationPrefix(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "sqlMigrationSuffix")) {
                flyway.setSqlMigrationSuffix(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "table")) {
                flyway.setTable(getArgumentValue(arg));
            } else if (isArgumentForProperty(arg, "target")) {
                flyway.setTarget(new SchemaVersion(getArgumentValue(arg)));
            } else if (isArgumentForProperty(arg, "validationErrorMode")) {
                flyway.setValidationErrorMode(ValidationErrorMode.valueOf(getArgumentValue(arg)));
            } else if (isArgumentForProperty(arg, "validationMode")) {
                flyway.setValidationMode(ValidationMode.valueOf(getArgumentValue(arg)));
            } else if (isArgumentForProperty(arg, "driver")) {
                driver = getArgumentValue(arg);
            } else if (isArgumentForProperty(arg, "url")) {
                url = getArgumentValue(arg);
            } else if (isArgumentForProperty(arg, "user")) {
                user = getArgumentValue(arg);
            } else if (isArgumentForProperty(arg, "password")) {
                password = getArgumentValue(arg);
            }
        }

        if ((driver != null) && (url != null) && (user != null) && (password != null)) {
            // All datasource properties set
            Driver driverClazz;
            try {
                driverClazz = (Driver) Class.forName(driver).newInstance();
            } catch (Exception e) {
                throw new FlywayException("Error instantiating database driver: " + driver, e);
            }

            flyway.setDataSource(new SimpleDriverDataSource(driverClazz, url, user, password));
        } else if ((driver != null) || (url != null) || (user != null) || (password != null)) {
            // Some, but not all datasource properties set
            LOG.warn("Discarding INCOMPLETE dataSource configuration!" +
                    " At least one of flyway.driver, flyway.url, flyway.user or flyway.password missing.");
        }
    }

    /**
     * Checks whether this command-line argument tries to set the property with this name.
     *
     * @param arg          The command-line argument to check.
     * @param propertyName The property name to check.
     *
     * @return {@code true} if it does, {@code false} if not.
     */
    private static boolean isArgumentForProperty(String arg, String propertyName) {
        return arg.startsWith("-" + propertyName + "=");
    }

    /**
     * Retrieves the value this command-line argument tries to assign.
     *
     * @param arg The command-line argument to check, typically in the form -key=value.
     *
     * @return The value or an empty string if no value is assigned.
     */
    private static String getArgumentValue(String arg) {
        int index = arg.indexOf("=");

        if ((index < 0) || (index == arg.length())) {
            return "";
        }

        return arg.substring(index + 1);
    }

    /**
     * Determine the operation Flyway should execute.
     *
     * @param args The command-line arguments passed in.
     *
     * @return The operation. {@code null} if it could not be determined.
     */
    private static String determineOperation(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                return arg;
            }
        }

        return null;
    }
}
