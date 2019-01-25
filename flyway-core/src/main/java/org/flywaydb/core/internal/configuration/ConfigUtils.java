/*
 * Copyright 2010-2019 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Configuration-related utilities.
 */
public class ConfigUtils {
    private static Log LOG = LogFactory.getLog(ConfigUtils.class);

    /**
     * The default configuration file name.
     */
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
    public static final String DRIVER = "flyway.driver";
    public static final String DRYRUN_OUTPUT = "flyway.dryRunOutput";
    public static final String ENCODING = "flyway.encoding";
    public static final String ERROR_OVERRIDES = "flyway.errorOverrides";
    public static final String GROUP = "flyway.group";
    public static final String IGNORE_FUTURE_MIGRATIONS = "flyway.ignoreFutureMigrations";
    public static final String IGNORE_MISSING_MIGRATIONS = "flyway.ignoreMissingMigrations";
    public static final String IGNORE_IGNORED_MIGRATIONS = "flyway.ignoreIgnoredMigrations";
    public static final String IGNORE_PENDING_MIGRATIONS = "flyway.ignorePendingMigrations";
    public static final String INIT_SQL = "flyway.initSql";
    public static final String INSTALLED_BY = "flyway.installedBy";
    public static final String LICENSE_KEY = "flyway.licenseKey";
    public static final String LOCATIONS = "flyway.locations";
    public static final String MIXED = "flyway.mixed";
    public static final String OUT_OF_ORDER = "flyway.outOfOrder";
    public static final String PASSWORD = "flyway.password";
    public static final String PLACEHOLDER_PREFIX = "flyway.placeholderPrefix";
    public static final String PLACEHOLDER_REPLACEMENT = "flyway.placeholderReplacement";
    public static final String PLACEHOLDER_SUFFIX = "flyway.placeholderSuffix";
    public static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";
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
    public static final String UNDO_SQL_MIGRATION_PREFIX = "flyway.undoSqlMigrationPrefix";
    public static final String URL = "flyway.url";
    public static final String USER = "flyway.user";
    public static final String VALIDATE_ON_MIGRATE = "flyway.validateOnMigrate";

    // Oracle-specific
    public static final String ORACLE_SQLPLUS = "flyway.oracle.sqlplus";

    // Command-line specific
    public static final String JAR_DIRS = "flyway.jarDirs";

    // Gradle specific
    public static final String CONFIGURATIONS = "flyway.configurations";

    private ConfigUtils() {
        // Utility class
    }

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
        if ("FLYWAY_DRIVER".equals(key)) {
            return DRIVER;
        }
        if ("FLYWAY_DRYRUN_OUTPUT".equals(key)) {
            return DRYRUN_OUTPUT;
        }
        if ("FLYWAY_ENCODING".equals(key)) {
            return ENCODING;
        }
        if ("FLYWAY_ERROR_OVERRIDES".equals(key)) {
            return ERROR_OVERRIDES;
        }
        if ("FLYWAY_GROUP".equals(key)) {
            return GROUP;
        }
        if ("FLYWAY_IGNORE_FUTURE_MIGRATIONS".equals(key)) {
            return IGNORE_FUTURE_MIGRATIONS;
        }
        if ("FLYWAY_IGNORE_MISSING_MIGRATIONS".equals(key)) {
            return IGNORE_MISSING_MIGRATIONS;
        }
        if ("FLYWAY_IGNORE_IGNORED_MIGRATIONS".equals(key)) {
            return IGNORE_IGNORED_MIGRATIONS;
        }
        if ("FLYWAY_IGNORE_PENDING_MIGRATIONS".equals(key)) {
            return IGNORE_PENDING_MIGRATIONS;
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
        if ("FLYWAY_PASSWORD".equals(key)) {
            return PASSWORD;
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
        if (key.matches("FLYWAY_PLACEHOLDERS_.+")) {
            return PLACEHOLDERS_PROPERTY_PREFIX + key.substring("FLYWAY_PLACEHOLDERS_".length()).toLowerCase(Locale.ENGLISH);
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

        // Oracle-specific
        if ("FLYWAY_ORACLE_SQLPLUS".equals(key)) {
            return ORACLE_SQLPLUS;
        }

        // Command-line specific
        if ("FLYWAY_JAR_DIRS".equals(key)) {
            return JAR_DIRS;
        }

        // Gradle specific
        if ("FLYWAY_CONFIGURATIONS".equals(key)) {
            return CONFIGURATIONS;
        }

        return null;
    }

    /**
     * Loads the configuration from this configuration file.
     *
     * @param configFile    The configuration file to load.
     * @param encoding      The encoding of the configuration file.
     * @param failIfMissing Whether to fail if the file is missing.
     * @return The properties from the configuration file. An empty Map if none.
     * @throws FlywayException when the configuration file could not be loaded.
     */
    public static Map<String, String> loadConfigurationFile(File configFile, String encoding, boolean failIfMissing) throws FlywayException {
        String errorMessage = "Unable to load config file: " + configFile.getAbsolutePath();

        if (!configFile.isFile() || !configFile.canRead()) {
            if (!failIfMissing) {
                LOG.debug(errorMessage);
                return new HashMap<>();
            }
            throw new FlywayException(errorMessage);
        }

        LOG.debug("Loading config file: " + configFile.getAbsolutePath());
        try {
            String contents = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(configFile), encoding));
            Properties properties = new Properties();
            properties.load(new StringReader(contents.replace("\\", "\\\\")));
            return propertiesToMap(properties);
        } catch (IOException e) {
            throw new FlywayException(errorMessage, e);
        }
    }

    /**
     * Converts this Properties object into a map.
     *
     * @param properties The Properties object to convert.
     * @return The resulting map.
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
     * @param key    The property name.
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
     * @param key    The property name.
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
     * Dumps the configuration to the console when debug output is activated.
     *
     * @param config The configured properties.
     */
    public static void dumpConfiguration(Map<String, String> config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using configuration:");
            for (Map.Entry<String, String> entry : new TreeMap<>(config).entrySet()) {
                String value = entry.getValue();
                value = ConfigUtils.PASSWORD.equals(entry.getKey()) ? StringUtils.trimOrPad("", value.length(), '*') : value;
                LOG.debug(entry.getKey() + " -> " + value);
            }
        }
    }
}