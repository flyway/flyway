/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.migration.spring;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Interface to be implemented by Spring Jdbc Java Migrations. By default the migration version and description will be extracted
 * from the class name. This can be overridden by also implementing the {@link org.flywaydb.core.api.migration.MigrationInfoProvider}
 * interface, in which case it can be specified programmatically. The checksum of this migration (for validation) will also
 * be null, unless the migration also implements the {@link org.flywaydb.core.api.migration.MigrationChecksumProvider},
 * in which case it can be returned programmatically.
 *
 * <p>When the JdbcMigration implements {@link org.flywaydb.core.api.configuration.ConfigurationAware},
 * the master {@link org.flywaydb.core.api.configuration.FlywayConfiguration} is automatically injected upon creation,
 * which is especially useful for getting placeholder and schema information.</p>
 *
 * It is encouraged not to implement this interface directly and subclass {@link BaseSpringJdbcMigration} instead.
 *
 * @deprecated Extend JavaMigration or BaseJavaMigration instead. Will be removed in Flyway 6.0.
 */
@Deprecated
public interface SpringJdbcMigration {
    /**
     * Executes this migration. The execution will automatically take place within a transaction, when the underlying
     * database supports it.
     *
     * @param jdbcTemplate The jdbcTemplate to use to execute statements.
     * @throws Exception when the migration failed.
     */
    void migrate(JdbcTemplate jdbcTemplate) throws Exception;
}