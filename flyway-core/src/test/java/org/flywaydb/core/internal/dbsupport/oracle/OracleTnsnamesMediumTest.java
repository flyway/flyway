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
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.flywaydb.core.internal.dbsupport.oracle.OracleMigrationMediumTest.JDBC_PASSWORD;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleMigrationMediumTest.JDBC_USER;
import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Oracle tnsnames.ora.
 */
@Category(DbCategory.Oracle.class)
public class OracleTnsnamesMediumTest {
    @Rule
    public Timeout globalTimeout = new Timeout(180, TimeUnit.SECONDS);

    @Test
    public void tnsnamesOra() throws Exception {
        System.setProperty("oracle.net.tns_admin", ClassLoader.getSystemResource("migration/dbsupport/oracle").getPath());

        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:oracle:thin:@ORACLE_10_XE", JDBC_USER, JDBC_PASSWORD);
        flyway.setSchemas("tnsnames");
        flyway.setLocations("migration/sql");
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }
}