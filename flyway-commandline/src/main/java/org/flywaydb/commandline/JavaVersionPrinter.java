package org.flywaydb.commandline;

import lombok.CustomLog;

@CustomLog
public class JavaVersionPrinter {

    public static void printJavaVersion() {
        LOG.debug("Java Version: " + Runtime.version().toString());
    }
}