/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@Category(DbCategory.H2.class)
public class H2MigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "", null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void mysqlMode() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:mysql_db;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setSchemas("mysql_schema");
        flyway.baseline();
    }

    @Test
    public void mysqlModePublic() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:mysql_public_db;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setSchemas("PUBLIC");
        flyway.baseline();
    }

    @Test
    public void schema() throws Exception {
        flyway.setLocations("migration/database/h2/sql/schema");
        flyway.setSchemas("main", "other");
        assertEquals(2, flyway.migrate());
    }

    @Test
    public void dollarQuotedString() throws Exception {
        flyway.setLocations("migration/database/h2/sql/dollar_quoted_string");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1.1", version.toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        assertEquals("'Mr. Semicolon+Linebreak;\nanother line'",
                jdbcTemplate.queryForString("select name from test_user where name like '%line'''"));
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/database/h2/sql/sequence");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Sequence", flyway.info().current().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("select nextval('the_beast')"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void domain() throws Exception {
        flyway.setLocations("migration/database/h2/sql/domain");
        flyway.migrate();

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1", version.toString());
        assertEquals("Domain", flyway.info().current().getDescription());

        assertEquals("axel@spam.la", jdbcTemplate.queryForString("select address from test_user where name = 'Axel'"));

        flyway.clean();
        flyway.migrate();
    }
}