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
package com.googlecode.flyway.commandline.largetest;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Command-Line Tool.
 */
public class CommandLineLargeTest {
    @Test
    public void showUsage() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, null);
        assertTrue(stdOut.contains("* Usage"));
    }

    @Ignore("Axel: Broken until multiple locations support is implemented.")
    @Test
    public void migrate() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    @Test
    public void migrateWithCustomBaseDir() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate", "-baseDir=migrations", "-basePackage=dummy");
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    @Test
    public void exitCodeForFailedMigration() throws Exception {
        String stdOut = runFlywayCommandLine(1, "largeTest.properties", "migrate", "-baseDir=invalid", "-basePackage=dummy");
        assertTrue(stdOut.contains("Migration to version 1 failed!"));
    }

    @Test
    public void validate() throws Exception {
        String stdOut = runFlywayCommandLine(1, "largeTest.properties", "validate", "-url=jdbc:hsqldb:file:sql/validate/flyway_sample;shutdown=true", "-baseDir=validate", "-basePackage=dummy");
        assertTrue(stdOut.contains("Validate failed. Found differences between applied migrations and available migrations"));
    }

    @Test
    public void sqlFolderRoot() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, "migrate", "-user=SA", "-url=jdbc:hsqldb:mem:flyway_db", "-driver=org.hsqldb.jdbcDriver", "-sqlMigrationPrefix=Mig", "-basePackage=dummy");
        assertTrue(stdOut.contains("777"));
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    /**
     * Runs the Flyway Command Line tool.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param configFileName     The config file name. {@code null} for default.
     * @param operation          The operation {@code null} for none.
     * @param extraArgs          The extra arguments to pass to the tool.
     * @return The standard output produced by the tool.
     * @throws Exception thrown when the invocation failed.
     */
    private String runFlywayCommandLine(int expectedReturnCode, String configFileName, String operation, String... extraArgs) throws Exception {
        List<String> args = new ArrayList<String>();

        String installDir = System.getProperty("installDir");
        args.add(installDir + "/flyway." + flywayCmdLineExtensionForCurrentSystem());

        if (operation != null) {
            args.add(operation);
        }
        if (configFileName != null) {
            String configFile = new ClassPathResource("largeTest.properties").getFile().getPath();
            args.add("-configFile=" + configFile);
        }
        args.addAll(Arrays.asList(extraArgs));

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

    /**
     * Extension for the current systems batch file.
     *
     * @return returns cmd for windows systems, sh for other systems
     */
    private String flywayCmdLineExtensionForCurrentSystem() {
        String osname = System.getProperty("os.name", "generic").toLowerCase();
        if (osname.startsWith("windows")) {
            return "cmd";
        }
        return "sh";
    }
}
