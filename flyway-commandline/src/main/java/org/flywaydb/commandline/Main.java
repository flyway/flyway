/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
import lombok.SneakyThrows;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
import org.flywaydb.commandline.logging.console.ConsoleLogCreator;
import org.flywaydb.commandline.logging.file.FileLogCreator;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.*;

import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.InfoTelemetryModel;
import org.flywaydb.core.extensibility.RgDomainChecker;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.TomlUtils;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.license.VersionPrinter;

import org.flywaydb.core.internal.logging.EvolvingLog;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;
import org.flywaydb.core.internal.logging.multi.MultiLogCreator;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.*;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static Log LOG;
    private static final PluginRegister pluginRegister = new PluginRegister();





    static LogCreator getLogCreator(CommandLineArguments commandLineArguments) {
        // JSON output uses a different mechanism, so we do not create any loggers
        if (commandLineArguments.shouldOutputJson() || (commandLineArguments.hasOperation("info") && commandLineArguments.isFilterOnMigrationIds())) {
            return MultiLogCreator.empty();
        }

        List<LogCreator> logCreators = new ArrayList<>();
        logCreators.add(new ConsoleLogCreator(commandLineArguments));
        if (commandLineArguments.isOutputFileSet()) {
            logCreators.add(new FileLogCreator(commandLineArguments));
        }

        return new MultiLogCreator(logCreators);
    }

    static void initLogging(CommandLineArguments commandLineArguments) {
        LogFactory.setFallbackLogCreator(getLogCreator(commandLineArguments));
        LOG = LogFactory.getLog(Main.class);
    }

    public static void main(String[] args) throws Exception {
        int exitCode=0;

        FlywayTelemetryManager flywayTelemetryManager = null;
        if(!StringUtils.hasText(System.getenv("REDGATE_DISABLE_TELEMETRY"))) {
            flywayTelemetryManager = new FlywayTelemetryManager(pluginRegister);
        }

        try {


            JavaVersionPrinter.printJavaVersion();
            CommandLineArguments commandLineArguments = new CommandLineArguments(pluginRegister, args);
            initLogging(commandLineArguments);

            try {
                commandLineArguments.validate();

                if (commandLineArguments.hasOperation("help") || commandLineArguments.shouldPrintUsage()) {
                    StringBuilder helpText = new StringBuilder();
                    boolean helpAsVerbWithOperation = commandLineArguments.hasOperation("help") && commandLineArguments.getOperations().size() > 1;
                    boolean helpAsFlagWithOperation = commandLineArguments.shouldPrintUsage() && commandLineArguments.getOperations().size() > 0;
                    if (helpAsVerbWithOperation || helpAsFlagWithOperation) {
                        for (String operation : commandLineArguments.getOperations()) {
                            String helpTextForOperation = pluginRegister.getPlugins(CommandExtension.class).stream()
                                                                        .filter(e -> e.handlesCommand(operation))
                                                                        .map(CommandExtension::getHelpText)
                                                                        .collect(Collectors.joining("\n\n"));

                            if (StringUtils.hasText(helpTextForOperation)) {
                                helpText.append(helpTextForOperation).append("\n\n");
                            }
                        }
                    }
                    if (!StringUtils.hasText(helpText.toString())) {
                        printUsage();
                    } else {
                        LOG.info(helpText.toString());
                    }
                    return;
                }

                Configuration configuration = useModernConfig(commandLineArguments) ? getConfiguration(commandLineArguments) : getLegacyConfiguration(commandLineArguments);

                Flyway flyway = Flyway.configure(configuration.getClassLoader()).configuration(configuration).load();

                if (flywayTelemetryManager != null) {
                    RootTelemetryModel rootTelemetryModel = flywayTelemetryManager.getRootTelemetryModel();
                    rootTelemetryModel.setApplicationVersion(VersionPrinter.getVersion());
                    rootTelemetryModel.setApplicationEdition(VersionPrinter.EDITION.getDescription());
                    RgDomainChecker domainChecker = pluginRegister.getPlugin(RgDomainChecker.class);
                    if (domainChecker != null) {
                        rootTelemetryModel.setRedgateEmployee(domainChecker.isInDomain(configuration));
                    }
                    ClassicConfiguration classicConfiguration = new ClassicConfiguration(configuration);
                    if(classicConfiguration.getDataSource() != null) {
                        try (JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(configuration.getDataSource(), configuration, null);
                             Database database = jdbcConnectionFactory.getDatabaseType().createDatabase(configuration, false, jdbcConnectionFactory, null)) {
                            rootTelemetryModel.setDatabaseEngine(database.getDatabaseType().getName());
                        }
                    } else {
                        rootTelemetryModel.setDatabaseEngine("UNKNOWN");
                    }

                }

                if (!commandLineArguments.skipCheckForUpdate()) {
                    MavenVersionChecker.checkForVersionUpdates();
                }

                OperationResult result;
                if (commandLineArguments.getOperations().size() == 1) {
                    String operation = commandLineArguments.getOperations().get(0);
                    result = executeOperation(flyway, operation, commandLineArguments, flywayTelemetryManager);
                } else {
                    CompositeResult<OperationResult> compositeResult = new CompositeResult<>();

                        for (String operation : commandLineArguments.getOperations()) {
                            compositeResult.individualResults.add(executeOperation(flyway, operation, commandLineArguments, flywayTelemetryManager));
                        }
                    result = compositeResult;
                }

                if (commandLineArguments.isCommunityFallback()) {
                    LOG.warn("A Flyway License was not provided; fell back to Community Edition. Please contact sales at sales@flywaydb.org for license information.");
                }

                if (commandLineArguments.shouldOutputJson()) {
                    printJson(commandLineArguments, result);
                }

            } catch (DbMigrate.FlywayMigrateException e) {
                MigrateErrorResult errorResult = ErrorOutput.fromMigrateException(e);
                printError(commandLineArguments, e, errorResult);
                exitCode = 1;
            } catch (Exception e) {
                ErrorOutput errorOutput = ErrorOutput.fromException(e);
                printError(commandLineArguments, e, errorOutput);
                exitCode = 1;
            } finally {
                flushLog(commandLineArguments);
            }
        } finally {
            if(flywayTelemetryManager != null) {
                flywayTelemetryManager.close();
            }
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static boolean useModernConfig(CommandLineArguments commandLineArguments) {
        List<File> tomlFiles = new ArrayList<>();
        tomlFiles.addAll(ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(Main.class))));
        tomlFiles.addAll(getTomlConfigFilePaths());
        tomlFiles.addAll(commandLineArguments.getConfigFiles().stream()
                                             .filter(f -> f.endsWith(".toml"))
                                             .map(File::new)
                                             .collect(Collectors.toList()));

        return tomlFiles.stream().anyMatch(File::exists);
    }

    private static Configuration getConfiguration(CommandLineArguments commandLineArguments) {
        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : ClassUtils.getInstallDir(Main.class);

        List<File> tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(new File(ClassUtils.getInstallDir(Main.class)));
        tomlFiles.addAll(getTomlConfigFilePaths());
        tomlFiles.addAll(commandLineArguments.getConfigFiles().stream().map(File::new)
                                             .collect(Collectors.toList()));

        List<File> existingFiles = tomlFiles.stream().filter(File::exists).collect(Collectors.toList());
        ConfigurationModel config = TomlUtils.loadConfigurationFiles(existingFiles, workingDirectory);

        ConfigurationModel commandLineArgumentsModel = TomlUtils.loadConfigurationFromCommandlineArgs(commandLineArguments.getConfiguration());
        config = config.merge(TomlUtils.loadConfigurationFromEnvironment())
                       .merge(commandLineArgumentsModel);

        if (commandLineArgumentsModel.getEnvironments().containsKey(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)) {
            EnvironmentModel defaultEnv = config.getEnvironments().get(config.getFlyway().getEnvironment());
            config.getEnvironments().put(config.getFlyway().getEnvironment(), defaultEnv.merge(commandLineArgumentsModel.getEnvironments().get(ClassicConfiguration.TEMP_ENVIRONMENT_NAME)));
            config.getEnvironments().remove(ClassicConfiguration.TEMP_ENVIRONMENT_NAME);
        }
        config.getFlyway().getLocations().add("filesystem:" + new File(workingDirectory, "sql").getAbsolutePath());

        List<String> jarDirs = new ArrayList<>();

        File jarDir = new File(workingDirectory, "jars");
        if (jarDir.exists()) {
            jarDirs.add(jarDir.getAbsolutePath());
        }

        String configuredJarDirs = commandLineArguments.getConfiguration().get(ConfigUtils.JAR_DIRS);
        if (StringUtils.hasText(configuredJarDirs)) {
            jarDirs.addAll(StringUtils.tokenizeToStringCollection(configuredJarDirs.replace(File.pathSeparator, ","), ","));
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<File> jarFiles = new ArrayList<>();
        jarFiles.addAll(getJdbcDriverJarFiles());
        jarFiles.addAll(getJavaMigrationJarFiles(jarDirs.toArray(new String[0])));

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        ClassicConfiguration cfg = new ClassicConfiguration(config);
        cfg.setClassLoader(classLoader);








        return cfg;
    }

    private static Configuration getLegacyConfiguration(CommandLineArguments commandLineArguments) {

        Map<String, String> config = new HashMap<>();
        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : ClassUtils.getInstallDir(Main.class);

        config.put(ConfigUtils.LOCATIONS, "filesystem:" + new File(workingDirectory, "sql").getAbsolutePath());

        File jarDir = new File(workingDirectory, "jars");
        if (jarDir.exists()) {
            config.put(ConfigUtils.JAR_DIRS, jarDir.getAbsolutePath());
        }

        Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

        loadConfigurationFromConfigFiles(config, commandLineArguments, envVars);

        config.putAll(envVars);
        config = overrideConfiguration(config, commandLineArguments.getConfiguration());

        if (commandLineArguments.isWorkingDirectorySet()) {
            makeRelativeLocationsBasedOnWorkingDirectory(commandLineArguments, config);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<File> jarFiles = new ArrayList<>(getJdbcDriverJarFiles());

        String jarDirs = config.get(ConfigUtils.JAR_DIRS);
        if (StringUtils.hasText(jarDirs)) {
            jarFiles.addAll(getJavaMigrationJarFiles(StringUtils.tokenizeToStringArray(jarDirs.replace(File.pathSeparator, ","), ",")));
        }

        if (!jarFiles.isEmpty()) {
            classLoader = ClassUtils.addJarsOrDirectoriesToClasspath(classLoader, jarFiles);
        }

        if (!commandLineArguments.shouldSuppressPrompt()) {
            promptForCredentialsIfMissing(config);
        }

        ConfigUtils.dumpConfiguration(config);
        filterProperties(config);








        return new FluentConfiguration(classLoader).configuration(config);
    }

    private static void printError(CommandLineArguments commandLineArguments, Exception e, OperationResult errorResult) {
        if (commandLineArguments.shouldOutputJson()) {
            printJson(commandLineArguments, errorResult);
        } else {
            if (commandLineArguments.getLogLevel() == Level.DEBUG) {
                LOG.error("Unexpected error", e);
            } else {
                LOG.error(getMessagesFromException(e));
            }
        }
        flushLog(commandLineArguments);
    }

    private static void flushLog(CommandLineArguments commandLineArguments) {
        Log currentLog = ((EvolvingLog) LOG).getLog();
        if (currentLog instanceof BufferedLog) {
            ((BufferedLog) currentLog).flush(getLogCreator(commandLineArguments).createLogger(Main.class));
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
        StringBuilder condensedMessages = new StringBuilder();
        String preamble = "";
        while (e != null) {
            if (e instanceof FlywayException) {
                condensedMessages.append(preamble).append(e.getMessage());
            } else {
                condensedMessages.append(preamble).append(e);
            }
            preamble = "\r\nCaused by: ";
            e = e.getCause();
        }
        return condensedMessages.toString();
    }

    @SneakyThrows
    private static OperationResult executeOperation(Flyway flyway, String operation, CommandLineArguments commandLineArguments, FlywayTelemetryManager telemetryManager) {
        OperationResult result = null;
        flyway.setFlywayTelemetryManager(telemetryManager);
        if ("clean".equals(operation)) {
            result = flyway.clean();
        } else if ("baseline".equals(operation)) {
            result = flyway.baseline();
        } else if ("migrate".equals(operation)) {
            result = flyway.migrate();
        } else if ("validate".equals(operation)) {
            try (EventTelemetryModel telemetryModel = new EventTelemetryModel("validate", telemetryManager)) {
                try {
                    if (commandLineArguments.shouldOutputJson()) {
                        result = flyway.validateWithResult();
                    } else {
                        flyway.validate();
                    }
                } catch (Exception e) {
                    telemetryModel.setException(e);
                    throw e;
                }
            }
        } else if ("info".equals(operation)) {
            try(InfoTelemetryModel infoTelemetryModel = new InfoTelemetryModel(telemetryManager)) {
                try {
                    MigrationInfoService info = flyway.info();
                    MigrationInfo current = info.current();
                    MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();

                    MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
                    LOG.info("Schema version: " + schemaVersionToOutput);
                    LOG.info("");







                     result = info.getInfoResult();
                     MigrationInfo[] infos = info.all();


                    if (commandLineArguments.isFilterOnMigrationIds()) {
                        System.out.print(Arrays.stream(infos)
                                               .map(m -> m.getVersion() == null ? m.getDescription() : m.getVersion().getVersion())
                                               .collect(Collectors.joining(",")));
                    } else {
                        LOG.info(MigrationInfoDumper.dumpToAsciiTable(infos));
                    }
                    infoTelemetryModel.setNumberOfMigrations(((InfoResult) result).migrations.size());
                    infoTelemetryModel.setNumberOfPendingMigrations((int) ((InfoResult) result).migrations.stream().filter(m -> "Pending".equals(m.state)).count());
                } catch (Exception e) {
                    infoTelemetryModel.setException(e);
                    throw e;
                }
            }
        } else if ("repair".equals(operation)) {
            result = flyway.repair();
        } else {
            result = pluginRegister.getPlugins(CommandExtension.class).stream()
                                   .filter(commandExtension -> commandExtension.handlesCommand(operation))
                                   .max(Comparator.comparingInt(CommandExtension::getPriority))
                                   .map(commandExtension -> commandExtension.handle(operation, flyway.getConfiguration(), commandLineArguments.getFlags(), telemetryManager))
                                   .orElseThrow(() -> new FlywayException("No command extension found to handle command: " + operation));
        }

        return result;
    }












    private static void printJson(CommandLineArguments commandLineArguments, OperationResult object) {
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
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .create();
        return gson.toJson(object);
    }

    /**
     * Filters the properties to remove the Flyway Commandline-specific ones.
     */
    private static void filterProperties(Map<String, String> config) {
        config.remove(ConfigUtils.JAR_DIRS);
        config.remove(ConfigUtils.CONFIG_FILES);
        config.remove(ConfigUtils.CONFIG_FILE_ENCODING);
    }












    private static void printUsage() {
        String indent = "    ";

        LOG.info("Usage");
        LOG.info(indent + "flyway [options] command");
        LOG.info("");
        LOG.info("By default, the configuration will be read from conf/flyway.conf.");
        LOG.info("Options passed from the command-line override the configuration.");
        LOG.info("");
        LOG.info("Commands");
        List<Pair<String, String>> usages = pluginRegister.getPlugins(CommandExtension.class).stream().flatMap(e -> e.getUsage().stream()).collect(Collectors.toList());
        int padSize = usages.stream().max(Comparator.comparingInt(u -> u.getLeft().length())).map(u -> u.getLeft().length() + 3).orElse(11);
        LOG.info(indent + StringUtils.rightPad("migrate", padSize, ' ') + "Migrates the database");
        LOG.info(indent + StringUtils.rightPad("clean", padSize, ' ') + "Drops all objects in the configured schemas");
        LOG.info(indent + StringUtils.rightPad("info", padSize, ' ') + "Prints the information about applied, current and pending migrations");
        LOG.info(indent + StringUtils.rightPad("validate", padSize, ' ') + "Validates the applied migrations against the ones on the classpath");
        LOG.info(indent + StringUtils.rightPad("baseline", padSize, ' ') + "Baselines an existing database at the baselineVersion");
        LOG.info(indent + StringUtils.rightPad("repair", padSize, ' ') + "Repairs the schema history table");
        usages.forEach(u -> LOG.info(indent + StringUtils.rightPad(u.getLeft(), padSize, ' ') + u.getRight()));
        LOG.info("");
        LOG.info("Configuration parameters (Format: -key=value)");
        LOG.info(indent + "driver                         Fully qualified classname of the JDBC driver");
        LOG.info(indent + "url                            Jdbc url to use to connect to the database");
        LOG.info(indent + "user                           User to use to connect to the database");
        LOG.info(indent + "password                       Password to use to connect to the database");
        LOG.info(indent + "connectRetries                 Maximum number of retries when attempting to connect to the database");
        LOG.info(indent + "initSql                        SQL statements to run to initialize a new database connection");
        LOG.info(indent + "schemas                        Comma-separated list of the schemas managed by Flyway");
        LOG.info(indent + "table                          Name of Flyway's schema history table");
        LOG.info(indent + "locations                      Classpath locations to scan recursively for migrations");
        LOG.info(indent + "failOnMissingLocations         Whether to fail if a location specified in the flyway.locations option doesn't exist");
        LOG.info(indent + "resolvers                      Comma-separated list of custom MigrationResolvers");
        LOG.info(indent + "skipDefaultResolvers           Skips default resolvers (jdbc, sql and Spring-jdbc)");
        LOG.info(indent + "sqlMigrationPrefix             File name prefix for versioned SQL migrations");
        LOG.info(indent + "undoSqlMigrationPrefix         [" + "teams] File name prefix for undo SQL migrations");
        LOG.info(indent + "repeatableSqlMigrationPrefix   File name prefix for repeatable SQL migrations");
        LOG.info(indent + "sqlMigrationSeparator          File name separator for SQL migrations");
        LOG.info(indent + "sqlMigrationSuffixes           Comma-separated list of file name suffixes for SQL migrations");
        LOG.info(indent + "stream                         [" + "teams] Stream SQL migrations when executing them");
        LOG.info(indent + "batch                          [" + "teams] Batch SQL statements when executing them");
        LOG.info(indent + "mixed                          Allow mixing transactional and non-transactional statements");
        LOG.info(indent + "encoding                       Encoding of SQL migrations");
        LOG.info(indent + "detectEncoding                 [" + "teams] Whether Flyway should try to automatically detect SQL migration file encoding");
        LOG.info(indent + "executeInTransaction           Whether SQL should execute within a transaction");
        LOG.info(indent + "placeholderReplacement         Whether placeholders should be replaced");
        LOG.info(indent + "placeholders                   Placeholders to replace in sql migrations");
        LOG.info(indent + "placeholderPrefix              Prefix of every placeholder");
        LOG.info(indent + "placeholderSuffix              Suffix of every placeholder");
        LOG.info(indent + "scriptPlaceholderPrefix        Prefix of every script placeholder");
        LOG.info(indent + "scriptPlaceholderSuffix        Suffix of every script placeholder");
        LOG.info(indent + "lockRetryCount                 The maximum number of retries when trying to obtain a lock");
        LOG.info(indent + "jdbcProperties                 Properties to pass to the JDBC driver object");
        LOG.info(indent + "installedBy                    Username that will be recorded in the schema history table");
        LOG.info(indent + "target                         Target version up to which Flyway should use migrations");
        LOG.info(indent + "cherryPick                     [" + "teams] Comma separated list of migrations that Flyway should consider when migrating");
        LOG.info(indent + "skipExecutingMigrations        [" + "teams] Whether Flyway should skip actually executing the contents of the migrations");
        LOG.info(indent + "outOfOrder                     Allows migrations to be run \"out of order\"");
        LOG.info(indent + "callbacks                      Comma-separated list of FlywayCallback classes, or locations to scan for FlywayCallback classes");
        LOG.info(indent + "skipDefaultCallbacks           Skips default callbacks (sql)");
        LOG.info(indent + "validateOnMigrate              Validate when running migrate");
        LOG.info(indent + "validateMigrationNaming        Validate file names of SQL migrations (including callbacks)");
        LOG.info(indent + "ignoreMigrationPatterns        Patterns of migrations and states to ignore during validate");
        LOG.info(indent + "cleanOnValidationError         Automatically clean on a validation error");
        LOG.info(indent + "cleanDisabled                  Whether to disable clean");
        LOG.info(indent + "baselineVersion                Version to tag schema with when executing baseline");
        LOG.info(indent + "baselineDescription            Description to tag schema with when executing baseline");
        LOG.info(indent + "baselineOnMigrate              Baseline on migrate against uninitialized non-empty schema");
        LOG.info(indent + "configFiles                    Comma-separated list of config files to use");
        LOG.info(indent + "configFileEncoding             Encoding to use when loading the config files");
        LOG.info(indent + "jarDirs                        Comma-separated list of dirs for Jdbc drivers & Java migrations");
        LOG.info(indent + "createSchemas                  Whether Flyway should attempt to create the schemas specified in the schemas property");
        LOG.info(indent + "dryRunOutput                   [" + "teams] File where to output the SQL statements of a migration dry run");
        LOG.info(indent + "errorOverrides                 [" + "teams] Rules to override specific SQL states and errors codes");
        LOG.info(indent + "oracle.sqlplus                 [" + "teams] Enable Oracle SQL*Plus command support");
        LOG.info(indent + "licenseKey                     [" + "teams] Your Flyway license key");
        LOG.info(indent + "color                          Whether to colorize output. Values: always, never, or auto (default)");
        LOG.info(indent + "outputFile                     Send output to the specified file alongside the console");
        LOG.info(indent + "outputType                     Serialise the output in the given format, Values: json");
        LOG.info("");
        LOG.info("Flags");
        LOG.info(indent + "-X                Print debug output");
        LOG.info(indent + "-q                Suppress all output, except for errors and warnings");
        LOG.info(indent + "-n                Suppress prompting for a user and password");
        LOG.info(indent + "--help, -h, -?    Print this usage info and exit");
        LOG.info(indent + "-community        Run the Flyway Community Edition (default)");
        LOG.info(indent + "-teams            Run the Flyway Teams Edition");
        LOG.info("");
        LOG.info("Flyway Usage Example");
        LOG.info(indent + "flyway -user=myuser -password=s3cr3t -url=jdbc:h2:mem -placeholders.abc=def migrate");
        LOG.info("");
        LOG.info("More info at " + FlywayDbWebsiteLinks.USAGE_COMMANDLINE);
        LOG.info("Learn more about Flyway Teams edition at " + FlywayDbWebsiteLinks.TRY_TEAMS_EDITION);
    }

    private static List<File> getJdbcDriverJarFiles() {
        File driversDir = new File(ClassUtils.getInstallDir(Main.class), "drivers");
        File[] files = driversDir.listFiles((dir, name) -> name.endsWith(".jar"));

        // see javadoc of listFiles(): null if given path is not a real directory
        if (files == null) {
            LOG.debug("Directory for Jdbc Drivers not found: " + driversDir.getAbsolutePath());
            return Collections.emptyList();
        }

        return Arrays.asList(files);
    }

    private static List<File> getJavaMigrationJarFiles(String[] dirs) {
        if (dirs.length == 0) {
            return Collections.emptyList();
        }

        List<File> jarFiles = new ArrayList<>();
        for (String dirName : dirs) {
            File dir = new File(dirName);
            File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jar"));

            // see javadoc of listFiles(): null if given path is not a real directory
            if (files == null) {
                throw new FlywayException("Directory for Java Migrations not found: " + dirName);
            }

            jarFiles.addAll(Arrays.asList(files));
        }

        return jarFiles;
    }

    protected static void loadConfigurationFromConfigFiles(Map<String, String> config, CommandLineArguments commandLineArguments, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(commandLineArguments, envVars);
        File installationDir = new File(ClassUtils.getInstallDir(Main.class));

        config.putAll(ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding));

        for (File configFile : determineLegacyConfigFilesFromArgs(commandLineArguments, envVars)) {
            config.putAll(ConfigUtils.loadConfigurationFile(configFile, encoding, true));
        }
    }

    /**
     * If no user or password has been provided, prompt for it. If you want to avoid the prompt, pass in an empty
     * user or password.
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

        boolean hasUser = config.containsKey(ConfigUtils.USER) || config.keySet().stream().anyMatch(p -> p.toLowerCase().endsWith(".user"));

        if (!hasUser



                && needsUser(url, config.getOrDefault(ConfigUtils.PASSWORD, null))) {
            config.put(ConfigUtils.USER, console.readLine("Database user: "));
        }

        boolean hasPassword = config.containsKey(ConfigUtils.PASSWORD) || config.keySet().stream().anyMatch(p -> p.toLowerCase().endsWith(".password"));

        if (!hasPassword



                && needsPassword(url, config.get(ConfigUtils.USER))) {
            char[] password = console.readPassword("Database password: ");
            config.put(ConfigUtils.PASSWORD, password == null ? "" : String.valueOf(password));
        }
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a username.
     */
    protected static boolean needsUser(String url, String password) {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForUrl(url);
        if (databaseType.detectUserRequiredByUrl(url)) {








             return true;

        }

        return false;
    }

    /**
     * Detect whether the JDBC URL specifies a known authentication mechanism that does not need a password.
     */
    protected static boolean needsPassword(String url, String username) {
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForUrl(url);
        if (databaseType.detectPasswordRequiredByUrl(url)) {








             return true;

        }

        return false;
    }

    private static List<File> determineLegacyConfigFilesFromArgs(CommandLineArguments commandLineArguments, Map<String, String> envVars) {

        String workingDirectory = commandLineArguments.isWorkingDirectorySet() ? commandLineArguments.getWorkingDirectory() : null;

        Stream<String> configFilePaths = envVars.containsKey(ConfigUtils.CONFIG_FILES) ?
                Arrays.stream(StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) :
                commandLineArguments.getConfigFiles().stream();

        return configFilePaths.map(path -> Paths.get(path).isAbsolute() ? new File(path) : new File(workingDirectory, path)).collect(Collectors.toList());
    }

    private static List<File> getTomlConfigFilePaths() {
        String[] fileLocations = StringUtils.tokenizeToStringArray(System.getenv("FLYWAY_CONFIG_FILES"), ",");

        return fileLocations == null ? new ArrayList<>() : Arrays.stream(fileLocations)
                                                                 .filter(f -> f.endsWith(".toml"))
                                                                 .map(File::new)
                                                                 .collect(Collectors.toList());
    }

    /**
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