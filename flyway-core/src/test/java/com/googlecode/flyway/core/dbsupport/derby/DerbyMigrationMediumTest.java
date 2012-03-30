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
package com.googlecode.flyway.core.dbsupport.derby;

import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using Derby.
 */
public class DerbyMigrationMediumTest extends MigrationTestCase {
    static {
        System.setProperty("derby.stream.error.field", "java.lang.System.err");
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(new EmbeddedDriver(), "jdbc:derby:memory:flyway_db;create=true", "flyway", "");
    }

    @Override
    protected String getQuoteBaseDir() {
        return "migration/quote";
    }
}