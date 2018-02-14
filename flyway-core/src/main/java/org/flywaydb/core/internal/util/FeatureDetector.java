/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

/**
 * Detects whether certain features are available or not.
 */
public final class FeatureDetector {
    private static final Log LOG = LogFactory.getLog(FeatureDetector.class);

    /**
     * The ClassLoader to use.
     */
    private ClassLoader classLoader;

    /**
     * Creates a new FeatureDetector.
     *
     * @param classLoader The ClassLoader to use.
     */
    public FeatureDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Flag indicating availability of the Apache Commons Logging.
     */
    private Boolean apacheCommonsLoggingAvailable;

    /**
     * Flag indicating availability of the Slf4j.
     */
    private Boolean slf4jAvailable;

    /**
     * Flag indicating availability of Spring JDBC.
     */
    private Boolean springJdbcAvailable;

    /**
     * Flag indicating availability of JBoss VFS v2.
     */
    private Boolean jbossVFSv2Available;

    /**
     * Flag indicating availability of JBoss VFS v3.
     */
    private Boolean jbossVFSv3Available;

    /**
     * Flag indicating availability of the OSGi framework classes.
     */
    private Boolean osgiFrameworkAvailable;

    /**
     * Flag indicating availability of the Android classes.
     */
    private Boolean androidAvailable;

    /**
     * Checks whether Apache Commons Logging is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isApacheCommonsLoggingAvailable() {
        if (apacheCommonsLoggingAvailable == null) {
            apacheCommonsLoggingAvailable = ClassUtils.isPresent("org.apache.commons.logging.Log", classLoader);
        }

        return apacheCommonsLoggingAvailable;
    }

    /**
     * Checks whether Slf4j is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isSlf4jAvailable() {
        if (slf4jAvailable == null) {
            slf4jAvailable = ClassUtils.isPresent("org.slf4j.Logger", classLoader);
        }

        return slf4jAvailable;
    }

    /**
     * Checks whether Spring Jdbc is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isSpringJdbcAvailable() {
        if (springJdbcAvailable == null) {
            springJdbcAvailable = ClassUtils.isPresent("org.springframework.jdbc.core.JdbcTemplate", classLoader);
            LOG.debug("Spring Jdbc available: " + springJdbcAvailable);
        }

        return springJdbcAvailable;
    }

    /**
     * Checks whether JBoss VFS v2 is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isJBossVFSv2Available() {
        if (jbossVFSv2Available == null) {
            jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS", classLoader);
            LOG.debug("JBoss VFS v2 available: " + jbossVFSv2Available);
        }

        return jbossVFSv2Available;
    }

    /**
     * Checks whether JBoss VFS is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isJBossVFSv3Available() {
        if (jbossVFSv3Available == null) {
            jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS", classLoader);
            LOG.debug("JBoss VFS v3 available: " + jbossVFSv3Available);
        }

        return jbossVFSv3Available;
    }

    /**
     * Checks if OSGi framework is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isOsgiFrameworkAvailable() {
        if (osgiFrameworkAvailable == null) {
            osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle", FeatureDetector.class.getClassLoader());
            LOG.debug("OSGi framework available: " + osgiFrameworkAvailable);
        }

        return osgiFrameworkAvailable;
    }

    /**
     * Checks if Android is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isAndroidAvailable() {
        if (androidAvailable == null) {
            androidAvailable = "Android Runtime".equals(System.getProperty("java.runtime.name"));
        }

        return androidAvailable;
    }
}