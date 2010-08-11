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

package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.ValidationType;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @goal migrate
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MigrateMojo extends AbstractFlywayMojo {
    /**
     * Prefix for additional placeholders that are configured through properties (System or POM).
     */
    private static final String ADDITIONAL_PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The base package where the Java migrations are located. (default: db.migration) <br>
     * default property: ${flyway.basePackage}
     *
     * @parameter default-value="${flyway.basePackage}"
     */
    private String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: sql/location)<br>
     * default property: ${flyway.baseDir}
     *
     * @parameter default-value="${flyway.baseDir}"
     */
    private String baseDir;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br>
     * default property: ${flyway.encoding}
     *
     * @parameter default-value="${flyway.encoding}"
     */
    private String encoding;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     *
     * @parameter
     */
    private Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. (default: ${ )<br>
     * default property: ${flyway.placeholderPrefix}
     *
     * @parameter default-value="${flyway.placeholderPrefix}"
     */
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )<br>
     * default property: ${flyway.placeholderSuffix}
     *
     * @parameter default-value="${flyway.placeholderSuffix}"
     */
    private String placeholderSuffix;


    /**
     * The prefix for sql migrations (default: V)
     * default property: ${flyway.sqlMigrationPrefix}
     *
     * @parameter default-value="${flyway.sqlMigrationPrefix}"
     */
    private String sqlMigrationPrefix;

    /**
     * The suffix for sql migrations (default: .sql)
     * default property: ${flyway.sqlMigrationSuffix}
     *
     * @parameter default-value="${flyway.sqlMigrationSuffix}"
     */
    private String sqlMigrationSuffix;

    /**
     * The validation type for performed before migrating.
     * For each sql migration a CRC32 checksum is calculated when the sql script is executed.
     * The validate mechanism checks if the sql migrations in the classpath still has the same checksum
     * as the sql migration already executed in the database.
     * Possible values are: none | all | all-clean (clean schema if validation fail and run database migration from scratch)
     * default property: ${flyway.validate}
     *
     * @parameter default-value="${flyway.validate}"
     */
    private String validate;


    /**
     * @parameter expression="${project}" required="true"
     */
    private MavenProject mavenProject;

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        if (basePackage != null) {
            flyway.setBasePackage(basePackage);
        }
        if (baseDir != null) {
            flyway.setBaseDir(baseDir);
        }
        if (encoding != null) {
            flyway.setEncoding(encoding);
        }

        Map<String, String> mergedPlaceholders = new HashMap<String, String>();
        addPlaceholdersFromProperties(mergedPlaceholders, mavenProject.getProperties());
        addPlaceholdersFromProperties(mergedPlaceholders, System.getProperties());
        if (placeholders != null) {
            mergedPlaceholders.putAll(placeholders);
        }
        flyway.setPlaceholders(mergedPlaceholders);

        if (placeholderPrefix != null) {
            flyway.setPlaceholderPrefix(placeholderPrefix);
        }
        if (placeholderSuffix != null) {
            flyway.setPlaceholderSuffix(placeholderSuffix);
        }
        if (sqlMigrationPrefix != null) {
            flyway.setSqlMigrationPrefix(sqlMigrationPrefix);
        }
        if (sqlMigrationSuffix != null) {
            flyway.setSqlMigrationSuffix(sqlMigrationSuffix);
        }
        if (validate != null) {
            final ValidationType validationType = ValidationType.fromCode(validate);
            if (validationType == null) {
                throw new IllegalStateException("unsupported value for validate: " + validate);
            }
            flyway.setValidationType(validationType);
        }

        flyway.migrate();
    }

    /**
     * Adds the additional placeholders contained in these properties to the existing list.
     *
     * @param placeholders The existing list of placeholders.
     * @param properties   The properties containing additional placeholders.
     */
    private void addPlaceholdersFromProperties(Map<String, String> placeholders, Properties properties) {
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
