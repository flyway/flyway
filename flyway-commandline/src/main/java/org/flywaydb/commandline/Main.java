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
package org.flywaydb.commandline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.flywaydb.commandline.ConsoleLog.Level;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.output.ErrorOutput;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Main class and central entry point of the Flyway command-line tool.
 */
public class Main {
    private static Log LOG;

    static LogCreator getLogCreator(CommandLineArguments commandLineArguments) {
        // JSON output uses a different mechanism, so we do not create any loggers
        if (commandLineArguments.shouldOutputJson()) {
            return MultiLogCreator.empty();
        }

        List<LogCreator> logCreators = new ArrayList<>();

        logCreators.add(new ConsoleLogCreator(commandLineArguments));

        if (commandLineArguments.isOutputFileSet() || commandLineArguments.isLogFilepathSet()) {
            logCreators.add(new FileLogCreator(commandLineArguments));
        }

        return new MultiLogCreator(logCreators);
    }

    /**
     * Initializes the logging.
     */
    static void initLogging(CommandLineArguments commandLineArguments) {
        LogCreator logCreator = getLogCreator(commandLineArguments);
        LogFactory.setFallbackLogCreator(logCreator);
        LOG = LogFactory.getLog(Main.class);
    }

    /**
     * Main method.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        CommandLineArguments commandLineArguments = new CommandLineArguments(args);
        initLogging(commandLineArguments);

        try {
            commandLineArguments.validate(LOG);

            if (commandLineArguments.shouldPrintVersionAndExit()) {
                printVersion();
                System.exit(0);
            }

            if (commandLineArguments.hasOperation("help") || commandLineArguments.shouldPrintUsage()) {
                printUsage();
                return;
            }

            Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

            Map<String, String> config = new HashMap<>();
            initializeDefaults(config, commandLineArguments);
            loadConfigurationFromConfigFiles(config, commandLineArguments, envVars);
            config.putAll(readConfigFromInputStream(System.in));

            if (commandLineArguments.isWorkingDirectorySet()) {
                makeRelativeLocationsBasedOnWorkingDirectory(commandLineArguments, config);
            }
            
            config.putAll(envVars);
            config = overrideConfiguration(config, commandLineArguments.getConfiguration());

            if (!commandLineArguments.shouldSuppressPrompt()) {
                promptForCredentialsIfMissing(config);
            }

            ConfigUtils.dumpConfiguration(config);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            List<File> jarFiles = new ArrayList<>();
            jarFiles.addAll(getJdbcDriverJarFiles());
            jarFiles.addAll(getJavaMigrationJarFiles(config));
            if (!jarFiles.isEmpty()) {
                classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
            }
            filterProperties(config);
            Flyway flyway = Flyway.configure(classLoader).configuration(config).load();

            for (String operation : commandLineArguments.getOperations()) {
                executeOperation(flyway, operation, commandLineArguments);
            }
        } catch (Exception e) {
            if (commandLineArguments.shouldOutputJson()) {
                ErrorOutput errorOutput = ErrorOutput.fromException(e);
                printJson(commandLineArguments, errorOutput);
            } else {
                if (commandLineArguments.getLogLevel() == Level.DEBUG) {
                    LOG.error("Unexpected error", e);
                } else {
                    LOG.error(getMessagesFromException(e));
                }
            }
            System.exit(1);
        }
    }

    private static void makeRelativeLocationsBasedOnWorkingDirectory(CommandLineArguments commandLineArguments, Map<String, String> config) {
        String[] locations = config.get(ConfigUtils.LOCATIONS).split(",");
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].startsWith(Location.FILESYSTEM_PREFIX)) {
                String newLocation = locations[i].substring(Location.FILESYSTEM_PREFIX.length());
                File file = new File(newLocation);
                if (!file.isAbsolute()) {
                    file = new File(commandLineArguments.getWorkingDirectory(), newLocation);
                }
                locations[i] = Location.FILESYSTEM_PREFIX + file.getAbsolutePath();
            }
        }

        config.put(ConfigUtils.LOCATIONS, StringUtils.arrayToCommaDelimitedString(locations));
    }

    private static Map<String, String> overrideConfiguration(Map<String, String> existingConfiguration, Map<String, String> newConfiguration) {
        Map<String, String> combinedConfiguration = new HashMap<>();

        combinedConfiguration.putAll(existingConfiguration);
        combinedConfiguration.putAll(newConfiguration);

        return combinedConfiguration;
    }


    static String getMessagesFromException(Throwable e) {
        String condensedMessages = "";
        String preamble = "";
        while (e != null) {
            if (e instanceof FlywayException) {
                condensedMessages += preamble + e.getMessage();
            } else {
                condensedMessages += preamble + e.toString();
            }
            preamble = "\r\nCaused by: ";
            e = e.getCause();
        }
        return condensedMessages;
    }


    /**
     * Executes this operation on this Flyway instance.
     *
     * @param flyway    The Flyway instance.
     * @param operation The operation to execute.
     */
    private static void executeOperation(Flyway flyway, String operation, CommandLineArguments commandLineArguments) {
        if ("clean".equals(operation)) {
            flyway.clean();
        } else if ("baseline".equals(operation)) {
            flyway.baseline();
        } else if ("migrate".equals(operation)) {
            flyway.migrate();
        } else if ("undo".equals(operation)) {
            flyway.undo();
        } else if ("validate".equals(operation)) {
            flyway.validate();
        } else if ("info".equals(operation)) {
            MigrationInfoService info = flyway.info();
            MigrationInfo current = info.current();
            MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();

            MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
            LOG.info("Schema version: " + schemaVersionToOutput);
            LOG.info("");
            LOG.info(MigrationInfoDumper.dumpToAsciiTable(info.all()));

            if (commandLineArguments.shouldOutputJson()) {
                printJson(commandLineArguments, info.getInfoOutput());
            }
        } else if ("repair".equals(operation)) {
            flyway.repair();
        } else {
            LOG.error("Invalid operation: " + operation);
            printUsage();
            System.exit(1);
        }
    }

