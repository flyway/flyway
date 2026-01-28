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
package org.flywaydb.commandline.configuration;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CustomLog
public class CommandLineArguments {
    private static final String COMMUNITY_FALLBACK_FLAG = "-communityFallback";
    private static final String DEBUG_FLAG = "-X";
    private static final String QUIET_FLAG = "-q";
    private static final List<String> PRINT_VERSION_AND_EXIT_FLAGS = Arrays.asList("-v", "--version");
    private static final List<String> PRINT_USAGE_FLAGS = Arrays.asList("-?", "-h", "--help");
    private static final String SKIP_CHECK_FOR_UPDATE_FLAG = "-skipCheckForUpdate";
    private static final String MIGRATIONS_IDS_FLAG = "-migrationIds";

    // Command line specific configuration options
    private static final String OUTPUT_FILE = "outputFile";
    private static final String OUTPUT_TYPE = "outputType";

    private static final String OUTPUT_LOGS_IN_JSON = "outputLogsInJson";
    private static final String CONFIG_FILE_ENCODING = "configFileEncoding";
    private static final String CONFIG_FILES = "configFiles";
    private static final String COLOR = "color";
    private static final String WORKING_DIRECTORY = "workingDirectory";
    private static final String INFO_SINCE_DATE = "infoSinceDate";
    private static final String INFO_UNTIL_DATE = "infoUntilDate";
    private static final String INFO_SINCE_VERSION = "infoSinceVersion";
    private static final String INFO_UNTIL_VERSION = "infoUntilVersion";
    private static final String INFO_OF_STATE = "infoOfState";
    private static final Collection<String> COMMAND_LINE_ONLY_OPTIONS = new HashSet<>(Arrays.asList(OUTPUT_FILE,
        WORKING_DIRECTORY,
        INFO_SINCE_DATE,
        INFO_UNTIL_DATE,
        INFO_SINCE_VERSION,
        INFO_UNTIL_VERSION,
        INFO_OF_STATE));
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final List<String> VALID_OPERATIONS_AND_FLAGS = getValidOperationsAndFlags();

    private final PluginRegister pluginRegister;
    private final String[] args;

    private static Collection<String> getParametersByNamespace(final String namespace) {
        return (new ClassicConfiguration()).getPluginRegister()
            .getInstancesOf(ConfigurationExtension.class)
            .stream()
            .filter(p -> p.getNamespace().equalsIgnoreCase(namespace))
            .flatMap(ext -> Arrays.stream(ext.getClass().getDeclaredFields())
                .filter(field -> !(Modifier.isStatic(field.getModifiers())))
                .flatMap(f -> {
                    if (shouldNotExpand(f.getType())) {
                        return Stream.of(f.getName());
                    } else {
                        return Arrays.stream(f.getType().getDeclaredFields())
                            .filter(subfield -> !(Modifier.isStatic(subfield.getModifiers())))
                            .map(subfield -> f.getName() + "." + subfield.getName());
                    }
                }))
            .collect(Collectors.toList());
    }

    public enum PrintUsage {
        PRINT_NONE,
        PRINT_ORIGINAL,
        PRINT_SHORT
    }

    public CommandLineArguments(final PluginRegister pluginRegister, final String... args) {
        this.pluginRegister = pluginRegister;
        this.args = Arrays.stream(args).filter(StringUtils::hasText).toArray(String[]::new);
    }

    private static boolean shouldNotExpand(final Class<?> c) {
        final String pn = c.getPackageName();
        return c.isEnum() || pn.startsWith("java.") || pn.startsWith("javax.") || pn.startsWith("jdk.");
    }

