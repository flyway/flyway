/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.api.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.logging.EvolvingLog;
import org.flywaydb.core.internal.logging.apachecommons.ApacheCommonsLogCreator;
import org.flywaydb.core.internal.logging.buffered.BufferedLogCreator;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.flywaydb.core.internal.logging.log4j2.Log4j2LogCreator;
import org.flywaydb.core.internal.logging.multi.MultiLogCreator;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for loggers. Custom MigrationResolver, MigrationExecutor, Callback and JavaMigration
 * implementations should use this to obtain a logger that will work with any logging framework across all environments
 * (API, Maven, Gradle, CLI, etc.).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogFactory {
    /**
     * Factory for implementation-specific loggers.
     * -- SETTER --
     * Sets the LogCreator that will be used. This will effectively override Flyway's default LogCreator auto-detection
     * logic and force Flyway to always use this LogCreator regardless of which log libraries are present on the
     * classpath.
     *
     * This is primarily meant for integrating Flyway into environments with their own logging system (like Ant,
     * Gradle, Maven, ...). This ensures Flyway is a good citizen in those environments and sends its logs through the
     * expected pipeline.
     *
     * @param logCreator The factory for implementation-specific loggers.
     */
    @Setter(onMethod = @__(@Synchronized))
    private static volatile LogCreator logCreator;
    /**
     * The factory for implementation-specific loggers to be used as a fallback when no other suitable loggers were found.
     * -- SETTER --
     * Sets the fallback LogCreator. This LogCreator will be used as a fallback solution when the default LogCreator
     * auto-detection logic fails to detect a suitable LogCreator based on the log libraries present on the classpath.
     *
     * @param fallbackLogCreator The factory for implementation-specific loggers to be used as a fallback when no other
     *                           suitable loggers were found.
     */
    @Setter(onMethod = @__(@Synchronized))
    private static LogCreator fallbackLogCreator;
    private static Configuration configuration;

    @Synchronized
    public static void setConfiguration(Configuration configuration) {
        LogFactory.configuration = configuration;
        logCreator = null;
    }

    /**
     * Retrieves the matching logger for this class.
     *
     * @param clazz The class to get the logger for.
     * @return The logger.
     */
    @Synchronized
    public static Log getLog(Class<?> clazz) {
        if (logCreator == null) {
            logCreator = getLogCreator(LogFactory.class.getClassLoader(), fallbackLogCreator);
        }

        return new EvolvingLog(logCreator.createLogger(clazz), clazz);
    }

    private static LogCreator getLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
        if (configuration == null) {
            return new BufferedLogCreator();
        }

        String[] loggers = configuration.getLoggers();
        List<LogCreator> logCreators = new ArrayList<>();
        
        for (String logger : loggers) {
            switch (logger.toLowerCase()) {
                case "auto":
                    logCreators.add(autoDetectLogCreator(classLoader, fallbackLogCreator));
                    break;
                case "maven":
                case "console":
                    logCreators.add(fallbackLogCreator);
                    break;
                case "slf4j":
                    logCreators.add(ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader));
                    break;
                case "log4j2":
                    logCreators.add(ClassUtils.instantiate(Log4j2LogCreator.class.getName(), classLoader));
                    break;
                case "apache-commons":
                    logCreators.add(ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader));
                    break;
                default:
                    logCreators.add(ClassUtils.instantiate(logger, classLoader));
            }
        }

        return new MultiLogCreator(logCreators);
    }

    private static LogCreator autoDetectLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isSlf4jAvailable()) {
            return ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isLog4J2Available()) {
            return ClassUtils.instantiate(Log4j2LogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isApacheCommonsLoggingAvailable()) {
            return ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader);
        }
        if (fallbackLogCreator == null) {
            return new JavaUtilLogCreator();
        }
        return fallbackLogCreator;
    }
}