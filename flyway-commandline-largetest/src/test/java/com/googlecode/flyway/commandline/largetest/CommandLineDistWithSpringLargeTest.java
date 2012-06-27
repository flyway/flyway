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
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Command-Line Tool Distribution With Spring.
 *
 * You must run the Maven build once before executing this in IntelliJ.
 */
public class CommandLineDistWithSpringLargeTest extends CommandLineLargeTest {
    @Override
    protected String getInstallDir() {
        return System.getProperty("installDirDistWithSpring",
                "flyway-commandline-largetest/target/install/dist with spring/flyway-commandline-" + getPomVersion());
    }

    /**
     * Execute 1 (SQL), 1.1 (SQL), 1.2 (Spring Jdbc) & 1.3 (Jdbc)
     */
    @Test
    public void migrate() throws Exception {
        String stdOut = runFlywayCommandLine(0, "largeTest.properties", "migrate", "-locations=db/migration");
        assertTrue(stdOut.contains("Successfully applied 4 migrations"));
    }
}
