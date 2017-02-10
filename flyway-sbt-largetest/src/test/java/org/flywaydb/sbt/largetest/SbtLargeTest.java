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
package org.flywaydb.sbt.largetest;

import org.flywaydb.core.internal.util.FileCopyUtils;
import org.junit.Test;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Large Test for the Flyway Sbt Plugin.
 */
@SuppressWarnings({"JavaDoc"})
public class SbtLargeTest {
    private File sbtLaunchJar = new File(System.getProperty("sbtLaunchDir", "../flyway-sbt"), "sbt-launch.jar");
    private File installDir = new File(System.getProperty("installDir", "flyway-sbt-largetest/target"));

    @Test(timeout = 60000)
    public void cleanMigrate() throws Exception {
        String stdOut = runSbt("test1", 0, "-Dflyway.placeholders.name=James", "flywayClean", "flywayMigrate");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    @Test(timeout = 60000)
    public void sysPropsOverride() throws Exception {
        String stdOut = runSbt("test1", 0, "-Dflyway.locations=db/migration", "flywayClean", "flywayMigrate");
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
    }

    @Test(timeout = 60000)
    public void useTestScope() throws Exception {
        String stdOut = runSbt("test1", 0, "-Dflyway.locations=filesystem:src/main/resources/db/migration,filesystem:src/test/resources/db/migration", "test:flywayClean", "test:flywayMigrate");
        assertTrue(stdOut.contains("Successfully applied 2 migration"));
    }

    @Test(timeout = 60000)
    public void flywayUrlAsSysProps() throws Exception {
        String stdOut = runSbt("test2", 0, "-Dflyway.url=jdbc:hsqldb:file:target/flyway_sample;shutdown=true", "flywayClean", "flywayMigrate");
        assertTrue(stdOut.contains("Successfully applied 2 migration"));
    }

    @Test(timeout = 60000)
    public void flywayCallbacks() throws Exception {
        String stdOut = runSbt("test3", 0, "-Dflyway.locations=db/migration", "flywayClean");
        assertTrue(stdOut.contains("beforeClean"));
    }

    /**
     * Runs sbt in install directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param extraArgs          The extra arguments (if any) for Gradle.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    private String runSbt(String dir, int expectedReturnCode, String... extraArgs) throws Exception {
        String root = new File(installDir,  "test-classes/" + dir).getAbsolutePath();

        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-jar");
        args.add(sbtLaunchJar.getAbsolutePath());
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(root));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        assertEquals("Unexpected return code: " + stdOut, expectedReturnCode, returnCode);

        return stdOut;
    }

}
