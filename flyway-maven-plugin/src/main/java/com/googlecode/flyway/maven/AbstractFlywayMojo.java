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
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.pyx4j.log4j.MavenLogAppender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Driver;

/**
 * Common base class for all mojos with all common attributes.<br>
 *
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 */
@SuppressWarnings({"JavaDoc"})
abstract class AbstractFlywayMojo extends AbstractMojo {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AbstractFlywayMojo.class);

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br> default property:
     * ${flyway.driver}
     *
     * @parameter expression="${flyway.driver}"
     * @required
     */
    /* private -> for testing */ String driver;

    /**
     * The jdbc url to use to connect to the database.<br> default property: ${flyway.url}
     *
     * @parameter expression="${flyway.url}"
     * @required
     */
    /* private -> for testing */ String url;

    /**
     * The user to use to connect to the database.<br> default property: ${flyway.user}<br>
     * The credentials can be specified by user/password or serverId from settings.xml
     *
     * @parameter expression="${flyway.user}"
     */
    /* private -> for testing */ String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)<br> default property: ${flyway.password}
     *
     * @parameter expression="${flyway.password}"
     */
    private String password = "";

    /**
     * Comma-separated list of the schemas managed by Flyway. The first schema in the list will be the one containing
     * the metadata table. (default: The default schema for the datasource connection)<br> default property:
     * ${flyway.schemas}
     *
     * @parameter expression="${flyway.schemas}"
     */
    private String schemas;

    /**
     * <p>The name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode) the
     * metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When the
     * <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema of
     * the list. </p> (default: schema_version)<br> default property: ${flyway.table}
     *
     * @parameter expression="${flyway.table}"
     */
    private String table;


    /**
     * The link to the settings.xml
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * The id of the server tag in settings.xml<br>default: flyway-db<br>
     * The credentials can be specified by user/password or serverId from settings.xml<br> default property:
     * ${flyway.serverId}
     *
     * @parameter expression="${flyway.serverId}"
     */
    private String serverId = "flyway-db";

    /**
     * Load username password from settings
     *
     * @throws FlywayException when the credentials could not be loaded.
     */
    private void loadCredentialsFromSettings() throws FlywayException {
        if (user == null) {
            final Server server = settings.getServer(serverId);
            if (server == null) {
                throw new FlywayException(String.format("Database username missing. It was not specified as a property" +
                        " and it was not defined in settings.xml for the server with the id '%s'", serverId));
            }
            user = server.getUsername();
            password = server.getPassword();
        }
    }

    /**
     * Creates the datasource based on the provided parameters.
     *
     * @return The fully configured datasource.
     * @throws Exception Thrown when the datasource could not be created.
     */
    /* private -> for testing */ DataSource createDataSource() throws Exception {
        DriverDataSource dataSource = new DriverDataSource();
        dataSource.setDriver(ClassUtils.<Driver>instantiate(driver));
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenLogAppender.startPluginLog(this);
        try {
            loadCredentialsFromSettings();

            Flyway flyway = new Flyway();
            flyway.setDataSource(createDataSource());
            if (schemas != null) {
                flyway.setSchemas(StringUtils.tokenizeToStringArray(schemas, ","));
            }
            if (table != null) {
                flyway.setTable(table);
            }

            doExecute(flyway);
        } catch (Exception e) {
            LOG.error(e.toString());

            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }
            throw new MojoExecutionException("Flyway Error: " + e.toString(), e);
        } finally {
            MavenLogAppender.endPluginLog(this);
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
