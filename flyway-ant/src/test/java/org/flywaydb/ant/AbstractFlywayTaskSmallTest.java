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

import org.flywaydb.ant.AbstractFlywayTask;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class AbstractFlywayTaskSmallTest {
    @Test
    public void adjustRelativeFileSystemLocationToBaseDir() {
        String root = File.listRoots()[0].getPath();

        File baseDir = new File("/tempo");
        assertEquals("db/migration",
                AbstractFlywayTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "db/migration"));

        assertEquals("filesystem:" + root + "test/migration",
                AbstractFlywayTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "filesystem:" + root + "test/migration"));

        assertEquals("filesystem:" + root + "tempo/test/migration",
                AbstractFlywayTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "filesystem:test/migration"));
    }
}
