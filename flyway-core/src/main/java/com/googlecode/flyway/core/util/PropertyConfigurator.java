/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Updates Flyway configuration from Properties.<br>
 */
public class PropertyConfigurator {

    /**
     * Prefix for additional placeholders that are configured through properties (System or POM).
     */
    private static final String ADDITIONAL_PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * Prevents instantiation.
     */
    private PropertyConfigurator() {
        //Do nothing
    }

    /**
     * Updates Flyway from properties.<br>
     * Property names are documented in the flyway maven plugin.
     * @param flyway Flyway instance to be configured.
     * @param placeholders existing placeholders
     * @param props properties used for configuration
     */
    public static void updateFromProperties(Flyway flyway, Map<String, String> placeholders, Properties props) {
        String driver = getProp("flyway.driver", props);
        String url = getProp("flyway.url", props);
        String user = getProp("flyway.user", props);
        String password = getProp("flyway.password", props);
        if (driver != null && url != null && user != null && password != null) {
            Driver driverClazz;
            try {
                driverClazz = (Driver) Class.forName(driver).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error instantiating database driver: " + driver, e);
            }
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource(driverClazz, url, user, password);
            flyway.setDataSource(dataSource);
        } else if (driver != null || url != null || user != null || password != null) {
            throw new RuntimeException(
                    "Missing value to configure datasource (driver, url, user and password are required)");
        }

        String baseDir = getProp("flyway.baseDir", props);
        if (baseDir != null) {
            flyway.setBaseDir(baseDir);
        }
        String placeholderPrefix = getProp("flyway.placeholderPrefix", props);
        if (placeholderPrefix != null) {
            flyway.setPlaceholderPrefix(placeholderPrefix);
        }
        String placeholderSuffix = getProp("flyway.placeholderSuffix", props);
        if (placeholderSuffix != null) {
            flyway.setPlaceholderSuffix(placeholderSuffix);
        }
        String sqlMigrationPrefix = getProp("flyway.sqlMigrationPrefix", props);
        if (sqlMigrationPrefix != null) {
            flyway.setSqlMigrationPrefix(sqlMigrationPrefix);
        }
        String sqlMigrationSuffix = getProp("flyway.sqlMigrationSuffix", props);
        if (sqlMigrationSuffix != null) {
            flyway.setSqlMigrationSuffix(sqlMigrationSuffix);
        }
        String basePackage = getProp("flyway.basePackage", props);
        if (basePackage != null) {
            flyway.setBasePackage(basePackage);
        }
        String encoding = getProp("flyway.encoding", props);
        if (encoding != null) {
            flyway.setEncoding(encoding);
        }
        String table = getProp("flyway.table", props);
        if (table != null) {
            flyway.setTable(table);
        }
        String validationErrorMode = getProp("flyway.validationErrorMode", props);
        if (validationErrorMode != null) {
            final ValidationErrorMode validationErrorModeEnum = ValidationErrorMode.valueOf(validationErrorMode);
            flyway.setValidationErrorMode(validationErrorModeEnum);
        }
        String validationMode = getProp("flyway.validationMode", props);
        if (validationErrorMode != null) {
            final ValidationMode validationModeEnum = ValidationMode.valueOf(validationMode);
            flyway.setValidationMode(validationModeEnum);
        }

        if (placeholders == null) {
            placeholders = new HashMap<String, String>();
        }
        addPlaceholdersFromProperties(placeholders, props);
        if (placeholders.size() > 0) {
            flyway.setPlaceholders(placeholders);
        }

    }

    private static String getProp(String key, Properties properties) {
        return (String) properties.get(key);
    }


    /**
     * Adds the additional placeholders contained in these properties to the existing list.
     *
     * @param placeholders The existing list of placeholders.
     * @param properties   The properties containing additional placeholders.
     */
    public static void addPlaceholdersFromProperties(Map<String, String> placeholders, Properties properties) {
        for (Object property : properties.keySet()) {
            String propertyName = (String) property;
            if (propertyName.startsWith(ADDITIONAL_PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > ADDITIONAL_PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(ADDITIONAL_PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = properties.getProperty(propertyName);
                placeholders.put(placeholderName, placeholderValue);
            }
        }
    }
}
