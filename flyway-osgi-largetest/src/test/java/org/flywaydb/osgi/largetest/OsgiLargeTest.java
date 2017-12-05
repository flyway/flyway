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
package org.flywaydb.osgi.largetest;

import org.flywaydb.core.internal.util.FileCopyUtils;
import org.junit.Test;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Command-Line Tool.
 */
public class OsgiLargeTest {
    @Test
    public void migrate() throws Exception {
        String stdOut = runFlywayOsgi(0);
        assertTrue(stdOut.contains("New schema version: 1.3"));
    }

    /**
     * Runs Flyway in the Equinox OSGi container.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @return The standard output produced by the container.
     * @throws Exception thrown when the invocation failed.
     */
    private String runFlywayOsgi(int expectedReturnCode) throws Exception {
        List<String> args = new ArrayList<String>();

        String installDir = new File(getInstallDir()).getAbsolutePath();
        addShellIfNeeded(args);
        args.add(installDir + "/run." + batchFileExtensionForCurrentSystem());

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(installDir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }

    private void addShellIfNeeded(List<String> args) {
        if (!isWindowsOs()) {
            args.add("sh");
        }
    }

    /**
     * Extension for the current systems batch file.
     *
     * @return returns cmd for windows systems, sh for other systems
     */
    private String batchFileExtensionForCurrentSystem() {
        return isWindowsOs() ? "cmd" : "sh";
    }

    /**
     * Check if we are running on Windows OS
     *
     * @return true for windows, otherwise false
     */
    private boolean isWindowsOs() {
        return System.getProperty("os.name", "generic").toLowerCase().startsWith("windows");
    }

    /**
     * @return The installation directory of the OSGi bundles to test.
     */
    private String getInstallDir() {
        return System.getProperty("installDir", "flyway-osgi-largetest/target/install");
    }
}
