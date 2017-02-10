/**
 * Copyright 2010-2016 Boxfuse GmbH
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.flywaydb.core.internal.dbsupport.informix;

import java.util.Properties;
import javax.sql.DataSource;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.ConcurrentMigrationTestCase;
import org.junit.experimental.categories.Category;

@Category(DbCategory.Informix.class)
public class InformixConcurrentMigrationTestCase extends ConcurrentMigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        
        String user = customProperties.getProperty("informix.user");
        String password = customProperties.getProperty("informix.password");
        String url = customProperties.getProperty("informix.url");
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    
    }

}
