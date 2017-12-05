/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
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
        LOG.info("Flyway"
                //[community-only]
                //+ " Community Edition"
                //[/community-only]
                //[pro-only]
                //+ " Pro Edition"
                //[/pro-only]
                //[enterprise-only]
                //+ " Enterprise Edition"
                //[/enterprise-only]
                //[trial-only]
                //+ " Trial Edition"
                //[/trial-only]
                + " " + version + " by Boxfuse"
        );
        //[trial-only]
        //LOG.warn("You are using the 30 day limited Flyway Trial Edition. After 30 days you must remove this version" +
        //        " and either upgrade to Flyway Pro Edition or Flyway Enterprise Edition or downgrade to Flyway Community Edition.");
        //[/trial-only]
    }
}
