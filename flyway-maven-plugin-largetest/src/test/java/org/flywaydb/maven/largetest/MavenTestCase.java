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
package org.flywaydb.maven.largetest;

import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;
import org.flywaydb.core.internal.util.FileCopyUtils;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Maven Plugin.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class MavenTestCase {
    private String installDir = System.getProperty("installDir", "flyway-maven-plugin-largetest/target");

    /**
     * The installation directory for the test POMs.
     */
    private String pomInstallDir = new File(installDir + "/test-classes").getAbsolutePath();

    @Test
    public void regular() throws Exception {
        String stdOut = runMaven(0, "regular", "clean", "compile", "flyway:baseline", "flyway:info", "-Dflyway.initVersion=0.1", "-Dflyway.user=SA");
        assertTrue(stdOut.contains("<< Flyway Baseline >>"));
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runMaven(0, "regular", "clean", "compile", "flyway:migrate", "-Dflyway.user=SA");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    @Test
    public void executions() throws Exception {
        String stdOut = runMaven(0, "executions", "clean", "install", "-Dflyway.user=SA");
        assertTrue(stdOut.contains("[INFO] Successfully cleaned schema \"PUBLIC\""));
        assertTrue(stdOut.contains("[echo] Property: flyway.current = 1.1"));
    }

    @Test
    public void sample() throws Exception {
        String stdOut = runMaven(0, "sample", "clean", "compile", "flyway:clean", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 5 migrations"));
    }

    @Test
    public void configFile() throws Exception {
        String stdOut = runMaven(0, "configfile", "clean", "compile", "flyway:clean", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 5 migrations"));
    }

    @Test
    public void configFileInvalid() throws Exception {
        String stdOut = runMaven(1, "configfile", "-Dflyway.configFile=test.properties", "flyway:info");
        assertTrue(stdOut.contains("Unable to read config file"));
    }

    @Test
    public void settings() throws Exception {
        String stdOut = runMaven(0, "settings", "clean", "compile", "flyway:baseline", "flyway:info", "-s", pomInstallDir + "/settings/settings.xml");
        assertTrue(stdOut.contains("<< Flyway Baseline >>"));
    }

    /**
     * Tests the use of settings.xml with a default server id.
     */
    @Test
    public void settingsDefault() throws Exception {
        String stdOut = runMaven(0, "settings-default", "clean", "compile", "flyway:baseline", "flyway:info", "-s", pomInstallDir + "/settings-default/settings.xml");
        assertTrue(stdOut.contains("<< Flyway Baseline >>"));
    }

    /**
     * Tests the use of settings.xml with an encrypted password.
     */
    @Test
    public void settingsEncrypted() throws Exception {
        String dir = pomInstallDir + "/settings-encrypted";
        String stdOut = runMaven(0, "settings-encrypted", "clean", "sql:execute", "flyway:baseline",
                "-s=" + dir + "/settings.xml",
                "-Dsettings.security=" + dir + "/settings-security.xml");
        assertTrue(stdOut.contains("Successfully baselined schema with version: 1"));
    }

    @Test
    public void locationsElements() throws Exception {
        String stdOut = runMaven(0, "locations-elements", "clean", "compile", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void locationsProperty() throws Exception {
        String stdOut = runMaven(0, "locations-property", "clean", "compile", "flyway:migrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
    }

    @Test
    public void callbacksProperty() throws Exception {
        String stdOut = runMaven(0, "callbacks-property", "clean", "compile", "flyway:info");
        assertTrue(stdOut.contains("beforeInfo"));
        assertTrue(stdOut.contains("afterInfo"));
    }

    @Test
    public void skip() throws Exception {
        String stdOut = runMaven(0, "skip", "flyway:migrate");
        assertTrue(stdOut.contains("Skipping Flyway execution"));
    }

    /**
     * Runs Maven in this directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param dir                The directory below src/test/resources to run maven in.
     * @param extraArgs          The extra arguments (if any) for Maven.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    private String runMaven(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        String flywayVersion = System.getProperty("flywayVersion", getPomVersion());

        String extension = "";
        if (System.getProperty("os.name").startsWith("Windows")) {
            extension = ".bat";
        }

        String mavenHome = installDir + "/install/apache-maven-" + getMavenVersion();

        List<String> args = new ArrayList<String>();
        args.add(mavenHome + "/bin/mvn" + extension);
        args.add("-Dflyway.version=" + flywayVersion);
        args.add("-X");
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(pomInstallDir + "/" + dir));
        builder.redirectErrorStream(true);
        builder.environment().put("M2_HOME", mavenHome);

        System.out.println("Executing: " + StringUtils.collectionToDelimitedString(builder.command(), " "));

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
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

    /**
     * @return The Maven version to test against.
     */
    protected abstract String getMavenVersion();
}
