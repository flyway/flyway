/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.commandline;

import static org.flywaydb.commandline.logging.LoggingUtils.getLogCreator;
import static org.flywaydb.commandline.logging.LoggingUtils.initLogging;
import static org.flywaydb.commandline.utils.TelemetryUtils.populateRootTelemetry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.SneakyThrows;
import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.commandline.configuration.ConfigurationManagerImpl;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
import org.flywaydb.commandline.utils.TelemetryUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationFilter;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.ErrorOutput;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.InfoTelemetryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.info.MigrationFilterImpl;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.license.FlywayExpiredLicenseKeyException;
import org.flywaydb.core.internal.license.FlywayLicensingException;
import org.flywaydb.core.internal.logging.EvolvingLog;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.publishing.OperationResultPublisher;
import org.flywaydb.core.internal.publishing.PublishingConfigurationExtension;
import org.flywaydb.core.internal.reports.ReportGenerationOutput;
import org.flywaydb.core.internal.reports.ResultReportGenerator;
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.util.CommandExtensionUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.JsonUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;




public class Main {
    private static Log LOG;
    private static final PluginRegister PLUGIN_REGISTER = new PluginRegister();
    private static boolean hasPrintedLicense;

    public static void main(String[] args) throws Exception {
        int exitCode = 0;

        final FlywayTelemetryManager flywayTelemetryManager = getFlywayTelemetryManager();

        try {
            JavaVersionPrinter.printJavaVersion();
            final CommandLineArguments commandLineArguments = new CommandLineArguments(PLUGIN_REGISTER, args);
            LOG = initLogging(Main.class, commandLineArguments);

            try {
                ReportGenerationOutput reportGenerationOutput = new ReportGenerationOutput();
                
                final Configuration configuration;
                try (final var ignored = new EventTelemetryModel("parse-args", flywayTelemetryManager)) {
                    commandLineArguments.validate();

                    if (printHelp(commandLineArguments)) {
                        return;
                    }

                    configuration = new ConfigurationManagerImpl().getConfiguration(commandLineArguments);
                }

                if (flywayTelemetryManager != null) {
                    flywayTelemetryManager.setRootTelemetryModel(populateRootTelemetry(flywayTelemetryManager.getRootTelemetryModel(),
                        configuration,
                        LicenseGuard.getPermit(configuration)));
                }

                if (!commandLineArguments.skipCheckForUpdate()) {
                    try (final var ignored = new EventTelemetryModel("check-for-update", flywayTelemetryManager)) {
                        MavenVersionChecker.checkForVersionUpdates();
                    }
                }

                LocalDateTime executionTime = LocalDateTime.now();
                OperationResult result = executeFlyway(flywayTelemetryManager, commandLineArguments, configuration);

                final List<ResultReportGenerator> reportGenerators = PLUGIN_REGISTER.getPlugins(ResultReportGenerator.class);
                for (final ResultReportGenerator resultReportGenerator : reportGenerators) {
                    reportGenerationOutput = resultReportGenerator.generateReport(result, configuration, executionTime);
                }

                if (configuration.getPluginRegister().getPlugin(PublishingConfigurationExtension.class).isPublishResult()) {
                    publishOperationResult(configuration, result);
                    publishReport(configuration, reportGenerationOutput.reportDetails);
                }

                if (reportGenerationOutput.aggregateException != null) {
                    throw reportGenerationOutput.aggregateException;
                }

                if (commandLineArguments.shouldOutputJson()) {
                    printJson(commandLineArguments, result, reportGenerationOutput.reportDetails);
                }
            } catch (final FlywayLicensingException e) {
                final OperationResult errorOutput = ErrorOutput.toOperationResult(e);
                printError(commandLineArguments, e, errorOutput);
                exitCode = 35;
            } catch (final Exception e) {
                final OperationResult errorOutput = ErrorOutput.toOperationResult(e);
                printError(commandLineArguments, e, errorOutput);
                exitCode = 1;
            } finally {
                flushLog(commandLineArguments);
            }
        } finally {
            if (flywayTelemetryManager != null) {
                flywayTelemetryManager.close();
            }
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static FlywayTelemetryManager getFlywayTelemetryManager() {
        if (StringUtils.hasText(System.getenv("REDGATE_DISABLE_TELEMETRY"))) {
            return null;
        }

        final var telemetryStartSpan = new EventTelemetryModel("telemetry-startup", null);
        final var flywayTelemetryManager = new FlywayTelemetryManager(PLUGIN_REGISTER);
        flywayTelemetryManager.setRootTelemetryModel(populateRootTelemetry(flywayTelemetryManager.getRootTelemetryModel(),
            null,
            null));
        flywayTelemetryManager.logEvent(telemetryStartSpan);
        return flywayTelemetryManager;
    }

    private static void printLicenseInfo(final Configuration configuration, final String operation) {
        if (!hasPrintedLicense && !"auth".equals(operation)) {
            try {
                LicenseGuard.getPermit(configuration).print();
                LOG.info("See release notes here: " + FlywayDbWebsiteLinks.RELEASE_NOTES);
            } catch (final FlywayExpiredLicenseKeyException e) {
                LOG.error(e.getMessage());
            }
            hasPrintedLicense = true;
        }
    }

    private static OperationResult executeFlyway(final FlywayTelemetryManager flywayTelemetryManager,
        final CommandLineArguments commandLineArguments,
        final Configuration configuration) {
        final Flyway flyway = Flyway.configure(configuration.getClassLoader()).configuration(configuration).load();
        final Configuration executionConfiguration = flyway.getConfiguration();
        final OperationResult result;

        if (commandLineArguments.getOperations().size() == 1) {
            final String operation = commandLineArguments.getOperations().get(0);
            printLicenseInfo(configuration, operation);
            result = executeOperation(flyway,
                operation,
                commandLineArguments,
                flywayTelemetryManager,
                executionConfiguration);
        } else {
            final CompositeResult<OperationResult> compositeResult = new CompositeResult<>();
            for (final String operation : commandLineArguments.getOperations()) {
                printLicenseInfo(configuration, operation);
                final OperationResult operationResult = executeOperation(flyway,
                    operation,
                    commandLineArguments,
                    flywayTelemetryManager,
                    executionConfiguration);
                compositeResult.individualResults.add(operationResult);
                if (operationResult instanceof HtmlResult
                    && ((HtmlResult) operationResult).exceptionObject instanceof FlywayMigrateException) {
                    break;
                }
            }
            result = compositeResult;
        }

        if (configuration instanceof final ClassicConfiguration classicConfiguration) {
            classicConfiguration.configure(executionConfiguration);
        }

        if (configuration instanceof final FluentConfiguration fluentConfiguration) {
            fluentConfiguration.configuration(executionConfiguration);
        }

        return result;
    }

    private static void printError(final CommandLineArguments commandLineArguments,
        final Exception e,
        final OperationResult errorResult) throws JsonProcessingException {
        if (commandLineArguments.shouldOutputJson()) {
            printJson(commandLineArguments, errorResult, null);
        } else {
            if (commandLineArguments.getLogLevel() == Level.DEBUG) {
                LOG.error("Unexpected error", e);
            } else {
                LOG.error(getMessagesFromException(e));
            }
        }
        flushLog(commandLineArguments);
    }

    private static void flushLog(final CommandLineArguments commandLineArguments) {
        final Log currentLog = ((EvolvingLog) LOG).getLog();
        if (currentLog instanceof BufferedLog) {
            ((BufferedLog) currentLog).flush(getLogCreator(commandLineArguments).createLogger(Main.class));
        }
    }

    private static String getMessagesFromException(Throwable e) {
        final StringBuilder condensedMessages = new StringBuilder();
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
    private static OperationResult executeOperation(final Flyway flyway,
        final String operation,
        final CommandLineArguments commandLineArguments,
        final FlywayTelemetryManager telemetryManager,
        final Configuration configuration) {
        OperationResult result = null;
        flyway.setFlywayTelemetryManager(telemetryManager);

        if ("clean".equals(operation)) {
            result = flyway.clean();
        } else if ("baseline".equals(operation)) {
            result = flyway.baseline();
        } else if ("migrate".equals(operation)) {
            try {







                
                result = flyway.migrate();
                



                
            } catch (final FlywayMigrateException e) {
                result = ErrorOutput.fromMigrateException(e);
                final HtmlResult hr = (HtmlResult) result;
                hr.setException(e);
            }
        } else if ("validate".equals(operation)) {
            try (final EventTelemetryModel telemetryModel = new EventTelemetryModel("validate", telemetryManager)) {
                try {
                    if (commandLineArguments.shouldOutputJson()) {
                        result = flyway.validateWithResult();
                    } else {
                        flyway.validate();
                    }
                } catch (final Exception e) {
                    telemetryModel.setException(e);
                    throw e;
                }
            }
        } else if ("info".equals(operation)) {
            try (final InfoTelemetryModel infoTelemetryModel = new InfoTelemetryModel(telemetryManager)) {
                try {
                    final MigrationInfoService info = flyway.info();
                    final MigrationInfo current = info.current();
                    final MigrationVersion currentSchemaVersion = current == null
                        ? MigrationVersion.EMPTY
                        : current.getVersion();

                    final MigrationVersion schemaVersionToOutput = currentSchemaVersion == null
                        ? MigrationVersion.EMPTY
                        : currentSchemaVersion;
                    LOG.info("Schema version: " + schemaVersionToOutput);
                    LOG.info("");

                    final MigrationFilter filter = getInfoFilter(commandLineArguments);
                    result = info.getInfoResult(filter);
                    final MigrationInfo[] infos = info.all(filter);

                    if (commandLineArguments.isFilterOnMigrationIds()) {
                        //Must use System.out here rather than LOG.info because LogCreator is empty.
                        System.out.print(MigrationInfoDumper.dumpToMigrationIds(infos));
                    } else {
                        LOG.info(MigrationInfoDumper.dumpToAsciiTable(infos));
                    }
                    infoTelemetryModel.setNumberOfMigrations(((InfoResult) result).migrations.size());
                    infoTelemetryModel.setNumberOfPendingMigrations((int) ((InfoResult) result).migrations.stream()
                        .filter(m -> "Pending".equals(m.state))
                        .count());
                    infoTelemetryModel.setOldestMigrationInstalledOnUTC(TelemetryUtils.getOldestMigration(((InfoResult) result).migrations));
                } catch (final Exception e) {
                    infoTelemetryModel.setException(e);
                    throw e;
                }
            }
        } else if ("repair".equals(operation)) {
            result = flyway.repair();
        } else if ("undo".equals(operation)) {
            result = flyway.undo();
        } else {
            result = CommandExtensionUtils.runCommandExtension(configuration,
                operation,
                commandLineArguments.getFlags(),
                telemetryManager);
        }

        return result;
    }

    private static void publishOperationResult(final Configuration configuration, final OperationResult result) {
        if (result == null) {
            LOG.debug("Unable to publish null operation result");
            return;
        }

        final List<OperationResultPublisher> publishers = configuration.getPluginRegister().getPlugins(
            OperationResultPublisher.class);
        for (final OperationResultPublisher publisher : publishers) {
            publisher.publish(configuration, result);
        }
    }

    private static void publishReport(final Configuration configuration, final ReportDetails reportDetails) {
        final List<OperationResultPublisher> publishers = configuration.getPluginRegister().getPlugins(
            OperationResultPublisher.class);

        for (final OperationResultPublisher publisher : publishers) {
            publisher.publishReport(configuration, reportDetails);
        }
    }

    private static MigrationFilterImpl getInfoFilter(final CommandLineArguments commandLineArguments) {
        return new MigrationFilterImpl(commandLineArguments.getInfoSinceDate(),
            commandLineArguments.getInfoUntilDate(),
            commandLineArguments.getInfoSinceVersion(),
            commandLineArguments.getInfoUntilVersion(),
            commandLineArguments.getInfoOfState());
    }

    private static void printJson(final CommandLineArguments commandLineArguments,
        final OperationResult object,
        final ReportDetails reportDetails) throws JsonProcessingException {
        final String json = convertObjectToJsonString(object, reportDetails);

        if (commandLineArguments.isOutputFileSet()) {
            final Path path = Paths.get(commandLineArguments.getOutputFile());
            final byte[] bytes = json.getBytes();

            try {
                Files.write(path,
                    bytes,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
            } catch (final IOException e) {
                throw new FlywayException("Could not write to output file " + commandLineArguments.getOutputFile(), e);
            }
        }

        System.out.println(json);
    }

    private static String convertObjectToJsonString(final Object object, final ReportDetails reportDetails)
        throws JsonProcessingException {
        final JsonMapper mapper = JsonUtils.getJsonMapper();
        final ObjectNode objectNode = mapper.valueToTree(object);

        if (reportDetails != null) {
            if (reportDetails.getJsonReportFilename() != null) {
                objectNode.put("jsonReport", reportDetails.getJsonReportFilename());
            }
            if (reportDetails.getHtmlReportFilename() != null) {
                objectNode.put("htmlReport", reportDetails.getHtmlReportFilename());
            }
        }

        return mapper.writeValueAsString(objectNode);
    }

    private static void printUsage(final Boolean fullVersion) {
        final String indent = "    ";

        LOG.info("Usage");
        LOG.info(indent + "flyway [options] [command]");
        LOG.info(indent + "flyway help [command]");
        LOG.info("");

        if (fullVersion) {
            LOG.info("By default, the configuration will be read from conf/flyway.conf.");
            LOG.info("Options passed from the command-line override the configuration.");
            LOG.info("");
        }

        LOG.info("Commands");
        final List<Pair<String, String>> usages = PLUGIN_REGISTER.getPlugins(CommandExtension.class)
            .stream()
            .flatMap(e -> e.getUsage().stream().map(p -> e.inPreview() ? Pair.of(p.getLeft() + " (preview)", p.getRight()) : p))
            .toList();
        final int padSize = usages.stream().max(Comparator.comparingInt(u -> u.getLeft().length())).map(u -> u.getLeft()
            .length() + 3).orElse(11);
        LOG.info(indent + StringUtils.rightPad("help", padSize, ' ') + "Print this usage info and exit");



        LOG.info(indent + StringUtils.rightPad("migrate", padSize, ' ') + "Migrates the database");
        LOG.info(indent + StringUtils.rightPad("clean", padSize, ' ') + "Drops all objects in the configured schemas");
        LOG.info(indent + StringUtils.rightPad("info", padSize, ' ') + "Prints the information about applied, current and pending migrations");
        LOG.info(indent + StringUtils.rightPad("validate", padSize, ' ') + "Validates the applied migrations against the ones on the classpath");
        LOG.info(indent + StringUtils.rightPad("baseline", padSize, ' ') + "Baselines an existing database at the baselineVersion");
        LOG.info(indent + StringUtils.rightPad("repair", padSize, ' ') + "Repairs the schema history table");
        for (final Pair<String, String> usage : usages) {
            final List<String> lines = Arrays.stream(usage.getRight().split("\n")).map(String::trim).toList();
            LOG.info(indent + StringUtils.rightPad(usage.getLeft(), padSize, ' ') + lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                LOG.info(indent + " ".repeat(padSize) + lines.get(i));
            }
        }
        LOG.info("");
        LOG.info("Configuration parameters (Format: -key=value)");
        LOG.info(indent + "driver                         Fully qualified classname of the JDBC driver");
        LOG.info(indent + "url                            Jdbc url to use to connect to the database");
        LOG.info(indent + "user                           User to use to connect to the database");
        LOG.info(indent + "password                       Password to use to connect to the database");

        if (fullVersion) {
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
            LOG.info(indent + "skipExecutingMigrations        Whether Flyway should skip actually executing the contents of the migrations");
            LOG.info(indent + "outOfOrder                     Allows migrations to be run \"out of order\"");
            LOG.info(indent + "callbacks                      Comma-separated list of FlywayCallback classes, or locations to scan for FlywayCallback classes");
            LOG.info(indent + "skipDefaultCallbacks           Skips default callbacks (sql)");
            LOG.info(indent + "validateOnMigrate              Validate when running migrate");
            LOG.info(indent + "validateMigrationNaming        Validate file names of SQL migrations (including callbacks)");
            LOG.info(indent + "ignoreMigrationPatterns        Patterns of migrations and states to ignore during validate");
            LOG.info(indent + "cleanOnValidationError         [Deprecated] Automatically clean on a validation error");
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
            LOG.info(indent + "licenseKey                     [" + "teams] Your Flyway license key");
            LOG.info(indent + "color                          Whether to colorize output. Values: always, never, or auto (default)");
            LOG.info(indent + "outputFile                     Send output to the specified file alongside the console");
            LOG.info(indent + "outputType                     Serialise the output in the given format, Values: json");
        } else {
            LOG.info(indent + "(To see all configuration options please run flyway --help)");
        }

        LOG.info("");
        LOG.info("Flags");
        LOG.info(indent + "-X                Print debug output");
        LOG.info(indent + "-q                Suppress all output, except for errors and warnings");
        LOG.info(indent + "-n                Suppress prompting for a user and password");
        LOG.info(indent + "--help, -h, -?    Print this usage info and exit");
        LOG.info("");
        LOG.info("Flyway Usage Example");
        LOG.info(indent + "flyway -user=myuser -password=s3cr3t -url=jdbc:h2:mem -placeholders.abc=def migrate");
        LOG.info(indent + "flyway help check");
        LOG.info("");
        LOG.info("More info at " + FlywayDbWebsiteLinks.USAGE_COMMANDLINE);
        LOG.info("Learn more about Flyway Teams edition at " + FlywayDbWebsiteLinks.TRY_TEAMS_EDITION);
    }

    private static boolean printHelp(final CommandLineArguments commandLineArguments) {
        final StringBuilder helpText = new StringBuilder();
        final CommandLineArguments.PrintUsage result = commandLineArguments.shouldPrintUsage(helpText);

        if (result == CommandLineArguments.PrintUsage.PRINT_NONE) {
            return false;
        } else {
            if (StringUtils.hasText(helpText.toString())) {
                LOG.info(helpText.toString());
            } else {
                printUsage(result == CommandLineArguments.PrintUsage.PRINT_ORIGINAL);
            }
            return true;
        }
    }
}
