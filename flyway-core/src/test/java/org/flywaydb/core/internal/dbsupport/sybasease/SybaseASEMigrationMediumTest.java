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
package org.flywaydb.core.internal.dbsupport.sybasease;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using Sybase ASE with the Jtds driver.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.SybaseASE.class)
public class SybaseASEMigrationMediumTest extends MigrationTestCase {
    static final String JDBC_URL = "jdbc:jtds:sybase://127.0.0.1:62080/guest";
    static final String JDBC_USER = "sa";
    static final String JDBC_PASSWORD = "password";

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @Override
    protected String getQuoteLocation() {
        //Sybase does not support quoting table names
        return getValidateLocation();
    }

    @Ignore("Table quote is not supported in Sybase ASE")
    @Override
    public void quotesAroundTableName() {
    }

    @Override
    protected String getMigrationDir() {
        return "migration/dbsupport/sybasease/sql";
    }

    @Override
    @Ignore("Not supported on Sybase ASE Server")
    public void setCurrentSchema() throws Exception {
        //Skip
    }

    @Override
    @Ignore("Schema reference is not supported on SAP ASE")
    public void migrateMultipleSchemas() throws Exception {
        //Skip
    }

    @Override
    @Ignore("Schema reference is not supported on SAP ASE")
    public void schemaExists() throws SQLException {
        //Skip
    }

    @Override
    @Ignore("Table name quote is not supported on SAP ASE")
    public void quote() throws Exception {
        //skip
    }

    @Override
    public void failedMigration() throws Exception {
        // It is line 22 as a go statement is added for Sybase
        doFailedMigration(22);
    }

    @Override
    @Ignore("Not needed as Sybase ASE support was first introduced in Flyway 4.0")
    public void columnExists() throws Exception {
        //Skip
    }
}
