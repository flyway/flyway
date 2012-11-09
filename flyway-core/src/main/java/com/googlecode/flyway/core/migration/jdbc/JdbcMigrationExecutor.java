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
package com.googlecode.flyway.core.migration.jdbc;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationExecutor;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;

/**
 * Adapter for executing migrations implementing JdbcMigration.
 */
public class JdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The JdbcMigration to execute.
     */
    private final JdbcMigration jdbcMigration;

    /**
     * Creates a new JdbcMigrationExecutor.
     *
     * @param jdbcMigration The JdbcMigration to execute.
     */
    public JdbcMigrationExecutor(JdbcMigration jdbcMigration) {
        this.jdbcMigration = jdbcMigration;
    }

    public void execute(JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        try {
            jdbcMigration.migrate(jdbcTemplate.getConnection());
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }
}
