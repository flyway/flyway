/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import org.apache.maven.project.MavenProject;
import org.h2.Driver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for AbstractFlywayMojo.
 */
public class AbstractFlywayMojoSmallTest {
    @Test
    public void execute() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                assertEquals(2, flyway.getSchemas().length);
                assertEquals("first", flyway.getSchemas()[0]);
                assertEquals("second", flyway.getSchemas()[1]);
            }
        };

        mojo.driver = Driver.class.getName();
        mojo.url = "jdbc:h2:mem:dummy";
        mojo.user = "sa";
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.getProperties().setProperty("flyway.schemas", "first,second");
        mojo.execute();
    }

    @Test
    public void skipExecute() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                assertNull(flyway.getDataSource());
            }
        };

        mojo.skip = true;
        mojo.url = "jdbc:h2:mem:dummy";
        mojo.mavenProject = new MavenProject();
        mojo.execute();
    }
}
