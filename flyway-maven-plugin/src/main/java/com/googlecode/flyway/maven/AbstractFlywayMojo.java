/**
 * Copyright (C) 2010-2011 the original author or authors.
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
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.pyx4j.log4j.MavenLogAppender;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.util.StringUtils;

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
     * The user to use to connect to the database.<br> default property: ${flyway.user}
     *
     * @parameter expression="${flyway.user}"
     * @required
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
     * Creates the datasource base on the provided parameters.
     *
     * @return The fully configured datasource.
     * @throws Exception Thrown when the datasource could not be created.
     */
    /* private -> for testing */ BasicDataSource createDataSource() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenLogAppender.startPluginLog(this);
        try {
            Flyway flyway = new Flyway();

            BasicDataSource dataSource = createDataSource();
            try {
                flyway.setDataSource(dataSource);
                if (schemas != null) {
                    flyway.setSchemas(StringUtils.tokenizeToStringArray(schemas, ","));
                }
                if (table != null) {
                    flyway.setTable(table);
                }

                doExecute(flyway);
            } finally {
                dataSource.close();
            }
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
