/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.commandline;

import org.flywaydb.commandline.ConsoleLog.Level;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.PropertiesUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.VersionPrinter;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Main class and central entry point of the Flyway command-line tool.
 */
public class Main {
    private static Log LOG;

    /**
     * Initializes the logging.
     *
     * @param level The minimum level to log at.
     */
    static void initLogging(Level level) {
        LogFactory.setLogCreator(new ConsoleLogCreator(level));
        LOG = LogFactory.getLog(Main.class);
    }

    /**
     * Main method.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        Level logLevel = getLogLevel(args);
        initLogging(logLevel);

        try {
            printVersion();

            List<String> operations = determineOperations(args);
            if (operations.isEmpty()) {
                printUsage();
                return;
            }

            Properties properties = new Properties();
            initializeDefaults(properties);
            loadConfiguration(properties, args);
            overrideConfiguration(properties, args);

            loadJdbcDrivers();
            loadJavaMigrationsFromJarDir(properties);
            loadJavaMigrationsFromJarDirs(properties);

            Flyway flyway = new Flyway();
            flyway.configure(properties);

            for (String operation : operations) {
                executeOperation(flyway, operation);
            }
        } catch (Exception e) {
            if (logLevel == Level.DEBUG) {
                LOG.error("Unexpected error", e);
            } else {
                if (e instanceof FlywayException) {
                    LOG.error(e.getMessage());
                } else {
                    LOG.error(e.toString());
                }
            }
            System.exit(1);
        }
    }

    /**
     * Executes this operation on this Flyway instance.
     *
     * @param flyway    The Flyway instance.
     * @param operation The operation to execute.
     */
    private static void executeOperation(Flyway flyway, String operation) {
        if ("clean".equals(operation)) {
            flyway.clean();
        } else if ("baseline".equals(operation)) {
            flyway.baseline();
        } else if ("init".equals(operation)) {
            LOG.warn("init is deprecated. Use baseline instead. Will be removed in Flyway 4.0.");
            flyway.baseline();
        } else if ("migrate".equals(operation)) {
            flyway.migrate();
        } else if ("validate".equals(operation)) {
            flyway.validate();
        } else if ("info".equals(operation)) {
            LOG.info("\n" + MigrationInfoDumper.dumpToAsciiTable(flyway.info().all()));
        } else if ("repair".equals(operation)) {
            flyway.repair();
        } else {
            LOG.error("Invalid operation: " + operation);
            printUsage();
            System.exit(1);
        }
    }

    /**
     * Checks the desired log level.
     *
     * @param args The command-line arguments.
     * @return The desired log level.
     */
    private static Level getLogLevel(String[] args) {
        for (String arg : args) {
            if ("-X".equals(arg)) {
                return Level.DEBUG;
            }
            if ("-q".equals(arg)) {
                return Level.WARN;
            }
        }
        return Level.INFO;
    }

    /**
     * Initializes the properties with the default configuration for the command-line tool.
     *
     * @param properties The properties object to initialize.
     */
    private static void initializeDefaults(Properties properties) {
        properties.put("flyway.locations", "filesystem:" + new File(getInstallationDir(), "sql").getAbsolutePath());
        properties.put("flyway.jarDirs", new File(getInstallationDir(), "jars").getAbsolutePath());
    }

    /**
     * Prints the version number on the console.
     *
     * @throws IOException when the version could not be read.
     */
    private static void printVersion() throws IOException {
        VersionPrinter.printVersion(Thread.currentThread().getContextClassLoader());
        LOG.info("");
    }

