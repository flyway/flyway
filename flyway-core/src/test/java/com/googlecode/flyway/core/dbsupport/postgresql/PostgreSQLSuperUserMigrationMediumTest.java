package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

/**
 * PostgreSQL medium tests that require SuperUser permissions.
 */
@ContextConfiguration(locations = {"classpath:migration/dbsupport/postgresql/postgresql-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PostgreSQLSuperUserMigrationMediumTest {
    /**
     * The datasource to use for single-threaded migration tests.
     */
    @Autowired
    @Qualifier("migrationDataSourceSuperUser")
    protected DataSource migrationDataSource;

    protected JdbcTemplate jdbcTemplate;

    protected Flyway flyway;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(migrationDataSource);

        flyway = new Flyway();
        flyway.setDataSource(migrationDataSource);
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Types.
     */
    @Test
    public void type() throws Exception {
        flyway.setBaseDir("migration/dbsupport/postgresql/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }
}
