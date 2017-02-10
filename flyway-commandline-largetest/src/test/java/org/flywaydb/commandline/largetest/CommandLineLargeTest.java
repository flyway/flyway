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
package org.flywaydb.commandline.largetest;

import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.FileCopyUtils;
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
public class CommandLineLargeTest {
    /**
     * Execute 1 (SQL), 1.1 (SQL) & 1.3 (Jdbc). 1.2 (Spring Jdbc) is not picked up.
     */
    @Test
    public void migrate() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate");
        assertTrue(stdOut.contains("Successfully applied 4 migrations"));
    }

    @Test
    public void multipleCommands() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "clean", "migrate");
        assertTrue(stdOut.contains("Successfully cleaned schema"));
        assertTrue(stdOut.contains("Successfully applied 4 migrations"));
    }

    @Test
    public void showUsage() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, null);
        assertTrue(stdOut.contains("Usage"));
        assertTrue(stdOut.contains("callback"));
    }
    
    @Test
    public void quietMode() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, null, "-q");
        assertTrue(stdOut.isEmpty());
    }

    @Test
    public void migrateWithCustomLocations() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate", "-locations=filesystem:sql/migrations", "-resolvers=");
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    @Test
    public void infoWithCallback() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "info", "-callbacks=org.flywaydb.sample.callback.DefaultFlywayCallback");
        assertTrue(stdOut.contains("beforeInfo"));
        assertTrue(stdOut.contains("afterInfo"));
    }

    @Test
    public void exitCodeForFailedMigration() throws Exception {
        String stdOut = runFlywayCommandLine(1, "largeTest.properties", "migrate", "-locations=filesystem:sql/invalid");
        assertTrue(stdOut.contains("Migration of schema \"PUBLIC\" to version 1 - Invalid failed!"));
        assertTrue(stdOut.contains("17"));
        assertTrue(stdOut.contains("InVaLiD SqL !!!"));
    }

    @Test
    public void sqlFolderRoot() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, "migrate", "-user=SA", "-url=jdbc:hsqldb:mem:flyway_db", "-jarDirs=jardir",
                "-driver=org.hsqldb.jdbcDriver", "-sqlMigrationPrefix=Mig", "-resolvers=");
        assertTrue(stdOut.contains("777"));
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    @Test
    public void jarFile() throws Exception {
        String stdOut = runFlywayCommandLine(0, null, "migrate", "-user=SA", "-url=jdbc:hsqldb:mem:flyway_db", "-jarDirs=jardir",
                "-driver=org.hsqldb.jdbcDriver", "-locations=db/migration,org/flywaydb/sample/migration", "-resolvers=");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
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
        args.add(installDir + "/flyway" + flywayCmdLineExtensionForCurrentSystem());

        if (operation != null) {
            args.add(operation);
        }
        if (configFileName != null) {
            String configFile = new ClassPathResource(configFileName, Thread.currentThread().getContextClassLoader()).getLocationOnDisk();
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

        // Normalize line endings across platforms
        return stdOut.replace("\r", "");
    }

    /**
     * Extension for the current systems batch file.
     *
     * @return returns cmd for windows systems, sh for other systems
     */
    private String flywayCmdLineExtensionForCurrentSystem() {
        String osName = System.getProperty("os.name", "generic").toLowerCase();
        if (osName.startsWith("windows")) {
            return ".cmd";
        }
        return "";
    }

    /**
     * @return The installation directory of the Flyway Command Line instance to test.
     */
    private String getInstallDir() {
        return System.getProperty("installDir",
                "flyway-commandline-largetest/target/install dir/flyway-" + getPomVersion());
    }

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