    /**
     * Prints the usage instructions on the console.
     */
    private static void printUsage() {
        LOG.info("********");
        LOG.info("* Usage");
        LOG.info("********");
        LOG.info("");
        LOG.info("flyway [options] command");
        LOG.info("");
        LOG.info("By default, the configuration will be read from conf/flyway.conf.");
        LOG.info("Options passed from the command-line override the configuration.");
        LOG.info("");
        LOG.info("Commands");
        LOG.info("========");
        LOG.info("migrate  : Migrates the database");
        LOG.info("clean    : Drops all objects in the configured schemas");
        LOG.info("info     : Prints the information about applied, current and pending migrations");
        LOG.info("validate : Validates the applied migrations against the ones on the classpath");
        LOG.info("baseline : Baselines an existing database at the baselineVersion");
        LOG.info("repair   : Repairs the metadata table");
        LOG.info("");
        LOG.info("Options (Format: -key=value)");
        LOG.info("=======");
        LOG.info("driver                 : Fully qualified classname of the jdbc driver");
        LOG.info("url                    : Jdbc url to use to connect to the database");
        LOG.info("user                   : User to use to connect to the database");
        LOG.info("password               : Password to use to connect to the database");
        LOG.info("schemas                : Comma-separated list of the schemas managed by Flyway");
        LOG.info("table                  : Name of Flyway's metadata table");
        LOG.info("locations              : Classpath locations to scan recursively for migrations");
        LOG.info("resolvers              : Comma-separated list of custom MigrationResolvers");
        LOG.info("sqlMigrationPrefix     : File name prefix for Sql migrations");
        LOG.info("sqlMigrationSeparator  : File name separator for Sql migrations");
        LOG.info("sqlMigrationSuffix     : File name suffix for Sql migrations");
        LOG.info("encoding               : Encoding of Sql migrations");
        LOG.info("placeholderReplacement : Whether placeholders should be replaced");
        LOG.info("placeholders           : Placeholders to replace in Sql migrations");
        LOG.info("placeholderPrefix      : Prefix of every placeholder");
        LOG.info("placeholderSuffix      : Suffix of every placeholder");
        LOG.info("target                 : Target version up to which Flyway should use migrations");
        LOG.info("outOfOrder             : Allows migrations to be run \"out of order\"");
        LOG.info("callbacks              : Comma-separated list of FlywayCallback classes");
        LOG.info("validateOnMigrate      : Validate when running migrate");
        LOG.info("cleanOnValidationError : Automatically clean on a validation error");
        LOG.info("baselineVersion        : Version to tag schema with when executing baseline");
        LOG.info("baselineDescription    : Description to tag schema with when executing baseline");
        LOG.info("baselineOnMigrate      : Baseline on migrate against uninitialized non-empty schema");
        LOG.info("configFile             : Config file to use (default: conf/flyway.properties)");
        LOG.info("configFileEncoding     : Encoding of the config file (default: UTF-8)");
        LOG.info("jarDirs                : Dirs for Jdbc drivers & Java migrations (default: jars)");
        LOG.info("");
        LOG.info("Add -X to print debug output");
        LOG.info("Add -q to suppress all output, except for errors and warnings");
        LOG.info("");
        LOG.info("Example");
        LOG.info("=======");
        LOG.info("flyway -target=1.5 -placeholders.user=my_user info");
        LOG.info("");
        LOG.info("More info at http://flywaydb.org/documentation/commandline");
    }

    /**
     * Loads all the driver jars contained in the drivers folder. (For Jdbc drivers)
     *
     * @throws IOException When the jars could not be loaded.
     */
    private static void loadJdbcDrivers() throws IOException {
        File driversDir = new File(getInstallationDir(), "drivers");
        File[] files = driversDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // see javadoc of listFiles(): null if given path is not a real directory
        if (files == null) {
            LOG.error("Directory for Jdbc Drivers not found: " + driversDir.getAbsolutePath());
            System.exit(1);
        }

        for (File file : files) {
            addJarOrDirectoryToClasspath(file.getPath());
        }
    }

    /**
     * Loads all the jars contained in the jars folder. (For Java Migrations)
     *
     * @param properties The configured properties.
     * @throws IOException When the jars could not be loaded.
     */
    private static void loadJavaMigrationsFromJarDir(Properties properties) throws IOException {
        String jarDir = properties.getProperty("flyway.jarDir");
        if (!StringUtils.hasLength(jarDir)) {
            return;
        }
        LOG.warn("flyway.jarDir is deprecated and will be removed in Flyway 4.0. Use flyway.jarDirs instead.");

        File dir = new File(jarDir);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // see javadoc of listFiles(): null if given path is not a real directory
        if (files == null) {
            LOG.error("Directory for Java Migrations not found: " + jarDir);
            System.exit(1);
        }

        for (File file : files) {
            addJarOrDirectoryToClasspath(file.getPath());
        }
    }

    /**
     * Loads all the jars contained in the jars folder. (For Java Migrations)
     *
     * @param properties The configured properties.
     * @throws IOException When the jars could not be loaded.
     */
    private static void loadJavaMigrationsFromJarDirs(Properties properties) throws IOException {
        String jarDirs = properties.getProperty("flyway.jarDirs");
        if (!StringUtils.hasLength(jarDirs)) {
            return;
        }

        jarDirs = jarDirs.replace(File.pathSeparator, ",");
        String[] dirs = StringUtils.tokenizeToStringArray(jarDirs, ",");

        for (String dirName : dirs) {
            File dir = new File(dirName);
            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });

