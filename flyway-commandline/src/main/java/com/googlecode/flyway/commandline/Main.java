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
import com.googlecode.flyway.core.util.MetaDataTableRowDumper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
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

            loadJdbcDriversAndJavaMigrations();
            loadSqlMigrations();

            Flyway flyway = new Flyway();

            Properties properties = new Properties();
            initializeDefaults(properties);
            loadConfigurationFile(properties, args);
            overrideConfiguration(properties, args);
            flyway.configure(properties);

            if ("clean".equals(operation)) {
                flyway.clean();
            } else if ("init".equals(operation)) {
                flyway.init();
            } else if ("migrate".equals(operation)) {
                flyway.migrate();
            } else if ("validate".equals(operation)) {
                flyway.validate();
            } else if ("status".equals(operation)) {
                MetaDataTableRowDumper.dumpMigration(flyway.status());
            } else if ("history".equals(operation)) {
                MetaDataTableRowDumper.dumpMigrations(flyway.history());
            } else {
                printUsage();
            }
        } catch (Exception e) {
            LOG.error(ClassUtils.getShortName(e.getClass()) + ": " + e.getMessage());

            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }

            System.exit(1);
        }
    }

    /**
     * Initializes the properties with the default configuration for the command-line tool.
     *
     * @param properties The properties object to initialize.
     */
    private static void initializeDefaults(Properties properties) {
        properties.put("flyway.password", "");
        properties.put("flyway.baseDir", "");
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
        LOG.info("validate : Validates the applied migrations against the ones on the classpath");
        LOG.info("status   : Prints the current version of the schema");
        LOG.info("history  : Prints the full migration history of the schema");
        LOG.info("");
        LOG.info("Options (Format: -key=value)");
        LOG.info("=======");
        LOG.info("driver              : Fully qualified classname of the jdbc driver");
        LOG.info("url                 : Jdbc url to use to connect to the database");
        LOG.info("user                : User to use to connect to the database");
        LOG.info("password            : Password to use to connect to the database");
        LOG.info("schemas             : Comma-separated list of the schemas managed by Flyway");
        LOG.info("table               : Name of Flyway's metadata table");
        LOG.info("basePackage         : Package to scan for Java migrations");
        LOG.info("baseDir             : Directory on the classpath to scan for Sql migrations");
        LOG.info("sqlMigrationPrefix  : File name prefix for Sql migrations");
        LOG.info("sqlMigrationSuffix  : File name suffix for Sql migrations");
        LOG.info("encoding            : Encoding of Sql migrations");
        LOG.info("placeholders        : Placeholders to replace in Sql migrations");
        LOG.info("placeholderPrefix   : Prefix of every placeholder");
        LOG.info("placeholderSuffix   : Suffix of every placeholder");
        LOG.info("target              : Target version up to which Flyway should run migrations");
        LOG.info("validationMode      : Type of validation to be performed before migrating");
        LOG.info("validationErrorMode : Action to take when validation fails");
        LOG.info("initialVersion      : Initial version to put in the database");
        LOG.info("initialDescription  : Description of the initial version");
        LOG.info("disableInitCheck    : Don't check that a non-empty schema has been initialized");
        LOG.info("configFile          : Config file to use (default: conf/flyway.properties)");
        LOG.info("configFileEncoding  : Encoding of the config file (default: UTF-8)");
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
     * Loads all the jars contained in the jars folder. (For Jdbc drivers and Java Migrations)
     *
     * @throws IOException When the jars could not be loaded.
     */
    private static void loadJdbcDriversAndJavaMigrations() throws IOException {
        File dir = new File(getInstallationDir() + "/../jars");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        for (File file : files) {
            addJarOrDirectoryToClasspath(file.getPath());
        }
    }

    /**
     * Loads all the jars contained in the jars folder. (For Jdbc drivers and Java Migrations)
     *
     * @throws IOException When the SQL migrations could not be loaded.
     */
    private static void loadSqlMigrations() throws IOException {
        addJarOrDirectoryToClasspath(getInstallationDir() + "/../sql");
    }

    /**
     * Adds a jar or a directory with this name to the classpath.
     *
     * @param name The name of the jar or directory to add.
     * @throws IOException when the jar or directory could not be found.
     */
    /* private -> for testing */
    static void addJarOrDirectoryToClasspath(String name) throws IOException {
        LOG.debug("Loading " + name);

        // Add the jar or dir to the classpath
        // Chain the current thread classloader
        URLClassLoader urlClassLoader =
                new URLClassLoader(new URL[]{new File(name).toURI().toURL()}, Thread.currentThread().getContextClassLoader());

        // Replace the thread classloader - assumes
        // you have permissions to do so
        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }

    /**
     * Loads the configuration from the configuration file. If a configuration file is specified using the -configfile
     * argument it will be used, otherwise the default config file (conf/flyway.properties) will be loaded.
     *
     * @param properties The properties object to load to configuration into.
     * @param args       The command-line arguments passed in.
     * @throws FlywayException when the configuration file could not be loaded.
     */
    /* private -> for testing */
    static void loadConfigurationFile(Properties properties, String[] args) throws FlywayException {
        String configFile = determineConfigurationFile(args);

        if (configFile != null) {
            try {
                PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
                propertiesFactoryBean.setFileEncoding(determineConfigurationFileEncoding(args));
                propertiesFactoryBean.setLocation(new FileSystemResource(configFile));
                propertiesFactoryBean.setProperties(properties);
                propertiesFactoryBean.afterPropertiesSet();

                properties.clear();
                properties.putAll((Properties) propertiesFactoryBean.getObject());
            } catch (IOException e) {
                throw new FlywayException("Unable to load config file: " + configFile, e);
            }
        }
    }

    /**
     * Determines the file to use for loading the configuration.
     *
     * @param args The command-line arguments passed in.
     * @return The configuration file.
     */
    private static String determineConfigurationFile(String[] args) {
        for (String arg : args) {
            if (isPropertyArgument(arg) && "configFile".equals(getArgumentProperty(arg))) {
                return getArgumentValue(arg);
            }
        }

        return getInstallationDir() + "/../conf/flyway.properties";
    }

    /**
     * @return The installation directory of the Flyway Command-line tool.
     */
    private static String getInstallationDir() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return path.substring(0, path.lastIndexOf("/"));
    }

    /**
     * Determines the encoding to use for loading the configuration.
     *
     * @param args The command-line arguments passed in.
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
    /* private -> for testing*/
    static void overrideConfiguration(Properties properties, String[] args) {
        for (String arg : args) {
            if (isPropertyArgument(arg)) {
                properties.put("flyway." + getArgumentProperty(arg), getArgumentValue(arg));
            }
        }
    }

    /**
     * Checks whether this command-line argument tries to set a property.
     *
     * @param arg The command-line argument to check.
     * @return {@code true} if it does, {@code false} if not.
     */
    /* private -> for testing*/
    static boolean isPropertyArgument(String arg) {
        return arg.startsWith("-") && arg.contains("=");
    }

    /**
     * Retrieves the property this command-line argument tries to assign.
     *
     * @param arg The command-line argument to check, typically in the form -key=value.
     * @return The property.
     */
    /* private -> for testing*/
    static String getArgumentProperty(String arg) {
        int index = arg.indexOf("=");

        return arg.substring(1, index);
    }

    /**
     * Retrieves the value this command-line argument tries to assign.
     *
     * @param arg The command-line argument to check, typically in the form -key=value.
     * @return The value or an empty string if no value is assigned.
     */
    /* private -> for testing*/
    static String getArgumentValue(String arg) {
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
