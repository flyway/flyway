/**
 * Copyright (C) 2010-2011 the original author or authors.
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
import com.googlecode.flyway.core.util.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try {
            printVersion();

            String operation = determineOperation(args);
            if (operation == null) {
                printUsage();
                return;
            }

            Flyway flyway = new Flyway();

            Properties properties = loadConfigurationFile(args);
            overrideConfiguration(properties, args);
            normalizeProperties(properties);
            flyway.configure(properties);

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
     *
     * @throws IOException when the version could not be read.
     */
    private static void printVersion() throws IOException {
        String version =
                FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("version.txt").getInputStream(), "UTF-8"));
        LOG.info("Flyway (Command-line Tool) v." + version);
        LOG.info("");
    }

    /**
     * Prints the usage instructions on the console.
     */
    private static void printUsage() {
        String extension;
        if (isWindows()) {
            extension = "cmd";
        } else {
            extension = "sh";
        }

        LOG.info("********");
        LOG.info("* Usage");
        LOG.info("********");
        LOG.info("");
        LOG.info("flyway." + extension + " [options] command");
        LOG.info("");
        LOG.info("By default, the configuration will be read from conf/flyway.properties.");
        LOG.info("Options passed from the command-line override the configuration.");
        LOG.info("");
        LOG.info("Commands");
        LOG.info("========");
        LOG.info("clean    : Drops all objects in the schema without dropping the schema itself");
        LOG.info("init     : Creates and initializes the metadata table in the schema");
        LOG.info("migrate  : Migrates the schema to the latest version");
        LOG.info("validate : Validates the applied migrations against the ones available on the classpath");
        LOG.info("status   : Prints the current version of the schema");
        LOG.info("history  : Prints the full migration history of the schema");
        LOG.info("");
        LOG.info("Options (Format: -key=value)");
        LOG.info("=======");
        LOG.info("driver              : The fully qualified classname of the jdbc driver to use to connect to the database");
        LOG.info("url                 : The jdbc url to use to connect to the database");
        LOG.info("user                : The user to use to connect to the database");
        LOG.info("password            : The password to use to connect to the database");
        LOG.info("table               : The name of Flyway's metadata table");
        LOG.info("basePackage         : The package to scan for Java migrations");
        LOG.info("baseDir             : The directory on the classpath to scan for Sql migrations");
        LOG.info("sqlMigrationPrefix  : The file name prefix for Sql migrations");
        LOG.info("sqlMigrationSuffix  : The file name suffix for Sql migrations");
        LOG.info("encoding            : The encoding of Sql migrations");
        LOG.info("placeholders        : Placeholders to replace in Sql migrations");
        LOG.info("placeholderPrefix   : The prefix of every placeholder");
        LOG.info("placeholderSuffix   : The suffix of every placeholder");
        LOG.info("target              : The target version up to which Flyway should run migrations");
        LOG.info("validationMode      : The type of validation to be performed before migrating");
        LOG.info("validationErrorMode : The action to take when validation fails");
        LOG.info("");
        LOG.info("Example");
        LOG.info("=======");
        LOG.info("flyway." + extension + " -target=1.5 -placeholder.user=my_user history");
        LOG.info("");
        LOG.info("More info at http://code.google.com/p/flyway/wiki/CommandLine");
    }

    /**
     * Checks whether we are running on Windows or not.
     *
     * @return {@code true} if we are, {@code false} if not.
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
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
            if (isPropertyArgument(arg) && "configFile".equals(getArgumentProperty(arg))) {
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
            if (isPropertyArgument(arg) && "configFileEncoding".equals(getArgumentProperty(arg))) {
                return getArgumentValue(arg);
            }
        }

        return "UTF-8";
    }

    /**
     * Overrides the configuration from the config file with the properties passed in directly from the command-line.
     *
     * @param properties The properties to override.
     * @param args       The command-line arguments that were passed in.
     */
    private static void overrideConfiguration(Properties properties, String[] args) {
        for (String arg : args) {
            if (isPropertyArgument(arg)) {
                properties.put(getArgumentProperty(arg), getArgumentValue(arg));
            }
        }
    }

    /**
     * Normalizes these properties so that properties prefixed with flyway. can freely be intermixed with others that
     * aren't.
     *
     * @param properties The properties to normalize.
     */
    private static void normalizeProperties(Properties properties) {
        for (String property : properties.stringPropertyNames()) {
            if (!property.startsWith("flyway.")) {
                properties.put("flyway." + property, properties.getProperty(property));
                properties.remove(property);
            }
        }
    }

    /**
     * Checks whether this command-line argument tries to set a property.
     *
     * @param arg The command-line argument to check.
     *
     * @return {@code true} if it does, {@code false} if not.
     */
    private static boolean isPropertyArgument(String arg) {
        return arg.startsWith("-") && arg.contains("=");
    }

    /**
     * Retrieves the property this command-line argument tries to assign.
     *
     * @param arg The command-line argument to check, typically in the form -key=value.
     *
     * @return The property.
     */
    private static String getArgumentProperty(String arg) {
        int index = arg.indexOf("=");

        return arg.substring(0, index - 1);
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
