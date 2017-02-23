/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

/**
 * Prints the Flyway version.
 */
public class VersionPrinter {
    private static final Log LOG = LogFactory.getLog(VersionPrinter.class);
    private static boolean printed;

    /**
     * Prevents instantiation.
     */
    private VersionPrinter() {
        // Do nothing.
    }

    /**
     * Prints the Flyway version.
     */
    public static void printVersion() {
        if (printed) {
            return;
        }
        printed = true;
        String version = new ClassPathResource("org/flywaydb/core/internal/version.txt", VersionPrinter.class.getClassLoader()).loadAsString("UTF-8");
        LOG.info("Flyway " + version + " by Boxfuse");
    }
}