    private static void printJson(CommandLineArguments commandLineArguments, Object object) {
        String json = convertObjectToJsonString(object);

        if (commandLineArguments.isOutputFileSet()) {
            Path path = Paths.get(commandLineArguments.getOutputFile());
            byte[] bytes = json.getBytes();

            try {
                Files.write(path, bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new FlywayException("Could not write to output file " + commandLineArguments.getOutputFile(), e);
            }
        }

        System.out.println(json);
    }

    private static String convertObjectToJsonString(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(object);
    }

    /**
     * Initializes the config with the default configuration for the command-line tool.
     *
     * @param config The config object to initialize.
     */
    private static void initializeDefaults(Map<String, String> config, CommandLineArguments commandLineArguments) {
        // To maintain override order, return extension value first if present
        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : getInstallationDir();

        config.put(ConfigUtils.LOCATIONS, "filesystem:" + new File(workingDirectory, "sql").getAbsolutePath());
        config.put(ConfigUtils.JAR_DIRS, new File(workingDirectory, "jars").getAbsolutePath());
    }


    /**
     * Filters there properties to remove the Flyway Commandline-specific ones.
     *
     * @param config The properties to filter.
     */
    private static void filterProperties(Map<String, String> config) {
        config.remove(ConfigUtils.JAR_DIRS);
        config.remove(ConfigUtils.CONFIG_FILES);
        config.remove(ConfigUtils.CONFIG_FILE_ENCODING);
    }

    /**
     * Prints the version number on the console.
     */
    private static void printVersion() {
        VersionPrinter.printVersionOnly();
        LOG.info("");

        LOG.debug("Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        LOG.debug(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");
    }

    /**
     * Prints the usage instructions on the console.
     */
    private static void printUsage() {
        LOG.info("Usage");
        LOG.info("=====");
        LOG.info("");
        LOG.info("flyway [options] command");
        LOG.info("");
        LOG.info("By default, the configuration will be read from conf/flyway.conf.");
        LOG.info("Options passed from the command-line override the configuration.");
        LOG.info("");
        LOG.info("Commands");
        LOG.info("--------");
        LOG.info("migrate  : Migrates the database");
        LOG.info("clean    : Drops all objects in the configured schemas");
        LOG.info("info     : Prints the information about applied, current and pending migrations");
        LOG.info("validate : Validates the applied migrations against the ones on the classpath");
        LOG.info("undo     : [" + "pro] Undoes the most recently applied versioned migration");
        LOG.info("baseline : Baselines an existing database at the baselineVersion");
        LOG.info("repair   : Repairs the schema history table");
        LOG.info("");
        LOG.info("Options (Format: -key=value)");
        LOG.info("-------");
        LOG.info("driver                       : Fully qualified classname of the JDBC driver");
        LOG.info("url                          : Jdbc url to use to connect to the database");
        LOG.info("user                         : User to use to connect to the database");
        LOG.info("password                     : Password to use to connect to the database");
        LOG.info("connectRetries               : Maximum number of retries when attempting to connect to the database");
        LOG.info("initSql                      : SQL statements to run to initialize a new database connection");
        LOG.info("schemas                      : Comma-separated list of the schemas managed by Flyway");
        LOG.info("table                        : Name of Flyway's schema history table");
        LOG.info("locations                    : Classpath locations to scan recursively for migrations");
        LOG.info("resolvers                    : Comma-separated list of custom MigrationResolvers");
        LOG.info("skipDefaultResolvers         : Skips default resolvers (jdbc, sql and Spring-jdbc)");
        LOG.info("sqlMigrationPrefix           : File name prefix for versioned SQL migrations");
        LOG.info("undoSqlMigrationPrefix       : [" + "pro] File name prefix for undo SQL migrations");
        LOG.info("repeatableSqlMigrationPrefix : File name prefix for repeatable SQL migrations");
        LOG.info("sqlMigrationSeparator        : File name separator for SQL migrations");
        LOG.info("sqlMigrationSuffixes         : Comma-separated list of file name suffixes for SQL migrations");
        LOG.info("stream                       : [" + "pro] Stream SQL migrations when executing them");
        LOG.info("batch                        : [" + "pro] Batch SQL statements when executing them");
        LOG.info("mixed                        : Allow mixing transactional and non-transactional statements");
        LOG.info("encoding                     : Encoding of SQL migrations");
        LOG.info("placeholderReplacement       : Whether placeholders should be replaced");
        LOG.info("placeholders                 : Placeholders to replace in sql migrations");
        LOG.info("placeholderPrefix            : Prefix of every placeholder");
        LOG.info("placeholderSuffix            : Suffix of every placeholder");
        LOG.info("installedBy                  : Username that will be recorded in the schema history table");
        LOG.info("target                       : Target version up to which Flyway should use migrations");
        LOG.info("outOfOrder                   : Allows migrations to be run \"out of order\"");
        LOG.info("callbacks                    : Comma-separated list of FlywayCallback classes");
        LOG.info("skipDefaultCallbacks         : Skips default callbacks (sql)");
        LOG.info("validateOnMigrate            : Validate when running migrate");
        LOG.info("validateMigrationNaming      : Validate file names of SQL migrations (including callbacks)");
        LOG.info("ignoreMissingMigrations      : Allow missing migrations when validating");
        LOG.info("ignoreIgnoredMigrations      : Allow ignored migrations when validating");
        LOG.info("ignorePendingMigrations      : Allow pending migrations when validating");
        LOG.info("ignoreFutureMigrations       : Allow future migrations when validating");
        LOG.info("cleanOnValidationError       : Automatically clean on a validation error");
        LOG.info("cleanDisabled                : Whether to disable clean");
        LOG.info("baselineVersion              : Version to tag schema with when executing baseline");
        LOG.info("baselineDescription          : Description to tag schema with when executing baseline");
        LOG.info("baselineOnMigrate            : Baseline on migrate against uninitialized non-empty schema");
        LOG.info("configFiles                  : Comma-separated list of config files to use");
        LOG.info("configFileEncoding           : Encoding to use when loading the config files");
        LOG.info("jarDirs                      : Comma-separated list of dirs for Jdbc drivers & Java migrations");
        LOG.info("createSchemas          : Whether Flyway should attempt to create the schemas specified in the schemas property");
        LOG.info("dryRunOutput                 : [" + "pro] File where to output the SQL statements of a migration dry run");
        LOG.info("errorOverrides               : [" + "pro] Rules to override specific SQL states and errors codes");
        LOG.info("oracle.sqlplus               : [" + "pro] Enable Oracle SQL*Plus command support");
        LOG.info("licenseKey                   : [" + "pro] Your Flyway license key");
        LOG.info("color                        : Whether to colorize output. Values: always, never, or auto (default)");
        LOG.info("outputFile                   : Send output to the specified file alongside the console");
        LOG.info("");
        LOG.info("Flags");
        LOG.info("-----");
        LOG.info("-X          : Print debug output");
        LOG.info("-q          : Suppress all output, except for errors and warnings");
        LOG.info("-n          : Suppress prompting for a user and password");
        LOG.info("-v          : Print the Flyway version and exit");
        LOG.info("-?          : Print this usage info and exit");
        LOG.info("-json       : Print the output in JSON format");
        LOG.info("-community  : Run the Flyway Community Edition (default)");
        LOG.info("-pro        : Run the Flyway Pro Edition");
        LOG.info("-enterprise : Run the Flyway Enterprise Edition");
        LOG.info("");
        LOG.info("Example");
        LOG.info("-------");
        LOG.info("flyway -user=myuser -password=s3cr3t -url=jdbc:h2:mem -placeholders.abc=def migrate");
        LOG.info("");
        LOG.info("More info at https://flywaydb.org/documentation/commandline");
    }

    /**
     * Gets the jar files of all the JDBC drivers contained in the drivers folder.
     *
     * @return The jar files.
     */
    private static List<File> getJdbcDriverJarFiles() {
        File driversDir = new File(getInstallationDir(), "drivers");
        File[] files = driversDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // see javadoc of listFiles(): null if given path is not a real directory
        if (files == null) {
            LOG.debug("Directory for Jdbc Drivers not found: " + driversDir.getAbsolutePath());
            return Collections.emptyList();
        }

        return Arrays.asList(files);
    }

    /**
     * Gets all the jar files contained in the jars folder. (For Java Migrations)
     *
     * @param config The configured properties.
     * @return The jar files.
     */
    private static List<File> getJavaMigrationJarFiles(Map<String, String> config) {
        String jarDirs = config.get(ConfigUtils.JAR_DIRS);
        if (!StringUtils.hasLength(jarDirs)) {
            return Collections.emptyList();
        }

        jarDirs = jarDirs.replace(File.pathSeparator, ",");
        String[] dirs = StringUtils.tokenizeToStringArray(jarDirs, ",");

        List<File> jarFiles = new ArrayList<>();
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

            jarFiles.addAll(Arrays.asList(files));
        }

        return jarFiles;
    }

