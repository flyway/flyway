/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DriverDataSourceSmallTest {
    @Test
    public void getConnectionException() throws Exception {
        String url = "jdbc:h2:<<<Invalid--URL>>";
        String user = "axel";
        String password = "superS3cr3t";

        try {
            new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null).getConnection();
        } catch (FlywayException e) {
            assertTrue(e.getCause() instanceof SQLException);
            assertTrue(e.getMessage().contains(url));
            assertTrue(e.getMessage().contains(user));
            assertFalse(e.getMessage().contains(password));
        }
    }

    @Test
    public void nullInitSqls() throws Exception {
        //Used to fail with NPE
        new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:h2:mem:abc", "axel", "superS3cr3t", null).getConnection().close();
    }
}
