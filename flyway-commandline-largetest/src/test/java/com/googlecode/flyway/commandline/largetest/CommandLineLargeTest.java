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
package com.googlecode.flyway.commandline.largetest;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Command-Line Tool.
 */
public class CommandLineLargeTest {
    @Test
    public void showUsage() throws Exception {
        String installDir = System.getProperty("installDir");

	ProcessBuilder builder = new ProcessBuilder(installDir + "/flyway." + flywayCmdLineExtensionForCurrentSystem());
        builder.directory(new File(installDir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals(0, returnCode);
        assertTrue(stdOut.contains("* Usage"));
    }

    @Test
    public void migrate() throws Exception {
        String installDir = System.getProperty("installDir");
        String configFile = new ClassPathResource("largeTest.properties").getFile().getPath();

	ProcessBuilder builder = new ProcessBuilder(installDir + "/flyway." + flywayCmdLineExtensionForCurrentSystem(),
		"migrate", "-configFile=" + configFile);
        builder.directory(new File(installDir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals(0, returnCode);
        assertTrue(stdOut.contains("Successfully applied 3 migrations"));
    }

    @Test
    public void migrateWithCustomBaseDir() throws Exception {
        String installDir = System.getProperty("installDir");
        String configFile = new ClassPathResource("largeTest.properties").getFile().getPath();

	ProcessBuilder builder = new ProcessBuilder(installDir + "/flyway." + flywayCmdLineExtensionForCurrentSystem(),
		"migrate", "-configFile=" + configFile, "-baseDir=migrations", "-basePackage=dummy");
        builder.directory(new File(installDir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals(0, returnCode);
        assertTrue(stdOut.contains("Successfully applied 1 migration"));
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
