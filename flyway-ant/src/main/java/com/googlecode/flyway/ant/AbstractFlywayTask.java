/**
 * Copyright (C) 2010-2013 the original author or authors.
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
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import javax.sql.DataSource;

/**
 * Base class for all Flyway Ant tasks.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractFlywayTask extends Task {
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
     * Comma-separated list of the schemas managed by Flyway. These schema names are case-sensitive.<br/>
     * (default: The default schema for the datasource connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the metadata table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     * Also configurable with Ant Property: ${flyway.schemas}
     */
    private String schemas;

    /**
     * <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     * metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     * <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     * the list. </p> (default: schema_version)<br/>Also configurable with Ant Property: ${flyway.table}
     */
    private String table;

    /**
     * The version to tag an existing schema with when executing init. (default: 1)<br/>Also configurable with Ant Property: ${flyway.initialVersion}
     *
     * @deprecated Use initVersion instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private String initialVersion;

    /**
     * The description to tag an existing schema with when executing init. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     * ${flyway.initialDescription}
     *
     * @deprecated Use initDescription instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private String initialDescription;

    /**
     * The version to tag an existing schema with when executing init. (default: 1)<br/>Also configurable with Ant Property: ${flyway.initVersion}
     */
    private String initVersion;

    /**
     * The description to tag an existing schema with when executing init. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     * ${flyway.initDescription}
     */
    private String initDescription;

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
        this.schemas = schemas;
    }

    /**
     * @param table <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     *              metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     *              <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     *              the list. </p> (default: schema_version)<br/>Also configurable with Ant Property: ${flyway.table}
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @param initialVersion The version to tag an existing schema with when executing init. (default: 1)<br/>Also configurable with Ant Property: ${flyway.initialVersion}
     * @deprecated Use setInitVersion instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialVersion(String initialVersion) {
        this.initialVersion = initialVersion;
    }

    /**
     * @param initialDescription The description to tag an existing schema with when executing init. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     *                           ${flyway.initialDescription}
     * @deprecated Use setInitDescription instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialDescription(String initialDescription) {
        this.initialDescription = initialDescription;
    }


    /**
     * @param initVersion The version to tag an existing schema with when executing init. (default: 1)<br/>Also configurable with Ant Property: ${flyway.initVersion}
     */
    public void setInitVersion(String initVersion) {
        this.initVersion = initVersion;
    }

    /**
     * @param initDescription The description to tag an existing schema with when executing init. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     *                        ${flyway.initDescription}
     */
    public void setInitDescription(String initDescription) {
        this.initDescription = initDescription;
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

        return new DriverDataSource(driverValue, urlValue, userValue, passwordValue);
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

    @Override
    public void execute() throws BuildException {
        LogFactory.setLogCreator(new AntLogCreator(getProject()));
        log = LogFactory.getLog(getClass());

        prepareClassPath();

        try {
            Flyway flyway = new Flyway();

            flyway.setDataSource(createDataSource());
            String schemasValue = useValueIfPropertyNotSet(schemas, "schemas");
            if (schemasValue != null) {
                flyway.setSchemas(StringUtils.tokenizeToStringArray(schemasValue, ","));
            }
            String tableValue = useValueIfPropertyNotSet(table, "table");
            if (tableValue != null) {
                flyway.setTable(tableValue);
            }
            String initialVersionValue = useValueIfPropertyNotSet(initialVersion, "initialVersion");
            if (initialVersionValue != null) {
                flyway.setInitialVersion(initialVersionValue);
            }
            String initialDescriptionValue = useValueIfPropertyNotSet(initialDescription, "initialDescription");
            if (initialDescriptionValue != null) {
                flyway.setInitialDescription(initialDescriptionValue);
            }
            String initVersionValue = useValueIfPropertyNotSet(initVersion, "initVersion");
            if (initVersionValue != null) {
                flyway.setInitVersion(initVersionValue);
            }
            String initDescriptionValue = useValueIfPropertyNotSet(initDescription, "initDescription");
            if (initDescriptionValue != null) {
                flyway.setInitDescription(initDescriptionValue);
            }

            doExecute(flyway);
        } catch (Exception e) {
            log.error(e.toString());

            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                log.error("Caused by " + rootCause.toString());
            }
            throw new BuildException("Flyway Error: " + e.toString(), e);
        }
    }

    /**
     * Executes this task.
     *
     * @param flyway The flyway instance to operate on.
     * @throws Exception any exception
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;
}
