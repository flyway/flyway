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
package org.flywaydb.ant;

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
 * Large Test for the Ant tasks.
 */
@SuppressWarnings({"JavaDoc"})
public class AntLargeTest {
    @Test
    public void baseline() throws Exception {
        String stdOut = runAnt(0, "baseline");
        assertTrue(stdOut.contains("A new beginning!"));
    }

    @Test
    public void callbackProperty() throws Exception {
        String stdOut = runAnt(0, "callback-property");
        assertTrue(stdOut.contains("No migrations found"));
        assertTrue(stdOut.contains("beforeInfo"));
        assertTrue(stdOut.contains("afterInfo"));
    }

    /**
     * Tests finding migrations in different jar files.
     */
    @Test
    public void jars() throws Exception {
        String stdOut = runAnt(0, "jars");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertTrue(stdOut.contains("Populate table"));
    }

    /**
     * Tests the migrations with multiple schemas.
     */
    @Test
    public void multi() throws Exception {
        String stdOut = runAnt(0, "multi");
        assertTrue(stdOut.contains("Successfully cleaned schema \"FLYWAY_1\""));
        assertTrue(stdOut.contains("Successfully cleaned schema \"FLYWAY_2\""));
        assertTrue(stdOut.contains("Successfully cleaned schema \"FLYWAY_3\""));
        assertTrue(stdOut.contains("Creating Metadata table: \"FLYWAY_1\".\"MASTER_OF_THE_VERSIONS\""));
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    @Test
    public void status() throws Exception {
        String stdOut = runAnt(0, "info");
        assertTrue(stdOut.contains("No migrations found"));
    }

    @Test
    public void locationsElement() throws Exception {
        String stdOut = runAnt(0, "locations-element");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void locationsProperty() throws Exception {
        String stdOut = runAnt(0, "locations-property");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runAnt(0, "", "-f", "migrate/build.xml");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    /**
     * Runs Ant in this directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param dir       The directory below src/test/resources to run maven in.
     * @param extraArgs The extra arguments (if any) for Maven.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    protected String runAnt(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-Dant.home=" + getInstallDir() + "/ant");
        args.add("-cp");
        args.add(getInstallDir() + "/ant/*");
        args.add("org.apache.tools.ant.launch.Launcher");
        // enable for debug
        // args.add("-d");
        args.add("clean");
        args.add("run");
        args.add("-DinstallDir=" + new File(getInstallDir()).getAbsolutePath());
        args.add("-DpomVersion=" + System.getProperty("pomVersion", getPomVersion()));
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(getInstallDir() + "/tests/" + dir).getAbsoluteFile());
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }

    /**
     * @return The installation directory of the Flyway Command Line instance to test.
     */
    private String getInstallDir() {
        return System.getProperty("installDir", "flyway-ant-largetest/target/install dir");
    }

    /**
     * Execute 1 (SQL), 1.1 (SQL) & 1.3 (Jdbc). 1.2 (Spring Jdbc) is not picked up.
     */
    @Test
    public void sample() throws Exception {
        String stdOut = runAnt(0, "sample");
        assertTrue(stdOut.contains("Successfully applied 4 migrations"));
    }

    /**
     * Retrieves the version embedded in the project pom. Useful for running these tests in IntelliJ.
     *
     * @return The POM version.
     */
    private String getPomVersion() {
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