    private static List<String> getValidOperationsAndFlags() {
        final List<String> operationsAndFlags = new ArrayList<>(Arrays.asList(DEBUG_FLAG,
            QUIET_FLAG,
            COMMUNITY_FALLBACK_FLAG,
            SKIP_CHECK_FOR_UPDATE_FLAG,
            MIGRATIONS_IDS_FLAG,
            OUTPUT_LOGS_IN_JSON,
            "help",
            "migrate",
            "clean",
            "info",
            "validate",
            "undo",
            "baseline",
            "repair"));
        operationsAndFlags.addAll(PRINT_VERSION_AND_EXIT_FLAGS);
        operationsAndFlags.addAll(PRINT_USAGE_FLAGS);
        return operationsAndFlags;
    }

    private static boolean isFlagSet(final String[] args, final String flag) {
        return Arrays.asList(args).contains(flag);
    }

    private static boolean isFlagSet(final String[] args, final List<String> flags) {
        return flags.stream().anyMatch(flag -> isFlagSet(args, flag));
    }

    private static String getArgumentValue(final String argName, final String[] allArgs) {
        return Arrays.stream(allArgs)
            .filter(arg -> arg.startsWith("-" + argName + "="))
            .findFirst()
            .map(CommandLineArguments::parseConfigurationOptionValueFromArg)
            .orElse("");
    }

    private static String parseConfigurationOptionValueFromArg(final String arg) {
        final int index = arg.indexOf("=");
        if (index < 0 || index == arg.length()) {
            return "";
        }
        return arg.substring(index + 1);
    }

    private List<String> getOperationsFromArgs(final String[] args) {
        final List<String> flags = Arrays.stream(args).filter(x -> x.startsWith("-")).toList();
        final List<String> operations = Arrays.stream(args)
            .filter(arg -> !arg.startsWith("-"))
            .collect(Collectors.toList());

        pluginRegister.getInstancesOf(CommandExtension.class)
            .forEach(extension -> flags.stream()
                .map(extension::getCommandForFlag)
                .filter(Objects::nonNull)
                .forEach(operations::add));

        return operations;
    }

    private static List<String> getConfigFilesFromArgs(final String[] args) {
        return Arrays.stream(StringUtils.tokenizeToStringArray(getArgumentValue(CONFIG_FILES, args), ","))
            .filter(i -> !i.isEmpty())
            .collect(Collectors.toList());
    }

    private static Map<String, Map<String, String>> getEnvironmentConfigurationFromArgs(final String[] args) {
        final Map<String, String> envConfigs = Arrays.stream(args)
            .filter(arg -> arg.toLowerCase().startsWith("-environments.") && arg.contains("="))
            .collect(Collectors.toMap(p -> getConfigurationOptionNameFromArg(p).substring("environments.".length()),
                CommandLineArguments::parseConfigurationOptionValueFromArg));

        final Map<String, Map<String, String>> envConfigMap = new HashMap<>();
        for (final String envKey : envConfigs.keySet()) {
            final String envName = envKey.split("\\.")[0];
            final String envConfigKey = envKey.substring(envName.length() + 1);
            if (!envConfigMap.containsKey(envName)) {
                envConfigMap.put(envName, new HashMap<>());
            }
            envConfigMap.get(envName).put(envConfigKey, envConfigs.get(envKey));
        }

        return envConfigMap;
    }

    private static boolean isConfigurationOptionCommandlineOnly(final String configurationOptionName) {
        return COMMAND_LINE_ONLY_OPTIONS.contains(configurationOptionName);
    }

    private static String getConfigurationOptionNameFromArg(final String arg) {
        return arg.substring(1, arg.indexOf("="));
    }

    private static String getTopNamespaceFromArg(final String arg) {
        return arg.substring(1, arg.contains(".") && arg.indexOf(".") < arg.indexOf("=") ? arg.indexOf(".") : arg.indexOf("="));
    }

    private static boolean isConfigurationArg(final String arg) {
        return arg.startsWith("-") && arg.contains("=");
    }

    public List<String> getFlags() {
        return Arrays.stream(args).filter(a -> a.startsWith("-") && !a.contains("=")).collect(Collectors.toList());
    }

