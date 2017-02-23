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
/**
 * SolidDB support developed 2014 by Sabine Gallus & Michael Forstner
 * Media-Saturn IT Services GmbH
 * Wankelstr. 5
 * 85046 Ingolstadt, Germany
 * http://www.media-saturn.com
 */

package org.flywaydb.core.internal.dbsupport.solid;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

@Category(DbCategory.SolidDB.class)
public class SolidMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(final Properties customProperties) throws Exception {
        final String user = customProperties.getProperty("solid.user", "flyway");
        final String password = customProperties.getProperty("solid.password", "flyway");
        final String url = customProperties.getProperty("solid.url", "jdbc:solid://localhost:1313");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), "solid.jdbc.SolidDriver",
                                    url, user, password, null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLE schema_version (\n" +
                "    version_rank INT NOT NULL,\n" +
                "    installed_rank INT NOT NULL,\n" +
                "    version VARCHAR(50) NOT NULL,\n" +
                "    description VARCHAR(200) NOT NULL,\n" +
                "    type VARCHAR(20) NOT NULL,\n" +
                "    script VARCHAR(1000) NOT NULL,\n" +
                "    checksum INT,\n" +
                "    installed_by VARCHAR(100) NOT NULL,\n" +
                "    installed_on TIMESTAMP,\n" +
                "    execution_time INT NOT NULL,\n" +
                "    success SMALLINT NOT NULL,\n" +
                "    PRIMARY KEY(version)\n" +
                ") STORE DISK");
        jdbcTemplate.execute("\"CREATE TRIGGER schema_version_create ON schema_version\n" +
                "    BEFORE INSERT REFERENCING NEW installed_on AS new_installed_on\n" +
                "    BEGIN\n" +
                "    SET new_installed_on = NOW();\n" +
                "    END\"");
        jdbcTemplate.execute("CREATE INDEX schema_version_vr_idx ON schema_version (version_rank)");
        jdbcTemplate.execute("CREATE INDEX schema_version_ir_idx ON schema_version (installed_rank)");
        jdbcTemplate.execute("CREATE INDEX schema_version_s_idx ON schema_version (success)");
        jdbcTemplate.execute("COMMIT WORK");
    }
}
