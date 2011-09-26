/**
 * Copyright (C) 2010-2011 the original author or authors.
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
 * Large Test for the status task.
 */
public class AntLargeTest {
    /**
     * The installation directory for the test POMs.
     */
    private String installDir = System.getProperty("installDir");

    @Test
    public void init() throws Exception {
        String stdOut = runAnt("init");
        assertTrue(stdOut.contains("A new beginning!"));
    }

    @Test
    public void migrate() throws Exception {
        String stdOut = runAnt("migrate", "-Dflyway.baseDir=largetest/sql");
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    @Test
    public void migrateWithAntPropertyForSchemas() throws Exception {
        String stdOut = runAnt("migrate", "-Dflyway.baseDir=largetest/sql", "-Dflyway.schemas=TEST");
        assertTrue(stdOut.contains("Schema: TEST"));
    }

    @Test
    public void status() throws Exception {
        String stdOut = runAnt("status");
        assertTrue(stdOut.contains("No migrations applied yet"));
    }

    /**
     * Runs Ant in this directory with these extra arguments.
     *
     * @param dir       The directory below src/test/resources to run maven in.
     * @param extraArgs The extra arguments (if any) for Maven.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    private String runAnt(String dir, String... extraArgs) throws Exception {
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

        assertEquals(0, returnCode);

        return stdOut;
    }
}