    public void validate() {

        IntStream.range(0, args.length)
            .filter(i -> !isConfigurationArg(args[i]))
            .filter(i -> !VALID_OPERATIONS_AND_FLAGS.contains(args[i]))
            .filter(i -> !isHandledByExtension(args[i]))
            .findAny()
            .ifPresent(i -> {
                if (i < args.length - 1 && "=".equals(args[i + 1])) {
                    throw new FlywayException("Invalid configuration argument: "
                        + args[i]
                        + ". Please check you have not included any spaces in your configuration argument.");
                } else {
                    String hint = "";
                    if (args.length > i + 1) {
                        if (args[i + 1] != null && args[i + 1].startsWith(".")) {
                            hint = "Shell may cause input parameters containing periods (.) to be misinterpreted - do you need to wrap your parameter in double quotes?";
                        }
                    }
                    throw new FlywayException("Invalid flag: " + args[i] + "\n" + hint);
                }
            });

        final Optional<String> environmentArgument = Arrays.stream(args)
            .filter(x -> x.startsWith("-environments"))
            .filter(x -> x.contains("="))
            .map(x -> x.substring(0, x.indexOf("=")))
            .filter(x -> x.split("\\.").length == 2)
            .findFirst();
        if (environmentArgument.isPresent()) {
            throw new FlywayException("Invalid configuration argument: "
                + environmentArgument.get()
                + ". Both the environment name and configuration option are required.");
        }

        final String outputTypeValue = getArgumentValue(OUTPUT_TYPE, args).toLowerCase();

        if (!("json".equals(outputTypeValue) || outputTypeValue.isEmpty())) {
            throw new FlywayException("'"
                + outputTypeValue
                + "' is an invalid value for the -outputType option. Use 'json'.");
        }

        final String colorArgumentValue = getArgumentValue(COLOR, args);

        if (!Color.isValid(colorArgumentValue)) {
            throw new FlywayException("'"
                + colorArgumentValue
                + "' is an invalid value for the -color option. Use 'always', 'never', or 'auto'.");
        }
    }

