/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import static org.flywaydb.commandline.ThreadUtils.terminate;
import static org.flywaydb.commandline.logging.LoggingUtils.getLogCreator;
import static org.flywaydb.commandline.logging.LoggingUtils.initLogging;

import org.flywaydb.core.extensibility.ConfigurationParameter;
import org.flywaydb.core.internal.configuration.HelpText;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.commandline.configuration.ConfigurationManagerImpl;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
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
import org.flywaydb.core.api.logging.LogFactory;
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
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.reports.ReportGenerationOutput;
import org.flywaydb.core.internal.reports.ReportGenerationOutputMerger;
import org.flywaydb.core.internal.reports.ResultReportGenerator;
import org.flywaydb.core.internal.util.CommandExtensionUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.JsonUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TelemetryUtils;

public class Main {
    private static Log LOG;
    private static final PluginRegister PLUGIN_REGISTER = new PluginRegister();
    private static boolean hasPrintedLicense;

    public static void main(final String[] args) throws Exception {
        int exitCode = 0;
        JavaVersionPrinter.printJavaVersion();

        final var telemetryStartSpan = new EventTelemetryModel("telemetry-startup", null);
        final var flywayTelemetryManager = PLUGIN_REGISTER.getInstanceOf(FlywayTelemetryManager.class);
        final var flywayTelemetryHandle = flywayTelemetryManager.start(telemetryStartSpan.getStartTime());
        telemetryStartSpan.setId(flywayTelemetryManager.startEvent(telemetryStartSpan));
        flywayTelemetryManager.logEvent(telemetryStartSpan);

        try {
            final CommandLineArguments commandLineArguments = new CommandLineArguments(PLUGIN_REGISTER, args);
            LOG = initLogging(Main.class, commandLineArguments);

            try {
                ReportGenerationOutput reportGenerationOutput = new ReportGenerationOutput();

                final Configuration configuration;
                try (final var ignored = new EventTelemetryModel("parse-args", flywayTelemetryManager)) {
                    commandLineArguments.validate();
                    LogFactory.setLogLevel(commandLineArguments.getLogLevel().toLogLevel());
                    LogFactory.setJsonLogsEnabled(commandLineArguments.shouldOutputLogsInJson());

                    if (printHelp(commandLineArguments)) {
                        terminate(0, flywayTelemetryHandle);
                        return;
                    }
                    configuration = new ConfigurationManagerImpl().getConfiguration(commandLineArguments);
                    flywayTelemetryManager.notifyPermitChanged(LicenseGuard.getPermit(configuration));
                    flywayTelemetryManager.notifyRootConfigChanged(configuration);
                }

                final boolean isSingleCommandExtension = commandLineArguments.getOperations().size() == 1
                    && CommandExtensionUtils.isLightweightCommandExtension(configuration,
                    commandLineArguments.getOperations().get(0));

                final CompletableFuture<String> updateCheckFuture = commandLineArguments.skipCheckForUpdate()
                    || "json".equalsIgnoreCase(configuration.getModernConfig().getFlyway().getOutputType())
                    ? CompletableFuture.completedFuture(null)
                    : CompletableFuture.supplyAsync(() -> {
                        try (final var ignored = new EventTelemetryModel("check-for-update", flywayTelemetryManager)) {
                            return new MavenVersionChecker().checkForVersionUpdates();
                        }
                    });

                final OperationResult result = executeFlyway(flywayTelemetryManager,
                    commandLineArguments,
                    configuration,
                    isSingleCommandExtension);

                if (!isSingleCommandExtension) {
                    final List<ResultReportGenerator> reportGenerators = PLUGIN_REGISTER.getInstancesOf(
                        ResultReportGenerator.class);
                    for (final ResultReportGenerator resultReportGenerator : reportGenerators) {
                        final ReportGenerationOutput output = resultReportGenerator.generateReport(result,
                            configuration);
                        reportGenerationOutput = ReportGenerationOutputMerger.merge(reportGenerationOutput, output);
                    }

                    if (configuration.getPluginRegister()
                        .getExact(PublishingConfigurationExtension.class)
                        .isPublishResult()) {
                        publishOperationResult(configuration, result);
                        publishReport(configuration, reportGenerationOutput.reportDetails);
                    }

                    if (reportGenerationOutput.aggregateException != null) {
                        throw reportGenerationOutput.aggregateException;
                    }
                }

                if (commandLineArguments.shouldOutputJson()) {
                    printJson(commandLineArguments, result, reportGenerationOutput.reportDetails);
                }

                printUpdateMessage(updateCheckFuture);
            } catch (final FlywayLicensingException e) {
                final OperationResult errorOutput = ErrorOutput.toOperationResult(e);
                printError(commandLineArguments, e, errorOutput);
                exitCode = 35;
            } catch (final Exception e) {
                final OperationResult errorOutput = ErrorOutput.toOperationResult(e);
                printError(commandLineArguments, e, errorOutput);
                exitCode = e instanceof final FlywayException fe ? fe.getErrorCode().getExitCode() : 1;
            } finally {
                flushLog(commandLineArguments);
            }
        } finally {
            terminate(exitCode, flywayTelemetryHandle);
        }
    }

