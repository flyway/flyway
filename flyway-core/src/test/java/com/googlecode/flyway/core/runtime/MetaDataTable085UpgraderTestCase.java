/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.runtime;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Testcase for the upgrade of the metadata table.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class MetaDataTable085UpgraderTestCase {
    /**
     * The datasource to use for tests.
     */
    @Autowired
    protected DataSource dataSource;

    protected Flyway flyway;

    @Before
    public void setUp() {
        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.clean();
    }

    /**
     * @return The directory containing the migrations for the tests.
     */
    protected abstract String getBaseDir();

    /**
     * @return The DbSupport class to test.
     */
    protected abstract DbSupport getDbSupport();

    /**
     * @return The location for the create script in the old format.
     */
    protected abstract String getMetaDataTable085CreateScriptLocation();

    @Test
    public void upgrade() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        createMetaDataTable085(transactionTemplate, jdbcTemplate);

        jdbcTemplate.update(
                "INSERT INTO schema_version (version, script, execution_time, state, current_version) VALUES (?, ?, ?, ?, ?)",
                new Object[]{"1.0", "Sql File: V1__Initial_version.sql", 100, MigrationState.SUCCESS.name(), false});
        jdbcTemplate.update(
                "INSERT INTO schema_version (version, script, execution_time, state, current_version) VALUES (?, ?, ?, ?, ?)",
                new Object[]{"1.1", "Java Class: V1_1__Sample_java_migration", 1234, MigrationState.SUCCESS.name(), true});

        MetaDataTable085Upgrader metaDataTable085Upgrader =
                new MetaDataTable085Upgrader(transactionTemplate, jdbcTemplate, getDbSupport(), "SCHEMA_VERSION", getBaseDir(), "UTF-8");
        metaDataTable085Upgrader.upgrade();

        //Second call should have no effect
        metaDataTable085Upgrader.upgrade();

        List<Migration> migrations = flyway.history();
        assertEquals(2, migrations.size());

        Migration migration1 = migrations.get(0);
        assertEquals(MigrationType.SQL, migration1.getMigrationType());
        assertEquals("V1__Initial_version.sql", migration1.getScriptName());
        assertEquals(new Integer(1996767037), migration1.getChecksum());
        //assertNotNull(migration1.getInstalledBy());

        Migration migration11 = migrations.get(1);
        assertEquals(MigrationType.JAVA, migration11.getMigrationType());
        assertEquals("V1_1__Sample_java_migration", migration11.getScriptName());
        assertNull(migration11.getChecksum());
        //assertNotNull(migration1.getInstalledBy());
    }

    /**
     * Creates the metadata table in 0.8.5 format.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        The jdbc template to use.
     */
    private void createMetaDataTable085(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate) {
        String location = getMetaDataTable085CreateScriptLocation();
        String scriptSource = ResourceUtils.loadResourceAsString(location);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "SCHEMA_VERSION");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        SqlScript sqlScript = new SqlScript(scriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }
}