    /**
     * Loads the configuration from the various possible locations.
     *
     * @param config  The properties object to load to configuration into.
     * @param commandLineArguments    The command-line arguments passed in.
     * @param envVars The environment variables, converted into properties.
     */
    /* private -> for testing */
    static void loadConfigurationFromConfigFiles(Map<String, String> config, CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(commandLineArguments, envVars);
        File installationDir = new File(getInstallationDir());

        config.putAll(ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding));

        for (File configFile : determineConfigFilesFromArgs(commandLineArguments, envVars)) {
            config.putAll(ConfigUtils.loadConfigurationFile(configFile, encoding, true));
        }
    }

    /* private -> for testing */
    static Map<String, String> readConfigFromInputStream(InputStream inputStream) {
        Map<String, String> config = new HashMap<>();

        try {
            // System.in.available() : returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
            // Used to check if there is any data in the stream
            if (inputStream != null && inputStream.available() > 0) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                LOG.debug("Attempting to load configuration from standard input");
                int firstCharacter = bufferedReader.read();

                if (bufferedReader.ready() && firstCharacter != -1) {
                    // Prepend the first character to the rest of the string
                    // This is a char, represented as an int, so we cast to a char
                    // which is implicitly converted to an string
                    String configurationString = (char)firstCharacter + FileCopyUtils.copyToString(bufferedReader);
                    Map<String, String> configurationFromStandardInput = ConfigUtils.loadConfigurationFromString(configurationString);

                    if (configurationFromStandardInput.isEmpty()) {
                        LOG.debug("Empty configuration provided from standard input");
                    } else {
                        LOG.info("Loaded configuration from standard input");
                        config.putAll(configurationFromStandardInput);
                    }
                } else {
                    LOG.debug("Could not load configuration from standard input");
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not load configuration from standard input " + e.getMessage());
        }

        return config;
    }

    /**
     * If no user or password has been provided, prompt for it. If you want to avoid the prompt,
     * pass in an empty user or password.
     *
     * @param config The properties object to load to configuration into.
     */
    private static void promptForCredentialsIfMissing(Map<String, String> config) {
        Console console = System.console();
        if (console == null) {
            // We are running in an automated build. Prompting is not possible.
            return;
        }

        if (!config.containsKey(ConfigUtils.URL)) {
            // URL is not set. We are doomed for failure anyway.
            return;
        }

        String url = config.get(ConfigUtils.URL);
        if (!config.containsKey(ConfigUtils.USER) && needsUser(url)) {
            config.put(ConfigUtils.USER, console.readLine("Database user: "));
        }

        if (!config.containsKey(ConfigUtils.PASSWORD) && needsPassword(url)) {
            char[] password = console.readPassword("Database password: ");
            config.put(ConfigUtils.PASSWORD, password == null ? "" : String.valueOf(password));
        }
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a username.
     */
    private static boolean needsUser(String url) {
        return DriverDataSource.detectUserRequiredByUrl(url);
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a password.
     */
    private static boolean needsPassword(String url) {
        return DriverDataSource.detectPasswordRequiredByUrl(url);
    }

    /**
     * Determines the files to use for loading the configuration.
     *
     * @param commandLineArguments    The command-line arguments passed in.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The configuration files.
     */
    private static List<File> determineConfigFilesFromArgs(CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        List<File> configFiles = new ArrayList<>();

        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : null;

        if (envVars.containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(new File(workingDirectory, file));
            }
            return configFiles;
        }


        for (String file : commandLineArguments.getConfigFiles()) {
            configFiles.add(new File(workingDirectory, file));
        }

        return configFiles;
    }

    /**
     * @return The installation directory of the Flyway Command-line tool.
     */
    @SuppressWarnings("ConstantConditions")
    private static String getInstallationDir() {
        String path = ClassUtils.getLocationOnDisk(Main.class);
        return new File(path) // jar file
                .getParentFile() // edition dir
                .getParentFile() // lib dir
                .getParentFile() // installation dir
                .getAbsolutePath();
    }

    /**
     * Determines the encoding to use for loading the configuration.
     *
     * @param commandLineArguments    The command-line arguments passed in.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The encoding. (default: UTF-8)
     */
    private static String determineConfigurationFileEncoding(CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        if (envVars.containsKey(ConfigUtils.CONFIG_FILE_ENCODING)) {
            return envVars.get(ConfigUtils.CONFIG_FILE_ENCODING);
        }

        if (commandLineArguments.isConfigFileEncodingSet()) {
            return commandLineArguments.getConfigFileEncoding();
        }

        return "UTF-8";
    }
}