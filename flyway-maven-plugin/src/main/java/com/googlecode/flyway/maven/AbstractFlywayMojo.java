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

import com.pyx4j.log4j.MavenLogAppender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

/**
 * Description.<br>
 */
abstract class AbstractFlywayMojo extends AbstractMojo {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AbstractFlywayMojo.class);

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br>
     * default property: ${flyway.driver}
     *
     * @parameter default-value="${flyway.driver}"
     * @required
     */
    protected String driver = null;

    /**
     * The jdbc url to use to connect to the database.<br>
     * default property: ${flyway.url}
     *
     * @parameter default-value="${flyway.url}"
     * @required
     */
    protected String url;

    /**
     * The user to use to connect to the database.<br>
     * default property: ${flyway.user}
     *
     * @parameter default-value="${flyway.user}"
     * @required
     */
    protected String user;

    /**
     * The password to use to connect to the database.<br>
     * default property: ${flyway.password}
     *
     * @parameter default-value="${flyway.password}"
     */
    protected String password = "";

    protected DataSource getDataSource() throws MojoExecutionException {
        try {
            Driver driverClazz = (Driver) Class.forName(driver).newInstance();
            return new SimpleDriverDataSource(driverClazz, url, user, password);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find driver class: " + driver, e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("Unable to instantiate driver class: " + driver, e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Unable to access driver class: " + driver, e);
        }
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenLogAppender.startPluginLog(this);
        try {
            doExecute();
        } catch (Exception e) {
            LOG.error(e);
            throw new MojoExecutionException("Flyway Error: " + e.getMessage(), e);
        } finally {
            MavenLogAppender.endPluginLog(this);
        }
    }

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    protected abstract void doExecute() throws Exception;
}
