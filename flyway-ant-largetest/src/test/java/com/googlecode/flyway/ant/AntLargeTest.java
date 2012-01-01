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
package com.googlecode.flyway.ant;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

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
    /**
     * The installation directory for the test POMs.
     */
    private String installDir = System.getProperty("installDir");

    @Test
    public void init() throws Exception {
        String stdOut = runAnt(0, "init");
        assertTrue(stdOut.contains("A new beginning!"));
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runAnt(0, "migrate", "-Dflyway.baseDir=largetest/sql");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
        assertTrue(stdOut.contains("Populate table"));
    }

    /**
     * Tests the migrations with multiple schemas.
     */
    @Test
    public void multi() throws Exception {
        String stdOut = runAnt(0, "multi");
        assertTrue(stdOut.contains("Cleaned database schema 'flyway_1'"));
        assertTrue(stdOut.contains("Cleaned database schema 'flyway_2'"));
        assertTrue(stdOut.contains("Cleaned database schema 'flyway_3'"));
        assertTrue(stdOut.contains("Metadata table created: MASTER_OF_THE_VERSIONS"));
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    @Test
    public void status() throws Exception {
        String stdOut = runAnt(0, "status");
        assertTrue(stdOut.contains("No migrations applied yet"));
    }

    @Test
    public void validate() throws Exception {
        String stdOut = runAnt(1, "validate");
        assertTrue(stdOut.contains("Validate failed. Found differences between applied migrations and available migrations"));
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
    private String runAnt(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        String antHome = System.getenv("ANT_HOME");

        String extension = "";
        if (System.getProperty("os.name").startsWith("Windows")) {
            extension = ".bat";
        }

        List<String> args = new ArrayList<String>();
        args.add(antHome + "/bin/ant" + extension);
        args.add("-DlibDir=" + System.getProperty("libDir"));
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(installDir + "/" + dir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }
}
