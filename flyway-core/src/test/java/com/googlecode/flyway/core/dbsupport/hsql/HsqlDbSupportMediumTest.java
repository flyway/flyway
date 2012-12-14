/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.hsqldb.jdbcDriver;
import org.junit.Test;

import java.sql.Connection;

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
