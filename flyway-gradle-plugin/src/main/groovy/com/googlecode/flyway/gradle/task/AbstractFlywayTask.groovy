/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.gradle.task

import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.api.FlywayException
import com.googlecode.flyway.core.util.StringUtils
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.gradle.FlywayExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A base class for all flyway tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 * @author Allan Morstein (alkamo@gmail.com)
 */
abstract class AbstractFlywayTask extends DefaultTask {
    /**
     * Property name prefix for placeholders that are configured through System properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders."

    /**
     * The flyway {} block in the build script.
     */
    private FlywayExtension extension

    AbstractFlywayTask() {
        group = 'Flyway'
        project.afterEvaluate {
            if (isJavaProject()) {
                this.dependsOn(project.tasks.testClasses)
            }
        }
        extension = project.flyway
    }

    @TaskAction
    def runTask() {
        if (isJavaProject()) {
            def classLoader = Thread.currentThread().getContextClassLoader()
            project.sourceSets.each {
                def classesUrl = it.output.classesDir.toURI().toURL()
                logger.debug("Adding directory to Classpath: " + classesUrl)
                classLoader.addURL(classesUrl)

                def resourcesUrl = it.output.resourcesDir.toURI().toURL()
                logger.debug("Adding directory to Classpath: " + resourcesUrl)
                classLoader.addURL(resourcesUrl)
            }
            project.configurations.getByName('testRuntime').resolvedConfiguration.resolvedArtifacts.each { artifact ->
                def artifactUrl = artifact.file.toURI().toURL()
                logger.debug("Adding Dependency to Classpath: " + artifactUrl)
                classLoader.addURL(artifactUrl)
            }
        }

        try {
            run(createFlyway())
        } catch (Exception e) {
            throw new FlywayException("Error occurred while executing ${this.getName()}", e);
        }
    }

    /** Executes the task's custom behavior. */
    def abstract run(Flyway flyway)

    /** Creates a new, configured flyway instance */
    protected def createFlyway() {
        def flyway = new Flyway()
        flyway.setDataSource(new DriverDataSource(prop("driver"), prop("url"), prop("user"), prop("password")))

        propSet(flyway, 'table')
        propSet(flyway, 'initVersion')
        propSet(flyway, 'initDescription')
        propSet(flyway, 'sqlMigrationPrefix')
        propSet(flyway, 'sqlMigrationSuffix')
        propSet(flyway, 'encoding')
        propSet(flyway, 'placeholderPrefix')
        propSet(flyway, 'placeholderSuffix')
        propSet(flyway, 'target')
        propSetAsBoolean(flyway, 'outOfOrder')
        propSetAsBoolean(flyway, 'validateOnMigrate')
        propSetAsBoolean(flyway, 'cleanOnValidationError')
        propSetAsBoolean(flyway, 'initOnMigrate')

        def sysSchemas = System.getProperty("flyway.schemas")
        if (sysSchemas != null) {
            flyway.schemas = StringUtils.tokenizeToStringArray(sysSchemas, ",")
        } else if (project.hasProperty("flyway.schemas")) {
            flyway.schemas = StringUtils.tokenizeToStringArray(project["flyway.schemas"].toString(), ",")
        } else if (extension.schemas != null) {
            flyway.schemas = extension.schemas
        }

        def sysLocations = System.getProperty("flyway.locations")
        if (sysLocations != null) {
            flyway.locations = StringUtils.tokenizeToStringArray(sysLocations, ",")
        } else if (project.hasProperty("flyway.locations")) {
            flyway.locations = StringUtils.tokenizeToStringArray(project["flyway.locations"].toString(), ",")
        } else if (extension.locations != null) {
            flyway.locations = extension.locations
        }

        Map<String, String> placeholders = [:]
        System.getProperties().each { String key, String value ->
            if (key.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)) {
                placeholders.put(key.substring(PLACEHOLDERS_PROPERTY_PREFIX), value)
            }
        }
        if (placeholders.isEmpty()) {
            project.properties.keySet().each { String key ->
                if (key.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)) {
                    placeholders.put(key.substring(PLACEHOLDERS_PROPERTY_PREFIX), project.properties[key])
                }
            }
        }
        if (placeholders.isEmpty() && (extension.placeholders != null)) {
            placeholders.putAll(extension.placeholders)
        }
        flyway.placeholders = placeholders

        flyway
    }

    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     */
    private void propSet(Flyway flyway, String property) {
        String value = prop(property);
        if (value != null) {
            // use method call instead of property as it does not work nice with overload GROOVY-6084
            flyway."set${property.capitalize()}"(value)
        }
    }
    /**
     * Sets this property on this Flyway instance if a value has been defined.
     * @param flyway The Flyway instance.
     * @param property The property to set.
     */
    private void propSetAsBoolean(Flyway flyway, String property) {
        String value = prop(property);
        if (value != null) {
            flyway."set${property.capitalize()}"(value.toBoolean())
        }
    }

    /**
     * Retrieves the value of this property, first trying System Properties, then Gradle properties and finally the Flyway extension.
     * @param property The property whose value to get.
     * @return The value. {@code null} if not found.
     */
    private String prop(String property) {
        String propertyName = "flyway.${property}"
        System.getProperty(propertyName) ?: project.hasProperty(propertyName) ? project[propertyName] : extension[property]
    }

    protected boolean isJavaProject() {
        project.plugins.hasPlugin('java')
    }

}
