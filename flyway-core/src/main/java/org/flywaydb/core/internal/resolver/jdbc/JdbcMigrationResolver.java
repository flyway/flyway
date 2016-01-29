/**
 * Copyright 2010-2015 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.resolver.AbstractJdbcResolver;

/**
 * Migration resolver for Jdbc migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
public class JdbcMigrationResolver extends AbstractJdbcResolver<JdbcMigration> {

    @Override
    protected Class<JdbcMigration> getTargetInterface() {
        return JdbcMigration.class;
    }

    @Override
    protected MigrationExecutor createExecutor(JdbcMigration migration) {
        return new JdbcMigrationExecutor(migration);
    }

    protected MigrationType getMigrationType() {
        return MigrationType.JDBC;
    }}
