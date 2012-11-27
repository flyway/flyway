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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import javax.sql.DataSource;

/**
 * Common base class for all mojos with all common attributes.<br>
 *
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 */
@SuppressWarnings({"JavaDoc", "FieldCanBeLocal", "UnusedDeclaration"})
abstract class AbstractFlywayMojo extends AbstractMojo {
    protected Log log;

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br>
     * <p>Also configurable with Maven or System Property: ${flyway.driver}</p>
     *
     * @parameter expression="${flyway.driver}"
     */
    /* private -> for testing */ String driver;

    /**
     * The jdbc url to use to connect to the database.<br>
     * <p>Also configurable with Maven or System Property: ${flyway.url}</p>
     *
     * @parameter expression="${flyway.url}"
     */
    /* private -> for testing */ String url;

    /**
     * The user to use to connect to the database. (default: <i>blank</i>)<br>
     * The credentials can be specified by user/password or {@code serverId} from settings.xml
     * <p>Also configurable with Maven or System Property: ${flyway.user}</p>
     *
     * @parameter expression="${flyway.user}"
     */
    /* private -> for testing */ String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.password}</p>
     *
     * @parameter expression="${flyway.password}"
     */
    private String password;

    /**
     * List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during
     * the migration. It will also be the one containing the metadata table. These schema names are case-sensitive.
     * (default: The default schema for the datasource connection)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.schemas} (comma-separated list)</p>
     *
     * @parameter expression="${flyway.schemas}"
     */
    private String[] schemas;

    /**
     * <p>The name of the metadata table that will be used by Flyway. (default: schema_version)</p>
     * <p> By default (single-schema mode) the
     * metadata table is placed in the default schema for the connection provided by the datasource. <br/> When the
     * {@code flyway.schemas} property is set (multi-schema mode), the metadata table is placed in the first schema of
     * the list. </p>
     * <p>Also configurable with Maven or System Property: ${flyway.table}</p>
     *
     * @parameter expression="${flyway.table}"
     */
    private String table;

    /**
     * The id of the server tag in settings.xml (default: flyway-db)<br/>
     * The credentials can be specified by user/password or {@code serverId} from settings.xml<br>
     * <p>Also configurable with Maven or System Property: ${flyway.serverId}</p>
     *
     * @parameter expression="${flyway.serverId}"
     */
    private String serverId = "flyway-db";

    /**
     * The link to the settings.xml
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * Reference to the current project that includes the Flyway Maven plugin.
     *
     * @parameter expression="${project}" required="true"
     */
    protected MavenProject mavenProject;

    /**
     * Load username password from settings
     *
     * @throws FlywayException when the credentials could not be loaded.
     */
    private void loadCredentialsFromSettings() throws FlywayException {
        if (user == null) {
            final Server server = settings.getServer(serverId);
            if (server != null) {
                user = server.getUsername();
                password = server.getPassword();
            }
        }
    }

    /**
     * Creates the datasource based on the provided parameters.
     *
     * @return The fully configured datasource.
     * @throws Exception Thrown when the datasource could not be created.
     */
    /* private -> for testing */ DataSource createDataSource() throws Exception {
        return new DriverDataSource(driver, url, user, password);
    }

    public final void execute() throws MojoExecutionException, MojoFailureException {
        LogFactory.setLogCreator(new MavenLogCreator(this));
        log = LogFactory.getLog(getClass());
        try {
            loadCredentialsFromSettings();

            Flyway flyway = new Flyway();
            flyway.setDataSource(createDataSource());

            String schemasProperty = mavenProject.getProperties().getProperty("flyway.schemas");
            if (schemasProperty != null) {
                flyway.setSchemas(StringUtils.tokenizeToStringArray(schemasProperty, ","));
            } else if (schemas != null) {
                flyway.setSchemas(schemas);
            }
            if (table != null) {
                flyway.setTable(table);
            }

            doExecute(flyway);
        } catch (Exception e) {
            log.error(e.toString());

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                log.error("Caused by " + rootCause.toString());
            }
            throw new MojoExecutionException("Flyway Error: " + e.toString(), e);
        }
    }

    /**
     * Executes this mojo.
     *
     * @param flyway The flyway instance to operate on.
     * @throws Exception any exception
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;
}
