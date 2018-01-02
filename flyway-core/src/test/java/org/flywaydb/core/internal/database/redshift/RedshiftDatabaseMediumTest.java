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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import static org.flywaydb.core.internal.database.redshift.RedshiftMigrationMediumTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

@Category(DbCategory.Redshift.class)
public class RedshiftDatabaseMediumTest {
    @Before
    public void ensureRedshiftEnabled() {
        assumeTrue(Boolean.valueOf(System.getProperty("flyway.test.redshift")));
    }

    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @Rule
    public Timeout globalTimeout = new Timeout(180, TimeUnit.SECONDS);

    /**
     * Checks that the search_path is extended and not overwritten so that objects in PUBLIC can still be found.
     */
    @Test
    public void setCurrentSchema() throws Exception {
        Connection connection = createDataSource().getConnection();
        RedshiftDatabase database = new RedshiftDatabase(new Flyway(), connection, null);
        Schema schema = database.getMainConnection().getSchema("search_path_test");
        try {
            schema.drop();
        } catch (Exception e) {
            // Ignore
        }
        schema.create();
        database.getMainConnection().changeCurrentSchemaTo(database.getMainConnection().getSchema("search_path_test"));
        String searchPath = database.getMainConnection().getJdbcTemplate().queryForString("SHOW search_path");
        assertEquals("\"search_path_test\",$user, public", searchPath);
        schema.drop();
        JdbcUtils.closeConnection(connection);
    }
}
