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
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import org.apache.tools.ant.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Base class for tasks that rely on loading migrations from the classpath.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractMigrationLoadingTask extends AbstractFlywayTask {
    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.locations}
     */
    private String[] locations;

    /**
     * The base package where the Java migrations are located. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.basePackage}
     */
    private String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: db/migration)<br/>Also configurable with Ant Property: ${flyway.baseDir}
     */
    private String baseDir;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br/>Also configurable with Ant Property: ${flyway.encoding}
     */
    private String encoding;

    /**
     * The file name prefix for Sql migrations (default: V)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationPrefix}
     */
    private String sqlMigrationPrefix;

    /**
     * The file name suffix for Sql migrations (default: .sql)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationSuffix}
     */
    private String sqlMigrationSuffix;

    /**
     * The action to take when validation fails.<br/> <br/> Possible values are:<br/> <br/> <b>FAIL</b> (default)<br/>
     * Throw an exception and fail.<br/> <br/> <b>CLEAN (Warning ! Do not use in produktion !)</b><br/> Cleans the
     * database.<br/> <br/> This is exclusively intended as a convenience for development. Even tough we strongly
     * recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of
     * dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next
     * migration will bring you back to the state checked into SCM.<br/> <br/> This property has no effect when
     * <i>validationMode</i> is set to <i>NONE</i>.<br/> <br/>Also configurable with Ant Property: ${flyway.validationErrorMode}
     *
     * @deprecated Use cleanOnValidationError instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private String validationErrorMode;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     * <br/>Also configurable with Ant Property: ${flyway.cleanOnValidationError}
     */
    private boolean cleanOnValidationError;

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     * applied. (default: the latest version)<br/>Also configurable with Ant Property: ${flyway.target}
     */
    private String target;

    /**
     * Allows migrations to be run "out of order" (default: {@code false}).
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * Also configurable with Ant Property: ${flyway.outOfOrder}
     */
    private boolean outOfOrder;

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
     * Do not use. For Ant itself.
     *
     * @param locations The locations on the classpath.
     */
    public void addConfiguredLocations(Locations locations) {
        this.locations = locations.locations.toArray(new String[locations.locations.size()]);
    }

    /**
     * @param basePackage The base package where the Java migrations are located. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.basePackage}
     * @deprecated Use locations instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations are located. (default: db/migration)<br/>Also configurable with Ant Property: ${flyway.baseDir}
     * @deprecated Use locations instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param encoding The encoding of Sql migrations. (default: UTF-8)<br/>Also configurable with Ant Property: ${flyway.encoding}
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param sqlMigrationPrefix The file name prefix for Sql migrations (default: V)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationPrefix}
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * @param sqlMigrationSuffix The file name suffix for Sql migrations (default: .sql)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationSuffix}
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    /**
     * @param target The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     *               applied. (default: the latest version)<br/>Also configurable with Ant Property: ${flyway.target}
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @param validationErrorMode The action to take when validation fails.<br/> <br/> Possible values are:<br/> <br/> <b>FAIL</b> (default)<br/>
     *                            Throw an exception and fail.<br/> <br/> <b>CLEAN (Warning ! Do not use in produktion !)</b><br/> Cleans the
     *                            database.<br/> <br/> This is exclusively intended as a convenience for development. Even tough we strongly
     *                            recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of
     *                            dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next
     *                            migration will bring you back to the state checked into SCM.<br/> <br/> This property has no effect when
     *                            <i>validationMode</i> is set to <i>NONE</i>.<br/> <br/>Also configurable with Ant Property: ${flyway.validationErrorMode}
     * @deprecated Use cleanOnValidationError instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setValidationErrorMode(String validationErrorMode) {
        this.validationErrorMode = validationErrorMode;
    }

    /**
     * @param cleanOnValidationError Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>
     *                               <p> This is exclusively intended as a convenience for development. Even tough we
     *                               strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     *                               way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     *                               the next migration will bring you back to the state checked into SCM.</p>
     *                               <p><b>Warning ! Do not enable in production !</b></p>
     *                               <br/>Also configurable with Ant Property: ${flyway.cleanOnValidationError}
     */
    public void setCleanOnValidationError(boolean cleanOnValidationError) {
        this.cleanOnValidationError = cleanOnValidationError;
    }

    /**
     * @param outOfOrder Allows migrations to be run "out of order" (default: {@code false}).
     *                   <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     *                   it will be applied too instead of being ignored.</p>
     *                   Also configurable with Ant Property: ${flyway.outOfOrder}
     */
    public void setOutOfOrder(boolean outOfOrder) {
        this.outOfOrder = outOfOrder;
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
     * Adds a placeholder from a nested &lt;placeholder&gt; element. Called by Ant.
     *
     * @param placeholder The fully configured placeholder element.
     * @deprecated Use the &lt;placeholders&gt; element instead of adding individual &lt;placeholder&gt; elements directly. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void addConfiguredPlaceholder(PlaceholderElement placeholder) {
        getProject().log(this, "The direct use of <placeholder> is deprecated." +
                " They should be nested inside a <placeholders> element." +
                " Support for this will be removed in Flyway 3.0.", null, Project.MSG_WARN);
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
    protected final void doExecute(Flyway flyway) throws Exception {
        String locationsProperty = getProject().getProperty("flyway.locations");
        if (locationsProperty != null) {
            flyway.setLocations(StringUtils.tokenizeToStringArray(locationsProperty, ","));
        } else if (locations != null) {
            flyway.setLocations(locations);
        }
        String basePackageValue = useValueIfPropertyNotSet(basePackage, "basePackage");
        if (basePackageValue != null) {
            flyway.setBasePackage(basePackageValue);
        }
        String baseDirValue = useValueIfPropertyNotSet(baseDir, "baseDir");
        if (baseDirValue != null) {
            flyway.setBaseDir(baseDirValue);
        }

        String encodingValue = useValueIfPropertyNotSet(encoding, "encoding");
        if (encodingValue != null) {
            flyway.setEncoding(encodingValue);
        }
        String sqlMigrationPrefixValue = useValueIfPropertyNotSet(sqlMigrationPrefix, "sqlMigrationPrefix");
        if (sqlMigrationPrefixValue != null) {
            flyway.setSqlMigrationPrefix(sqlMigrationPrefixValue);
        }
        String sqlMigrationSuffixValue = useValueIfPropertyNotSet(sqlMigrationSuffix, "sqlMigrationSuffix");
        if (sqlMigrationSuffixValue != null) {
            flyway.setSqlMigrationSuffix(sqlMigrationSuffixValue);
        }
        String validationErrorModeValue = useValueIfPropertyNotSet(validationErrorMode, "validationErrorMode");
        if (validationErrorModeValue != null) {
            flyway.setValidationErrorMode(ValidationErrorMode.valueOf(validationErrorModeValue.toUpperCase()));
        }
        flyway.setCleanOnValidationError(useValueIfPropertyNotSet(cleanOnValidationError, "cleanOnValidationError"));
        flyway.setOutOfOrder(useValueIfPropertyNotSet(outOfOrder, "outOfOrder"));
        String targetValue = useValueIfPropertyNotSet(target, "target");
        if (targetValue != null) {
            flyway.setTarget(new MigrationVersion(targetValue));
        }

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

        doExecuteWithMigrationConfig(flyway);
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
     * Executes Flyway fully configured for loading migrations.
     *
     * @param flyway The instance of Flyway to launch.
     * @throws Exception when the execution failed.
     */
    protected abstract void doExecuteWithMigrationConfig(Flyway flyway) throws Exception;

    /**
     * The nested &lt;locations&gt; element of the task. Contains 1 or more &lt;location&gt; sub-elements.
     */
    public static class Locations {
        /**
         * The classpath locations.
         */
        List<String> locations = new ArrayList<String>();

        /**
         * Do not use. For Ant itself.
         *
         * @param location A location on the classpath.
         */
        public void addConfiguredLocation(LocationElement location) {
            locations.add(location.path);
        }
    }

    /**
     * One &lt;location&gt; sub-element within the &lt;locations&gt; element.
     */
    public static class LocationElement {
        /**
         * The path of the location.
         */
        private String path;

        /**
         * Do not use. For Ant itself.
         *
         * @param path The path of the location.
         */
        public void setPath(String path) {
            this.path = path;
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
