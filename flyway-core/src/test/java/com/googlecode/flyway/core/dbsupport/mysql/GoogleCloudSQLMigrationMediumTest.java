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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.google.appengine.api.rdbms.dev.LocalRdbmsService;
import com.google.appengine.tools.development.testing.LocalRdbmsServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test to demonstrate the migration functionality using Mysql.
 */
@SuppressWarnings({"JavaDoc"})
@ContextConfiguration(locations = {"classpath:migration/dbsupport/mysql/googlecloudsql-context.xml"})
@Ignore("Broken as long as Flyway spawns new threads")
public class GoogleCloudSQLMigrationMediumTest extends MySQLMigrationTestCase {
    private static LocalServiceTestHelper helper;

    @BeforeClass
    public static void setUpDb() {
        LocalRdbmsServiceTestConfig config = new LocalRdbmsServiceTestConfig();
        config.setServerType(LocalRdbmsService.ServerType.LOCAL);
        config.setDriverClass("com.mysql.jdbc.Driver");
        config.setDatabase("flyway_cloudsql_db");
        config.setJdbcConnectionStringFormat("jdbc:mysql://localhost:3306/flyway_cloudsql_db");
        config.setUser("flyway");
        config.setPassword("flyway");
        helper = new LocalServiceTestHelper(config);
        helper.setUp();
    }

    @AfterClass
    public static void tearDownDb() {
        helper.tearDown();
    }
}