    private static void printLicenseInfo(final Configuration configuration, final String operation) {
        if (!hasPrintedLicense && !"auth".equals(operation)) {
            try {
                LicenseGuard.getPermit(configuration).print();
                LOG.info("See release notes here: " + FlywayDbWebsiteLinks.RELEASE_NOTES);
            } catch (final FlywayExpiredLicenseKeyException e) {
                LOG.error(e.getMessage());
            }
            for (final String warning : LicenseGuard.consumeDeferredWarnings(configuration)) {
                LOG.warn(warning);
            }
            hasPrintedLicense = true;
        }
    }

    private static void printUpdateMessage(final CompletableFuture<String> updateCheckFuture)
        throws ExecutionException, InterruptedException {
        if (updateCheckFuture.isDone()
            && !updateCheckFuture.isCompletedExceptionally()
            && !updateCheckFuture.isCancelled()) {
            final var updateMessage = updateCheckFuture.get();
            if (updateMessage != null) {
                LOG.info(updateMessage);
            }
        }
    }

    private static OperationResult executeFlyway(final FlywayTelemetryManager flywayTelemetryManager,
        final CommandLineArguments commandLineArguments,
        final Configuration configuration,
        final boolean isSingleCommandExtension) {
        // Command extensions don't need a full Flyway instance — they use configuration directly
        if (isSingleCommandExtension) {
            final String operation = commandLineArguments.getOperations().get(0);
            LogFactory.setConfiguration(configuration);
            printLicenseInfo(configuration, operation);
            return CommandExtensionUtils.runCommandExtension(configuration, operation, commandLineArguments.getFlags());
        }

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
            final Collection<OperationResult> individualResults = new ArrayList<>(commandLineArguments.getOperations()
                .size());
            for (final String operation : commandLineArguments.getOperations()) {
                printLicenseInfo(configuration, operation);
                final OperationResult operationResult = executeOperation(flyway,
                    operation,
                    commandLineArguments,
                    flywayTelemetryManager,
                    executionConfiguration);

                if (operationResult == null) {
                    continue;
                }

                individualResults.add(operationResult);
                if (operationResult instanceof HtmlResult
                    && ((HtmlResult) operationResult).exceptionObject instanceof FlywayMigrateException) {
                    break;
                }
            }
            result = new CompositeResult<>(individualResults);
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
        final OperationResult errorResult) throws JacksonException {
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
                    final boolean hasOnDiskMigrations = Arrays.stream(infos)
                        .map(MigrationInfo::getPhysicalLocation)
                        .anyMatch(StringUtils::hasLength);

                    if (!hasOnDiskMigrations) {
                        LOG.info("No migrations found on disk.\nHere are some relevant configuration settings.");
                        MigrationConfigPrinter.print(LOG, configuration);
                    }

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
                commandLineArguments.getFlags());
        }

        return result;
    }

    private static void publishOperationResult(final Configuration configuration, final OperationResult result) {
        if (result == null) {
            LOG.debug("Unable to publish null operation result");
            return;
        }

        final List<OperationResultPublisher> publishers = configuration.getPluginRegister()
            .getInstancesOf(OperationResultPublisher.class);
        for (final OperationResultPublisher publisher : publishers) {
            publisher.publish(configuration, result);
        }
    }

    private static void publishReport(final Configuration configuration, final ReportDetails reportDetails) {
        final List<OperationResultPublisher> publishers = configuration.getPluginRegister()
            .getInstancesOf(OperationResultPublisher.class);

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
        final ReportDetails reportDetails) throws JacksonException {
        final String json = convertObjectToJsonString(object, reportDetails);

        if (commandLineArguments.isOutputFileSet()) {
            final Path path = Paths.get(commandLineArguments.getOutputFile());
            final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

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
        throws JacksonException {
        final JsonMapper mapper = JsonUtils.getJsonMapper();
        final ObjectNode objectNode = mapper.valueToTree(object);

        if (reportDetails != null) {
            if (reportDetails.getJsonReportFilename() != null) {
                objectNode.put("jsonReport", reportDetails.getJsonReportFilename());
            }
            if (reportDetails.getHtmlReportFilename() != null) {
                objectNode.put("htmlReport", reportDetails.getHtmlReportFilename());
            }
            if (reportDetails.getSarifReportFilename() != null) {
                objectNode.put("sarifReport", reportDetails.getSarifReportFilename());
            }
        }

        return mapper.writeValueAsString(objectNode);
    }

    private static void printUsage(final Boolean fullVersion) {
        final HelpText help = new HelpText();
        final List<String> usage = List.of("flyway [options] [command]", "flyway help [command]");
        help.setUsage(usage);

        if (fullVersion) {
            final String additionalInfo = "By default, the configuration will be read from conf/flyway.toml file.\n"
                + "Options passed from the command-line override the configuration.";
            help.setAdditionalInfo(additionalInfo);
        }

        final List<Pair<String, String>> commands = new ArrayList<>(PLUGIN_REGISTER.getInstancesOf(CommandExtension.class)
            .stream()
            .flatMap(e -> e.getUsage()
                .stream()
                .map(p -> e.inPreview() ? Pair.of(p.getLeft() + " (preview)", p.getRight()) : p))
            .toList());
        commands.add(Pair.of("help", "Print this usage info and exit"));

        help.setCommands(commands);

        final List<ConfigurationParameter> parameters = new ArrayList<>(List.of(new ConfigurationParameter("driver",
                "Fully qualified classname of the JDBC driver",
                false),
            new ConfigurationParameter("url", "Jdbc url to use to connect to the database", false),
            new ConfigurationParameter("user", "User to use to connect to the database", false),
            new ConfigurationParameter("password", "Password to use to connect to the database", false)));
        if (fullVersion) {
            parameters.addAll(List.of(new ConfigurationParameter("connectRetries",
                    "Maximum number of retries when attempting to connect to the database",
                    false),
                new ConfigurationParameter("initSql",
                    "SQL statements to run to initialize a new database connection",
                    false),
                new ConfigurationParameter("schemas", "Comma-separated list of the schemas managed by Flyway", false),
                new ConfigurationParameter("table", "Name of Flyway's schema history table", false),
                new ConfigurationParameter("locations",
                    "Classpath locations to scan recursively for migrations",
                    false),
                new ConfigurationParameter("failOnMissingLocations",
                    "Whether to fail if a location specified in the flyway.locations option doesn't exist",
                    false),
                new ConfigurationParameter("resolvers", "Comma-separated list of custom MigrationResolvers", false),
                new ConfigurationParameter("skipDefaultResolvers",
                    "Skips default resolvers (jdbc, sql and Spring-jdbc)",
                    false),
                new ConfigurationParameter("sqlMigrationPrefix",
                    "File name prefix for versioned SQL migrations",
                    false),
                new ConfigurationParameter("undoSqlMigrationPrefix",
                    "[teams] File name prefix for undo SQL migrations",
                    false),
                new ConfigurationParameter("repeatableSqlMigrationPrefix",
                    "File name prefix for repeatable SQL migrations",
                    false),
                new ConfigurationParameter("sqlMigrationSeparator", "File name separator for SQL migrations", false),
                new ConfigurationParameter("sqlMigrationSuffixes",
                    "Comma-separated list of file name suffixes for SQL migrations",
                    false),
                new ConfigurationParameter("stream", "[teams] Stream SQL migrations when executing them", false),
                new ConfigurationParameter("batch", "[teams] Batch SQL statements when executing them", false),
                new ConfigurationParameter("mixed",
                    "Allow mixing transactional and non-transactional statements",
                    false),
                new ConfigurationParameter("encoding", "Encoding of SQL migrations", false),
                new ConfigurationParameter("detectEncoding",
                    "[teams] Whether Flyway should try to automatically detect SQL migration file encoding",
                    false),
                new ConfigurationParameter("executeInTransaction",
                    "Whether SQL should execute within a transaction",
                    false),
                new ConfigurationParameter("placeholderReplacement", "Whether placeholders should be replaced", false),
                new ConfigurationParameter("placeholders", "Placeholders to replace in sql migrations", false),
                new ConfigurationParameter("placeholderPrefix", "Prefix of every placeholder", false),
                new ConfigurationParameter("placeholderSuffix", "Suffix of every placeholder", false),
                new ConfigurationParameter("scriptPlaceholderPrefix", "Prefix of every script placeholder", false),
                new ConfigurationParameter("scriptPlaceholderSuffix", "Suffix of every script placeholder", false),
                new ConfigurationParameter("lockRetryCount",
                    "The maximum number of retries when trying to obtain a lock",
                    false),
                new ConfigurationParameter("jdbcProperties", "Properties to pass to the JDBC driver object", false),
                new ConfigurationParameter("installedBy",
                    "Username that will be recorded in the schema history table",
                    false),
                new ConfigurationParameter("target", "Target version up to which Flyway should use migrations", false),
                new ConfigurationParameter("cherryPick",
                    "[teams] Comma separated list of migrations that Flyway should consider when migrating",
                    false),
                new ConfigurationParameter("skipExecutingMigrations",
                    "Whether Flyway should skip actually executing the contents of the migrations",
                    false),
                new ConfigurationParameter("outOfOrder", "Allows migrations to be run \"out of order\"", false),
                new ConfigurationParameter("callbacks",
                    "Comma-separated list of FlywayCallback classes, or locations to scan for FlywayCallback classes",
                    false),
                new ConfigurationParameter("skipDefaultCallbacks", "Skips default callbacks (sql)", false),
                new ConfigurationParameter("validateOnMigrate", "Validate when running migrate", false),
                new ConfigurationParameter("validateMigrationNaming",
                    "Validate file names of SQL migrations (including callbacks)",
                    false),
                new ConfigurationParameter("ignoreMigrationPatterns",
                    "Patterns of migrations and states to ignore during validate",
                    false),
                new ConfigurationParameter("cleanDisabled", "Whether to disable clean", false),
                new ConfigurationParameter("baselineVersion",
                    "Version to tag schema with when executing baseline",
                    false),
                new ConfigurationParameter("baselineDescription",
                    "Description to tag schema with when executing baseline",
                    false),
                new ConfigurationParameter("baselineOnMigrate",
                    "Baseline on migrate against uninitialized non-empty schema",
                    false),
                new ConfigurationParameter("configFiles", "Comma-separated list of config files to use", false),
                new ConfigurationParameter("configFileEncoding",
                    "Encoding to use when loading the config files",
                    false),
                new ConfigurationParameter("jarDirs",
                    "Comma-separated list of dirs for Jdbc drivers & Java migrations",
                    false),
                new ConfigurationParameter("createSchemas",
                    "Whether Flyway should attempt to create the schemas specified in the schemas property",
                    false),
                new ConfigurationParameter("dryRunOutput",
                    "[teams] File where to output the SQL statements of a migration dry run",
                    false),
                new ConfigurationParameter("errorOverrides",
                    "[teams] Rules to override specific SQL states and errors codes",
                    false),
                new ConfigurationParameter("color",
                    "Whether to colorize output. Values: always, never, or auto (default)",
                    false),
                new ConfigurationParameter("outputFile",
                    "Send output to the specified file alongside the console",
                    false),
                new ConfigurationParameter("outputType",
                    "Serialise the output in the given format, Values: json",
                    false)));
        } else {
            parameters.add(new ConfigurationParameter("(To see all configuration options please run flyway --help)",
                null,
                false));
        }
        help.setParameters(parameters);

        final List<ConfigurationParameter> flags = List.of(new ConfigurationParameter("-X",
                "Print debug output",
                false),
            new ConfigurationParameter("-q", "Suppress all output, except for errors and warnings", false),
            new ConfigurationParameter("--help, -h, -?", "Print this usage info and exit", false));
        help.setFlags(flags);

        final List<String> examples = List.of(
            "flyway -user=myuser -password=s3cr3t -url=jdbc:h2:mem -placeholders.abc=def migrate",
            "flyway help check");
        help.setExamples(examples);
        help.setDocumentationLink(FlywayDbWebsiteLinks.USAGE_COMMANDLINE);

        LOG.info(help.getText());
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
