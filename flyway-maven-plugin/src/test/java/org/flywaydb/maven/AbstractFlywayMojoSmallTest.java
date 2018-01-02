/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.maven;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.flywaydb.core.Flyway;
import org.h2.Driver;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for AbstractFlywayMojo.
 */
public class AbstractFlywayMojoSmallTest {
    private static final String FLYWAY_MY_PROPERTY = "flyway.myProperty";

    @Before
    public void cleanCustomSystemProperty() {
        System.clearProperty(FLYWAY_MY_PROPERTY);
    }

    @Test
    public void execute() throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                new ClassRealm(new ClassWorld("a", originalClassLoader), "a", originalClassLoader));

        try {
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
            mojo.settings = new Settings();
            mojo.mavenProject = new MavenProject();
            mojo.mavenProject.setFile(new File(".").getAbsoluteFile());
            mojo.mavenProject.getProperties().setProperty("flyway.schemas", "first,second");
            mojo.mavenProject.getBuild().setOutputDirectory(".");
            mojo.execute();
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
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

    @Test
    public void shouldHaveABooleanPropertyWithTrue() throws Exception {
        System.setProperty(FLYWAY_MY_PROPERTY, "true");
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
            }
        };

        boolean booleanProperty = mojo.getBooleanProperty(FLYWAY_MY_PROPERTY, false);
        assertEquals(true, booleanProperty);
    }

    @Test
    public void shouldHaveTheMavenPropertyWithFalse() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
            }
        };

        boolean booleanProperty = mojo.getBooleanProperty(FLYWAY_MY_PROPERTY, false);
        assertEquals(false, booleanProperty);
    }
}
