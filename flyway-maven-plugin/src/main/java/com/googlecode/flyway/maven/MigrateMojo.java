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
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @goal migrate
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MigrateMojo extends AbstractMigrationLoadingMojo {
    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The target version up to which migrations should run. (default: the latest version)
     * Also configurable with Maven or System Property: ${flyway.target}
     *
     * @parameter expression="${flyway.target}"
     */
    private String target;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     *
     * @parameter
     */
    private Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. (default: ${ )<br>
     * Also configurable with Maven or System Property: ${flyway.placeholderPrefix}
     *
     * @parameter expression="${flyway.placeholderPrefix}"
     */
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )<br>
     * Also configurable with Maven or System Property: ${flyway.placeholderSuffix}
     *
     * @parameter expression="${flyway.placeholderSuffix}"
     */
    private String placeholderSuffix;

    /**
     * The type of validation to be performed before migrating.<br/>
     * <br/>
     * Possible values are:<br/>
     * <br/>
     * <b>NONE</b> (default)<br/>
     * No validation is performed.<br/>
     * <br/>
     * <b>ALL</b><br/>
     * For each sql migration a CRC32 checksum is calculated when the sql script is executed.
     * The validate mechanism checks if the sql migrations in the classpath still has the same checksum
     * as the sql migration already executed in the database.<br/>
     * <br/>
     * Also configurable with Maven or System Property: ${flyway.validationMode}
     *
     * @parameter expression="${flyway.validationMode}"
     */
    private String validationMode;

    /**
     * The action to take when validation fails.<br/>
     * <br/>
     * Possible values are:<br/>
     * <br/>
     * <b>FAIL</b> (default)<br/>
     * Throw an exception and fail.<br/>
     * <br/>
     * <b>CLEAN (Warning ! Do not use in produktion !)</b><br/>
     * Cleans the database.<br/>
     * <br/>
     * This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change
     * migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in
     * a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you
     * back to the state checked into SCM.<br/>
     * <br/>
     * This property has no effect when <i>validationMode</i> is set to <i>NONE</i>.<br/>
     * <br/>
     * Also configurable with Maven or System Property: ${flyway.validationErrorMode}
     *
     * @parameter expression="${flyway.validationErrorMode}"
     */
    private String validationErrorMode;

    /**
     * Reference to the current project that includes the Flyway Maven plugin.
     *
     * @parameter expression="${project}" required="true"
     */
    private MavenProject mavenProject;

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        super.doExecute(flyway);

        if (target != null) {
            flyway.setTarget(new SchemaVersion(target));
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
        if (validationMode != null) {
            flyway.setValidationMode(ValidationMode.valueOf(validationMode.toUpperCase()));
        }
        if (validationErrorMode != null) {
            flyway.setValidationErrorMode(ValidationErrorMode.valueOf(validationErrorMode.toUpperCase()));
        }

        flyway.migrate();
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
            if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = properties.getProperty(propertyName);
                placeholders.put(placeholderName, placeholderValue);
            }
        }
    }
}
