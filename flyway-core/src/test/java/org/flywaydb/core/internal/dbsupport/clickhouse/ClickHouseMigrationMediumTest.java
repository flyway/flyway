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
package org.flywaydb.core.internal.dbsupport.clickhouse;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertNull;

/**
 * Test to demonstrate the migration functionality using ClickHouse.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.ClickHouse.class)
public class ClickHouseMigrationMediumTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String url = customProperties.getProperty("clickhouse.url", "jdbc:clickhouse://127.0.0.1:8123/flyway_db");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, null, null);
    }

    @Override
    protected void createTestTable() throws SQLException {
        jdbcTemplate.execute("CREATE TABLE t1 (name String) ENGINE = TinyLog");
    }

    @Override
    protected String getBasedir() {
        return "migration/dbsupport/clickhouse/sql/default";
    }

    @Override
    protected String getMigrationDir() {
        return "migration/dbsupport/clickhouse/sql";
    }

    @Override
    protected String getCommentLocation() {
        return "migration/dbsupport/clickhouse/sql/comment";
    }

    @Override
    protected String getFutureFailedLocation() {
        return "migration/dbsupport/clickhouse/sql/future_failed";
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/clickhouse/sql/quote";
    }

    @Override
    protected String getSemiColonLocation() {
        return "migration/dbsupport/clickhouse/sql/semicolon";
    }

    @Override
    protected String getValidateLocation() {
        return "migration/dbsupport/clickhouse/sql/validate";
    }

    @Test
    @Override
    public void quote() throws Exception {
        flyway.setLocations(getQuoteLocation());
        flyway.migrate();
        // select count(x) on an empty table produces an empty result set instead of a single 0 on ClickHouse.
        assertNull(jdbcTemplate.queryForString("SELECT COUNT(name) FROM " + dbSupport.quote(flyway.getSchemas()[0], "table")));
    }

    @Ignore("Not necessary")
    @Test
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {
    }

    @Ignore("ClickHouse doesn't support delete")
    @Test
    @Override
    public void repair() throws Exception {
    }

    @Ignore("ClickHouse doesn't support update")
    @Test
    @Override
    public void repairChecksum() {
    }
}
