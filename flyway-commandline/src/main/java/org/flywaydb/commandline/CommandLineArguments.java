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

import lombok.RequiredArgsConstructor;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommandLineArguments {

    @RequiredArgsConstructor
    public enum Color {
        ALWAYS("always"),
        NEVER("never"),
        AUTO("auto");

        private final String value;

        public static Color fromString(String value) {
            if (value.isEmpty()) {
                return AUTO;
            }

            return Arrays.stream(values())
                    .filter(color -> color.value.equals(value))
                    .findFirst()
                    .orElse(null);
        }

        public static boolean isValid(String value) {
            return fromString(value) != null;
        }
    }

    // Flags
    // GNU standards require --version, --help and their single-letter forms
    // RG standards mandate --help / -h
    private static final String DEBUG_FLAG = "-X";
    private static final String QUIET_FLAG = "-q";
    private static final String SUPPRESS_PROMPT_FLAG = "-n";
    private static final List<String> PRINT_VERSION_AND_EXIT_FLAGS = Arrays.asList("-v", "--version");
    private static final String CHECK_LICENCE = "-checkLicence";
    private static final List<String> PRINT_USAGE_FLAGS = Arrays.asList("-?", "-h", "--help");
    private static final String SKIP_CHECK_FOR_UPDATE_FLAG = "-skipCheckForUpdate";
    private static final String COMMUNITY_FLAG = "-community";
    private static final String ENTERPRISE_FLAG = "-enterprise";
    private static final String PRO_FLAG = "-pro";
    private static final String TEAMS_FLAG = "-teams";

    // Command line specific configuration options
    private static final String OUTPUT_FILE = "outputFile";
    private static final String OUTPUT_TYPE = "outputType";
    private static final String CONFIG_FILE_ENCODING = "configFileEncoding";
    private static final String CONFIG_FILES = "configFiles";
    private static final String COLOR = "color";
    private static final String WORKING_DIRECTORY = "workingDirectory";
    private static final String INFO_SINCE_DATE = "infoSinceDate";
    private static final String INFO_UNTIL_DATE = "infoUntilDate";
    private static final String INFO_SINCE_VERSION = "infoSinceVersion";
    private static final String INFO_UNTIL_VERSION = "infoUntilVersion";
    private static final String INFO_OF_STATE = "infoOfState";

    private static final Set<String> COMMAND_LINE_ONLY_OPTIONS = new HashSet<>(Arrays.asList(
            OUTPUT_FILE, OUTPUT_TYPE, COLOR, WORKING_DIRECTORY, INFO_SINCE_DATE,
            INFO_UNTIL_DATE, INFO_SINCE_VERSION, INFO_UNTIL_VERSION, INFO_OF_STATE
                                                                                            ));

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final List<String> VALID_OPERATIONS_AND_FLAGS = getValidOperationsAndFlags();

    private static List<String> getValidOperationsAndFlags() {
        List<String> operationsAndFlags = new ArrayList<>(Arrays.asList(
                DEBUG_FLAG,
                QUIET_FLAG,
                SUPPRESS_PROMPT_FLAG,
                SKIP_CHECK_FOR_UPDATE_FLAG,
                COMMUNITY_FLAG,
                ENTERPRISE_FLAG,
                PRO_FLAG,
                TEAMS_FLAG,
                CHECK_LICENCE,
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

    private final String[] args;

    public CommandLineArguments(String... args) {
        this.args = args;
    }

    private static boolean isFlagSet(String[] args, String flag) {
        return Arrays.asList(args).contains(flag);
    }

    private static boolean isFlagSet(String[] args, List<String> flags) {
        return flags.stream().anyMatch(flag -> isFlagSet(args, flag));
    }

    private static String getArgumentValue(String argName, String[] allArgs) {
        return Arrays.stream(allArgs)
                .filter(arg -> arg.startsWith("-" + argName + "="))
                .findFirst()
                .map(CommandLineArguments::parseConfigurationOptionValueFromArg)
                .orElse("");
    }

    private static String parseConfigurationOptionValueFromArg(String arg) {
        int index = arg.indexOf("=");

        if (index < 0 || index == arg.length()) {
            return "";
        }

        return arg.substring(index + 1);
    }

    private static List<String> getOperationsFromArgs(String[] args) {
        return Arrays.stream(args).filter(arg -> !arg.startsWith("-")).collect(Collectors.toList());
    }

    private static List<String> getConfigFilesFromArgs(String[] args) {
        return Arrays.asList(StringUtils.tokenizeToStringArray(getArgumentValue(CONFIG_FILES, args), ","));
    }

    private static Map<String, String> getConfigurationFromArgs(String[] args) {
        Map<String, String> configuration = new HashMap<>();

        for (String arg : args) {
            if (isConfigurationArg(arg)) {
                String configurationOptionName = getConfigurationOptionNameFromArg(arg);

                if (!isConfigurationOptionCommandlineOnly(configurationOptionName)) {
                    configuration.put("flyway." + configurationOptionName, parseConfigurationOptionValueFromArg(arg));
                }
            }
        }

        return configuration;
    }

    private static boolean isConfigurationOptionCommandlineOnly(String configurationOptionName) {
        return COMMAND_LINE_ONLY_OPTIONS.contains(configurationOptionName);
    }

    private static String getConfigurationOptionNameFromArg(String arg) {
        return arg.substring(1, arg.indexOf("="));
    }

    private static boolean isConfigurationArg(String arg) {
        return arg.startsWith("-") && arg.contains("=");
    }

    public void validate() {
        for (String arg : args) {
            if (!isConfigurationArg(arg) && !VALID_OPERATIONS_AND_FLAGS.contains(arg) && !isHandledByExtension(arg)) {
                throw new FlywayException("Invalid argument: " + arg);
            }
        }

        String outputTypeValue = getArgumentValue(OUTPUT_TYPE, args).toLowerCase();

        if (!("json".equals(outputTypeValue) || "".equals(outputTypeValue))) {
            throw new FlywayException("'" + outputTypeValue + "' is an invalid value for the -outputType option. Use 'json'.");
        }

        String colorArgumentValue = getArgumentValue(COLOR, args);

        if (!Color.isValid(colorArgumentValue)) {
            throw new FlywayException("'" + colorArgumentValue + "' is an invalid value for the -color option. Use 'always', 'never', or 'auto'.");
        }
    }

    private boolean isHandledByExtension(String arg) {
        for (CommandExtension extension : PluginRegister.getPlugins(CommandExtension.class)) {
            if (extension.handlesCommand(arg) || extension.handlesParameter(arg)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSuppressPrompt() {
        return isFlagSet(args, SUPPRESS_PROMPT_FLAG);
    }

    public boolean shouldPrintVersionAndExit() {
        return isFlagSet(args, PRINT_VERSION_AND_EXIT_FLAGS);
    }

    public boolean shouldCheckLicenseAndExit() {
        return isFlagSet(args, CHECK_LICENCE);
    }

    public boolean shouldOutputJson() {
        return "json".equalsIgnoreCase(getArgumentValue(OUTPUT_TYPE, args));
    }

    public boolean shouldPrintUsage() {
        return (isFlagSet(args, PRINT_USAGE_FLAGS) || getOperations().isEmpty()) && !isFlagSet(args, CHECK_LICENCE);
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

    public boolean hasOperation(String operation) {
        return getOperations().contains(operation);
    }

    public List<String> getOperations() {
        return getOperationsFromArgs(args);
    }

    public List<String> getConfigFiles() {
        return getConfigFilesFromArgs(args);
    }

    public String getOutputFile() {
        return getArgumentValue(OUTPUT_FILE, args);
    }

    public String getWorkingDirectory() {
        return getArgumentValue(WORKING_DIRECTORY, args);
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

    public MigrationState getInfoOfState() {
        String stateStr = getArgumentValue(INFO_OF_STATE, args);

        if (!StringUtils.hasText(stateStr)) {
            return null;
        }

        return MigrationState.valueOf(stateStr.toUpperCase(Locale.ENGLISH));
    }

    private MigrationVersion parseVersion(String argument) {
        String versionStr = getArgumentValue(argument, args);

        if (versionStr.isEmpty()) {
            return null;
        }

        return MigrationVersion.fromVersion(versionStr);
    }

    private Date parseDate(String argument) {
        String dateStr = getArgumentValue(argument, args);

        if (dateStr.isEmpty()) {
            return null;
        }

        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            throw new FlywayException("'" + dateStr + "' is an invalid value for the " + argument + " option. " +
                                              "The expected format is 'dd/mm/yyyy hh:mm', like '13/10/2020 16:30'. " +
                                              "See the Flyway documentation for help: " + FlywayDbWebsiteLinks.FILTER_INFO_OUTPUT);
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

    public Map<String, String> getConfiguration() {
        return getConfigurationFromArgs(args);
    }
}