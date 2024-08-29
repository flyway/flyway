/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resolver.java.JavaMigrationExecutor;
import org.flywaydb.core.internal.util.ClassUtils;

/**
 * Interface to be implemented by Java-based Migrations.
 *
 * <p>Java-based migrations are a great fit for all changes that can not easily be expressed using SQL.</p>
 *
 * <p>These would typically be things like</p>
 * <ul>
 *     <li>BLOB &amp; CLOB changes</li>
 *     <li>Advanced bulk data changes (Recalculations, advanced format changes, …)</li>
 * </ul>
 *
 * <p>Migration classes implementing this interface will be
 * automatically discovered when placed in a location on the classpath.</p>
 *
 * <p>Most users will be better served by subclassing subclass {@link BaseJavaMigration} instead of implementing this
 * interface directly, as {@link BaseJavaMigration} encourages the use of Flyway's default naming convention and
 * comes with sensible default implementations of all methods (except migrate of course) while at the same time also
 * providing better isolation against future additions to this interface.</p>
 */
public interface JavaMigration {
    /**
     * @return The version of the schema after the migration is complete. {@code null} for repeatable migrations.
     */
    MigrationVersion getVersion();

    /**
     * @return The description of this migration for the migration history. Never {@code null}.
     */
    String getDescription();

    /**
     * @return The checksum of this migration.
     */
    Integer getChecksum();

    default ResolvedMigration getResolvedMigration(Configuration config, StatementInterceptor statementInterceptor) {
        return new ResolvedMigrationImpl(getVersion(),
                                         getDescription(),
                                         getClass().getName(),
                                         getChecksum(),
                                         null,
                                         getType(),
                                         ClassUtils.getLocationOnDisk(getClass()),
                                         new JavaMigrationExecutor(this, statementInterceptor));
    }

    /**
     * Whether the execution should take place inside a transaction. Almost all implementations should return {@code true}.
     * This however makes it possible to execute certain migrations outside a transaction. This is useful for databases
     * like PostgreSQL and SQL Server where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    boolean canExecuteInTransaction();

    /**
     * Executes this migration. The execution will automatically take place within a transaction, when the underlying
     * database supports it and the canExecuteInTransaction returns {@code true}.
     *
     * @param context The context relevant for this migration, containing things like the JDBC connection to use and the
     * current Flyway configuration.
     * @throws Exception when the migration failed.
     */
    void migrate(Context context) throws Exception;

    default MigrationType getType() {
        return CoreMigrationType.JDBC;
    }
}
