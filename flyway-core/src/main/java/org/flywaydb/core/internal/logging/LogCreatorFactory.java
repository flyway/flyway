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
package org.flywaydb.core.internal.logging;

import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.logging.android.AndroidLogCreator;
import org.flywaydb.core.internal.logging.apachecommons.ApacheCommonsLogCreator;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;

public class LogCreatorFactory {
    /**
     * Prevent instantiation.
     */
    private LogCreatorFactory() {
        // Do nothing
    }

    public static LogCreator getLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isAndroidAvailable()) {
            return ClassUtils.instantiate(AndroidLogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isSlf4jAvailable()) {
            return ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isApacheCommonsLoggingAvailable()) {
            return ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader);
        }
        if (fallbackLogCreator == null) {
            return ClassUtils.instantiate(JavaUtilLogCreator.class.getName(), classLoader);
        }
        return fallbackLogCreator;
    }
}