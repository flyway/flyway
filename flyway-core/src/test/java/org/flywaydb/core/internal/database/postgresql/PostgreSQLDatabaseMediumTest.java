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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.flywaydb.core.internal.database.postgresql.PostgreSQLMigrationMediumTest.*;
import static org.junit.Assert.assertEquals;

@Category(DbCategory.PostgreSQL.class)
@RunWith(Parameterized.class)
public class PostgreSQLDatabaseMediumTest {
    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_POSTGRESQL_100},
                {JDBC_URL_POSTGRESQL_96},
                {JDBC_URL_POSTGRESQL_95},
                {JDBC_URL_POSTGRESQL_94},
                {JDBC_URL_POSTGRESQL_93},
                {JDBC_URL_POSTGRESQL_92}
        });
    }

    public PostgreSQLDatabaseMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }

    @Rule
    public Timeout globalTimeout = new Timeout(180, TimeUnit.SECONDS);

    /**
     * Checks that the search_path is extended and not overwritten so that objects in PUBLIC can still be found.
     */
    @Test
    public void setCurrentSchema() throws Exception {
        Connection connection = createDataSource().getConnection();
        PostgreSQLDatabase database = new PostgreSQLDatabase(new Flyway(), connection, null);
        Schema schema = database.getMainConnection().getSchema("search_path_test");
        try {
            schema.drop();
        } catch (Exception e) {
            // Ignore
        }
        schema.create();
        database.getMainConnection().changeCurrentSchemaTo(database.getMainConnection().getSchema("search_path_test"));
        String searchPath = database.getMainConnection().getJdbcTemplate().queryForString("SHOW search_path");
        assertEquals("search_path_test, \"$user\", public", searchPath);
        schema.drop();
        JdbcUtils.closeConnection(connection);
    }
}
