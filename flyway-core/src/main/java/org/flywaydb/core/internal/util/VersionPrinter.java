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
import org.flywaydb.core.internal.util.line.LineReader;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.nio.charset.Charset;

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
        String version;
        LoadableResource resource = new ClassPathResource("org/flywaydb/core/internal/version.txt",
                VersionPrinter.class.getClassLoader(), Charset.forName("UTF-8"));
        LineReader lineReader = null;
        try {
            lineReader = resource.loadAsString();
            version = lineReader.readLine().getLine();
        } finally {
            IOUtils.close(lineReader);
        }
        LOG.info("Flyway"

                + " Community Edition"










                + " " + version + " by Boxfuse"
        );




    }
}