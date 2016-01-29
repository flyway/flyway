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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.resolver.AbstractJdbcResolver;

/**
 * Migration resolver for Jdbc migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 *
 * <p>This class an be replaced with a custom subclass. Note however that since this class is considered
 * internal API, such a subclass is tied to a specific version and my need to be updated when switching to
 * a new flyway version. In order to use a custom subclass:</p>
 * <ul>
 *     <li>create a subclass of this class</li>
 *     <li>disable the usage of the default resolvers using {@link org.flywaydb.core.Flyway#setSkipDefaultResolvers(boolean)}
 *     or the respective property in the flyway configuration file</li>
 *     <li>include the custom subclass as custom resolver using {@link org.flywaydb.core.Flyway#setResolvers(MigrationResolver...)},
 *     {@link org.flywaydb.core.Flyway#setResolversAsClassNames(String...)} or the respective property in the flyway configuration file</li>
 *     <li><b>if you replace this class with a subclass, and want to use the other default resolvers, you need
 *     to include them as custom resolvers as well!</b></li>
 * </ul>
 */
public class JdbcMigrationResolver extends AbstractJdbcResolver<JdbcMigration> {

    @Override
    protected Class<JdbcMigration> getMigrationBaseType() {
        return JdbcMigration.class;
    }

    @Override
    protected MigrationType getMigrationType() {
        return MigrationType.JDBC;
    }

    @Override
    protected MigrationExecutor createMigrationExecutor(JdbcMigration migration) {
        return new JdbcMigrationExecutor(migration);
    }
}
