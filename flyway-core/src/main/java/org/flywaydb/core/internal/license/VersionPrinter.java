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
package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.line.LineReader;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * Prints the Flyway version.
 */
public class VersionPrinter {
    private static final Log LOG = LogFactory.getLog(VersionPrinter.class);
    private static final String version = readVersion();
    private static boolean printed;

    public static final Edition EDITION =

            Edition.COMMUNITY










            ;

    /**
     * Prevents instantiation.
     */
    private VersionPrinter() {
        // Do nothing.
    }

    /**
     * Prints the Flyway version.
     */
    public static void printVersion(



    ) {
        if (printed) {
            return;
        }
        printed = true;


        printVersionOnly();












    }

    public static void printVersionOnly() {
        LOG.info(EDITION + " " + version + " by Boxfuse");
    }
































    private static String readVersion() {
        String version;
        LoadableResource resource = new ClassPathResource(null,
                "org/flywaydb/core/internal/version.txt",
                VersionPrinter.class.getClassLoader(), Charset.forName("UTF-8"));
        LineReader lineReader = null;
        try {
            lineReader = resource.loadAsString();
            version = lineReader.readLine().getLine();
        } finally {
            IOUtils.close(lineReader);
        }
        return version;
    }
}