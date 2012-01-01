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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.migration.ConcurrentMigrationTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test to demonstrate the migration functionality using MySQL.
 */
@ContextConfiguration(locations = {"classpath:migration/dbsupport/mysql/mysql-context.xml"})
public class MySQLConcurrentMigrationMediumTest extends ConcurrentMigrationTestCase {
}