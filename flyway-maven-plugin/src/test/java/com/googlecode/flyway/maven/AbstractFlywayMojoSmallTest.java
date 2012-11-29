package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import org.apache.maven.project.MavenProject;
import org.h2.Driver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
