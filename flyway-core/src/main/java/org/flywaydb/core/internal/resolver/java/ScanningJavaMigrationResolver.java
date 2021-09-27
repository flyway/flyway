/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration resolver for Java-based migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
@RequiredArgsConstructor
public class ScanningJavaMigrationResolver implements MigrationResolver {
    /**
     * The Scanner to use.
     */
    private final ClassProvider<JavaMigration> classProvider;

    /**
     * The configuration to inject (if necessary) in the migration classes.
     */
    private final Configuration configuration;

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (Class<?> clazz : classProvider.getClasses()) {
            JavaMigration javaMigration = ClassUtils.instantiate(clazz.getName(), configuration.getClassLoader());
            migrations.add(new ResolvedJavaMigration(javaMigration));
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }
}