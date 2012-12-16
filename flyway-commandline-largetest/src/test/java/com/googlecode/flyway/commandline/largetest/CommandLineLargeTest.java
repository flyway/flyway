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

import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.FileCopyUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
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
public abstract class CommandLineLargeTest {
    @Test
    public void showUsage() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, null);
        assertTrue(stdOut.contains("* Usage"));
    }

    @Test
    public void migrateWithCustomLocations() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate", "-locations=migrations");
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    @Test
    public void exitCodeForFailedMigration() throws Exception {
        String stdOut = runFlywayCommandLine(1, "largeTest.properties", "migrate", "-locations=invalid");
        assertTrue(stdOut.contains("Migration of schema \"PUBLIC\" to version 1 failed!"));
    }

    @Test
    public void sqlFolderRoot() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, "migrate", "-user=SA", "-url=jdbc:hsqldb:mem:flyway_db", "-driver=org.hsqldb.jdbcDriver", "-sqlMigrationPrefix=Mig");
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
    protected String runFlywayCommandLine(int expectedReturnCode, String configFileName, String operation, String... extraArgs) throws Exception {
        List<String> args = new ArrayList<String>();

        String installDir = new File(getInstallDir()).getAbsolutePath();
        args.add(installDir + "/flyway." + flywayCmdLineExtensionForCurrentSystem());

        if (operation != null) {
            args.add(operation);
        }
        if (configFileName != null) {
            String configFile = new ClassPathResource(configFileName).getLocationOnDisk();
            args.add("-configFile=" + configFile);
        }
        args.addAll(Arrays.asList(extraArgs));

        //Debug mode
        args.add("-X");

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

    /**
     * @return The installation directory of the Flyway Command Line instance to test.
     */
    protected abstract String getInstallDir();

    /**
     * Retrieves the version embedded in the project pom. Useful for running these tests in IntelliJ.
     *
     * @return The POM version.
     */
    protected String getPomVersion() {
        try {
            File pom = new File("pom.xml");
            if (!pom.exists()) {
                return "unknown";
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(pom);
            return xPath.evaluate("/project/version", document);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read POM version", e);
        }
    }
}
