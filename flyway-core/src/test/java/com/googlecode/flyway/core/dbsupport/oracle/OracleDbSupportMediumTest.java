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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@SuppressWarnings({"JavaDoc"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:migration/dbsupport/oracle/oracle-context.xml"})
public class OracleDbSupportMediumTest {
    @Autowired
    @Qualifier("migrationDataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("proxyUserDataSource")
    private DataSource proxyUserDataSource;

    @Autowired
    @Qualifier("userNameBean")
    private String userName;

    /**
     * Tests that the current schema for a connection is correct;
     */
    @Test
    public void currentSchema() throws Exception {
        String currentSchema = new OracleDbSupport(dataSource.getConnection()).getCurrentSchema();
        assertEquals(userName.toUpperCase(), currentSchema);
    }

    /**
     * Tests that the current schema for a proxy connection with conn_user[schema_user] is schema_user and not conn_user;
     */
    @Test
    public void currentSchemaWithProxy() throws Exception {
        String currentSchema = new OracleDbSupport(proxyUserDataSource.getConnection()).getCurrentSchema();
        assertEquals(userName.toUpperCase(), currentSchema);
    }
}
