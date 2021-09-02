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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

public final class FeatureDetector {
    private static final Log LOG = LogFactory.getLog(FeatureDetector.class);

    private final ClassLoader classLoader;
    private Boolean apacheCommonsLoggingAvailable;
    private Boolean log4J2Available;
    private Boolean slf4jAvailable;
    private Boolean jbossVFSv2Available;
    private Boolean jbossVFSv3Available;
    private Boolean osgiFrameworkAvailable;
    private Boolean androidAvailable;
    private Boolean awsAvailable;
    private Boolean gcsAvailable;

    public FeatureDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public boolean isApacheCommonsLoggingAvailable() {
        if (apacheCommonsLoggingAvailable == null) {
            apacheCommonsLoggingAvailable = ClassUtils.isPresent("org.apache.commons.logging.Log", classLoader);
        }

        return apacheCommonsLoggingAvailable;
    }

    public boolean isLog4J2Available() {
        if (log4J2Available == null) {
            log4J2Available = ClassUtils.isPresent("org.apache.logging.log4j.Logger", classLoader);
        }

        return log4J2Available;
    }

    public boolean isSlf4jAvailable() {
        if (slf4jAvailable == null) {
            // We need to ensure there's an actual implementation; AWS SDK pulls in the Logger interface but doesn't
            // provide any implementation, causing SLF4J to drop what we want to be console output on the floor.
            // Versions up to 1.7 have a StaticLoggerBinder
            slf4jAvailable = ClassUtils.isPresent("org.slf4j.Logger", classLoader)
                    && ClassUtils.isPresent("org.slf4j.impl.StaticLoggerBinder", classLoader);
            // Versions 1.8 and later use a ServiceLocator to bind to the implementation
            slf4jAvailable |= ClassUtils.isImplementationPresent("org.slf4j.spi.SLF4JServiceProvider", classLoader);
        }

        return slf4jAvailable;
    }

    public boolean isJBossVFSv2Available() {
        if (jbossVFSv2Available == null) {
            jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS", classLoader);
            LOG.debug("JBoss VFS v2 available: " + jbossVFSv2Available);
        }

        return jbossVFSv2Available;
    }

    public boolean isJBossVFSv3Available() {
        if (jbossVFSv3Available == null) {
            jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS", classLoader);
            LOG.debug("JBoss VFS v3 available: " + jbossVFSv3Available);
        }

        return jbossVFSv3Available;
    }

    public boolean isOsgiFrameworkAvailable() {
        if (osgiFrameworkAvailable == null) {
            // Use this class' classloader to detect the OSGi framework
            ClassLoader classLoader = FeatureDetector.class.getClassLoader();
            osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle", classLoader);
            LOG.debug("OSGi framework available: " + osgiFrameworkAvailable);
        }

        return osgiFrameworkAvailable;
    }

    public boolean isAndroidAvailable() {
        if (androidAvailable == null) {
            androidAvailable = "Android Runtime".equals(System.getProperty("java.runtime.name"));
        }

        return androidAvailable;
    }

    public boolean isAwsAvailable() {
        if (awsAvailable == null) {
            awsAvailable = ClassUtils.isPresent("software.amazon.awssdk.services.s3.S3Client", classLoader);
            LOG.debug("AWS SDK available: " + awsAvailable);
        }

        return awsAvailable;
    }

    public boolean isGCSAvailable() {
        if (gcsAvailable == null) {
            gcsAvailable = ClassUtils.isPresent("com.google.cloud.storage.Storage", classLoader);
            LOG.debug("Google Cloud Storage available: " + gcsAvailable);
        }

        return gcsAvailable;
    }

    /**
     * Checks if the "experimental features" flag is set in the environment. You should not activate this flag
     * outside a development environment; features activated may be in development and/or undocumented.
     *
     * @return {@code true} if it is, {@code false} if it is not
     */
    public static boolean areExperimentalFeaturesEnabled() {













        return false;
    }

    public static boolean isRedgateUpdateCheckEnabled() {
        String value = null;

        try {
            value = System.getenv("FLYWAY_REDGATE_UPDATE_CHECK");

            if (value != null) {
                LOG.debug("FLYWAY_REDGATE_UPDATE_CHECK: " + value);
                return Boolean.parseBoolean(value);
            }

        } catch (IllegalArgumentException e) {
            LOG.debug("FLYWAY_REDGATE_UPDATE_CHECK has an illegal value: " + value);
            throw e;
        }

        return false;
    }
}