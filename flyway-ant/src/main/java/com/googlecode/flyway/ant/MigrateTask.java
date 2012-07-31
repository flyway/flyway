/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.ant;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.apache.tools.ant.Project;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Ant task that triggers the migration of the configured database to the latest version.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MigrateTask extends AbstractMigrationLoadingTask {
    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: false)<br/>Also configurable with Ant Property: ${flyway.ignoreFailedFutureMigration}
     */
    private boolean ignoreFailedFutureMigration;

    /**
     * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    private Map<String, String> placeholders = new HashMap<String, String>();

    /**
     * The prefix of every placeholder. (default: ${ )<br/>Also configurable with Ant Property: ${flyway.placeholderPrefix}
     */
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )<br/>Also configurable with Ant Property: ${flyway.placeholderSuffix}
     */
    private String placeholderSuffix;

    /**
     * The type of validation to be performed before migrating.<br/> <br/> Possible values are:<br/> <br/> <b>NONE</b>
     * (default)<br/> No validation is performed.<br/> <br/> <b>ALL</b><br/> For each sql migration a CRC32 checksum is
     * calculated when the sql script is executed. The validate mechanism checks if the sql migrations in the classpath
     * still has the same checksum as the sql migration already executed in the database.<br/> <br/>Also configurable with Ant Property: ${flyway.validationMode}
     */
    private String validationMode;

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this! (default: false)<br/>Also configurable with Ant Property: ${flyway.disableInitCheck}
     */
    private boolean disableInitCheck;

    /**
     * @param ignoreFailedFutureMigration Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     *                                    newer deployment of the application that are not yet available in this version. For example: we have migrations
     *                                    available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     *                                    (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     *                                    warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     *                                    an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     *                                    bad migration. (default: false)<br/>Also configurable with Ant Property: ${flyway.ignoreFailedFutureMigration}
     */
    public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration) {
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )<br/>Also configurable with Ant Property: ${flyway.placeholderPrefix}
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * @param placeholderSuffix The suffix of every placeholder. (default: } )<br/>Also configurable with Ant Property: ${flyway.placeholderSuffix}
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * @param validationMode The type of validation to be performed before migrating.<br/> <br/> Possible values are:<br/> <br/> <b>NONE</b>
     *                       (default)<br/> No validation is performed.<br/> <br/> <b>ALL</b><br/> For each sql migration a CRC32 checksum is
     *                       calculated when the sql script is executed. The validate mechanism checks if the sql migrations in the classpath
     *                       still has the same checksum as the sql migration already executed in the database.<br/> <br/>Also configurable with Ant Property: ${flyway.validationMode}
     */
    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    /**
     * @param disableInitCheck Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     *                         Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     *                         this! (default: false)<br/>Also configurable with Ant Property: ${flyway.disableInitCheck}
     */
    public void setDisableInitCheck(boolean disableInitCheck) {
        this.disableInitCheck = disableInitCheck;
    }

    /**
     * Adds a placeholder from a nested &lt;placeholder&gt; element. Called by Ant.
     *
     * @param placeholder The fully configured placeholder element.
     * @deprecated Use the &lt;placeholders&gt; element instead of adding individual &lt;placeholder&gt; elements directly. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void addConfiguredPlaceholder(PlaceholderElement placeholder) {
        getProject().log(this, "The direct use of <placeholder> is deprecated." +
                " They should be nested inside a <placeholders> element." +
                " Support for this will be removed in Flyway 2.0.", null, Project.MSG_WARN);
        placeholders.put(placeholder.name, placeholder.value);
    }

    /**
     * Adds placeholders from a nested &lt;placeholders&gt; element. Called by Ant.
     *
     * @param placeholders The fully configured placeholders element.
     */
    public void addConfiguredPlaceholders(PlaceholdersElement placeholders) {
        this.placeholders = placeholders.placeholders;
    }

    @Override
    protected void doExecuteWithMigrationConfig(Flyway flyway) throws Exception {
        boolean ignoreFailedFutureMigrationValue =
                Boolean.valueOf(
                        useValueIfPropertyNotSet(
                                Boolean.toString(ignoreFailedFutureMigration),
                                "ignoreFailedFutureMigration"));
        flyway.setIgnoreFailedFutureMigration(ignoreFailedFutureMigrationValue);

        addPlaceholdersFromProperties(placeholders, getProject().getProperties());
        flyway.setPlaceholders(placeholders);

        String placeholderPrefixValue = useValueIfPropertyNotSet(placeholderPrefix, "placeholderPrefix");
        if (placeholderPrefixValue != null) {
            flyway.setPlaceholderPrefix(placeholderPrefixValue);
        }
        String placeholderSuffixValue = useValueIfPropertyNotSet(placeholderSuffix, "placeholderSuffix");
        if (placeholderSuffixValue != null) {
            flyway.setPlaceholderSuffix(placeholderSuffixValue);
        }
        String validationModeValue = useValueIfPropertyNotSet(validationMode, "validationMode");
        if (validationModeValue != null) {
            flyway.setValidationMode(ValidationMode.valueOf(validationModeValue.toUpperCase()));
        }
        boolean disableInitCheckValue =
                Boolean.valueOf(
                        useValueIfPropertyNotSet(
                                Boolean.toString(disableInitCheck), "disableInitCheck"));
        flyway.setDisableInitCheck(disableInitCheckValue);

        if (flyway.info().all().length == 0) {
            LOG.warn("Possible solution: run the Ant javac and copy tasks first so Flyway can find the migrations");
            return;
        }

        flyway.migrate();
    }

    /**
     * Adds the additional placeholders contained in these properties to the existing list.
     *
     * @param placeholders The existing list of placeholders.
     * @param properties   The properties containing additional placeholders.
     */
    private static void addPlaceholdersFromProperties(Map<String, String> placeholders, Hashtable properties) {
        for (Object property : properties.keySet()) {
            String propertyName = (String) property;
            if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = (String) properties.get(propertyName);
                placeholders.put(placeholderName, placeholderValue);
            }
        }
    }

    /**
     * Nested &lt;placeholders&gt; element of the migrate Ant task.
     */
    public static class PlaceholdersElement {
        /**
         * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
         */
        Map<String, String> placeholders = new HashMap<String, String>();

        /**
         * Adds a placeholder from a nested &lt;placeholder&gt; element. Called by Ant.
         *
         * @param placeholder The fully configured placeholder element.
         */
        public void addConfiguredPlaceholder(PlaceholderElement placeholder) {
            placeholders.put(placeholder.name, placeholder.value);
        }
    }

    /**
     * Nested &lt;placeholder&gt; element inside the &lt;placeholders&gt; element of the migrate Ant task.
     */
    public static class PlaceholderElement {
        /**
         * The name of the placeholder.
         */
        private String name;

        /**
         * The value of the placeholder.
         */
        private String value;

        /**
         * @param name The name of the placeholder.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @param value The value of the placeholder.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
