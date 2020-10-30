/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
     * Flag indicating the availability of Log4J v2
     */
    private Boolean log4J2Available;

    /**
     * Flag indicating availability of the Slf4j.
     */
    private Boolean slf4jAvailable;

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
     * Flag indicating availability of the AWS SDK classes.
     */
    private Boolean awsAvailable;

    /**
     * Flag indicating availability of the Google Cloud Storage SDK classes.
     */
    private Boolean gcsAvailable;

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
     * Checks whether Log4J 2 is available (without a SJF4J - Log4J2 bridge).
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isLog4J2Available() {
        if (log4J2Available == null) {
            log4J2Available = ClassUtils.isPresent("org.apache.logging.log4j.Logger", classLoader);
        }

        return log4J2Available;
    }


    /**
     * Checks whether Slf4j is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
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
            // Use this class' classloader to detect the OSGi framework
            ClassLoader classLoader = FeatureDetector.class.getClassLoader();
            osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle", classLoader);
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

    /**
     * Checks if AWS is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isAwsAvailable() {
        if (awsAvailable == null) {
            awsAvailable = ClassUtils.isPresent("software.amazon.awssdk.services.s3.S3Client", classLoader);
            LOG.debug("AWS SDK available: " + awsAvailable);
        }

        return awsAvailable;
    }

    /**
     * Checks if GCS is available.
     *
     * @return {@code true} if it is, {@code false if it is not}
     */
    public boolean isGCSAvailable() {
        if (gcsAvailable == null) {
            gcsAvailable = ClassUtils.isPresent("com.google.cloud.storage.Storage", classLoader);
            LOG.debug("Google Cloud Storage available: " + gcsAvailable);
        }

        return gcsAvailable;
    }
}