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

import org.flywaydb.commandline.ConsoleLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.*;

class CommandLineArguments {

    enum Color {
        ALWAYS("always"),
        NEVER("never"),
        AUTO("auto");

        private final String value;

        Color(String value) {
            this.value = value;
        }

        public static Color fromString(String value) {
            if (value.isEmpty()) {
                return AUTO;
            }

            for (Color color : values()) {
                if (color.value.equals(value)) {
                    return color;
                }
            }

            return null;
        }

        public static boolean isValid(String value) {
            return fromString(value) != null;
        }
    }

    // Flags
    private static String DEBUG_FLAG = "-X";
    private static String QUIET_FLAG = "-q";
    private static String SUPPRESS_PROMPT_FLAG = "-n";
    private static String PRINT_VERSION_AND_EXIT_FLAG = "-v";
    private static String JSON_FLAG = "-json";
    private static String PRINT_USAGE_FLAG = "-?";
    private static String COMMUNITY_FLAG = "-community";
    private static String PRO_FLAG = "-pro";
    private static String ENTERPRISE_FLAG = "-enterprise";

    // Command line specific configuration options
    private static String OUTPUT_FILE = "outputFile";
    private static String LOG_FILE = "logFile";
    private static String CONFIG_FILE_ENCODING = "configFileEncoding";
    private static String CONFIG_FILES = "configFiles";
    private static String COLOR = "color";
    private static String WORKING_DIRECTORY = "workingDirectory";

    private static List<String> VALID_OPERATIONS_AND_FLAGS = Arrays.asList(
            DEBUG_FLAG,
            QUIET_FLAG,
            SUPPRESS_PROMPT_FLAG,
            PRINT_VERSION_AND_EXIT_FLAG,
            JSON_FLAG,
            PRINT_USAGE_FLAG,
            COMMUNITY_FLAG,
            PRO_FLAG,
            ENTERPRISE_FLAG,
            "help",
            "migrate",
            "clean",
            "info",
            "validate",
            "undo",
            "baseline",
            "repair"
    );

    private final String[] args;

    CommandLineArguments(String[] args) {
        this.args = args;
    }

    private static boolean isFlagSet(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static String getArgumentValue(String argName, String[] allArgs) {
        for (String arg : allArgs) {
            if (arg.startsWith("-" + argName + "=")) {
                return parseConfigurationOptionValueFromArg(arg);
            }
        }
        return "";
    }

    private static String parseConfigurationOptionValueFromArg(String arg) {
        int index = arg.indexOf("=");

        if (index < 0 || index == arg.length()) {
            return "";
        }

        return arg.substring(index + 1);
    }

    private static List<String> getOperationsFromArgs(String[] args) {
        List<String> operations = new ArrayList<>();

        for (String arg : args) {
            if (!arg.startsWith("-")) {
                operations.add(arg);
            }
        }
        return operations;
    }

    private static List<String> getConfigFilesFromArgs(String[] args) {
        String configFilesCommaSeparatedList = getArgumentValue(CONFIG_FILES, args);

        return Arrays.asList(StringUtils.tokenizeToStringArray(configFilesCommaSeparatedList, ","));
    }

    private static Map<String, String> getConfigurationFromArgs(String[] args) {
        Map<String, String> configuration = new HashMap<>();

        for (String arg : args) {
            if (isConfigurationArg(arg)) {
                String configurationOptionName = getConfigurationOptionNameFromArg(arg);

                if (!isConfigurationOptionIgnored(configurationOptionName)) {
                    configuration.put("flyway." + configurationOptionName, parseConfigurationOptionValueFromArg(arg));
                }
            }
        }

        return configuration;
    }

    private static boolean isConfigurationOptionIgnored(String configurationOptionName) {
        return OUTPUT_FILE.equals(configurationOptionName) ||
                LOG_FILE.equals(configurationOptionName) ||
                COLOR.equals(configurationOptionName) ||
                WORKING_DIRECTORY.equals(configurationOptionName);
    }

    private static String getConfigurationOptionNameFromArg(String arg) {
        int index = arg.indexOf("=");

        return arg.substring(1, index);
    }

    private static boolean isConfigurationArg(String arg) {
        return arg.startsWith("-") && arg.contains("=");
    }

    void validate(Log log) {
        for (String arg : args) {
            if (!isConfigurationArg(arg) && !CommandLineArguments.VALID_OPERATIONS_AND_FLAGS.contains(arg)) {
                throw new FlywayException("Invalid argument: " + arg);
            }
        }

        if (isLogFilepathSet()) {
            if (isOutputFileSet()) {
                throw new FlywayException("-logFile and -outputFile are incompatible. -logFile is deprecated. Instead use -outputFile.");
            }

            if (shouldOutputJson()) {
                throw new FlywayException("-logFile and -json are incompatible. -logFile is deprecated. Instead use -outputFile to print JSON to a file.");
            }

            log.warn("-logFile is deprecated. Instead use -outputFile.");
        }

        if (shouldOutputJson() && !hasOperation("info") ) {
            throw new FlywayException("The -json flag is only supported by the info command.");
        }

        String colorArgumentValue = getArgumentValue(COLOR, args);
        if (!Color.isValid(colorArgumentValue)) {
            throw new FlywayException("'" + colorArgumentValue + "' is an invalid value for the -color option. Use 'always', 'never', or 'auto'.");
        }
    }

    boolean shouldSuppressPrompt() {
        return isFlagSet(args, SUPPRESS_PROMPT_FLAG);
    }

    boolean shouldPrintVersionAndExit() {
        return isFlagSet(args, PRINT_VERSION_AND_EXIT_FLAG);
    }

    boolean shouldOutputJson() {
        return isFlagSet(args, JSON_FLAG);
    }

    boolean shouldPrintUsage() {
        return isFlagSet(args, PRINT_USAGE_FLAG) || getOperations().isEmpty();
    }

    Level getLogLevel() {
        if (isFlagSet(args, QUIET_FLAG)) {
            return Level.WARN;
        }

        if (isFlagSet(args, DEBUG_FLAG)) {
            return Level.DEBUG;
        }

        return Level.INFO;
    }

    boolean hasOperation(String operation) {
        return getOperations().contains(operation);
    }

    List<String> getOperations() {
        return getOperationsFromArgs(args);
    }

    List<String> getConfigFiles() {
        return getConfigFilesFromArgs(args);
    }

    String getOutputFile() {
        return getArgumentValue(OUTPUT_FILE, args);
    }

    String getLogFilepath() {
        return getArgumentValue(LOG_FILE, args);
    }

    String getWorkingDirectory() {
        return getArgumentValue(WORKING_DIRECTORY, args);
    }

    boolean isOutputFileSet() {
        return !getOutputFile().isEmpty();
    }

    boolean isLogFilepathSet() {
        return !getLogFilepath().isEmpty();
    }

    boolean isWorkingDirectorySet() {
        return !getWorkingDirectory().isEmpty();
    }

    String getConfigFileEncoding() {
        return getArgumentValue(CONFIG_FILE_ENCODING, args);
    }

    boolean isConfigFileEncodingSet() {
        return !getConfigFileEncoding().isEmpty();
    }

    Color getColor() {
        return Color.fromString(getArgumentValue(COLOR, args));
    }

    Map<String, String> getConfiguration() {
        return getConfigurationFromArgs(args);
    }
}