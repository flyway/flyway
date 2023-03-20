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
package org.flywaydb.core.internal.configuration;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.flywaydb.core.internal.sqlscript.SqlScriptMetadata.isMultilineBooleanExpression;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUtils {
    public static final String CONFIG_FILE_NAME = "flyway.conf";
    public static final String CONFIG_FILES = "flyway.configFiles";
    public static final String CONFIG_FILE_ENCODING = "flyway.configFileEncoding";
    public static final String BASELINE_DESCRIPTION = "flyway.baselineDescription";
    public static final String BASELINE_ON_MIGRATE = "flyway.baselineOnMigrate";
    public static final String BASELINE_VERSION = "flyway.baselineVersion";
    public static final String BATCH = "flyway.batch";
    public static final String CALLBACKS = "flyway.callbacks";
    public static final String CLEAN_DISABLED = "flyway.cleanDisabled";
    public static final String CLEAN_ON_VALIDATION_ERROR = "flyway.cleanOnValidationError";
    public static final String CONNECT_RETRIES = "flyway.connectRetries";
    public static final String CONNECT_RETRIES_INTERVAL = "flyway.connectRetriesInterval";
    public static final String DEFAULT_SCHEMA = "flyway.defaultSchema";
    public static final String DRIVER = "flyway.driver";
    public static final String DRYRUN_OUTPUT = "flyway.dryRunOutput";
    public static final String ENCODING = "flyway.encoding";
    public static final String DETECT_ENCODING = "flyway.detectEncoding";
    public static final String ERROR_OVERRIDES = "flyway.errorOverrides";
    public static final String EXECUTE_IN_TRANSACTION = "flyway.executeInTransaction";
    public static final String GROUP = "flyway.group";
    public static final String IGNORE_MIGRATION_PATTERNS = "flyway.ignoreMigrationPatterns";
    public static final String INIT_SQL = "flyway.initSql";
    public static final String INSTALLED_BY = "flyway.installedBy";
    public static final String LICENSE_KEY = "flyway.licenseKey";
    public static final String LOCATIONS = "flyway.locations";
    public static final String MIXED = "flyway.mixed";
    public static final String OUT_OF_ORDER = "flyway.outOfOrder";
    public static final String SKIP_EXECUTING_MIGRATIONS = "flyway.skipExecutingMigrations";
    public static final String OUTPUT_QUERY_RESULTS = "flyway.outputQueryResults";
    public static final String PASSWORD = "flyway.password";
    public static final String PLACEHOLDER_PREFIX = "flyway.placeholderPrefix";
    public static final String PLACEHOLDER_REPLACEMENT = "flyway.placeholderReplacement";
    public static final String PLACEHOLDER_SUFFIX = "flyway.placeholderSuffix";
    public static final String PLACEHOLDER_SEPARATOR = "flyway.placeholderSeparator";
    public static final String SCRIPT_PLACEHOLDER_PREFIX = "flyway.scriptPlaceholderPrefix";
    public static final String SCRIPT_PLACEHOLDER_SUFFIX = "flyway.scriptPlaceholderSuffix";
    public static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";
    public static final String LOCK_RETRY_COUNT = "flyway.lockRetryCount";
    public static final String JDBC_PROPERTIES_PREFIX = "flyway.jdbcProperties.";
    public static final String REPEATABLE_SQL_MIGRATION_PREFIX = "flyway.repeatableSqlMigrationPrefix";
    public static final String RESOLVERS = "flyway.resolvers";
    public static final String SCHEMAS = "flyway.schemas";
    public static final String SKIP_DEFAULT_CALLBACKS = "flyway.skipDefaultCallbacks";
    public static final String SKIP_DEFAULT_RESOLVERS = "flyway.skipDefaultResolvers";
    public static final String SQL_MIGRATION_PREFIX = "flyway.sqlMigrationPrefix";
    public static final String SQL_MIGRATION_SEPARATOR = "flyway.sqlMigrationSeparator";
    public static final String SQL_MIGRATION_SUFFIXES = "flyway.sqlMigrationSuffixes";
    public static final String STREAM = "flyway.stream";
    public static final String TABLE = "flyway.table";
    public static final String TABLESPACE = "flyway.tablespace";
    public static final String TARGET = "flyway.target";
    public static final String CHERRY_PICK = "flyway.cherryPick";
    public static final String UNDO_SQL_MIGRATION_PREFIX = "flyway.undoSqlMigrationPrefix";
    public static final String URL = "flyway.url";
    public static final String USER = "flyway.user";
    public static final String VALIDATE_ON_MIGRATE = "flyway.validateOnMigrate";
    public static final String VALIDATE_MIGRATION_NAMING = "flyway.validateMigrationNaming";
    public static final String CREATE_SCHEMAS = "flyway.createSchemas";
    public static final String FAIL_ON_MISSING_LOCATIONS = "flyway.failOnMissingLocations";
    public static final String LOGGERS = "flyway.loggers";
    public static final String KERBEROS_CONFIG_FILE = "flyway.kerberosConfigFile";

    // Oracle-specific
    public static final String ORACLE_SQLPLUS = "flyway.oracle.sqlplus";
    public static final String ORACLE_SQLPLUS_WARN = "flyway.oracle.sqlplusWarn";
    public static final String ORACLE_KERBEROS_CACHE_FILE = "flyway.oracle.kerberosCacheFile";
    public static final String ORACLE_WALLET_LOCATION = "flyway.oracle.walletLocation";

    // Command-line specific
    public static final String JAR_DIRS = "flyway.jarDirs";

    // Gradle specific
    public static final String CONFIGURATIONS = "flyway.configurations";

    // Plugin specific
    public static final String FLYWAY_PLUGINS_PREFIX = "flyway.plugins.";

    private static final PluginRegister PLUGIN_REGISTER = new PluginRegister();

    @Getter
    private static String stdInContent = "";

    /**
     * Converts Flyway-specific environment variables to their matching properties.
     *
     * @return The properties corresponding to the environment variables.
     */
    public static Map<String, String> environmentVariablesToPropertyMap() {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String convertedKey = convertKey(entry.getKey());
            if (convertedKey != null) {
                // Known environment variable
                result.put(convertKey(entry.getKey()), entry.getValue());
            }
        }

        return result;
    }

    private static String convertKey(String key) {
        if ("FLYWAY_BASELINE_DESCRIPTION".equals(key)) {
            return BASELINE_DESCRIPTION;
        }
        if ("FLYWAY_BASELINE_ON_MIGRATE".equals(key)) {
            return BASELINE_ON_MIGRATE;
        }
        if ("FLYWAY_BASELINE_VERSION".equals(key)) {
            return BASELINE_VERSION;
        }
        if ("FLYWAY_BATCH".equals(key)) {
            return BATCH;
        }
        if ("FLYWAY_CALLBACKS".equals(key)) {
            return CALLBACKS;
        }
        if ("FLYWAY_CLEAN_DISABLED".equals(key)) {
            return CLEAN_DISABLED;
        }
        if ("FLYWAY_CLEAN_ON_VALIDATION_ERROR".equals(key)) {
            return CLEAN_ON_VALIDATION_ERROR;
        }
        if ("FLYWAY_CONFIG_FILE_ENCODING".equals(key)) {
            return CONFIG_FILE_ENCODING;
        }
        if ("FLYWAY_CONFIG_FILES".equals(key)) {
            return CONFIG_FILES;
        }
        if ("FLYWAY_CONNECT_RETRIES".equals(key)) {
            return CONNECT_RETRIES;
        }

        if ("FLYWAY_CONNECT_RETRIES_INTERVAL".equals(key)) {
            return CONNECT_RETRIES_INTERVAL;
        }

        if ("FLYWAY_DEFAULT_SCHEMA".equals(key)) {
            return DEFAULT_SCHEMA;
        }
        if ("FLYWAY_DRIVER".equals(key)) {
            return DRIVER;
        }
        if ("FLYWAY_DRYRUN_OUTPUT".equals(key)) {
            return DRYRUN_OUTPUT;
        }
        if ("FLYWAY_ENCODING".equals(key)) {
            return ENCODING;
        }
        if ("FLYWAY_EXECUTE_IN_TRANSACTION".equals(key)) {
            return EXECUTE_IN_TRANSACTION;
        }
        if ("FLYWAY_DETECT_ENCODING".equals(key)) {
            return DETECT_ENCODING;
        }
        if ("FLYWAY_ERROR_OVERRIDES".equals(key)) {
            return ERROR_OVERRIDES;
        }
        if ("FLYWAY_GROUP".equals(key)) {
            return GROUP;
        }
        if ("FLYWAY_IGNORE_MIGRATION_PATTERNS".equals(key)) {
            return IGNORE_MIGRATION_PATTERNS;
        }
        if ("FLYWAY_INIT_SQL".equals(key)) {
            return INIT_SQL;
        }
        if ("FLYWAY_INSTALLED_BY".equals(key)) {
            return INSTALLED_BY;
        }
        if ("FLYWAY_LICENSE_KEY".equals(key)) {
            return LICENSE_KEY;
        }
        if ("FLYWAY_LOCATIONS".equals(key)) {
            return LOCATIONS;
        }
        if ("FLYWAY_MIXED".equals(key)) {
            return MIXED;
        }
        if ("FLYWAY_OUT_OF_ORDER".equals(key)) {
            return OUT_OF_ORDER;
        }
        if ("FLYWAY_SKIP_EXECUTING_MIGRATIONS".equals(key)) {
            return SKIP_EXECUTING_MIGRATIONS;
        }
        if ("FLYWAY_OUTPUT_QUERY_RESULTS".equals(key)) {
            return OUTPUT_QUERY_RESULTS;
        }
        if ("FLYWAY_PASSWORD".equals(key)) {
            return PASSWORD;
        }
        if ("FLYWAY_LOCK_RETRY_COUNT".equals(key)) {
            return LOCK_RETRY_COUNT;
        }
        if ("FLYWAY_PLACEHOLDER_PREFIX".equals(key)) {
            return PLACEHOLDER_PREFIX;
        }
        if ("FLYWAY_PLACEHOLDER_REPLACEMENT".equals(key)) {
            return PLACEHOLDER_REPLACEMENT;
        }
        if ("FLYWAY_PLACEHOLDER_SUFFIX".equals(key)) {
            return PLACEHOLDER_SUFFIX;
        }
        if ("FLYWAY_PLACEHOLDER_SEPARATOR".equals(key)) {
            return PLACEHOLDER_SEPARATOR;
        }
        if ("FLYWAY_SCRIPT_PLACEHOLDER_PREFIX".equals(key)) {
            return SCRIPT_PLACEHOLDER_PREFIX;
        }
        if ("FLYWAY_SCRIPT_PLACEHOLDER_SUFFIX".equals(key)) {
            return SCRIPT_PLACEHOLDER_SUFFIX;
        }
        if (key.matches("FLYWAY_PLACEHOLDERS_.+")) {
            return PLACEHOLDERS_PROPERTY_PREFIX + key.substring("FLYWAY_PLACEHOLDERS_".length()).toLowerCase(Locale.ENGLISH);
        }

        if (key.matches("FLYWAY_JDBC_PROPERTIES_.+")) {
            return JDBC_PROPERTIES_PREFIX + key.substring("FLYWAY_JDBC_PROPERTIES_".length());
        }

        if ("FLYWAY_REPEATABLE_SQL_MIGRATION_PREFIX".equals(key)) {
            return REPEATABLE_SQL_MIGRATION_PREFIX;
        }
        if ("FLYWAY_RESOLVERS".equals(key)) {
            return RESOLVERS;
        }
        if ("FLYWAY_SCHEMAS".equals(key)) {
            return SCHEMAS;
        }
        if ("FLYWAY_SKIP_DEFAULT_CALLBACKS".equals(key)) {
            return SKIP_DEFAULT_CALLBACKS;
        }
        if ("FLYWAY_SKIP_DEFAULT_RESOLVERS".equals(key)) {
            return SKIP_DEFAULT_RESOLVERS;
        }
        if ("FLYWAY_SQL_MIGRATION_PREFIX".equals(key)) {
            return SQL_MIGRATION_PREFIX;
        }
        if ("FLYWAY_SQL_MIGRATION_SEPARATOR".equals(key)) {
            return SQL_MIGRATION_SEPARATOR;
        }
        if ("FLYWAY_SQL_MIGRATION_SUFFIXES".equals(key)) {
            return SQL_MIGRATION_SUFFIXES;
        }
        if ("FLYWAY_STREAM".equals(key)) {
            return STREAM;
        }
        if ("FLYWAY_TABLE".equals(key)) {
            return TABLE;
        }
        if ("FLYWAY_TABLESPACE".equals(key)) {
            return TABLESPACE;
        }
        if ("FLYWAY_TARGET".equals(key)) {
            return TARGET;
        }
        if ("FLYWAY_CHERRY_PICK".equals(key)) {
            return CHERRY_PICK;
        }
        if ("FLYWAY_LOGGERS".equals(key)) {
            return LOGGERS;
        }
        if ("FLYWAY_UNDO_SQL_MIGRATION_PREFIX".equals(key)) {
            return UNDO_SQL_MIGRATION_PREFIX;
        }
        if ("FLYWAY_URL".equals(key)) {
            return URL;
        }
        if ("FLYWAY_USER".equals(key)) {
            return USER;
        }
        if ("FLYWAY_VALIDATE_ON_MIGRATE".equals(key)) {
            return VALIDATE_ON_MIGRATE;
        }
        if ("FLYWAY_VALIDATE_MIGRATION_NAMING".equals(key)) {
            return VALIDATE_MIGRATION_NAMING;
        }
        if ("FLYWAY_CREATE_SCHEMAS".equals(key)) {
            return CREATE_SCHEMAS;
        }
        if ("FLYWAY_FAIL_ON_MISSING_LOCATIONS".equals(key)) {
            return FAIL_ON_MISSING_LOCATIONS;
        }
        if ("FLYWAY_KERBEROS_CONFIG_FILE".equals(key)) {
            return KERBEROS_CONFIG_FILE;
        }

        // Oracle-specific
        if ("FLYWAY_ORACLE_SQLPLUS".equals(key)) {
            return ORACLE_SQLPLUS;
        }
        if ("FLYWAY_ORACLE_SQLPLUS_WARN".equals(key)) {
            return ORACLE_SQLPLUS_WARN;
        }
        if ("FLYWAY_ORACLE_KERBEROS_CACHE_FILE".equals(key)) {
            return ORACLE_KERBEROS_CACHE_FILE;
        }
        if ("FLYWAY_ORACLE_WALLET_LOCATION".equals(key)) {
            return ORACLE_WALLET_LOCATION;
        }

        // Command-line specific
        if ("FLYWAY_JAR_DIRS".equals(key)) {
            return JAR_DIRS;
        }

        // Gradle specific
        if ("FLYWAY_CONFIGURATIONS".equals(key)) {
            return CONFIGURATIONS;
        }

        for (ConfigurationExtension configurationExtension : PLUGIN_REGISTER.getPlugins(ConfigurationExtension.class)) {
            String configurationParameter = configurationExtension.getConfigurationParameterFromEnvironmentVariable(key);
            if (configurationParameter != null) {
                return configurationParameter;
            }
        }

        return null;
    }

    /**
     * Load configuration files from the default locations:
     * $installationDir$/conf/flyway.conf
     * $user.home$/flyway.conf
     * $workingDirectory$/flyway.conf
     *
     * @param encoding The conf file encoding.
     * @throws FlywayException When the configuration failed.
     */
    public static Map<String, String> loadDefaultConfigurationFiles(File installationDir, String encoding) {
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(ConfigUtils.loadConfigurationFile(new File(installationDir.getAbsolutePath() + "/conf/" + ConfigUtils.CONFIG_FILE_NAME), encoding, false));
        configMap.putAll(ConfigUtils.loadConfigurationFile(new File(System.getProperty("user.home") + "/" + ConfigUtils.CONFIG_FILE_NAME), encoding, false));
        configMap.putAll(ConfigUtils.loadConfigurationFile(new File(ConfigUtils.CONFIG_FILE_NAME), encoding, false));

        return configMap;
    }

    public static List<File> getDefaultTomlConfigFileLocations(File installationDir) {
        return Arrays.asList(new File(installationDir.getAbsolutePath() + "/conf/flyway.toml"),
                      new File(System.getProperty("user.home") + "/flyway.toml"),
                      new File("flyway.toml"),
                      new File("flyway.user.toml"));
    }

    /**
     * Loads the configuration from this configuration file.
     *
     * @param configFile The configuration file to load.
     * @param encoding The encoding of the configuration file.
     * @param failIfMissing Whether to fail if the file is missing.
     * @return The properties from the configuration file. An empty Map if none.
     * @throws FlywayException When the configuration file could not be loaded.
     */
    public static Map<String, String> loadConfigurationFile(File configFile, String encoding, boolean failIfMissing) throws FlywayException {
        String errorMessage = "Unable to load config file: " + configFile.getAbsolutePath();

        if ("-".equals(configFile.getName())) {
            Map<String, String> inputStreamConfig = loadConfigurationFromInputStream(System.in);
            for (String key : inputStreamConfig.keySet()) {
                stdInContent += key + "=" + inputStreamConfig.get(key) + " \n";
            }
            return inputStreamConfig;
        } else if (!configFile.isFile() || !configFile.canRead()) {
            if (!failIfMissing) {
                LOG.debug(errorMessage);
                return new HashMap<>();
            }
            throw new FlywayException(errorMessage);
        }

        LOG.debug("Loading config file: " + configFile.getAbsolutePath());

        try {
            return loadConfigurationFromReader(new InputStreamReader(new FileInputStream(configFile), encoding));
        } catch (IOException | FlywayException e) {
            throw new FlywayException(errorMessage, e);
        }
    }

    public static Map<String, String> loadConfigurationFromInputStream(InputStream inputStream) {
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
                    String configurationString = (char) firstCharacter + FileUtils.copyToString(bufferedReader);
                    Map<String, String> configurationFromStandardInput = loadConfigurationFromString(configurationString);

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
     * Reads the configuration from a Reader.
     *
     * @return The properties from the configuration file. An empty Map if none.
     * @throws FlywayException When the configuration could not be read.
     */
    public static Map<String, String> loadConfigurationFromReader(Reader reader) throws FlywayException {
        try {
            String contents = FileUtils.copyToString(reader);
            return loadConfigurationFromString(contents);
        } catch (IOException e) {
            throw new FlywayException("Unable to read config", e);
        }
    }

    public static Map<String, String> loadConfigurationFromString(String configuration) throws IOException {
        String[] lines = configuration.replace("\r\n", "\n").split("\n");

        StringBuilder confBuilder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String replacedLine = lines[i].trim().replace("\\", "\\\\");

            // if the line ends in a \\, then it may be a multiline property
            if (replacedLine.endsWith("\\\\")) {
                // if we aren't the last line
                if (i < lines.length - 1) {
                    // look ahead to see if the next line is a blank line, a property, or another multiline
                    String nextLine = lines[i + 1];
                    boolean restoreMultilineDelimiter = false;
                    if (nextLine.isEmpty()) {
                        // blank line
                    } else if (nextLine.trim().startsWith("flyway.") && nextLine.contains("=")) {
                        if (isMultilineBooleanExpression(nextLine)) {
                            // next line is an extension of a boolean expression
                            restoreMultilineDelimiter = true;
                        }
                        // next line is a property
                    } else {
                        // line with content, this was a multiline property
                        restoreMultilineDelimiter = true;
                    }

                    if (restoreMultilineDelimiter) {
                        // it's a multiline property, so restore the original single slash
                        replacedLine = replacedLine.substring(0, replacedLine.length() - 2) + "\\";
                    }
                }
            }

            confBuilder.append(replacedLine).append("\n");
        }
        String contents = confBuilder.toString();

        Properties properties = new Properties();
        contents = expandEnvironmentVariables(contents, System.getenv());
        properties.load(new StringReader(contents));
        return propertiesToMap(properties);
    }

    static String expandEnvironmentVariables(String value, Map<String, String> environmentVariables) {
        Pattern pattern = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
        Matcher matcher = pattern.matcher(value);
        String expandedValue = value;

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = environmentVariables.getOrDefault(variableName, "");

            LOG.debug("Expanding environment variable in config: " + variableName + " -> " + variableValue);
            expandedValue = expandedValue.replaceAll(Pattern.quote(matcher.group(0)), Matcher.quoteReplacement(variableValue));
        }

        return expandedValue;
    }

    /**
     * Converts this Properties object into a map.
     */
    public static Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> props = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            props.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return props;
    }

    /**
     * Puts this property in the config if it has been set in any of these values.
     *
     * @param config The config.
     * @param key The property name.
     * @param values The values to try. The first non-null value will be set.
     */
    public static void putIfSet(Map<String, String> config, String key, Object... values) {
        for (Object value : values) {
            if (value != null) {
                config.put(key, value.toString());
                return;
            }
        }
    }

    /**
     * Puts this property in the config if it has been set in any of these values.
     *
     * @param config The config.
     * @param key The property name.
     * @param values The values to try. The first non-null value will be set.
     */
    public static void putArrayIfSet(Map<String, String> config, String key, String[]... values) {
        for (String[] value : values) {
            if (value != null) {
                config.put(key, StringUtils.arrayToCommaDelimitedString(value));
                return;
            }
        }
    }

    /**
     * @param config The config.
     * @param key The property name.
     * @return The property value as a boolean if it exists, otherwise {@code null}.
     * @throws FlywayException when the property value is not a valid boolean.
     */
    public static Boolean removeBoolean(Map<String, String> config, String key) {
        String value = config.remove(key);
        if (value == null) {
            return null;
        }
        if (!"true".equals(value) && !"false".equals(value)) {
            throw new FlywayException("Invalid value for " + key + " (should be either true or false): " + value,
                                      ErrorCode.CONFIGURATION);
        }
        return Boolean.valueOf(value);
    }

    /**
     * @param config The config.
     * @param key The property name.
     * @return The property value as an integer if it exists, otherwise {@code null}.
     * @throws FlywayException When the property value is not a valid integer.
     */
    public static Integer removeInteger(Map<String, String> config, String key) {
        String value = config.remove(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new FlywayException("Invalid value for " + key + " (should be an integer): " + value,
                                      ErrorCode.CONFIGURATION);
        }
    }

    public static void dumpConfiguration(Map<String, String> config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using configuration:");
            for (Map.Entry<String, String> entry : new TreeMap<>(config).entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.toLowerCase().endsWith("password")) {
                    value = StringUtils.trimOrPad("", value.length(), '*');
                } else if (ConfigUtils.LICENSE_KEY.equals(key)) {
                    value = value.substring(0, 8) + "******" + value.substring(value.length() - 4);
                } else if (ConfigUtils.URL.equals(key)) {
                    value = DatabaseTypeRegister.redactJdbcUrl(value);
                }

                LOG.debug(key + " -> " + value);
            }
        }
    }

    /**
     * Checks the configuration for any unrecognised properties remaining after expected ones have been consumed.
     *
     * @param config The configured properties.
     * @param prefix The expected prefix for Flyway configuration parameters. {@code null} if none.
     */
    public static void checkConfigurationForUnrecognisedProperties(Map<String, String> config, String prefix) {
        ArrayList<String> unknownFlywayProperties = new ArrayList<>();
        for (String key : config.keySet()) {
            if (prefix == null || (key.startsWith(prefix) && !key.startsWith(FLYWAY_PLUGINS_PREFIX))) {
                unknownFlywayProperties.add(key);
            }
        }

        if (!unknownFlywayProperties.isEmpty()) {
            String property = (unknownFlywayProperties.size() == 1) ? "property" : "properties";
            String message = String.format("Unknown configuration %s: %s",
                                           property,
                                           StringUtils.arrayToCommaDelimitedString(unknownFlywayProperties.toArray()));
            throw new FlywayException(message, ErrorCode.CONFIGURATION);
        }
    }
}