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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

/**
 * Detects whether certain features are available or not.
 */
public final class FeatureDetector {
    private static final Log LOG = LogFactory.getLog(FeatureDetector.class);

    /**
     * Prevent instantiation.
     */
    private FeatureDetector() {
        // Do nothing
    }

    /**
     * Flag indicating availability of the Apache Commons Logging.
     */
    private static Boolean apacheCommonsLoggingAvailable;

    /**
     * Flag indicating availability of Spring JDBC.
     */
    private static Boolean springJdbcAvailable;

    /**
     * Flag indicating availability of JBoss VFS v2.
     */
    private static Boolean jbossVFSv2Available;

    /**
     * Flag indicating availability of JBoss VFS v3.
     */
    private static Boolean jbossVFSv3Available;

    /**
     * Flag indicating availability of the OSGi framework classes.
     */
    private static Boolean osgiFrameworkAvailable;

    /**
     * Checks whether Apache Commons Logging is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isApacheCommonsLoggingAvailable() {
        if (apacheCommonsLoggingAvailable == null) {
            apacheCommonsLoggingAvailable = ClassUtils.isPresent("org.apache.commons.logging.Log");
        }

        return apacheCommonsLoggingAvailable;
    }

    /**
     * Checks whether Spring Jdbc is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isSpringJdbcAvailable() {
        if (springJdbcAvailable == null) {
            springJdbcAvailable = ClassUtils.isPresent("org.springframework.jdbc.core.JdbcTemplate");
            LOG.debug("Spring Jdbc available: " + springJdbcAvailable);
        }

        return springJdbcAvailable;
    }

    /**
     * Checks whether JBoss VFS v2 is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isJBossVFSv2Available() {
        if (jbossVFSv2Available == null) {
            jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS");
            LOG.debug("JBoss VFS v2 available: " + jbossVFSv2Available);
        }

        return jbossVFSv2Available;
    }

    /**
     * Checks whether JBoss VFS is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isJBossVFSv3Available() {
        if (jbossVFSv3Available == null) {
            jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS");
            LOG.debug("JBoss VFS v3 available: " + jbossVFSv3Available);
        }

        return jbossVFSv3Available;
    }

    /**
     * Checks if OSGi framework is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isOsgiFrameworkAvailable() {
        if (osgiFrameworkAvailable == null) {
            osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle");
            LOG.debug("OSGi framework available: " + osgiFrameworkAvailable);
        }

        return osgiFrameworkAvailable;
    }
}
