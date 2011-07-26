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

import org.apache.tools.ant.BuildFileTest;
import org.junit.Before;

/**
 * Medium Test for the status task.
 */
public class StatusLargeTest extends BuildFileTest {
    @Before
    public void setUp() {
        String installDir = System.getProperty("installDir");

        // initialize Ant
        configureProject(installDir + "/status/build.xml");
    }

    public void testWithout() {
        executeTarget("use.without");
        assertEquals("Message was logged but should not.", getLog(), "");
    }

    public void testMessage() {
        // execute target 'use.nestedText' and expect a message
        // 'attribute-text' in the log
        expectLog("use.message", "attribute-text");
    }

    public void testFail() {
        // execute target 'use.fail' and expect a BuildException
        // with text 'Fail requested.'
        expectBuildException("use.fail", "Fail requested.");
    }

    public void testNestedText() {
        expectLog("use.nestedText", "nested-text");
    }

    public void testNestedElement() {
        executeTarget("use.nestedElement");
        assertLogContaining("Nested Element 1");
        assertLogContaining("Nested Element 2");
    }
}
