/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.gradle.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.net.URL;
import java.util.*;

/**
 * A base class for all flyway tasks.
 */
abstract class AbstractFlywayTask extends DefaultTask {
    /**
     * Property name prefix for placeholders that are configured through System properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The flyway {} block in the build script.
     */
    protected FlywayExtension masterExtension;

    public FlywayExtension taskExtension = new FlywayExtension();

    public AbstractFlywayTask() {
        super();
        setGroup("Flyway");
        masterExtension = (FlywayExtension) getProject().getExtensions().getByName("flyway");
    }

    @TaskAction
    public Object runTask() {
        try {
            if (isJavaProject()) {
                JavaPluginConvention plugin = getProject().getConvention().getPlugin(JavaPluginConvention.class);

                for (SourceSet sourceSet : plugin.getSourceSets()) {
                    URL classesUrl = sourceSet.getOutput().getClassesDir().toURI().toURL();
                    getLogger().debug("Adding directory to Classpath: " + classesUrl);
                    ClassUtils.addJarOrDirectoryToClasspath(UrlUtils.toFilePath(classesUrl));

                    URL resourcesUrl = sourceSet.getOutput().getResourcesDir().toURI().toURL();
                    getLogger().debug("Adding directory to Classpath: " + resourcesUrl);
                    ClassUtils.addJarOrDirectoryToClasspath(UrlUtils.toFilePath(resourcesUrl));
                }

                for (ResolvedArtifact artifact : getProject().getConfigurations().getByName("testRuntime").getResolvedConfiguration().getResolvedArtifacts()) {
                    URL artifactUrl = artifact.getFile().toURI().toURL();
                    getLogger().debug("Adding Dependency to Classpath: " + artifactUrl);
                    ClassUtils.addJarOrDirectoryToClasspath(UrlUtils.toFilePath(artifactUrl));
                }
            }

            NamedDomainObjectContainer container = (NamedDomainObjectContainer) masterExtension.extensions.findByName("databases");
            if (0 == container.size()) {
                try {
                    run(FlywayCreator.create(getProject(), taskExtension, masterExtension, masterExtension));
                } catch (Exception e) {
                    handleException(e);
                }
            } else {
                for (Iterator iterator = container.iterator(); iterator.hasNext();) {
                    FlywayContainer flywayLocal = (FlywayContainer) iterator.next();
                    getLogger().info("Executing ${this.getName()} for ${flywayLocal.name}");
                    try {
                        run(flywayLocal.name, FlywayCreator.create(getProject(), taskExtension, flywayLocal, masterExtension));
                    } catch (Exception e) {
                        throw new FlywayException(
                                "Error occurred while executing ${this.getName()} for ${flywayLocal.name}", e);
                    }
                }
            }

            return null;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Executes the task's custom behavior.
     */
    protected abstract Object run(Flyway flyway);

    protected Object run(String name, Flyway flyway) {
        return this.run(flyway);
    }

    /**
     * @param throwable Throwable instance to be handled
     */
    private void handleException(Throwable throwable) {
        String message = "Error occurred while executing " + getName();
        throw new FlywayException(collectMessages(throwable, message), throwable);
    }

    /**
     * Collect error messages from the stack trace
     *
     * @param throwable Throwable instance from which the message should be build
     * @param message   the message to which the error message will be appended
     * @return a String containing the composed messages
     */
    private String collectMessages(Throwable throwable, String message) {
        if (throwable != null) {
            message += "\n" + throwable.getMessage();
            return collectMessages(throwable.getCause(), message);
        }
        return message;
    }

    private boolean isJavaProject() {
        return getProject().getPluginManager().hasPlugin("java");
    }
}