    private boolean isHandledByExtension(final String arg) {
        for (final CommandExtension<?> extension : pluginRegister.getInstancesOf(CommandExtension.class)) {
            if (extension.handlesCommand(arg) || extension.handlesParameter(arg)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldOutputJson() {
        return "json".equalsIgnoreCase(getArgumentValue(OUTPUT_TYPE, args));
    }

    public boolean shouldOutputLogsInJson() {
        return "true".equalsIgnoreCase(getArgumentValue(OUTPUT_LOGS_IN_JSON, args));
    }

    public PrintUsage shouldPrintUsage(final StringBuilder helpText) {

        if (hasOperation("help") || isFlagSet(args, PRINT_USAGE_FLAGS)) {

            getHelpTextForOperations(helpText);

            return PrintUsage.PRINT_ORIGINAL;
        } else if (getOperations().isEmpty()) {
            return PrintUsage.PRINT_SHORT;
        } else {
            return PrintUsage.PRINT_NONE;
        }
    }

    private void getHelpTextForOperations(final StringBuilder helpText) {

        if (helpText == null) {
            return;
        }

        for (final String operation : getOperations()) {
            final String helpTextForOperation = pluginRegister.getInstancesOf(CommandExtension.class)
                .stream()
                .filter(e -> e.handlesCommand(operation))
                .map(e -> e.getHelpText(getFlags()))
                .collect(Collectors.joining("\n\n"));
            if (StringUtils.hasText(helpTextForOperation)) {
                helpText.append(helpTextForOperation).append("\n\n");
            }
        }
    }

    public Level getLogLevel() {
        if (isFlagSet(args, QUIET_FLAG)) {
            return Level.WARN;
        }
        if (isFlagSet(args, DEBUG_FLAG)) {
            return Level.DEBUG;
        }
        return Level.INFO;
    }

    public boolean isCommunityFallback() {
        return isFlagSet(args, COMMUNITY_FALLBACK_FLAG);
    }

    public boolean hasOperation(final String operation) {
        return getOperations().contains(operation);
    }

    public List<String> getOperations() {
        return getOperationsFromArgs(args);
    }

    public List<String> getConfigFiles() {
        final String workingDirectory = getWorkingDirectoryOrNull();

        return getConfigFilesFromArgs(args).stream()
            .map(File::new)
            .map(file -> !file.isAbsolute() && workingDirectory != null ? new File(workingDirectory,
                file.getPath()).getAbsolutePath() : file.getAbsolutePath())
            .collect(Collectors.toList());
    }

    public List<File> getConfigFilePathsFromEnv(final boolean loadToml) {
        final String workingDirectory = getWorkingDirectoryOrNull();
        final String[] fileLocations = StringUtils.tokenizeToStringArray(System.getenv("FLYWAY_CONFIG_FILES"), ",");

        return fileLocations == null
            ? new ArrayList<>()
            : Arrays.stream(fileLocations)
                .filter(loadToml ? f -> f.endsWith(".toml") : f -> !f.endsWith(".toml"))
                .map(File::new)
                .map(file -> !file.isAbsolute() && workingDirectory != null
                    ? new File(workingDirectory, file.getPath())
                    : file)
                .collect(Collectors.toList());
    }

    public String getOutputFile() {
        return getArgumentValue(OUTPUT_FILE, args);
    }

    public String getWorkingDirectory() {
        return getArgumentValue(WORKING_DIRECTORY, args);
    }

    public String getWorkingDirectoryOrNull() {
        return isWorkingDirectorySet() ? getWorkingDirectory() : null;
    }

    public Date getInfoSinceDate() {
        return parseDate(INFO_SINCE_DATE);
    }

    public Date getInfoUntilDate() {
        return parseDate(INFO_UNTIL_DATE);
    }

    public MigrationVersion getInfoSinceVersion() {
        return parseVersion(INFO_SINCE_VERSION);
    }

    public MigrationVersion getInfoUntilVersion() {
        return parseVersion(INFO_UNTIL_VERSION);
    }

    public MigrationState[] getInfoOfState() {
        final String stateStr = getArgumentValue(INFO_OF_STATE, args);
        if (!StringUtils.hasText(stateStr)) {
            return null;
        }
        return Arrays.stream(stateStr.split(","))
            .map(s -> MigrationState.valueOf(s.toUpperCase(Locale.ENGLISH)))
            .toArray(MigrationState[]::new);
    }

    public boolean isFilterOnMigrationIds() {
        return isFlagSet(args, MIGRATIONS_IDS_FLAG);
    }

    private MigrationVersion parseVersion(final String argument) {
        final String versionStr = getArgumentValue(argument, args);
        if (versionStr.isEmpty()) {
            return null;
        }
        return MigrationVersion.fromVersion(versionStr);
    }

    private Date parseDate(final String argument) {
        final String dateStr = getArgumentValue(argument, args);

        if (dateStr.isEmpty()) {
            return null;
        }

        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (final ParseException e) {
            throw new FlywayException("'"
                + dateStr
                + "' is an invalid value for the "
                + argument
                + " option. "
                + "The expected format is 'dd/mm/yyyy hh:mm', like '13/10/2020 16:30'. "
                + "See the Flyway documentation for help: "
                + FlywayDbWebsiteLinks.FILTER_INFO_OUTPUT);
        }
    }

    public boolean isOutputFileSet() {
        return !getOutputFile().isEmpty();
    }

    public boolean isWorkingDirectorySet() {
        return !getWorkingDirectory().isEmpty();
    }

    public String getConfigFileEncoding() {
        return getArgumentValue(CONFIG_FILE_ENCODING, args);
    }

    public boolean isConfigFileEncodingSet() {
        return !getConfigFileEncoding().isEmpty();
    }

    public boolean skipCheckForUpdate() {
        return isFlagSet(args, SKIP_CHECK_FOR_UPDATE_FLAG);
    }

    public Color getColor() {
        return Color.fromString(getArgumentValue(COLOR, args));
    }

    public Map<String, String> getConfiguration(final boolean isModernConfig) {
        //scoped configuration processing.
        boolean processingValid = true;
        String validNamespace = "";
        final String[] processedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            processedArgs[i] = arg;

            if (!arg.startsWith("-")) {
                //it's a verb, lets set the namespace to this.
                validNamespace = arg;
            } else {
                if (validNamespace.isEmpty()) {
                    continue;
                }

                if (arg.contains("=")) {
                    //it's a param, lets scope it.
                    final String paramName = arg.substring(1, arg.contains("=") ? arg.indexOf("=") : arg.length());
                    final String paramValue = arg.substring(arg.indexOf("=") + 1);
                    if (paramName.contains(".") && !paramName.startsWith("environments.")) {
                        //there's a `.` in this param name so likely namespaced so lets break out of the scoping and process as normal.
                        processingValid = false;
                        break;
                    } else {
                        final var paramsInNamespace = getParametersByNamespace(validNamespace);
                        if (paramsInNamespace.contains(paramName)) {
                            final String replacedParamName = "-" + validNamespace + "." + paramName;
                            processedArgs[i] = replacedParamName + "=" + paramValue;
                        } else {
                            String preArg = args[i - 1];
                            if (preArg.startsWith("-") && !preArg.contains("=")) {
                                preArg = preArg.substring(1);
                                if (paramsInNamespace.contains(preArg + "." + paramName)) {
                                    final String replacedParamName = "-"
                                        + validNamespace
                                        + "."
                                        + preArg
                                        + "."
                                        + paramName;
                                    processedArgs[i] = replacedParamName + "=" + paramValue;
                                }
                            }
                        }
                    }
                }
            }
        }

        final List<String> rootNamespaces = Optional.ofNullable(pluginRegister).map(p -> p.getInstancesOf(ConfigurationExtension.class)
            .stream()
            .filter(c -> c.getNamespace().startsWith("\\"))
            .map(c -> c.getNamespace().substring(1))
            .toList()).orElse(Collections.emptyList());

        return Arrays.stream(processingValid ? processedArgs : args)
            .filter(CommandLineArguments::isConfigurationArg)
            .filter(arg -> !arg.startsWith("-" + CONFIG_FILES + "="))
            .filter(arg -> !arg.startsWith("-" + CONFIG_FILE_ENCODING + "="))
            .filter(arg -> !arg.startsWith("-environments."))
            .filter(f -> !isConfigurationOptionCommandlineOnly(getConfigurationOptionNameFromArg(f)))
            .collect(Collectors.toMap(p -> (Arrays.stream((EnvironmentModel.class).getDeclaredFields())
                    .anyMatch(x -> x.getName().equals(getConfigurationOptionNameFromArg(p))) && isModernConfig ?
                    "environments."
                        + ClassicConfiguration.TEMP_ENVIRONMENT_NAME
                        + "." : rootNamespaces.contains(getTopNamespaceFromArg(p)) ? "" : "flyway.") + getConfigurationOptionNameFromArg(p),
                CommandLineArguments::parseConfigurationOptionValueFromArg));
    }

    public Map<String, Map<String, String>> getEnvironmentConfiguration() {
        return getEnvironmentConfigurationFromArgs(args);
    }

    @RequiredArgsConstructor
    public enum Color {
        ALWAYS("always"),
        NEVER("never"),
        AUTO("auto");

        private final String value;

        public static Color fromString(final String value) {
            if (value.isEmpty()) {
                return AUTO;
            }

            return Arrays.stream(values()).filter(color -> color.value.equals(value)).findFirst().orElse(null);
        }

        public static boolean isValid(final String value) {
            return fromString(value) != null;
        }
    }
}
