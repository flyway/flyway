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
package org.flywaydb.core.internal.resolver.java;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.resolver.AbstractJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;

/**
 * Migration resolver for Java-based migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
public class JavaMigrationResolver extends AbstractJavaMigrationResolver<JavaMigration, JavaMigrationExecutor> {
    /**
     * Creates a new instance.
     *
     * @param classProvider The class provider.
     * @param configuration The configuration to inject (if necessary) in the migration classes.
     */
    public JavaMigrationResolver(ClassProvider classProvider, Configuration configuration) {
        super(classProvider, configuration);
    }

    @Override
    protected String getMigrationTypeStr() {
        return "JDBC";
    }

    @Override
    protected Class<JavaMigration> getImplementedInterface() {
        return JavaMigration.class;
    }

    @Override
    protected JavaMigrationExecutor createExecutor(JavaMigration migration) {
        return new JavaMigrationExecutor(migration);
    }

    @Override
    protected MigrationType getMigrationType(



    ) {
        return



                        MigrationType.JDBC;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param migration The migration to analyse.
     * @return The migration info.
     */
    public ResolvedMigrationImpl extractMigrationInfo(JavaMigration migration) {
        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(migration.getVersion());
        resolvedMigration.setDescription(migration.getDescription());
        resolvedMigration.setScript(migration.getClass().getName());
        resolvedMigration.setChecksum(migration.getChecksum());
        resolvedMigration.setType(getMigrationType(



        ));
        return resolvedMigration;
    }
}