            // see javadoc of listFiles(): null if given path is not a real directory
            if (files == null) {
                LOG.error("Directory for Java Migrations not found: " + dirName);
                System.exit(1);
            }

            for (File file : files) {
                addJarOrDirectoryToClasspath(file.getPath());
            }
        }
    }

    /**
     * Adds a jar or a directory with this name to the classpath.
     *
     * @param name The name of the jar or directory to add.
     * @throws IOException when the jar or directory could not be found.
     */
    /* private -> for testing */
    static void addJarOrDirectoryToClasspath(String name) throws IOException {
        LOG.debug("Adding location to classpath: " + name);

        try {
            URL url = new File(name).toURI().toURL();
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, url);
        } catch (Exception e) {
            throw new FlywayException("Unable to load " + name, e);
        }
    }

    /**
     * Loads the configuration from the various possible locations.
     *
     * @param properties The properties object to load to configuration into.
     * @param args       The command-line arguments passed in.
     */
    /* private -> for testing */
    static void loadConfiguration(Properties properties, String[] args) {
        String encoding = determineConfigurationFileEncoding(args);

        if (loadConfigurationFile(properties, getInstallationDir() + "/conf/flyway.properties", encoding, false)) {
            LOG.warn("conf/flyway.properties usage is deprecated and will be removed in Flyway 4.0. Use conf/flyway.conf instead.");
        }
        loadConfigurationFile(properties, getInstallationDir() + "/conf/flyway.conf", encoding, false);
        loadConfigurationFile(properties, System.getProperty("user.home") + "/flyway.conf", encoding, false);
        loadConfigurationFile(properties, "flyway.conf", encoding, false);

        String configFile = determineConfigurationFileArgument(args);
        if (configFile != null) {
            loadConfigurationFile(properties, configFile, encoding, true);
        }
    }

    /**
     * Loads the configuration from the configuration file. If a configuration file is specified using the -configfile
     * argument it will be used, otherwise the default config file (conf/flyway.properties) will be loaded.
     *
     * @param properties    The properties object to load to configuration into.
     * @param file          The configuration file to load.
     * @param encoding      The encoding of the configuration file.
     * @param failIfMissing Whether to fail if the file is missing.
     * @return Whether the file was loaded successfully.
     * @throws FlywayException when the configuration file could not be loaded.
     */
    private static boolean loadConfigurationFile(Properties properties, String file, String encoding, boolean failIfMissing) throws FlywayException {
        File configFile = new File(file);
        String errorMessage = "Unable to load config file: " + configFile.getAbsolutePath();

        if (!configFile.isFile() || !configFile.canRead()) {
            if (!failIfMissing) {
                LOG.debug(errorMessage);
                return false;
            }
            throw new FlywayException(errorMessage);
        }

        LOG.debug("Loading config file: " + configFile.getAbsolutePath());
        try {
            Reader fileReader = new InputStreamReader(new FileInputStream(configFile), encoding);
            String propertiesData = FileCopyUtils.copyToString(fileReader).replace("\\", "\\\\");

            properties.putAll(PropertiesUtils.loadPropertiesFromString(propertiesData));
            return true;
        } catch (IOException e) {
            throw new FlywayException(errorMessage, e);
        }
    }

    /**
     * Determines the file to use for loading the configuration.
     *
     * @param args The command-line arguments passed in.
     * @return The path of the configuration file on disk.
     */
    private static String determineConfigurationFileArgument(String[] args) {
        for (String arg : args) {
            if (isPropertyArgument(arg) && "configFile".equals(getArgumentProperty(arg))) {
                return getArgumentValue(arg);
            }
        }

        return null;
    }

    /**
     * @return The installation directory of the Flyway Command-line tool.
     */
    private static String getInstallationDir() {
        String path = ClassUtils.getLocationOnDisk(Main.class);
        return new File(path).getParentFile().getParentFile().getAbsolutePath();
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
     * Determine the operations Flyway should execute.
     *
     * @param args The command-line arguments passed in.
     * @return The operations. An empty list if none.
     */
    private static List<String> determineOperations(String[] args) {
        List<String> operations = new ArrayList<String>();

        for (String arg : args) {
            if (!arg.startsWith("-")) {
                operations.add(arg);
            }
        }

        return operations;
    }
}
