package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.hsqldb.jdbcDriver;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

/**
 * Test for HsqlDbSupport.
 */
public class HsqlDbSupportMediumTest {
    @Test
    public void isSchemaEmpty() throws Exception {
        DriverDataSource dataSource = new DriverDataSource(new jdbcDriver(), "jdbc:hsqldb:mem:flyway_db", "SA", "");

        Connection connection = dataSource.getConnection();
        HsqlDbSupport dbSupport = new HsqlDbSupport(connection);
        dbSupport.getJdbcTemplate().execute("CREATE TABLE mytable (mycol INT)");
        assertFalse(dbSupport.isSchemaEmpty("PUBLIC"));
        assertFalse(dbSupport.isSchemaEmpty("public"));
        connection.close();
    }
}
