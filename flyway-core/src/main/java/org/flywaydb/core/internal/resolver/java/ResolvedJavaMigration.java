/*
 * Copyright 2010-2019 Boxfuse GmbH
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
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.ClassUtils;

/**
 * A resolved Java migration.
 */
public class ResolvedJavaMigration extends ResolvedMigrationImpl {
    /**
     * Creates a new ResolvedJavaMigration based on this JavaMigration.
     *
     * @param javaMigration The JavaMigration to use.
     */
    public ResolvedJavaMigration(JavaMigration javaMigration) {
        setVersion(javaMigration.getVersion());
        setDescription(javaMigration.getDescription());
        setScript(javaMigration.getClass().getName());
        setChecksum(javaMigration.getChecksum());
        setType(



                        MigrationType.JDBC
        );
        setPhysicalLocation(ClassUtils.getLocationOnDisk(javaMigration.getClass()));
        setExecutor(new JavaMigrationExecutor(javaMigration));
    }
}