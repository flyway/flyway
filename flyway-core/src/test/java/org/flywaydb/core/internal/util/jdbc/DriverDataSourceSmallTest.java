/*
 * Copyright 2010-2017 Boxfuse GmbH
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
