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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
public class DB2MigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("db2.user", "db2admin");
        String password = customProperties.getProperty("db2.password", "flyway");
        String url = customProperties.getProperty("db2.url", "jdbc:db2://localhost:50000/flyway");

        return new DriverDataSource(null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/sequence");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Sequence", flyway.status().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("VALUES NEXTVAL FOR BEAST_SEQ"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void mqt() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/mqt");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Mqt", flyway.status().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM empl_mqt"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void alias() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/alias");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());
        assertEquals("Alias", flyway.status().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM POOR_SLAVE"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }
}