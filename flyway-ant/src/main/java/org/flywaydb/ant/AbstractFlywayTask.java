/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.ant;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.util.ExceptionUtils;
import org.flywaydb.core.util.Location;
import org.flywaydb.core.util.StringUtils;
import org.flywaydb.core.util.jdbc.DriverDataSource;
import org.flywaydb.core.util.logging.Log;
import org.flywaydb.core.util.logging.LogFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for all Flyway Ant tasks.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractFlywayTask extends Task {
    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    private Flyway flyway = new Flyway();

    /**
     * Logger.
     */
    protected Log log;

    /**
     * The classpath used to load the JDBC driver and the migrations.
     */
    private Path classPath;

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br/>Also configurable with Ant Property:
     * ${flyway.driver}
     */
    private String driver;

    /**
     * The jdbc url to use to connect to the database.<br/>Also configurable with Ant Property: ${flyway.url}
     */
    private String url;

    /**
     * The user to use to connect to the database. (default: <i>blank</i>)<br/>Also configurable with Ant Property: ${flyway.user}<br>
     * The credentials can be specified by user/password or serverId from settings.xml
     */
    private String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)<br/>Also configurable with Ant Property: ${flyway.password}
     */
    private String password;

    /**
     * Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.locations}
     */
    private String[] locations = flyway.getLocations();

    /**
     * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    private Map<String, String> placeholders = flyway.getPlaceholders();

    /**
     * @param classpath The classpath used to load the JDBC driver and the migrations.<br/>Also configurable with Ant
     *                  Property: ${flyway.classpath}
     */
    public void setClasspath(Path classpath) {
        this.classPath = classpath;
    }

    /**
     * @param classpathref The reference to the classpath used to load the JDBC driver and the migrations.<br/>Also
     *                     configurable with Ant Property: ${flyway.classpathref}
     */
    public void setClasspathref(Reference classpathref) {
        Path classPath = new Path(getProject());
        classPath.setRefid(classpathref);
        this.classPath = classPath;
    }

    /**
     * @param driver The fully qualified classname of the jdbc driver to use to connect to the database.<br/>
     *               By default, the driver is autodetected based on the url.<br/>
     *               Also configurable with Ant Property: ${flyway.driver}
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * @param url The jdbc url to use to connect to the database.<br/>Also configurable with Ant Property: ${flyway.url}
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @param user The user to use to connect to the database.<br/>Also configurable with Ant Property: ${flyway.user}
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param password The password to use to connect to the database. (default: <i>blank</i>)<br/>Also configurable with Ant Property: ${flyway.password}
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param schemas Comma-separated list of the schemas managed by Flyway. These schema names are case-sensitive.<br/>
     *                (default: The default schema for the datasource connection)
     *                <p>Consequences:</p>
     *                <ul>
     *                <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     *                <li>The first schema in the list will also be the one containing the metadata table.</li>
     *                <li>The schemas will be cleaned in the order of this list.</li>
     *                </ul>Also configurable with Ant Property:
     *                ${flyway.schemas}
     */
    public void setSchemas(String schemas) {
        flyway.setSchemas(StringUtils.tokenizeToStringArray(schemas, ","));
    }
    
    /**
     * @param callbacks A comma-separated list of fully qualified FlywayCallback implementation class names.  These classes
     * 					will be instantiated and wired into the Flyway lifecycle notification events.
     */
    public void setCallbacks(String callbacks) {
    	flyway.initCallbackDefs(callbacks);
    }

    /**
     * @param table <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     *              metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     *              <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     *              the list. </p> (default: schema_version)<br/>Also configurable with Ant Property: ${flyway.table}
     */
    public void setTable(String table) {
        flyway.setTable(table);
    }

    /**
     * @param initVersion The version to tag an existing schema with when executing init. (default: 1)<br/>Also configurable with Ant Property: ${flyway.initVersion}
     */
    public void setInitVersion(String initVersion) {
        flyway.setInitVersion(initVersion);
    }

    /**
     * @param initDescription The description to tag an existing schema with when executing init. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     *                        ${flyway.initDescription}
     */
    public void setInitDescription(String initDescription) {
        flyway.setInitDescription(initDescription);
    }

    /**
     * Creates the datasource base on the provided parameters.
     *
     * @return The fully configured datasource.
     * @throws Exception Thrown when the datasource could not be created.
     */
    /* private -> for testing */ DataSource createDataSource() throws Exception {
        String driverValue = useValueIfPropertyNotSet(driver, "driver");
        String urlValue = useValueIfPropertyNotSet(url, "url");
        String userValue = useValueIfPropertyNotSet(user, "user");
        String passwordValue = useValueIfPropertyNotSet(password, "password");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), driverValue, urlValue, userValue, passwordValue);
    }

    /**
     * Retrieves a value either from an Ant property or if not set, directly.
     *
     * @param value          The value to check.
     * @param flywayProperty The flyway Ant property. Ex. 'url' for 'flyway.url'
     * @return The value.
     */
    protected String useValueIfPropertyNotSet(String value, String flywayProperty) {
        String propertyValue = getProject().getProperty("flyway." + flywayProperty);
        if (propertyValue != null) {
            return propertyValue;
        }

        return value;
    }

    /**
     * Retrieves a boolean value either from an Ant property or if not set, directly.
     *
     * @param value          The boolean value to check.
     * @param flywayProperty The flyway Ant property. Ex. 'url' for 'flyway.url'
     * @return The boolean value.
     */
    protected boolean useValueIfPropertyNotSet(boolean value, String flywayProperty) {
        String propertyValue = getProject().getProperty("flyway." + flywayProperty);
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }

        return value;
    }

    /**
     * Prepares the classpath this task runs in, so that it includes both the classpath for Flyway and the classpath for
     * the JDBC drivers and migrations.
     */
    private void prepareClassPath() {
        Path classpath = (Path) getProject().getReference("flyway.classpath");
        if (classpath != null) {
            setClasspath(classpath);
        } else {
            Reference classpathRef = (Reference) getProject().getReference("flyway.classpathref");
            if (classpathRef != null) {
                setClasspathref(classpathRef);
            }
        }

        ClassLoader classLoader =
                new AntClassLoader(getClass().getClassLoader(), getProject(), classPath);
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * Do not use. For Ant itself.
     *
     * @param locations The locations on the classpath.
     */
    public void addConfiguredLocations(Locations locations) {
        this.locations = locations.locations.toArray(new String[locations.locations.size()]);
    }

    /**
     * @param encoding The encoding of Sql migrations. (default: UTF-8)<br/>Also configurable with Ant Property: ${flyway.encoding}
     */
    public void setEncoding(String encoding) {
        flyway.setEncoding(encoding);
    }

    /**
     * @param sqlMigrationPrefix The file name prefix for Sql migrations (default: V)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationPrefix}
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        flyway.setSqlMigrationPrefix(sqlMigrationPrefix);
    }

    /**
     * @param sqlMigrationSuffix The file name suffix for Sql migrations (default: .sql)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationSuffix}
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        flyway.setSqlMigrationSuffix(sqlMigrationSuffix);
    }

    /**
     * @param target The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     *               applied. (default: the latest version)<br/>Also configurable with Ant Property: ${flyway.target}
     */
    public void setTarget(String target) {
        flyway.setTarget(target);
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
        flyway.setCleanOnValidationError(cleanOnValidationError);
    }

    /**
     * @param outOfOrder Allows migrations to be run "out of order" (default: {@code false}).
     *                   <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     *                   it will be applied too instead of being ignored.</p>
     *                   Also configurable with Ant Property: ${flyway.outOfOrder}
     */
    public void setOutOfOrder(boolean outOfOrder) {
        flyway.setOutOfOrder(outOfOrder);
    }

    /**
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )<br/>Also configurable with Ant Property: ${flyway.placeholderPrefix}
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        flyway.setPlaceholderPrefix(placeholderPrefix);
    }

    /**
     * @param placeholderSuffix The suffix of every placeholder. (default: } )<br/>Also configurable with Ant Property: ${flyway.placeholderSuffix}
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        flyway.setPlaceholderSuffix(placeholderSuffix);
    }

    /**
     * Adds placeholders from a nested &lt;placeholders&gt; element. Called by Ant.
     *
     * @param placeholders The fully configured placeholders element.
     */
    public void addConfiguredPlaceholders(PlaceholdersElement placeholders) {
        this.placeholders = placeholders.placeholders;
    }

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
        flyway.setIgnoreFailedFutureMigration(ignoreFailedFutureMigration);
    }

    /**
     * @param validateOnMigrate Whether to automatically call validate or not when running migrate. (default: {@code false})<br/>
     *                          Also configurable with Ant Property: ${flyway.validateOnMigrate}
     */
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        flyway.setValidateOnMigrate(validateOnMigrate);
    }

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     * Also configurable with Ant Property: ${flyway.initOnMigrate}
     *
     * @param initOnMigrate {@code true} if init should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public void setInitOnMigrate(boolean initOnMigrate) {
        flyway.setInitOnMigrate(initOnMigrate);
    }

    @Override
    public void execute() throws BuildException {
        LogFactory.setLogCreator(new AntLogCreator(getProject()));
        log = LogFactory.getLog(getClass());

        prepareClassPath();

        try {
            flyway.setClassLoader(Thread.currentThread().getContextClassLoader());
            flyway.setDataSource(createDataSource());

            Properties projectProperties = new Properties();
            projectProperties.putAll(getProject().getProperties());
            flyway.configure(projectProperties);
            flyway.configure(System.getProperties());

            flyway.setLocations(getLocations());

            addPlaceholdersFromProperties(placeholders, getProject().getProperties());
            flyway.setPlaceholders(placeholders);

            doExecute(flyway);
        } catch (Exception e) {
            throw new BuildException("Flyway Error: " + e.toString(), ExceptionUtils.getRootCause(e));
        }
    }

    /**
     * Executes this task.
     *
     * @param flyway The flyway instance to operate on.
     * @throws Exception any exception
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;

    /**
     * @return The locations configured through Ant.
     */
    private String[] getLocations() {
        String[] locationsVal = locations;
        String locationsProperty = getProject().getProperty("flyway.locations");
        if (locationsProperty != null) {
            locationsVal = StringUtils.tokenizeToStringArray(locationsProperty, ",");
        }

        //Adjust relative locations to be relative from Ant's basedir.
        File baseDir = getProject().getBaseDir();
        for (int i = 0; i < locationsVal.length; i++) {
            locationsVal[i] = adjustRelativeFileSystemLocationToBaseDir(baseDir, locationsVal[i]);
        }

        return locationsVal;
    }

    /**
     * Adjusts a relative filesystem location to Ant's basedir. All other locations are left untouched.
     *
     * @param baseDir     Ant's basedir.
     * @param locationStr The location to adjust.
     * @return The adjusted location.
     */
    /* private -> testing */
    static String adjustRelativeFileSystemLocationToBaseDir(File baseDir, String locationStr) {
        Location location = new Location(locationStr);
        if (location.isFileSystem() && !new File(location.getPath()).isAbsolute()) {
            return Location.FILESYSTEM_PREFIX + baseDir.getAbsolutePath() + "/" + location.getPath();
        }
        return locationStr;
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
