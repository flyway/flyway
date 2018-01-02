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
package org.flywaydb.core;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.resolver.MyCustomMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test for the main Flyway class.
 */
public class FlywaySmallTest {
    @Test
    public void configure() {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());

        flyway.execute(new Flyway.Command<Void>() {
            public Void execute(MigrationResolver migrationResolver,
                                SchemaHistory schemaHistory, Database database, Schema[] schemas,
                                List<FlywayCallback> effectiveCallbacks, DryRunStatementInterceptor dryRunStatementInterceptor) {
                assertEquals("PUBLIC", flyway.getSchemas()[0]);
                return null;
            }
        });
    }

    /**
     * This must be possible to enable NTLM authentication on SQL Server.
     */
    @Test
    public void configureNoUserNoPassword() {
        Properties properties = new Properties();
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertNotNull(flyway.getDataSource());
    }

    @Test
    public void configureTarget() {
        Properties properties = new Properties();
        properties.setProperty("flyway.target", "666");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals("666", flyway.getTarget().toString());
    }

    @Test
    public void configureOutOfOrder() {
        Properties properties = new Properties();
        properties.setProperty("flyway.outOfOrder", "true");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(true, flyway.isOutOfOrder());
    }

    @Test(expected = FlywayException.class)
    public void configureBadBoolean() {
        Properties properties = new Properties();
        properties.setProperty("flyway.outOfOrder", "nope");

        Flyway flyway = new Flyway();
        flyway.configure(properties);
    }

    @Test
    public void configureSchemas() {
        Properties properties = new Properties();
        properties.setProperty("flyway.schemas", "  schema1,schema2, schema3 ,");

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(3, flyway.getSchemas().length);
        assertEquals("schema1", flyway.getSchemas()[0]);
        assertEquals("schema2", flyway.getSchemas()[1]);
        assertEquals("schema3", flyway.getSchemas()[2]);
    }

    @Test
    public void configurePlaceholders() {
        Flyway flyway = new Flyway();
        flyway.getPlaceholders().put("mykey", "myvalue");

        flyway.configure(new Properties());
        assertEquals("myvalue", flyway.getPlaceholders().get("mykey"));

        Properties properties = new Properties();
        properties.setProperty("flyway.placeholders.lucky", "luke");
        flyway.configure(properties);
        assertEquals("myvalue", flyway.getPlaceholders().get("mykey"));
        assertEquals("luke", flyway.getPlaceholders().get("lucky"));
    }

    @Test
    public void configurePlaceholderReplacement() {
        Flyway flyway = new Flyway();
        flyway.configure(new Properties());
        assertTrue(flyway.isPlaceholderReplacement());

        Properties properties = new Properties();
        properties.setProperty("flyway.placeholderReplacement", "false");
        flyway.configure(properties);
        assertFalse(flyway.isPlaceholderReplacement());
    }

    @Test
    public void configureCustomMigrationResolvers() {
        Properties properties = new Properties();
        properties.setProperty("flyway.resolvers", MyCustomMigrationResolver.class.getName());

        Flyway flyway = new Flyway();
        flyway.configure(properties);

        assertEquals(MyCustomMigrationResolver.class, flyway.getResolvers()[0].getClass());
    }

    @Test
    public void configureWithExistingDataSource() {
        DataSource dataSource = new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "", null);

        Properties properties = new Properties();

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.getDataSource());
    }

    @Test
    public void configureWithPartialDbConfigInProperties() {
        DataSource dataSource = new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "", null);

        Properties properties = new Properties();
        properties.setProperty("flyway.user", "dummy_user");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.configure(properties);

        assertEquals(dataSource, flyway.getDataSource());
    }

    @Test
    public void dataSourceWithSeparateParams() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1", "sa", "");

        assertNotNull(flyway.getDataSource());
    }

    @Test
    public void getLocations() {
        Flyway flyway = new Flyway();
        flyway.setLocations("db/migrations1", "filesystem:db/migrations2");
        String[] locations = flyway.getLocations();
        assertEquals(2, locations.length);
        assertEquals("classpath:db/migrations1", locations[0]);
        assertEquals("filesystem:db/migrations2", locations[1]);
    }

    @Test
    public void setSqlMigrationSeparator() {
        Flyway flyway = new Flyway();
        assertEquals("__", flyway.getSqlMigrationSeparator());

        flyway.setSqlMigrationSeparator("-");
        assertEquals("-", flyway.getSqlMigrationSeparator());

        flyway.setSqlMigrationSeparator(" ");
        assertEquals(" ", flyway.getSqlMigrationSeparator());

        try {
            flyway.setSqlMigrationSeparator("");
            fail();
        } catch (FlywayException e) {
            //expected
        }
    }
}
