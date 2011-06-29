/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void currentSchema() {
        String currentSchema = new OracleDbSupport(new JdbcTemplate(dataSource)).getCurrentSchema();
        assertEquals(userName.toUpperCase(), currentSchema);
    }

    /**
     * Tests that the current schema for a proxy connection with conn_user[schema_user] is schema_user and not conn_user;
     */
    @Test
    public void currentSchemaWithProxy() {
        String currentSchema = new OracleDbSupport(new JdbcTemplate(proxyUserDataSource)).getCurrentSchema();
        assertEquals(userName.toUpperCase(), currentSchema);
    }
}
