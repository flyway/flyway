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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.internal.resolver.JavaMigrationResolver;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.scanner.Scanner;

/**
 * Migration resolver for JDBC migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
public class JdbcMigrationResolver extends JavaMigrationResolver<JdbcMigration, JdbcMigrationExecutor> {
    /**
     * Creates a new instance.
     *
     * @param locations     The base packages on the classpath where to migrations are located.
     * @param scanner       The Scanner for loading migrations on the classpath.
     * @param configuration The configuration to inject (if necessary) in the migration classes.
     */
    public JdbcMigrationResolver(Scanner scanner, Locations locations, FlywayConfiguration configuration) {
        super(scanner, locations, configuration);
    }

    @Override
    protected String getMigrationTypeStr() {
        return "JDBC";
    }

    @Override
    protected Class<JdbcMigration> getImplementedInterface() {
        return JdbcMigration.class;
    }

    @Override
    protected JdbcMigrationExecutor createExecutor(JdbcMigration migration) {
        return new JdbcMigrationExecutor(migration);
    }

    @Override
    protected MigrationType getMigrationType(



    ) {
        return



                        MigrationType.JDBC;
    }
}