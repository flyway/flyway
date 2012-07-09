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
package com.googlecode.flyway.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * Flag indicating availability of Spring JDBC.
     */
    private static Boolean springJdbcAvailable;

    /**
     * Flag indicating availability of JBoss VFS.
     */
    private static Boolean jbossVfsAvailable;

    /**
     * Flag indicating availability of the Equinox Common OSGi Bundle.
     */
    private static Boolean equinoxCommonAvailable;

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
     * Checks whether JBoss VFS is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isJBossVFSAvailable() {
        if (jbossVfsAvailable == null) {
            jbossVfsAvailable = ClassUtils.isPresent("org.jboss.vfs.VFS");
            LOG.debug("JBoss VFS available: " + jbossVfsAvailable);
        }

        return jbossVfsAvailable;
    }

    /**
     * Checks whether the Equinox Common OSGi Bundle is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public static boolean isEquinoxCommonAvailable() {
        if (equinoxCommonAvailable == null) {
            equinoxCommonAvailable = ClassUtils.isPresent("org.eclipse.core.runtime.FileLocator");
            LOG.debug("Equinox Common OSGi Bundle available: " + equinoxCommonAvailable);
        }

        return equinoxCommonAvailable;
    }
}
